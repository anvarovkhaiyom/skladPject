const API_BASE = "http://194.163.157.81:9090/admin";
let categoriesMap = {};
let allProducts = []; // Храним товары здесь для поиска
let warehouseCurrency = "";
function getHeaders() {
  return {
    "Content-Type": "application/json",
    Authorization: "Bearer " + localStorage.getItem("token"),
  };
}

const API_WAREHOUSES = "http://194.163.157.81:9090/super/warehouses";

async function init() {
  const role = localStorage.getItem("role");

  // Если Супер-админ — показываем и заполняем фильтр складов
  if (role && role.includes("SUPER_ADMIN")) {
    document.getElementById("warehouseFilterGroup").style.display = "block";
    await fillWarehouseFilter();
  }

  await loadWarehouseInfo();
  await loadCategories();
  await loadProducts();
}
async function fillWarehouseFilter() {
  try {
    const res = await fetch(API_WAREHOUSES, { headers: getHeaders() });
    if (res.ok) {
      const warehouses = await res.json();
      const select = document.getElementById("warehouseFilter");
      warehouses.forEach((w) => {
        select.innerHTML += `<option value="${w.id}">${w.name}</option>`;
      });
    }
  } catch (e) {
    console.error("Ошибка загрузки складов", e);
  }
}
async function loadWarehouseInfo() {
  try {
    const res = await fetch(`${API_BASE}/warehouse/current`, {
      headers: getHeaders(),
    });
    if (res.ok) {
      const data = await res.json();
      warehouseCurrency = data.baseCurrencyCode || ""; // Например "TJS"
    }
  } catch (e) {
    console.error("Не удалось узнать валюту склада", e);
  }
}
async function loadCategories() {
  try {
    const res = await fetch(`${API_BASE}/category`, { headers: getHeaders() });
    if (res.ok) {
      const categories = await res.json();
      const select = document.getElementById("prodCategory");
      select.innerHTML = '<option value="">Без категории</option>';

      categories.forEach((cat) => {
        // Сохраняем как число, чтобы точно совпало с p.categoryId
        categoriesMap[Number(cat.id)] = cat.name;
        select.innerHTML += `<option value="${cat.id}">${cat.name}</option>`;
      });
    }
  } catch (e) {
    console.error("Ошибка загрузки категорий", e);
  }
}

async function loadProducts() {
  const warehouseId = document.getElementById("warehouseFilter")?.value || "";
  const url = warehouseId
    ? `${API_BASE}/product?warehouseId=${warehouseId}`
    : `${API_BASE}/product`;

  try {
    const res = await fetch(url, { headers: getHeaders() });
    if (res.ok) {
      allProducts = await res.json();
      renderTable(allProducts);
    } else {
      showAlert("Не удалось загрузить товары", "Ошибка", "error");
    }
  } catch (error) {
    showAlert("Ошибка сети при загрузке товаров", "Ошибка", "error");
  }
}

function renderTable(products) {
  const body = document.getElementById("productBody");
  body.innerHTML = "";

  if (products.length === 0) {
    body.innerHTML =
      "<tr><td colspan='6' style='text-align:center'>Товары не найдены</td></tr>";
    return;
  }

  products.forEach((p) => {
    // Красный цвет для низкого остатка
    const stockColor =
      p.stockQuantity < 5 ? "color: #e74c3c; font-weight: bold;" : "";
    const priceDisplay = p.salePrice ? p.salePrice.toLocaleString() : "0";
    const categoryName = categoriesMap[p.categoryId] || "—";

    body.innerHTML += `
      <tr>
          <td data-label="Артикул">
            <small>${p.sku || "—"}</small>
          </td>
          <td data-label="Наименование">
            <strong>${p.name}</strong>
            <br><small style="color:gray">${p.barcode || ""}</small>
          </td>
          <td data-label="Категория">${categoryName}</td>
          <td data-label="Остаток" style="${stockColor}">
            ${p.stockQuantity} ${p.unit || "шт"}
          </td>
          <td data-label="Цена">${priceDisplay} ${warehouseCurrency}</td>
          <td data-label="Действия">
              <button class="btn-edit" onclick="editProduct(${p.id})">⚙️</button>
              <button class="btn-delete" onclick="deleteProduct(${p.id})">🗑️</button>
          </td>
      </tr>`;
  });
}

