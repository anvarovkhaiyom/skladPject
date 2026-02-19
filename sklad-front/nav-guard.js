window.showAlert = function (message, title = "Уведомление", type = "info") {
  const modal = document.getElementById("alertModal");
  const titleEl = document.getElementById("alertTitle");
  const btn = document.getElementById("alertCloseBtn");

  titleEl.textContent = title;
  document.getElementById("alertMessage").textContent = message;

  titleEl.style.color = type === "error" ? "#e74c3c" : "#2d9a80";
  if (btn) btn.style.backgroundColor = type === "error" ? "#e74c3c" : "#34dba1";

  modal.style.display = "flex";
  btn.onclick = () => (modal.style.display = "none");
};

document.addEventListener("DOMContentLoaded", () => {
  const role = (localStorage.getItem("role") || "")
    .replace("ROLE_", "")
    .toUpperCase();
  const navItems = document.querySelectorAll(".nav-links li[data-role]");

  navItems.forEach((item) => {
    const allowed = item.getAttribute("data-role").split(",");
    if (!allowed.includes(role)) {
      item.remove();
    }
  });

  const menuToggle = document.getElementById("mobile-menu");
  const navLinks = document.querySelector(".nav-links");

  if (menuToggle) {
    menuToggle.onclick = () => {
      navLinks.classList.toggle("active");
      menuToggle.classList.toggle("is-active");
    };
  }

  window.onclick = (event) => {
    if (event.target.classList.contains("modal")) {
      event.target.style.display = "none";
    }
  };

  const whName = localStorage.getItem("warehouseName");
  const whDisplay = document.getElementById("currentWarehouseName");
  if (whDisplay && whName) {
    whDisplay.textContent = `| ${whName}`;
  }

  const path = window.location.pathname;
  document.querySelectorAll(".nav-links a").forEach((link) => {
    if (link.getAttribute("href") === path) link.classList.add("active");
  });
});

window.logout = function () {
  localStorage.clear();
  window.location.href = "/index.html";
};
