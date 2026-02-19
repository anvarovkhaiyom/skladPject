const API_BASE = "http://194.163.157.81:9090/admin";

function getHeaders() {
  const token = localStorage.getItem("token");
  return {
    "Content-Type": "application/json",
    Authorization: "Bearer " + token,
  };
}

// 1. ЗАГРУЗКА СПИСКА ВАЛЮТ
async function loadCurrencies() {
  try {
    const response = await fetch(`${API_BASE}/currency`, {
      headers: getHeaders(),
    });
    if (!response.ok) throw new Error("Ошибка загрузки");

    const currencies = await response.json();
    renderTable(currencies);
  } catch (error) {
    console.error(error);
  }
}

function renderTable(data) {
  const body = document.getElementById("currencyBody");
  body.innerHTML = "";

  data.forEach((curr) => {
    const date = new Date(curr.createdAt).toLocaleDateString("ru-RU");

    body.innerHTML += `
      <tr>
        <td data-label="Код ISO"><strong>${curr.code}</strong></td>
        <td data-label="Название">${curr.name}</td>
        <td data-label="Курс">${curr.rate || 1.0}</td>
        <td data-label="Действия">
          <button onclick='editCurrency(${JSON.stringify(curr)})'>Изменить</button>
          <button class="btn-delete" onclick="deleteCurrency(${curr.id})">Удалить</button>
        </td>
      </tr>`;
  });
}

async function saveCurrency() {
  const id = document.getElementById("currencyId").value;
  const payload = {
    code: document.getElementById("currencyCode").value.toUpperCase(),
    name: document.getElementById("currencyName").value,
    rate: parseFloat(document.getElementById("currencyRate").value) || 1.0,
  };

  if (!payload.code || !payload.name) {
    showAlert("Заполните все поля", "Внимание", "error");
    return;
  }

  const method = id ? "PUT" : "POST";
  const url = id ? `${API_BASE}/currency/${id}` : `${API_BASE}/currency`;

  try {
    const response = await fetch(url, {
      method: method,
      headers: getHeaders(),
      body: JSON.stringify(payload),
    });

    if (response.ok) {
      closeModal();
      loadCurrencies();
      showAlert("Данные успешно сохранены", "Успех", "info");
    } else {
      showAlert("Ошибка при сохранении валюты", "Ошибка", "error");
    }
  } catch (error) {
    showAlert("Сервер не отвечает", "Ошибка", "error");
  }
}

async function deleteCurrency(id) {
  // Вместо confirm можно использовать кастомную логику,
  // но для скорости оставим showAlert для уведомления о результате
  if (!confirm("Удалить валюту?")) return;

  try {
    const response = await fetch(`${API_BASE}/currency/${id}`, {
      method: "DELETE",
      headers: getHeaders(),
    });

    if (response.ok) {
      loadCurrencies();
      showAlert("Валюта удалена", "Успех", "info");
    } else {
      showAlert("Не удалось удалить валюту", "Ошибка", "error");
    }
  } catch (error) {
    showAlert("Произошла ошибка при удалении", "Ошибка", "error");
  }
}

// Открытие модалки для редактирования
function editCurrency(curr) {
  document.getElementById("currencyId").value = curr.id;
  document.getElementById("currencyCode").value = curr.code;
  document.getElementById("currencyCode").readOnly = true; // Код ISO обычно не меняют
  document.getElementById("currencyName").value = curr.name;
  document.getElementById("currencyRate").value = curr.rate;
  document.getElementById("modalTitle").innerText = "Изменить курс";
  document.getElementById("currencyModal").style.display = "flex";
}

function openModal() {
  document.getElementById("currencyId").value = "";
  document.getElementById("currencyCode").value = "";
  document.getElementById("currencyCode").readOnly = false;
  document.getElementById("currencyName").value = "";
  document.getElementById("currencyRate").value = "1.0";
  document.getElementById("modalTitle").innerText = "Добавить валюту";
  document.getElementById("currencyModal").style.display = "flex";
}

function closeModal() {
  document.getElementById("currencyModal").style.display = "none";
}

function logout() {
  localStorage.removeItem("token");
  window.location.href = "/index.html";
}

// Старт
loadCurrencies();
