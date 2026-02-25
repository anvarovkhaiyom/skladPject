const API_BASE = "http://localhost:9090/admin";
let allCategories = [];
function getHeaders() {
  return {
    "Content-Type": "application/json",
    Authorization: "Bearer " + localStorage.getItem("token"),
  };
}

async function loadCategories() {
  try {
    const response = await fetch(`${API_BASE}/category`, {
      headers: getHeaders(),
    });
    if (!response.ok) throw new Error("Ошибка загрузки");

    allCategories = await response.json(); // Сохраняем в глобальную переменную
    renderTable(allCategories); // Рисуем всё
  } catch (error) {
    console.error(error);
  }
}
function renderTable(data) {
  const body = document.getElementById("categoryBody");
  body.innerHTML = "";

  if (data.length === 0) {
    body.innerHTML =
      "<tr><td colspan='2' style='text-align:center'>Ничего не найдено</td></tr>";
    return;
  }

  data.forEach((cat) => {
    body.innerHTML += `
      <tr>
        <td data-label="Название">${cat.name}</td>
        <td data-label="Действия">
          <button onclick="editCategory(${cat.id})">Редактировать</button>
          <button class="btn-delete" onclick="deleteCategory(${cat.id})">Удалить</button>
        </td>
      </tr>
    `;
  });
}

async function saveCategory() {
  const id = document.getElementById("categoryId").value;
  const name = document.getElementById("categoryName").value.trim();

  if (!name) return showAlert("Введите название!", "Внимание", "error");

  const method = id ? "PUT" : "POST";
  const url = id ? `${API_BASE}/category/${id}` : `${API_BASE}/category`;

  try {
    const response = await fetch(url, {
      method: method,
      headers: getHeaders(),
      body: JSON.stringify({ name: name }),
    });

    if (response.ok) {
      closeModal();
      loadCategories();
      showAlert(id ? "Категория обновлена" : "Категория добавлена", "Успех");
    } else {
      showAlert("Ошибка сохранения", "Ошибка", "error");
    }
  } catch (error) {
    showAlert("Связь с сервером потеряна", "Ошибка", "error");
  }
}
function handleSearch() {
  const query = document.getElementById("searchInput").value.toLowerCase();

  // Фильтруем массив в памяти
  const filtered = allCategories.filter((cat) =>
    cat.name.toLowerCase().includes(query),
  );

  renderTable(filtered);
}
// 4. РЕДАКТИРОВАНИЕ (Безопасный метод)
function editCategory(id) {
  // Ищем категорию в памяти по ID
  const cat = allCategories.find((c) => c.id === id);
  if (!cat) return;

  document.getElementById("modalTitle").innerText = "Редактировать категорию";
  document.getElementById("categoryId").value = cat.id;
  document.getElementById("categoryName").value = cat.name;
  document.getElementById("categoryModal").style.display = "flex";
}

// 5. УДАЛЕНИЕ
async function deleteCategory(id) {
  if (
    !confirm(
      "Удалить категорию? Если в ней есть товары, это может вызвать ошибку.",
    )
  )
    return;

  try {
    const response = await fetch(`${API_BASE}/category/${id}`, {
      method: "DELETE",
      headers: getHeaders(),
    });

    if (response.ok) {
      loadCategories();
    } else {
      alert("Не удалось удалить. Возможно, к категории привязаны товары.");
    }
  } catch (error) {
    console.error(error);
  }
}

// --- МОДАЛКА ---
function openModal() {
  document.getElementById("modalTitle").innerText = "Добавить категорию";
  document.getElementById("categoryId").value = "";
  document.getElementById("categoryName").value = "";
  document.getElementById("categoryModal").style.display = "flex";
}

function closeModal() {
  document.getElementById("categoryModal").style.display = "none";
}

function logout() {
  localStorage.removeItem("token");
  window.location.href = "/index.html";
}
document.addEventListener("DOMContentLoaded", () => {
  loadCategories();
});
