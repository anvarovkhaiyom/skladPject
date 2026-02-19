const SUPER_API = "http://localhost:9090/super";
const COMMON_API = "http://localhost:9090/admin";

function getHeaders() {
  return {
    "Content-Type": "application/json",
    Authorization: "Bearer " + localStorage.getItem("token"),
  };
}

let allWarehouses = [];
let allCurrencies = [];

async function init() {
  await loadCurrencies();
  await loadWarehouses();
}

async function loadCurrencies() {
  try {
    const res = await fetch(`${COMMON_API}/currency`, {
      headers: getHeaders(),
    });
    if (res.ok) {
      allCurrencies = await res.json();
      const select = document.getElementById("whCurrency");
      select.innerHTML = "";
      allCurrencies.forEach((c) => {
        select.innerHTML += `<option value="${c.id}">${c.code} (${c.name})</option>`;
      });
    }
  } catch (e) {
    showAlert("Не удалось загрузить список валют", "Ошибка", "error");
  }
}

async function loadWarehouses() {
  try {
    const res = await fetch(`${SUPER_API}/warehouses`, {
      headers: getHeaders(),
    });
    if (res.ok) {
      allWarehouses = await res.json();
      renderTable(allWarehouses);
    } else {
      showAlert(
        "Ошибка доступа. Проверьте права администратора",
        "Доступ запрещен",
        "error",
      );
    }
  } catch (e) {
    showAlert("Ошибка при подключении к серверу", "Ошибка", "error");
  }
}

function renderTable(data) {
  const body = document.getElementById("warehouseBody");
  body.innerHTML = "";

  data.forEach((wh) => {
    const currencyDisplay = wh.baseCurrencyCode || "—";

    body.innerHTML += `
            <tr>
                <td data-label="Название"><strong>${wh.name}</strong></td>
                <td data-label="Адрес">${wh.address || "—"}</td>
                <td data-label="Валюта">${currencyDisplay}</td>
                <td data-label="Действия">
                    <button onclick="editWarehouse(${wh.id})">✏️</button>
                    <button class="btn-delete" onclick="deleteWarehouse(${wh.id})">🗑️</button>
                </td>
            </tr>
        `;
  });
}

async function saveWarehouse() {
  const id = document.getElementById("whId").value;
  const payload = {
    name: document.getElementById("whName").value.trim(),
    address: document.getElementById("whAddress").value.trim(),
    baseCurrencyId: parseInt(document.getElementById("whCurrency").value),
  };

  if (!payload.name)
    return showAlert("Введите название склада", "Внимание", "error");

  const method = id ? "PUT" : "POST";
  const url = id ? `${SUPER_API}/warehouses/${id}` : `${SUPER_API}/warehouses`;

  try {
    const res = await fetch(url, {
      method: method,
      headers: getHeaders(),
      body: JSON.stringify(payload),
    });

    if (res.ok) {
      closeModal();
      loadWarehouses();
      showAlert(
        id ? "Данные склада обновлены" : "Новый склад успешно создан",
        "Успех",
      );
    } else {
      const err = await res.json();
      showAlert(err.message || "Ошибка при сохранении", "Ошибка", "error");
    }
  } catch (e) {
    showAlert("Ошибка сервера", "Ошибка", "error");
  }
}

function editWarehouse(id) {
  const wh = allWarehouses.find((w) => w.id === id);
  if (!wh) return;

  document.getElementById("whId").value = wh.id;
  document.getElementById("whName").value = wh.name;
  document.getElementById("whAddress").value = wh.address;

  if (wh.baseCurrencyId) {
    document.getElementById("whCurrency").value = wh.baseCurrencyId;
  } else if (wh.currency && wh.currency.id) {
    document.getElementById("whCurrency").value = wh.currency.id;
  }

  document.getElementById("modalTitle").innerText = "Редактировать склад";
  document.getElementById("whModal").style.display = "flex";
}

async function deleteWarehouse(id) {
  // Тут оставляем стандартный confirm, так как showAlert у нас просто уведомление (без кнопок Да/Нет)
  // Либо в будущем можно сделать кастомный confirmModal
  if (
    !confirm(
      "Удалить этот склад? Это удалит всех сотрудников и товары этого склада!",
    )
  )
    return;

  try {
    const res = await fetch(`${SUPER_API}/warehouses/${id}`, {
      method: "DELETE",
      headers: getHeaders(),
    });

    if (res.ok) {
      loadWarehouses();
      showAlert("Склад удален", "Успех");
    } else {
      showAlert("Не удалось удалить склад", "Ошибка", "error");
    }
  } catch (e) {
    showAlert("Ошибка при удалении", "Ошибка", "error");
  }
}

function openModal() {
  document.getElementById("whId").value = "";
  document.getElementById("whName").value = "";
  document.getElementById("whAddress").value = "";
  document.getElementById("modalTitle").innerText = "Новый склад";
  document.getElementById("whModal").style.display = "flex";
}

function closeModal() {
  document.getElementById("whModal").style.display = "none";
}

init();
