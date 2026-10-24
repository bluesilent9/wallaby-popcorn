# Android Mobile build and update policy

The latest Mobile SSH repair built successfully on 25 October 2026. All21 focused unit tests passed, and both requested phones were updated in place. Both completed SSH connection, automatic sign-in and media-library loading. Playback and external-network tests were not performed in that validation.

## Stable source and upstream updates

`Stable` holds the tested Mobile source. The fork remains based on Jellyfin Android commit `21795394ea14854330784862221d424b8bfa152a`; publishing this repair does not merge newer upstream features. Upstream changes are reviewed and incorporated only when the maintainer requests them.

Jellyfin's original `buildSrc/src/main/kotlin/VersionUtils.kt` is retained unchanged, including its development fallback `0.0.0-dev.1` and its existing release-version override. Do not invent a Wallaby-specific version sequence. Already installed repair APKs retain their historical1.0.0-dev.1/code1000001 metadata. A future update must use an appropriate upstream release number/version code above the installed code to preserve Android's normal upgrade behavior. Do not uninstall or clear data to work around version numbering.

## When another build is requested

- Build only the Android Mobile libre variant, one worker at a time. Do not launch TV, desktop, aggregate builds or GitHub APK builds without a separate request.
- Keep local SDK paths and the existing private signing key outside Git. Match the installed signing certificate and application ID `org.jellyfin.mobile.debug` to retain saved SSH keys, pinned host keys and Jellyfin login.
- Run `:app:testLibreDebugUnitTest :app:assembleLibreDebug` with the established JDK/SDK. Use the existing version override when producing an upstream-numbered release.
- Verify the APK signature, version, label, artwork and repaired SSH source; update in place with ADB, then check connection, retained sign-in and playback.
- `docs/PRESERVED_SSH_SHA256.json` records the earlier baseline. `docs/SSH_REPAIR_SHA256.json` records the deliberately changed connection sources. The repair retains IPv6 first, fallback, strict host verification and loopback-only forwarding.

The source publication restores upstream version logic only; it does not trigger a new build. Inherited workflows remain manual-only placeholders with original workflow references retained under `docs/upstream-workflows`.
