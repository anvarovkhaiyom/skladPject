const BASE_URL = "http://localhost:9090/admin";
const API_SALES = `${BASE_URL}/archive`;
const API_REPORTS = `${BASE_URL}`;
const API_CLIENTS = `${BASE_URL}/client`;
const API_EMPLOYEES = `${BASE_URL}/employee`;
const API_ANALYTICS = `${BASE_URL}/sales-summary`;
const API_STOCK_SUMMARY = `${BASE_URL}/stock-summary`;
const API_WAREHOUSES = `http://localhost:9090/super/warehouses`;
function getHeaders() {
  const token = localStorage.getItem("token");
  return {
    "Content-Type": "application/json",
    Authorization: "Bearer " + token,
  };
}
document.addEventListener("DOMContentLoaded", async () => {
  const today = new Date().toISOString().split("T")[0];
  document.getElementById("dateStart").value = today;
  document.getElementById("dateEnd").value = today;

  const role = localStorage.getItem("role");
  if (role === "SUPER_ADMIN" || role === "ROLE_SUPER_ADMIN") {
    const whGroup = document.getElementById("warehouseFilterGroup");
    if (whGroup) {
      whGroup.style.display = "flex";
    }
    await fillWarehouseFilter();
  }

  await fillSelectFilters();
  setupAutoFilters();

  refreshData();
});
function setupAutoFilters() {
  const filterIds = [
    "dateStart",
    "dateEnd",
    "clientFilter",
    "employeeFilter",
    "warehouseFilter",
    "nameFilter",
  ];

  filterIds.forEach((id) => {
    const el = document.getElementById(id);
    if (el) {
      // 'change' подходит для дат и селектов, 'input' для текстовых полей
      const eventType =
        el.tagName === "INPUT" && el.type === "text" ? "input" : "change";
      el.addEventListener(eventType, () => {
        refreshData();
      });
    }
  });
}

async function refreshData() {
  try {
    await Promise.all([loadAnalytics(), loadSales()]);
  } catch (err) {
    showAlert(
      "Не удалось обновить данные при применении фильтров",
      "Ошибка",
      "error",
    );
  }
}

async function fillSelectFilters() {
  try {
    const headers = getHeaders();

    const [resClients, resEmployees] = await Promise.all([
      fetch(API_CLIENTS, { headers }),
      fetch(API_EMPLOYEES, { headers }),
    ]);

    if (resClients.ok) {
      const clients = await resClients.json();
      const clientSelect = document.getElementById("clientFilter");
      clientSelect.innerHTML = '<option value="">Все клиенты</option>';
      clients.forEach((c) => {
        const name = c.fullName || c.name;
        clientSelect.innerHTML += `<option value="${name}">${name}</option>`;
      });
    }

    if (resEmployees.ok) {
      const employees = await resEmployees.json();
      const empSelect = document.getElementById("employeeFilter");
      empSelect.innerHTML = '<option value="">Все сотрудники</option>';
      employees.forEach((e) => {
        const name = e.fullName || e.name;
        empSelect.innerHTML += `<option value="${name}">${name}</option>`;
      });
    }
  } catch (err) {
    showAlert("Ошибка при загрузке списков фильтрации", "Ошибка", "error");
  }
}

async function loadSales() {
  const start = document.getElementById("dateStart").value;
  const end = document.getElementById("dateEnd").value;
  const warehouseId = document.getElementById("warehouseFilter")?.value || "";

  const clientName = document.getElementById("clientFilter")?.value || "";
  const employeeName = document.getElementById("employeeFilter")?.value || "";

  const params = new URLSearchParams();
  params.append("start", start);
  params.append("end", end);

  if (warehouseId) params.append("warehouseId", warehouseId);
  if (clientName) params.append("clientName", clientName);
  if (employeeName) params.append("employeeName", employeeName);

  try {
    const res = await fetch(`${API_SALES}?${params.toString()}`, {
      method: "GET",
      headers: getHeaders(),
    });

    if (res.ok) {
      renderTable(await res.json());
    } else {
      showAlert("Ошибка при загрузке архива", "Ошибка", "error");
    }
  } catch (err) {
    showAlert("Проблема с сетью", "Ошибка", "error");
  }
}

