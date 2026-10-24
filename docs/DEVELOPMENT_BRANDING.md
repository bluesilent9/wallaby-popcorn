# Development-only logo replacement

Status: source changes only. No compilation, test run, APK build or sideload requested. Stable remains at c95908f7431136ea97c38ed1e878e4b14c0acd2a. This work is based on that revision and preserves its SSH source and upstream version logic.

## Why logos remained

The Android app loads Jellyfin's web interface from the connected server. The previous theme changed only `.pageTitleWithDefaultLogo`, leaving the server's splash, default image logos, screensaver and favicons in place. The installed server currently uses hashed `icon-transparent`, `banner-light`, `banner-dark`, `notificationicon` and `touchicon` asset names. The Android WebView now substitutes only those known branding filenames, including hashed variants, with bundled artwork. The branding script also replaces default logo image sources after SPA navigation and picks a wordmark that contrasts with the rendered background. CSS covers initial splash/header backgrounds and centres the screensaver mark. Custom media artwork and arbitrary server-supplied images are not matched.

## Artwork

The existing launcher illustration and launcher resources remain unchanged. The generated transparent cutout retains the same wallaby, expression, popcorn bucket, colours and 3D style. Its PNG alpha spans0–255. The native connection screen and native in-app logo use this transparent mark. Two SVG wordmarks embed the same PNG and add the Wallaby Popcorn name in light or dark lettering. They are self-contained so image loading does not depend on external SVG resources.

Assets: `app/src/main/assets/native/wallaby-popcorn-mark.png`, `wallaby-popcorn-wordmark-light.svg`, `wallaby-popcorn-wordmark-dark.svg`; native copy: `app/src/main/res/drawable-nodpi/wallaby_mark.png`.

The built-in image generation tool was used, with the original `branding/wallaby-popcorn.png` as an edit reference. Full prompt is recorded in `branding/transparent-logo-prompt.txt`.

## Deferred validation

When a build is requested, inspect native connection and settings screens, web splash/header/login/about dialogs, theme changes, logo screensaver, favicons and repeated navigation. Confirm image cache behaviour and normal playback/SSH reconnect then. No claim of runtime coverage is made before that work. If an upstream web client introduces a new logo asset name or inline SVG, its specific branding selector/path may require an additional mapping.
