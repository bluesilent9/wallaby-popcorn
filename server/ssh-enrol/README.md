# Restricted SSH key enrollment for Jellyfin Android

This directory contains a narrowly scoped `ForceCommand` enrollment helper. The client protocol is one line:

```text
JFIN-SSH-ENROLL/1 ssh-ed25519 BASE64_KEY [comment]
```

The response is `JFIN-SSH-ENROLL/1 OK INSTALLED`, `... OK ALREADY_PRESENT`, or a stable `... ERROR CODE`. The helper accepts no command or subsystem request. It creates only `~/.ssh/authorized_keys`, only when that file is absent. An exact existing Ed25519 key is idempotent; any other existing key file requires administrator intervention.

## Install on Kubuntu

Review the script, then install it root-owned and not writable by the target accounts:

```sh
sudo install -o root -g root -m 0755 jellyfin-ssh-enrol.py /usr/local/libexec/jellyfin-ssh-enrol.py
```

Merge `sshd-match.conf` into the server's existing `Match Group ssh-tunnel-only` block. Do not duplicate `Match` blocks blindly: OpenSSH uses the first obtained value for many directives, so inspect all included files and existing match blocks. The intended effective settings for that group are `MaxSessions 1`, the forced helper, and the existing `PermitOpen 127.0.0.1:8096` restrictions shown in the sample. The trusted accounts must remain outside this match block.

Validate before applying, then reload SSH without closing the current administrator session:

```sh
sudo sshd -t
sudo sshd -T -C user=dave-mobile,host=calder.vip,addr=127.0.0.1 | grep -E '^(maxsessions|forcecommand|allowtcpforwarding|permitopen)'
sudo sshd -T -C user=dave-laptop,host=calder.vip,addr=127.0.0.1 | grep -E '^(maxsessions|forcecommand|allowtcpforwarding|permitopen)'
sudo systemctl reload ssh
```

From a test client, verify that a shell command and SFTP request are rejected, and that the only local forward accepted is `127.0.0.1:8096`. Verify trusted account shell and file transfer from a separate session. Never test by replacing the live config without a rollback path.

## Authentication and security model

The helper runs as the authenticated Unix account, so it does not need root to write that account's key file. `ForceCommand` replaces every requested command for the matched group. The helper then rejects non-empty `SSH_ORIGINAL_COMMAND`; SFTP subsystem requests also become forced-helper invocations and cannot start an SFTP server. A session can carry only the bounded enrollment record. TCP forwarding does not require a session channel and remains limited by `PermitOpen`.

The sample enables one session channel for the group because `MaxSessions 0` blocks enrollment. The forced command confines that channel. Do not treat `MaxSessions 1` by itself as a security boundary.

After a public key is enrolled, a client choosing public-key authentication does **not** make the server reject passwords. OpenSSH still permits password authentication if it is enabled by the effective server configuration. This bootstrap design leaves password authentication available to tunnel-only accounts, but the forced command still confines any authenticated session. Disabling passwords only after per-account enrollment is not implemented here; do not disable password authentication globally as part of rollout.

Host-key verification must happen in the client before it sends the account password. The helper is not a substitute for host-key pinning. The private key belongs on the device only and must never be sent to this script; only its public half is accepted.

The Android app integration and a physical-device/server end-to-end test are separate requirements. This helper package alone does not establish that the app already implements SSH enrollment or tunneling.