async function saveProduct() {
  const id = document.getElementById("prodId").value;
  const payload = {
    name: document.getElementById("prodName").value.trim(),
    categoryId: document.getElementById("prodCategory").value
      ? Number(document.getElementById("prodCategory").value)
      : null,
    unit: document.getElementById("prodUnit").value || "шт",
    costPrice: Number(document.getElementById("prodCostPrice").value) || 0,
    salePrice: Number(document.getElementById("prodSalePrice").value) || 0,
    itemsInBox: Number(document.getElementById("prodItemsInBox").value) || 1,
    weightBrutto: Number(document.getElementById("prodWeight").value) || 0,
    stockQuantity: Number(document.getElementById("prodStock").value) || 0,
    barcode: document.getElementById("prodBarcode").value.trim(),
    photoUrl: "",
  };

  if (!payload.name) {
    return showAlert("Название товара обязательно!", "Внимание", "error");
  }

  const method = id ? "PUT" : "POST";
  const url = id ? `${API_BASE}/product/${id}` : `${API_BASE}/product`;

  try {
    const res = await fetch(url, {
      method: method,
      headers: getHeaders(),
      body: JSON.stringify(payload),
    });

    if (res.ok) {
      closeModal();
      loadProducts();
      showAlert(
        id ? "Товар обновлен" : "Товар успешно добавлен",
        "Успех",
        "success",
      );
    } else {
      const err = await res.json();
      showAlert(
        "Ошибка: " + (err.message || "Проверьте данные"),
        "Ошибка",
        "error",
      );
    }
  } catch (e) {
    showAlert("Ошибка сервера при сохранении товара", "Ошибка", "error");
  }
}

async function deleteProduct(id) {
  if (!confirm("Вы уверены? Удаление товара необратимо.")) return;

  try {
    const res = await fetch(`${API_BASE}/product/${id}`, {
      method: "DELETE",
      headers: getHeaders(),
    });

    if (res.ok) {
      loadProducts();
      showAlert("Товар удален", "Успех", "info");
    } else {
      showAlert(
        "Не удалось удалить товар. Возможно, по нему были продажи.",
        "Ошибка",
        "error",
      );
    }
  } catch (e) {
    showAlert("Ошибка при удалении товара", "Ошибка", "error");
  }
}

function handleSearch() {
  const query = document
    .getElementById("searchInput")
    .value.toLowerCase()
    .trim();

  const filtered = allProducts.filter(
    (p) =>
      p.name.toLowerCase().includes(query) ||
      (p.sku && p.sku.toLowerCase().includes(query)) ||
      (p.barcode && p.barcode.includes(query)),
  );

  renderTable(filtered);
}

async function editProduct(id) {
  // Ищем товар в памяти (чтобы не делать лишний запрос)
  const p = allProducts.find((item) => item.id === id);
  if (!p) return;

  document.getElementById("prodId").value = p.id;
  document.getElementById("prodName").value = p.name;
  document.getElementById("prodSku").value = p.sku || "";
  document.getElementById("prodCategory").value = p.categoryId || "";
  document.getElementById("prodUnit").value = p.unit;
  document.getElementById("prodCostPrice").value = p.costPrice;
  document.getElementById("prodSalePrice").value = p.salePrice;
  document.getElementById("prodStock").value = p.stockQuantity;
  document.getElementById("prodBarcode").value = p.barcode || "";
  document.getElementById("prodItemsInBox").value = p.itemsInBox || "";
  document.getElementById("prodWeight").value = p.weightBrutto || "";

  document.getElementById("modalTitle").innerText = "Редактировать товар";
  document.getElementById("productModal").style.display = "flex";
}

function openModal() {
  document.getElementById("prodId").value = "";
  document.querySelectorAll(".modal input").forEach((i) => (i.value = ""));
  document.getElementById("modalTitle").innerText = "Добавить товар";
  document.getElementById("productModal").style.display = "flex";
}

function closeModal() {
  document.getElementById("productModal").style.display = "none";
}

init();
