export function getCurrentUsername() {
  return localStorage.getItem("username");
}

export function notifyCartChanged() {
  window.dispatchEvent(new Event("storage"));
}
