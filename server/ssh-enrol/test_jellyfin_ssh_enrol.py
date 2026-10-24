import base64
import importlib.util
import io
import os
import struct
import tempfile
import unittest
from pathlib import Path


MODULE_PATH = Path(__file__).with_name("jellyfin-ssh-enrol.py")
SPEC = importlib.util.spec_from_file_location("jellyfin_ssh_enrol", MODULE_PATH)
helper = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(helper)


def key_line(seed=b"k" * 32):
    blob = struct.pack(">I", 11) + b"ssh-ed25519" + struct.pack(">I", 32) + seed
    return b"JFIN-SSH-ENROLL/1 ssh-ed25519 " + base64.b64encode(blob) + b"\n"


class EnrollmentHelperTest(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.home = Path(self.temp.name)
        self.uid, self.gid = os.getuid(), os.getgid()

    def tearDown(self):
        self.temp.cleanup()

    def run_request(self, request, command=""):
        output = io.StringIO()
        previous = helper.sys.stdout
        helper.sys.stdout = output
        try:
            status = helper.main(
                home=str(self.home), uid=self.uid, gid=self.gid,
                environ={"SSH_ORIGINAL_COMMAND": command}, stdin=io.BytesIO(request),
            )
        finally:
            helper.sys.stdout = previous
        return status, output.getvalue()

    def test_installs_key_once_with_restricted_permissions(self):
        status, response = self.run_request(key_line())
        self.assertEqual(status, 0)
        self.assertEqual(response, "JFIN-SSH-ENROLL/1 OK INSTALLED\n")
        ssh_dir = self.home / ".ssh"
        auth = ssh_dir / "authorized_keys"
        self.assertEqual(ssh_dir.stat().st_mode & 0o777, 0o700)
        self.assertEqual(auth.stat().st_mode & 0o777, 0o600)
        self.assertIn(b"ssh-ed25519", auth.read_bytes())

    def test_same_key_is_idempotent(self):
        self.run_request(key_line())
        status, response = self.run_request(key_line())
        self.assertEqual(status, 0)
        self.assertIn("OK ALREADY_PRESENT", response)

    def test_unrelated_existing_key_is_refused(self):
        self.run_request(key_line())
        status, response = self.run_request(key_line(b"z" * 32))
        self.assertEqual(status, 1)
        self.assertIn("EXISTING_KEY_REQUIRES_ADMIN", response)
        self.assertEqual(len((self.home / ".ssh/authorized_keys").read_text().splitlines()), 1)

    def test_rejects_malformed_and_oversized_input(self):
        for request in (b"ssh-rsa fake\n", b"JFIN-SSH-ENROLL/1 ssh-ed25519 !!!\n", b"x" * 4097):
            status, response = self.run_request(request)
            self.assertEqual(status, 1)
            self.assertIn("ERROR INVALID_OR_UNSAFE_STATE", response)
            self.assertFalse((self.home / ".ssh/authorized_keys").exists())

    def test_rejects_commands_and_subsystems(self):
        status, response = self.run_request(key_line(), command="id")
        self.assertEqual(status, 2)
        self.assertIn("COMMAND_NOT_ALLOWED", response)
        self.assertFalse((self.home / ".ssh/authorized_keys").exists())


if __name__ == "__main__":
    unittest.main()
