const API_BASE = "http://localhost:9090/admin/employee";
const API_WAREHOUSES = "http://localhost:9090/super/warehouses";

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
const API_DRIVER = "http://localhost:9090/employee/driver";

document.addEventListener("DOMContentLoaded", async () => {
  const role = localStorage.getItem("role");
  const isSuper = role === "SUPER_ADMIN" || role === "ROLE_SUPER_ADMIN";

  if (isSuper) {
    document.getElementById("warehouseFilterGroup").style.display = "flex";
    document.getElementById("driverWhHeader").style.display = "table-cell";
    await fillWarehouseFilter();
  }

  loadEmployees();
  loadDrivers(); // Загружаем водителей тоже
});
loadEmployees();
async function fillWarehouseDropdowns() {
  try {
    const res = await fetch(API_WAREHOUSES, { headers: getHeaders() });
    if (res.ok) {
      const warehouses = await res.json();
      const filterSelect = document.getElementById("warehouseFilter");
      const empWhSelect = document.getElementById("empWarehouse");
      const drWhSelect = document.getElementById("drWarehouse");

      warehouses.forEach((w) => {
        const opt = `<option value="${w.id}">${w.name}</option>`;
        if (filterSelect) filterSelect.innerHTML += opt;
        if (empWhSelect) empWhSelect.innerHTML += opt;
        if (drWhSelect) drWhSelect.innerHTML += opt;
      });
    }
  } catch (e) {
    console.error("Ошибка заполнения списков складов", e);
  }
}

function openDriverModal() {
  document.getElementById("drId").value = "";
  document.getElementById("drFullName").value = "";
  document.getElementById("drPhone").value = "";
  document.getElementById("drCarMark").value = "";
  document.getElementById("drCarNumber").value = "";
  document.getElementById("drInfo").value = "";
  document.getElementById("drWarehouse").value = "";

  const role = localStorage.getItem("role");
  if (role.includes("SUPER_ADMIN")) {
    document.getElementById("drWarehouseGroup").style.display = "block";
  }

  document.getElementById("driverModalTitle").innerText = "Новый водитель";
  document.getElementById("driverModal").style.display = "flex";
}

function prepareDriverEdit(encoded) {
  const dr = JSON.parse(decodeURIComponent(escape(atob(encoded))));
  document.getElementById("drId").value = dr.id;
  document.getElementById("drFullName").value = dr.fullName;
  document.getElementById("drPhone").value = dr.phone || "";
  document.getElementById("drCarMark").value = dr.carMark || "";
  document.getElementById("drCarNumber").value = dr.carNumber || "";
  document.getElementById("drInfo").value = dr.additionalInfo || "";

  const role = localStorage.getItem("role");
  if (role.includes("SUPER_ADMIN")) {
    document.getElementById("drWarehouseGroup").style.display = "block";
    document.getElementById("drWarehouse").value = dr.warehouseId || "";
  }

  document.getElementById("driverModalTitle").innerText =
    "Редактировать водителя";
  document.getElementById("driverModal").style.display = "flex";
}

function closeDriverModal() {
  document.getElementById("driverModal").style.display = "none";
}

async function deleteDriver(id) {
  if (!confirm("Удалить водителя?")) return;
  const res = await fetch(`${API_DRIVER}/${id}`, {
    method: "DELETE",
    headers: getHeaders(),
  });
  if (res.ok) {
    loadDrivers();
    showAlert("Водитель удален", "Инфо", "info");
  }
}

document.getElementById("warehouseFilter").onchange = refreshAllData;

function getHeaders() {
  return {
    "Content-Type": "application/json",
    Authorization: "Bearer " + localStorage.getItem("token"),
  };
}

document.addEventListener("DOMContentLoaded", async () => {
  const role = localStorage.getItem("role") || "";
  const isSuper = role.includes("SUPER_ADMIN");

  if (isSuper) {
    const whGroup = document.getElementById("warehouseFilterGroup");
    if (whGroup) whGroup.style.display = "flex";

    const drWhHeader = document.getElementById("driverWhHeader");
    if (drWhHeader) drWhHeader.style.display = "table-cell";

    await fillWarehouseDropdowns();
  }

  refreshAllData();
});

