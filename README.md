# Wallaby Popcorn

<img src="branding/wallaby-popcorn.png" width="256" alt="Wallaby with a bucket of popcorn in a cinema">

Your Jellyfin library, with built-in SSH tunnelling and a little cinema magic.

Wallaby Popcorn is an independent fork of [Jellyfin Android](https://github.com/jellyfin/jellyfin-android). It keeps Jellyfin’s navigation, library and playback functionality, adds the existing integrated SSH setup, and applies Wallaby Popcorn branding. It is not an official Jellyfin release.

## Development branding work

`Development` adds transparent Wallaby Popcorn branding for the web header, splash, default logo images, screensaver and favicons, using the same mascot and popcorn bucket as the launcher. It includes wordmarks for light and dark backgrounds and a transparent native connection-screen logo. This work is **source only: not compiled, tested or installed**, and has not been promoted to `Stable`. See [branding notes](docs/DEVELOPMENT_BRANDING.md).

## Current scope and status

Android **Mobile only**, using the libre variant. The SSH repair was built and installed on two phones on 25 October 2026. Both reconnected through SSH, signed in automatically and displayed the media library. All 21 focused tests passed. Playback and external-network testing remain separate checks. Android TV, desktop and other Wallaby Popcorn packages are future work.

## Preserved connection behaviour

- IPv6-first DNS address selection for both SSH host-key probes and authenticated connections, with the existing resolved-address fallback.
- Existing first-time key setup, pinned SSH host key and strict host-key checking during login.
- The stable local tunnel port `18096` and existing Jellyfin session handling.
- Existing server-side forwarding target and restricted-key compatibility.

The original IPv6-first connection path is retained. The latest repair explicitly retrieves IPv6 DNS records when Android omits them from a hostname lookup, and gives clearer connection errors. See [SSH_REPAIR.md](docs/SSH_REPAIR.md). No server configuration change is required.

## Branding and updates

Midnight plum surfaces, popcorn-gold actions, teal accents and the approved cinema wallaby illustration. Main and debug resource sets both display **Wallaby Popcorn**. Internal Kotlin namespaces and the installed package ID `org.jellyfin.mobile.debug` are deliberately retained for compatibility; the technical ID is not the displayed app name.

Each update must be signed with the same private signing key as the installed SSH app. That key is not included here. Do not uninstall the existing app before updating: uninstalling removes its saved keys and app data.

## Source history and upstream updates

Base: Jellyfin Android commit `21795394ea14854330784862221d424b8bfa152a`. The tested Mobile source lives on the `Stable` branch. Jellyfin upstream changes are incorporated only when the maintainer chooses: review and merge selected upstream changes into a separate working branch, test SSH and playback, then promote a tested revision to `Stable`. A fork retains full source and history; Git records the differences in our commits. Upstream updates are not automatic. The source uses Jellyfin’s original version-name and version-code logic; the upstream development fallback is `0.0.0-dev.1`, with release numbers supplied through the existing version override. Wallaby Popcorn does not introduce a separate version sequence. The APK already installed on the two phones retains its earlier `1.0.0-dev.1` metadata; this source-only correction does not rebuild or downgrade those installations. Future updates must keep an Android version code that can upgrade them.

See [BUILD_NEXT.md](BUILD_NEXT.md) for the single-variant build and validation policy. Inherited upstream workflows are retained as disabled reference files in `docs/upstream-workflows`; active workflow stubs are manual-only and cannot compile an APK.

## Attribution and licence

Based on Jellyfin Android by the Jellyfin contributors. Existing copyright notices and [GPL-2.0 licence](LICENSE.md) are preserved. The app still connects to a Jellyfin server and uses its web client; server and protocol references to Jellyfin remain where they describe functionality. Wallaby Popcorn artwork was generated with OpenAI image generation for this project; the approved original and generation brief are in `branding/`.
