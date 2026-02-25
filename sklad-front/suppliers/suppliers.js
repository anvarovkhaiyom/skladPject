const API_BASE = "http://localhost:9090/admin/supplier";

function getHeaders() {
  const token = localStorage.getItem("token");
  if (!token) window.location.href = "/index.html";
  return {
    "Content-Type": "application/json",
    Authorization: "Bearer " + token,
  };
}

async function loadSuppliers() {
  try {
    const res = await fetch(API_BASE, { headers: getHeaders() });
    if (res.ok) {
      const data = await res.json();
      renderTable(data);
    } else if (res.status === 403) {
      showAlert(
        "Ошибка доступа: недостаточно прав.",
        "Доступ запрещен",
        "error",
      );
    } else {
      showAlert("Не удалось загрузить список поставщиков", "Ошибка", "error");
    }
  } catch (err) {
    showAlert(
      "Ошибка сети: проверьте соединение с сервером",
      "Ошибка",
      "error",
    );
  }
}

function renderTable(data) {
  const body = document.getElementById("supplierBody");
  body.innerHTML = "";

  if (data.length === 0) {
    body.innerHTML =
      "<tr><td colspan='3' style='text-align:center'>Список поставщиков пуст</td></tr>";
    return;
  }

  data.forEach((sup) => {
    const supData = btoa(unescape(encodeURIComponent(JSON.stringify(sup))));

    body.innerHTML += `
      <tr>
        <td data-label="Поставщик"><strong>${sup.fullName}</strong></td>
        <td data-label="Контакты">${sup.contacts || "—"}</td>
        <td data-label="Действия">
          <button class="btn-edit" onclick="editSupplierAction('${supData}')">Изменить</button>
          <button class="btn-delete" onclick="deleteSupplier(${sup.id})">Удалить</button>
        </td>
      </tr>`;
  });
}

async function saveSupplier() {
  const id = document.getElementById("supId").value;
  const payload = {
    fullName: document.getElementById("supName").value.trim(),
    contacts: document.getElementById("supContacts").value.trim(),
  };

  if (!payload.fullName) {
    return showAlert("Введите название поставщика!", "Внимание", "error");
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
      loadSuppliers();
      showAlert(
        id ? "Данные обновлены" : "Поставщик добавлен",
        "Успех",
        "success",
      );
    } else {
      const err = await res.json();
      showAlert(
        "Ошибка сохранения: " + (err.message || "Проверьте данные"),
        "Ошибка",
        "error",
      );
    }
  } catch (err) {
    showAlert("Ошибка связи с сервером", "Ошибка", "error");
  }
}

async function deleteSupplier(id) {
  // Стандартный confirm остается для подтверждения критического действия
  if (!confirm("Удалить этого поставщика из вашей базы?")) return;

  try {
    const res = await fetch(`${API_BASE}/${id}`, {
      method: "DELETE",
      headers: getHeaders(),
    });

    if (res.ok) {
      loadSuppliers();
      showAlert("Поставщик удален", "Успех", "info");
    } else {
      showAlert(
        "Не удалось удалить. Возможно, на этом поставщике висят поставки.",
        "Ошибка",
        "error",
      );
    }
  } catch (err) {
    showAlert("Произошла ошибка при удалении", "Ошибка", "error");
  }
}
/**/
function editSupplierAction(encodedSup) {
  const sup = JSON.parse(decodeURIComponent(escape(atob(encodedSup))));

  document.getElementById("supId").value = sup.id;
  document.getElementById("supName").value = sup.fullName;
  document.getElementById("supContacts").value = sup.contacts || "";
  document.getElementById("modalTitle").innerText = "Редактировать поставщика";
  document.getElementById("supplierModal").style.display = "flex";
}

function openModal() {
  document.getElementById("supId").value = "";
  document.getElementById("supName").value = "";
  document.getElementById("supContacts").value = "";
  document.getElementById("modalTitle").innerText = "Добавить поставщика";
  document.getElementById("supplierModal").style.display = "flex";
}

function editSupplier(sup) {
  document.getElementById("supId").value = sup.id;
  document.getElementById("supName").value = sup.fullName;
  document.getElementById("supContacts").value = sup.contacts || "";
  document.getElementById("modalTitle").innerText = "Редактировать поставщика";
  document.getElementById("supplierModal").style.display = "flex";
}

function closeModal() {
  document.getElementById("supplierModal").style.display = "none";
}

document.addEventListener("DOMContentLoaded", loadSuppliers);
