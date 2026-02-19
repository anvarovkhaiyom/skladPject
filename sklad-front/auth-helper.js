function getAuthHeaders() {
  const token = localStorage.getItem("token");
  if (!token) {
    window.location.href = "/index.html";
    return {};
  }
  return {
    Authorization: "Bearer " + token,
    "Content-Type": "application/json",
  };
}

function logout() {
  localStorage.clear();
  window.location.href = "/index.html";
}
