# THDR Personal Estimator PWA

This is a separate, local-first copy of the standalone estimator. It does not replace the Base44 estimator or the public website.

## What stays on the device

The estimator stores its editable price book and saved proposals in the browser's local storage. That data belongs to this browser profile and app address; it does not sync to Base44 or another phone. Export a price-book backup before moving the app to a different address.

## Local preview

From this folder, run `python -m http.server 4176` and open `http://127.0.0.1:4176/` on the computer. This is only a local preview; that URL will not work on your phone.

## Install on Android

To install from Chrome, host this folder at a private HTTPS address, open it once while online, wait for the offline cache to finish, then choose **Install app** or **Add to Home screen**. The service worker caches the app shell for offline use after the first successful online visit.

Do not host this folder publicly without access protection. It contains internal estimator and pricing logic. This starter is not yet a Play Store package and does not provide cloud backup or cross-device sync.
