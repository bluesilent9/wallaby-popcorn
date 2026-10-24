#!/usr/bin/python3
"""Fail-closed, one-key enrollment helper for an sshd ForceCommand."""

import base64
import fcntl
import os
import pwd
import re
import stat
import sys
import tempfile

PROTOCOL = b"JFIN-SSH-ENROLL/1 "
MAX_REQUEST = 4096
KEY_PATTERN = re.compile(rb"ssh-ed25519 ([A-Za-z0-9+/]+={0,2})(?: ([^\r\n]{1,128}))?\n?$")


def _response(code, detail):
    sys.stdout.write(f"JFIN-SSH-ENROLL/1 {code} {detail}\n")
    sys.stdout.flush()


def _read_key(stream):
    data = stream.readline(MAX_REQUEST + 1)
    if len(data) > MAX_REQUEST or not data.endswith(b"\n"):
        raise ValueError("invalid request length")
    if stream.read(1):
        raise ValueError("unexpected trailing input")
    if not data.startswith(PROTOCOL):
        raise ValueError("unsupported protocol")
    match = KEY_PATTERN.fullmatch(data[len(PROTOCOL):])
    if not match:
        raise ValueError("only a valid ssh-ed25519 public key is accepted")
    encoded = match.group(1)
    blob = base64.b64decode(encoded, validate=True)
    if len(blob) != 51 or not blob.startswith(b"\x00\x00\x00\x0bssh-ed25519\x00\x00\x00\x20"):
        raise ValueError("invalid Ed25519 key blob")
    return b"ssh-ed25519 " + encoded


def enroll(home, key, uid, gid):
    ssh_dir = os.path.join(home, ".ssh")
    try:
        os.mkdir(ssh_dir, 0o700)
    except FileExistsError:
        pass
    info = os.lstat(ssh_dir)
    if not stat.S_ISDIR(info.st_mode) or info.st_uid != uid or info.st_gid != gid:
        raise PermissionError("unsafe .ssh directory")
    os.chmod(ssh_dir, 0o700)

    lock_path = os.path.join(ssh_dir, ".jellyfin-enroll.lock")
    lock_fd = os.open(lock_path, os.O_CREAT | os.O_RDWR | getattr(os, "O_NOFOLLOW", 0), 0o600)
    try:
        os.fchmod(lock_fd, 0o600)
        fcntl.flock(lock_fd, fcntl.LOCK_EX)
        auth_path = os.path.join(ssh_dir, "authorized_keys")
        try:
            auth_info = os.lstat(auth_path)
        except FileNotFoundError:
            auth_info = None
        if auth_info is not None:
            if not stat.S_ISREG(auth_info.st_mode) or auth_info.st_uid != uid or auth_info.st_gid != gid:
                raise PermissionError("unsafe authorized_keys file")
            with open(auth_path, "rb") as existing_file:
                existing = existing_file.read(1024 * 1024 + 1)
            if len(existing) > 1024 * 1024:
                raise ValueError("authorized_keys is too large")
            for line in existing.splitlines():
                fields = line.strip().split()
                for index, field in enumerate(fields[:-1]):
                    if field == b"ssh-ed25519" and fields[index + 1] == key.split()[1]:
                        os.chmod(auth_path, 0o600)
                        return "ALREADY_PRESENT"
            raise FileExistsError("authorized_keys already contains another key")

        fd, temp_path = tempfile.mkstemp(prefix=".authorized_keys.", dir=ssh_dir)
        try:
            os.fchmod(fd, 0o600)
            os.fchown(fd, uid, gid)
            with os.fdopen(fd, "wb") as output:
                output.write(key + b" jellyfin-android\n")
                output.flush()
                os.fsync(output.fileno())
            os.link(temp_path, auth_path)
            directory_fd = os.open(ssh_dir, os.O_RDONLY | getattr(os, "O_DIRECTORY", 0))
            try:
                os.fsync(directory_fd)
            finally:
                os.close(directory_fd)
            return "INSTALLED"
        finally:
            try:
                os.unlink(temp_path)
            except FileNotFoundError:
                pass
    finally:
        os.close(lock_fd)


def main(home=None, uid=None, gid=None, environ=None, stdin=None):
    env = os.environ if environ is None else environ
    # sshd sets SSH_ORIGINAL_COMMAND for any requested command/subsystem.
    if env.get("SSH_ORIGINAL_COMMAND", ""):
        _response("ERROR", "COMMAND_NOT_ALLOWED")
        return 2
    if home is None:
        account = pwd.getpwuid(os.getuid())
        home, uid, gid = account.pw_dir, account.pw_uid, account.pw_gid
    try:
        key = _read_key(sys.stdin.buffer if stdin is None else stdin)
        result = enroll(home, key, uid, gid)
        _response("OK", result)
        return 0
    except FileExistsError:
        _response("ERROR", "EXISTING_KEY_REQUIRES_ADMIN")
    except (ValueError, PermissionError, OSError) as exc:
        # Do not return filesystem details to the remote client.
        _response("ERROR", "INVALID_OR_UNSAFE_STATE")
    return 1


if __name__ == "__main__":
    raise SystemExit(main())
