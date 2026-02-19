const API_BASE = "http://194.163.157.81:9090/admin";

function getHeaders() {
  const token = localStorage.getItem("token");
  return {
    "Content-Type": "application/json",
    Authorization: "Bearer " + token,
  };
}

async function loadClients() {
  const warehouseId = document.getElementById("warehouseFilter")?.value || "";
  const url = warehouseId
    ? `${API_BASE}/client?warehouseId=${warehouseId}`
    : `${API_BASE}/client`;

  try {
    const res = await fetch(url, { headers: getHeaders() });
    if (res.ok) {
      const clients = await res.json();
      renderTable(clients);
    } else {
      showAlert("Не удалось загрузить список клиентов", "Ошибка", "error");
    }
  } catch (e) {
    showAlert("Ошибка сети при загрузке клиентов", "Ошибка", "error");
  }
}

function renderTable(data) {
  const body = document.getElementById("clientBody");
  body.innerHTML = "";

  if (data.length === 0) {
    body.innerHTML =
      '<tr><td colspan="3" style="text-align:center">Клиенты не найдены</td></tr>';
    return;
  }

  data.forEach((client) => {
    body.innerHTML += `
            <tr>
                <td data-label="ФИО">${client.fullName}</td>
                <td data-label="Контакты">${client.contacts || "—"}</td>
                <td data-label="Действия">
                    <button onclick="editClient(${client.id}, '${client.fullName}', '${client.contacts || ""}')">Изменить</button>
                    <button class="btn-delete" onclick="deleteClient(${client.id})">Удалить</button>
                </td>
            </tr>
        `;
  });
}

async function saveClient() {
  const id = document.getElementById("clientId").value;
  const fullName = document.getElementById("fullName").value.trim();
  const contacts = document.getElementById("contacts").value.trim();

  if (!fullName) {
    showAlert("ФИО обязательно для заполнения", "Внимание", "error");
    return;
  }

  const payload = {
    fullName: fullName,
    contacts: contacts,
  };

  const method = id ? "PUT" : "POST";
  const url = id ? `${API_BASE}/client/${id}` : `${API_BASE}/client`;

  try {
    const response = await fetch(url, {
      method: method,
      headers: getHeaders(),
      body: JSON.stringify(payload),
    });

    if (response.ok) {
      closeModal();
      loadClients();
      showAlert(
        id ? "Данные клиента обновлены" : "Клиент успешно добавлен",
        "Успех",
        "success",
      );
    } else {
      showAlert("Ошибка при сохранении клиента", "Ошибка", "error");
    }
  } catch (error) {
    showAlert("Произошла ошибка на стороне сервера", "Ошибка", "error");
  }
}

async function deleteClient(id) {
  // Используем стандартный confirm для подтверждения действия
  if (
    !confirm(
      "Вы уверены, что хотите удалить клиента? Это может повлиять на историю продаж.",
    )
  ) {
    return;
  }

  try {
    const response = await fetch(`${API_BASE}/client/${id}`, {
      method: "DELETE",
      headers: getHeaders(),
    });

    if (response.ok) {
      loadClients();
      showAlert("Клиент успешно удален", "Удаление", "success");
    } else {
      showAlert(
        "Не удалось удалить клиента (возможно, есть связанные продажи)",
        "Ошибка",
        "error",
      );
    }
  } catch (error) {
    showAlert("Ошибка при выполнении запроса на удаление", "Ошибка", "error");
  }
}

function openModal() {
  document.getElementById("modalTitle").innerText = "Добавить клиента";
  document.getElementById("clientId").value = "";
  document.getElementById("fullName").value = "";
  document.getElementById("contacts").value = "";
  document.getElementById("clientModal").style.display = "flex";
}

function editClient(id, name, contacts) {
  document.getElementById("modalTitle").innerText = "Редактировать клиента";
  document.getElementById("clientId").value = id;
  document.getElementById("fullName").value = name;
  document.getElementById("contacts").value = contacts;
  document.getElementById("clientModal").style.display = "flex";
}

function closeModal() {
  document.getElementById("clientModal").style.display = "none";
}

function logout() {
  localStorage.removeItem("token");
  window.location.href = "/index.html";
}

// Инициализация
loadClients();
