# Android Mobile SSH repair

The system hostname lookup can omit AAAA records on Android Wi-Fi without an IPv6 default route, even when the SSH server is on the directly reachable local IPv6 subnet. The existing IPv6-first socket factory only sees the returned IPv4 address in that case.

Resolve an explicit AAAA query when the system lookup yields no IPv6 address. Use Android’s platform DnsResolver on Android 10+, preserving its network and Private DNS. Older Android queries the active network’s configured DNS servers with bounded timeouts; it does not bypass active Private DNS. No domain, server address or external DNS service is hardcoded. All resolved addresses still pass through the existing IPv6-first connection/fallback and pinned host-key verification.

Connection errors now inspect the full exception chain. Android’s nested ErrnoException previously hid an enclosing ConnectException and produced misleading SSH-port advice. Messages distinguish hostname lookup, routes, timeout, refused connections, interrupted connections, authentication, host identity, algorithm negotiation and tunnel failures.

The original preserved-source manifest documents the earlier baseline. SSH_REPAIR_SHA256.json records the repaired source. Other original SSH behavior, stored device keys, host pins, application ID, signing key and artwork remain unchanged.

Validation: Mobile has 21 regression tests covering address parsing, address ordering/fallback, omitted AAAA recovery, bounded DNS responses and connection error classification. See the build and APK verification report delivered with the repaired APK for execution results. TV uses its existing later working APK; no TV build is required for this Mobile fix.
