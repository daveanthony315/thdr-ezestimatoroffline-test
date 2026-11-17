if ("serviceWorker" in navigator) {
  window.addEventListener("load", () => {
    navigator.serviceWorker.register("/sw.js", { scope: "/" }).catch((error) => {
      console.error("Could not enable offline support:", error);
    });
  });
} else {
  console.warn("This browser does not support service workers.");
}
