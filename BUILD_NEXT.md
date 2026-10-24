# Build paused until reboot

Do not run Gradle, compile, package, install, or trigger GitHub Actions before the user returns after reboot and authorizes the build.

Prepared checkout in this chat: `/home/dave/Documents/Codex/2026-10-24/i/work/wallaby-popcorn`.

## Next session

1. Confirm this branch and the saved validation results. Do not change the preserved SSH implementation.
2. Restore local-only SDK configuration (`sdk.dir=/home/dave/Documents/Codex/android-sdk`) or set ANDROID_HOME. No SDK paths or signing credentials are published.
3. Build **only** `:app:assembleLibreDebug` with the established JDK/SDK. The initial APK follows the already device-tested variant; the visible label and artwork are Wallaby Popcorn. Do not use aggregate `build` or `assembleDebug`, which may compile multiple flavours. A later hardened release is separate work.
4. Run the existing focused unit tests for the mobile variant after the reboot. The seven baseline tests passed before rebranding; they have not been rerun during this no-build preparation.
5. Sign the resulting APK with the original app signing key, using the earlier local signing setup. Compare its certificate to the installed APK. Do not publish signing keys, local passwords or credentials.
6. Verify application ID `org.jellyfin.mobile.debug`, label Wallaby Popcorn, upgraded version code, expected icon and a complete dex package before installation.
7. Install as an update, then test startup, saved SSH identity/pin, retained Jellyfin login, IPv6/mobile-data connection, LAN connection, video playback, audio controls and reconnect.
8. Publish only the validated Android Mobile APK. No TV or desktop builds.

## Preparation checks

Only source comparisons, XML/resource checks, JavaScript syntax checks and artwork/layout previews are permitted during preparation. They do not establish that the APK compiles or works on a device. Full Android validation is pending the reboot/build phase.