function renderTable(documents) {
  const body = document.getElementById("salesBody");
  if (!body) return;
  body.innerHTML = "";

  if (!Array.isArray(documents) || documents.length === 0) {
    body.innerHTML =
      "<tr><td colspan='5' style='text-align:center'>За выбранный период документов нет</td></tr>";
    return;
  }

  documents.forEach((doc) => {
    const dateStr = doc.date ? new Date(doc.date).toLocaleString("ru-RU") : "—";
    const amount = (doc.amount || 0).toLocaleString();

    // Определяем стили и названия на основе типа документа
    let typeLabel = doc.type; // "Отпуск", "Приход", "Списание", "Расход"
    let amountColor = "#4d6dfc"; // По умолчанию синий
    let rowBg = "#ffffff";

    if (doc.type === "Отпуск") {
      amountColor = "#4d6dfc"; // Синий
    } else if (doc.type === "Приход") {
      amountColor = "#12cc7b"; // Зеленый
    } else if (doc.type === "Списание") {
      amountColor = "#e67e22"; // Оранжевый
      rowBg = "#fffcf9";
    } else if (doc.type === "Расход") {
      amountColor = "#e74c3c"; // Красный
      rowBg = "#fffafb";
    }

    body.innerHTML += `
      <tr style="background-color: ${rowBg};">
        <td data-label="Документ">
            <span style="font-size: 0.8em; color: #888; display: block; font-weight: normal; text-transform: uppercase;">
                ${typeLabel}
            </span>
            <strong>${doc.documentNumber || "№ " + doc.id}</strong>
        </td>
        <td data-label="Дата/Время">${dateStr}</td>
        <td data-label="Контрагент">${doc.counterparty || "—"}</td>
        <td data-label="Сумма" style="color: ${amountColor}; font-weight: bold;">
            ${amount} ₸
        </td>
        <td data-label="Выгрузка" class="report-buttons">
          ${
            doc.type === "Отпуск"
              ? `
            <button title="Сборочный лист" class="btn-report" onclick="downloadFile(${doc.id}, 'picking-list')">📦</button>
            <button title="Счет-фактура" class="btn-report" onclick="downloadFile(${doc.id}, 'invoice-boxes')">📄</button>
            <button title="Отчет Z-2" class="btn-report" onclick="downloadFile(${doc.id}, 'z2-report')">📋</button>
            <button title="ТТН" class="btn-report" onclick="downloadFile(${doc.id}, 'ttn')">🚛</button>
          `
              : `<span style="color: #bdc3c7; font-size: 0.85em;">Документов нет</span>`
          }
        </td>
      </tr>`;
  });
}
async function downloadFile(id, type) {
  const url = `${API_REPORTS}/${type}/${id}`;
  try {
    const response = await fetch(url, { headers: getHeaders() });
    if (!response.ok) throw new Error("Сервер не смог сгенерировать файл");

    const blob = await response.blob();
    const downloadUrl = window.URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.href = downloadUrl;
    link.setAttribute("download", `${type}_sale_${id}.xlsx`);
    document.body.appendChild(link);
    link.click();
    link.remove();
    window.URL.revokeObjectURL(downloadUrl);
  } catch (error) {
    showAlert("Ошибка скачивания: " + error.message, "Ошибка", "error");
  }
}

async function downloadGlobal(type) {
  const start = document.getElementById("dateStart").value;
  const end = document.getElementById("dateEnd").value;
  const warehouseId = document.getElementById("warehouseFilter")?.value || "";

  const params = new URLSearchParams();

  // Для всех отчетов, кроме остатков склада, нужны даты
  if (type !== "stock-excel") {
    params.append("start", start);
    params.append("end", end);
  }

  if (warehouseId) {
    params.append("warehouseId", warehouseId);
  }

  try {
    // API_REPORTS у тебя ведет на BASE_URL, убедись что пути совпадают с контроллером
    const response = await fetch(
      `${API_REPORTS}/reports/${type}?${params.toString()}`,
      {
        headers: getHeaders(),
      },
    );

    if (!response.ok) throw new Error("Не удалось экспортировать данные");

    const blob = await response.blob();
    const downloadUrl = window.URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.href = downloadUrl;
    link.setAttribute("download", `${type}_${start}_${end}.xlsx`);
    document.body.appendChild(link);
    link.click();
    link.remove();
  } catch (error) {
    showAlert("Ошибка экспорта: " + error.message, "Ошибка", "error");
  }
}