function refreshAllData() {
  loadEmployees();
  loadDrivers();
}
async function loadEmployees() {
  const role = localStorage.getItem("role") || "";
  let url = API_BASE;
  const warehouseId = document.getElementById("warehouseFilter")?.value;

  if (role.includes("SUPER_ADMIN") && warehouseId) {
    url += `?warehouseId=${warehouseId}`;
  }

  try {
    const res = await fetch(url, { headers: getHeaders() });
    if (res.ok) {
      const data = await res.json();
      renderEmployeeTable(data);
    }
  } catch (err) {
    console.error("Ошибка загрузки сотрудников", err);
  }
}

function renderEmployeeTable(data) {
  const body = document.getElementById("employeeBody");
  const role = localStorage.getItem("role") || "";
  body.innerHTML = "";

  data.forEach((emp) => {
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
async function loadDrivers() {
  let url = API_DRIVER;
  const warehouseId = document.getElementById("warehouseFilter")?.value;

  if (warehouseId) {
    url += `?warehouseId=${warehouseId}`;
  }

  try {
    const res = await fetch(url, { headers: getHeaders() });
    if (res.ok) {
      const data = await res.json();
      renderDriverTable(data);
    }
  } catch (err) {
    console.error("Ошибка загрузки водителей", err);
  }
}

function renderDriverTable(data) {
  const body = document.getElementById("driverBody");
  const role = localStorage.getItem("role") || "";
  const isSuper = role.includes("SUPER_ADMIN");
  body.innerHTML = "";

  data.forEach((dr) => {
    const drData = btoa(unescape(encodeURIComponent(JSON.stringify(dr))));
    let warehouseCell = isSuper ? `<td>${dr.warehouseName || "—"}</td>` : "";

    body.innerHTML += `
				<tr>
					<td data-label="ФИО"><strong>${dr.fullName}</strong></td>
					<td data-label="Контакты">${dr.phone || "—"}</td>
					<td data-label="Машина">${dr.carMark || ""} <br> <small>${dr.carNumber || ""}</small></td>
					<td data-label="Зарплата">${dr.salary || ""}</td>
					${warehouseCell}
					<td data-label="Действия">
						<button class="btn-edit" onclick="prepareDriverEdit('${drData}')">⚙️</button>
						<button class="btn-delete" onclick="deleteDriver(${dr.id})">🗑️</button>
					</td>
				</tr>`;
  });
}

async function saveDriver() {
  const id = document.getElementById("drId").value;
  const payload = {
    fullName: document.getElementById("drFullName").value.trim(),
    phone: document.getElementById("drPhone").value.trim(),
    carMark: document.getElementById("drCarMark").value.trim(),
    carNumber: document.getElementById("drCarNumber").value.trim(),
    salary: parseFloat(document.getElementById("drSalary").value) || 0, // Собираем зарплату
    additionalInfo: document.getElementById("drInfo").value.trim(),
    warehouseId: document.getElementById("drWarehouse").value || null,
  };

  // Базовая проверка на фронте, чтобы не дергать сервер зря
  if (!payload.fullName || !payload.phone || !payload.carNumber) {
    return showAlert(
      "Заполните ФИО, Телефон и Номер машины!",
      "Внимание",
      "error",
    );
  }

  const method = id ? "PUT" : "POST";
  const url = id ? `${API_DRIVER}/${id}` : API_DRIVER;

  try {
    const res = await fetch(url, {
      method: method,
      headers: getHeaders(),
      body: JSON.stringify(payload),
    });

    if (res.ok) {
      closeDriverModal();
      loadDrivers();
      showAlert("Данные успешно сохранены", "Успех", "success");
    } else {
      const errorData = await res.json();
      // Выводим ошибку с бэкенда (например, "Этот телефон уже занят")
      showAlert(
        errorData.message || "Ошибка при сохранении",
        "Ошибка",
        "error",
      );
    }
  } catch (err) {
    showAlert("Связь с сервером потеряна", "Ошибка", "error");
  }
}
