function login() {
  const username = document.getElementById("login").value;
  const password = document.getElementById("password").value;

  const errorElement = document.getElementById("error-message");
  if (errorElement) errorElement.textContent = "";

  fetch("http://194.163.157.81:9090/auth/login", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Accept: "application/json",
    },
    credentials: "include",
    body: JSON.stringify({ username: username, password: password }),
  })
    .then((response) => {
      if (!response.ok) {
        if (response.status === 403) {
          throw new Error(
            "Доступ запрещен. Возможно, ваш аккаунт деактивирован.",
          );
        }

        const contentType = response.headers.get("content-type");
        if (contentType && contentType.includes("application/json")) {
          return response.json().then((err) => {
            throw new Error(err.error || "Неверные учетные данные");
          });
        } else {
          return response.text().then((text) => {
            throw new Error(text || "Ошибка сервера (" + response.status + ")");
          });
        }
      }
      return response.json();
    })
    .then((data) => {
      console.log("Ответ сервера:", data);

      if (!data.token || !data.role) {
        throw new Error("Ошибка: Токен или роль не получены!");
      }

      if (typeof localStorage !== "undefined") {
        localStorage.setItem("token", data.token);
        localStorage.setItem("role", data.role);

        if (data.fullName) {
          localStorage.setItem("userName", data.fullName);
        }

        if (data.warehouseId) {
          localStorage.setItem("warehouseId", data.warehouseId);
        }

        if (data.warehouseName) {
          localStorage.setItem("warehouseName", data.warehouseName);
        } else if (data.role.includes("SUPER_ADMIN")) {
          localStorage.setItem("warehouseName", "Главный офис");
        } else {
          localStorage.setItem("warehouseName", "Склад не назначен");
        }
      } else {
        throw new Error("Ошибка: localStorage недоступен!");
      }

      redirectUser(data.role);
    })
    .catch((error) => {
      console.error("Ошибка:", error);
      if (typeof showAlert === "function") {
        showAlert(error.message, "Ошибка авторизации");
      } else {
        if (errorElement) errorElement.textContent = error.message;
        alert(error.message);
      }
    });
}
function redirectUser(role) {
  if (!role) return;

  const cleanRole = role.trim().toUpperCase();

  if (cleanRole === "SUPER_ADMIN" || cleanRole === "ROLE_SUPER_ADMIN") {
    window.location.href = "/admin/warehouses.html";
  } else if (cleanRole === "ADMIN" || cleanRole === "ROLE_ADMIN") {
    window.location.href = "/category/categories.html";
  } else if (cleanRole === "EMPLOYEE" || cleanRole === "ROLE_EMPLOYEE") {
    window.location.href = "/checkout/checkout.html";
  } else {
    const errorElement = document.getElementById("error-message");
    if (errorElement) {
      errorElement.textContent = `Ошибка: Неизвестная роль (${cleanRole})`;
    }
  }
}
function updateDateTime() {
  const now = new Date();

  const time = now.toLocaleTimeString("ru-RU", {
    hour: "2-digit",
    minute: "2-digit",
  });

  const date = now.toLocaleDateString("ru-RU", {
    weekday: "long",
    day: "2-digit",
    month: "long",
    year: "numeric",
  });

  document.getElementById("currentTime").textContent = time;
  document.getElementById("currentDate").textContent = date;
}
function showAlert(message, title = "Уведомление") {
  const alertModal = document.getElementById("alertModal");
  document.getElementById("alertTitle").textContent = title;
  document.getElementById("alertMessage").textContent = message;

  alertModal.style.display = "block";
  document.getElementById("alertCloseBtn").onclick = () => {
    alertModal.style.display = "none";
  };
}
updateDateTime();
setInterval(updateDateTime, 1000);
