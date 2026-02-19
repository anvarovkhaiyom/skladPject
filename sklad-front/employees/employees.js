const API_BASE = "http://194.163.157.81:9090/admin/employee";
const API_WAREHOUSES = "http://194.163.157.81:9090/super/warehouses";

function getHeaders() {
  return {
    "Content-Type": "application/json",
    Authorization: "Bearer " + localStorage.getItem("token"),
  };
}
document.addEventListener("DOMContentLoaded", async () => {
  const role = localStorage.getItem("role");

  if (role === "SUPER_ADMIN" || role === "ROLE_SUPER_ADMIN") {
    const whGroup = document.getElementById("warehouseFilterGroup");
    if (whGroup) whGroup.style.display = "flex";
    await fillWarehouseFilter();
  }

  loadEmployees();
});
async function loadEmployees() {
  const role = localStorage.getItem("role") || "";
  let url = API_BASE;

  if (role.includes("SUPER_ADMIN")) {
    const warehouseId = document.getElementById("warehouseFilter")?.value;
    if (warehouseId) {
      url += `?warehouseId=${warehouseId}`;
    }
  }

  try {
    const res = await fetch(url, { headers: getHeaders() });
    if (res.ok) {
      const data = await res.json();
      renderTable(data);
    } else {
      showAlert("Не удалось загрузить список сотрудников", "Ошибка", "error");
    }
  } catch (err) {
    showAlert("Ошибка сети при загрузке сотрудников", "Ошибка", "error");
  }
}

function renderTable(data) {
  const body = document.getElementById("employeeBody");
  const role = localStorage.getItem("role") || "";
  body.innerHTML = "";

  // Проверка заголовков таблицы для супер-админа
  const headerRow = document.querySelector("#employeeTable thead tr");
  if (
    role.includes("SUPER_ADMIN") &&
    headerRow &&
    headerRow.cells.length === 5
  ) {
    const th = document.createElement("th");
    th.innerText = "Склад";
    headerRow.insertBefore(th, headerRow.cells[4]); // Вставляем перед действиями
  }

  data.forEach((emp) => {
    // Безопасная передача данных в атрибут через Base64
    const empData = btoa(unescape(encodeURIComponent(JSON.stringify(emp))));

    let warehouseCell = role.includes("SUPER_ADMIN")
      ? `<td data-label="Склад">${emp.warehouseName || "Глобальный"}</td>`
      : "";

    body.innerHTML += `
      <tr>
        <td data-label="ФИО"><strong>${emp.fullName}</strong></td>
        <td data-label="Логин">${emp.login}</td>
        <td data-label="Должность">${emp.position || "—"}</td>
        <td data-label="Роль"><span class="badge ${emp.role.toLowerCase()}">${emp.role}</span></td>
        ${warehouseCell}
        <td data-label="Действия">
          <button class="btn-edit" onclick="prepareEdit('${empData}')">⚙️</button>
          <button class="btn-delete" onclick="deleteEmployee(${emp.id})">🗑️</button>
        </td>
      </tr>`;
  });
}

async function saveEmployee() {
  const id = document.getElementById("empId").value;
  const payload = {
    fullName: document.getElementById("empFullName").value.trim(),
    login: document.getElementById("empLogin").value.trim(),
    password: document.getElementById("empPassword").value,
    position: document.getElementById("empPosition").value,
    role: document.getElementById("empRole").value,
    warehouseId: document.getElementById("empWarehouse").value || null,
  };

  if (!payload.fullName || !payload.login) {
    return showAlert("Заполните ФИО и Логин!", "Внимание", "error");
  }

  const method = id ? "PUT" : "POST";
  const url = id ? `${API_BASE}/${id}` : API_BASE;

  try {
    const res = await fetch(url, {
      method: method,
      headers: getHeaders(),
      body: JSON.stringify(payload),
    });

    if (res.ok) {
      closeModal();
      loadEmployees();
      showAlert(
        id ? "Данные сотрудника обновлены" : "Сотрудник успешно создан",
        "Успех",
        "success",
      );
    } else {
      const err = await res.json();
      showAlert(err.message || "Ошибка при сохранении", "Ошибка", "error");
    }
  } catch (err) {
    showAlert("Ошибка сервера при сохранении", "Ошибка", "error");
  }
}

async function deleteEmployee(id) {
  if (!confirm("Удалить доступ сотруднику?")) return;

  try {
    const res = await fetch(`${API_BASE}/${id}`, {
      method: "DELETE",
      headers: getHeaders(),
    });

    if (res.ok) {
      loadEmployees();
      showAlert("Доступ аннулирован", "Успех", "info");
    } else {
      showAlert("Не удалось удалить сотрудника", "Ошибка", "error");
    }
  } catch (e) {
    showAlert("Ошибка при выполнении запроса", "Ошибка", "error");
  }
}

async function fillWarehouseFilter() {
  try {
    const res = await fetch(API_WAREHOUSES, { headers: getHeaders() });
    if (res.ok) {
      const warehouses = await res.json();
      const filterSelect = document.getElementById("warehouseFilter");
      const modalSelect = document.getElementById("empWarehouse");

      warehouses.forEach((w) => {
        const opt = `<option value="${w.id}">${w.name}</option>`;
        if (filterSelect) filterSelect.innerHTML += opt;
        if (modalSelect) modalSelect.innerHTML += opt;
      });
    }
  } catch (e) {
    console.error("Ошибка загрузки складов для фильтра", e);
  }
}
function prepareEdit(encodedData) {
  const emp = JSON.parse(decodeURIComponent(escape(atob(encodedData))));
  editEmployee(emp);
}
function openModal() {
  const role = localStorage.getItem("role");

  document.getElementById("empId").value = "";
  document.getElementById("empFullName").value = "";
  document.getElementById("empLogin").value = "";
  document.getElementById("empPassword").value = "";
  document.getElementById("empPosition").value = "";
  document.getElementById("empRole").value = "ROLE_EMPLOYEE";
  document.getElementById("empWarehouse").value = "";

  if (
    role &&
    (role.includes("SUPER_ADMIN") || role.includes("ROLE_SUPER_ADMIN"))
  ) {
    document.getElementById("empWarehouseGroup").style.display = "block";
  }

  document.getElementById("modalTitle").innerText = "Новый сотрудник";
  document.getElementById("employeeModal").style.display = "flex";
}

function editEmployee(emp) {
  const role = localStorage.getItem("role");

  document.getElementById("empId").value = emp.id;
  document.getElementById("empFullName").value = emp.fullName;
  document.getElementById("empLogin").value = emp.login;
  document.getElementById("empPassword").value = "";
  document.getElementById("empPosition").value = emp.position || "";
  document.getElementById("empRole").value = emp.role;

  if (
    role &&
    (role.includes("SUPER_ADMIN") || role.includes("ROLE_SUPER_ADMIN"))
  ) {
    document.getElementById("empWarehouseGroup").style.display = "block";
    document.getElementById("empWarehouse").value = emp.warehouseId || "";
  }

  document.getElementById("modalTitle").innerText = "Редактировать сотрудника";
  document.getElementById("employeeModal").style.display = "flex";
}

function closeModal() {
  document.getElementById("employeeModal").style.display = "none";
}

loadEmployees();