async function fillWarehouseFilter() {
  try {
    const res = await fetch(API_WAREHOUSES, { headers: getHeaders() });
    if (res.ok) {
      const warehouses = await res.json();
      const select = document.getElementById("warehouseFilter");
      select.innerHTML = '<option value="">Все склады</option>';
      warehouses.forEach((w) => {
        const opt = document.createElement("option");
        opt.value = w.id;
        opt.textContent = w.name;
        select.appendChild(opt);
      });
    }
  } catch (e) {
    showAlert(
      "Не удалось загрузить список доступных складов",
      "Ошибка",
      "error",
    );
  }
}

async function resetFilters() {
  const today = new Date().toISOString().split("T")[0];
  document.getElementById("dateStart").value = today;
  document.getElementById("dateEnd").value = today;

  if (document.getElementById("clientFilter")) {
    document.getElementById("clientFilter").value = "";
  }
  if (document.getElementById("employeeFilter")) {
    document.getElementById("employeeFilter").value = "";
  }

  const nameFilter = document.getElementById("nameFilter");
  if (nameFilter) nameFilter.value = "";

  console.log("Фильтры сброшены, загрузка данных за сегодня...");
  await refreshData();
}

async function loadAnalytics() {
  const role = localStorage.getItem("role");
  const start = document.getElementById("dateStart").value;
  const end = document.getElementById("dateEnd").value;
  const warehouseId = document.getElementById("warehouseFilter")?.value || "";

  let statsUrl, stockUrl;

  if (role.includes("SUPER_ADMIN") && !warehouseId) {
    statsUrl = `http://localhost:9090/super/reports/sales?start=${start}&end=${end}`;
    stockUrl = `http://localhost:9090/super/reports/summary`;
  } else {
    const query = `?start=${start}&end=${end}${warehouseId ? "&warehouseId=" + warehouseId : ""}`;
    statsUrl = `${BASE_URL}/sales-summary${query}`;
    stockUrl = `${BASE_URL}/stock-summary${warehouseId ? "?warehouseId=" + warehouseId : ""}`;
  }

  try {
    const [resSales, resStock] = await Promise.all([
      fetch(statsUrl, { headers: getHeaders() }),
      fetch(stockUrl, { headers: getHeaders() }),
    ]);

    if (!resSales.ok || !resStock.ok) throw new Error();

    const salesData = await resSales.json();
    const stockData = await resStock.json();

    // Основные статы
    document.getElementById("statRevenue").textContent =
      `${(salesData.totalRevenue || 0).toLocaleString()} ₸`;
    document.getElementById("statProfit").textContent =
      `${(salesData.totalProfit || 0).toLocaleString()} ₸`;
    document.getElementById("statStockItems").textContent =
      `${stockData.totalItems || 0} шт`;
    document.getElementById("statStockValue").textContent =
      `${(stockData.totalCostValue || 0).toLocaleString()} ₸`;

    // НОВЫЕ СТАТЫ (Расходы и Списания)
    const expEl = document.getElementById("statExpenses");
    if (expEl)
      expEl.textContent = `${(salesData.totalExpenses || 0).toLocaleString()} ₸`;

    const woEl = document.getElementById("statWriteOffs");
    if (woEl)
      woEl.textContent = `${(salesData.totalWriteOffCost || 0).toLocaleString()} ₸`;
  } catch (err) {
    console.error(err);
    showAlert("Ошибка при расчете аналитических данных", "Внимание", "error");
  }
}
