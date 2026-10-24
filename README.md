# Wallaby Popcorn

<img src="branding/wallaby-popcorn.png" width="256" alt="Wallaby with a bucket of popcorn in a cinema">

Your Jellyfin library, with built-in SSH tunnelling and a little cinema magic.

Wallaby Popcorn is an independent fork of [Jellyfin Android](https://github.com/jellyfin/jellyfin-android). It keeps Jellyfin’s navigation, library and playback functionality, adds the existing integrated SSH setup, and applies Wallaby Popcorn branding. It is not an official Jellyfin release.

## Current scope and status

Android **Mobile only**, initially the libre variant used by the tested SSH app. Branding preparation is complete; **no Wallaby Popcorn APK has been built yet**. Build and device validation are paused until the requested computer reboot. Android TV, desktop and other packages are future work.

## Preserved connection behaviour

- IPv6-first DNS address selection for both SSH host-key probes and authenticated connections, with the existing resolved-address fallback.
- Existing first-time key setup, pinned SSH host key and strict host-key checking during login.
- The stable local tunnel port `18096` and existing Jellyfin session handling.
- Existing server-side forwarding target and restricted-key compatibility.

The connection implementation is carried over byte-for-byte from the Android Mobile source successfully tested over IPv6/mobile data on 24 October 2026. Branding changes do not require a server configuration change.

## Branding and updates

Midnight plum surfaces, popcorn-gold actions, teal accents and the approved cinema wallaby illustration. Main and debug resource sets both display **Wallaby Popcorn**. Internal Kotlin namespaces and the installed package ID `org.jellyfin.mobile.debug` are deliberately retained for compatibility; the technical ID is not the displayed app name.

The eventual update must be signed with the same private signing key as the installed SSH app. That key is not included here. Do not uninstall the existing app before updating: uninstalling removes its saved keys and app data.

## Source history and upstream updates

Base: Jellyfin Android commit `21795394ea14854330784862221d424b8bfa152a`. The app’s SSH and branding work lives on the `wallaby-popcorn` branch. Keep upstream changes separate, review merges into this branch, then repeat SSH and playback checks. A fork retains full source and history; Git records the differences in our commits. Upstream updates are not automatic.

See [BUILD_NEXT.md](BUILD_NEXT.md) for the staged single-variant build and validation plan. Inherited upstream workflows are retained as disabled reference files in `docs/upstream-workflows`; active workflow stubs are manual-only and cannot compile an APK.

## Attribution and licence

Based on Jellyfin Android by the Jellyfin contributors. Existing copyright notices and [GPL-2.0 licence](LICENSE.md) are preserved. The app still connects to a Jellyfin server and uses its web client; server and protocol references to Jellyfin remain where they describe functionality. Wallaby Popcorn artwork was generated with OpenAI image generation for this project; the approved original and generation brief are in `branding/`.
