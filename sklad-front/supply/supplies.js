const API_BASE = "http://194.163.157.81:9090/employee";
let allProducts = [];
let supplyItems = []; // Наша невидимая корзина

function getHeaders() {
  return {
    "Content-Type": "application/json",
    Authorization: "Bearer " + localStorage.getItem("token"),
  };
}

async function init() {
  await loadSuppliers();
  await fetchProducts();
}

async function loadSuppliers() {
  try {
    const res = await fetch(`${API_BASE}/supplier`, { headers: getHeaders() });
    const data = await res.json();
    const select = document.getElementById("supplySupplier");
    select.innerHTML = '<option value="">Выберите поставщика...</option>';
    data.forEach(
      (s) =>
        (select.innerHTML += `<option value="${s.id}">${s.fullName}</option>`),
    );
  } catch (e) {
    console.error("Ошибка поставщиков", e);
  }
}

async function fetchProducts() {
  try {
    const res = await fetch(`${API_BASE}/product`, { headers: getHeaders() });
    allProducts = await res.json();
    renderCatalog(allProducts);
  } catch (e) {
    console.error("Ошибка товаров", e);
  }
}

function addToCart(p) {
  const existing = supplyItems.find((item) => item.id === p.id);
  if (existing) {
    existing.quantity++;
  } else {
    supplyItems.push({
      id: p.id,
      name: p.name,
      quantity: 1,
      costPrice: p.costPrice || 0,
    });
  }
  document.getElementById("cartCount").innerText = supplyItems.length;
}

// Открываем модалку, где происходит вся магия
function openConfirmModal() {
  const supplierSelect = document.getElementById("supplySupplier");
  if (!supplierSelect.value)
    return showAlert("Сначала выберите поставщика!", "Внимание", "error");
  if (supplyItems.length === 0)
    return showAlert("Вы не выбрали ни одного товара!", "Внимание", "error");

  renderModalTable();
  document.getElementById("confirmSupplierName").innerText =
    supplierSelect.options[supplierSelect.selectedIndex].text;
  document.getElementById("confirmSupplyModal").style.display = "block";
}

function renderCatalog(products) {
  const body = document.getElementById("catalogBody");
  body.innerHTML = "";
  products
    .filter((p) => !p.deleted)
    .forEach((p) => {
      body.innerHTML += `
      <tr>
        <td data-label="Товар"><b>${p.name}</b><br><small style="color:gray">${p.barcode || "—"}</small></td>
        <td data-label="Остаток">${p.stockQuantity} ${p.unit || ""}</td>
        <td data-label="Цена">${p.costPrice || 0}</td>
        <td data-label="Действие" style="text-align: right;">
          <button class="btn" style="background:#28a745; color:white; border:none; padding:8px 15px; border-radius:4px; cursor:pointer; width: 100%; max-width: 150px;"
            onclick='addToCart(${JSON.stringify(p)})'>+ Добавить</button>
        </td>
      </tr>`;
    });
}

function renderModalTable() {
  const body = document.getElementById("confirmTableBody");
  body.innerHTML = "";
  let total = 0;

  supplyItems.forEach((item, index) => {
    const sum = item.quantity * item.costPrice;
    total += sum;
    body.innerHTML += `
      <tr>
        <td data-label="Товар"><strong>${item.name}</strong></td>
        <td data-label="Кол-во"><input type="number" step="any" value="${item.quantity}" oninput="updateItem(${index}, 'quantity', this.value)" style="width:100%; max-width: 80px; padding:5px"></td>
        <td data-label="Цена закупа"><input type="number" step="any" value="${item.costPrice}" oninput="updateItem(${index}, 'costPrice', this.value)" style="width:100%; max-width: 100px; padding:5px"></td>
        <td data-label="Сумма" id="rowSum-${index}">${sum.toFixed(2)}</td>
        <td data-label="Удалить" style="text-align: right;"><button onclick="removeFromModal(${index})" style="background:#ff4757; border:none; color:white; border-radius: 4px; padding: 5px 10px; cursor:pointer;">&times;</button></td>
      </tr>`;
  });
  document.getElementById("confirmTotalAmount").innerText = total.toFixed(2);
}
function updateItem(index, field, value) {
  supplyItems[index][field] = parseFloat(value) || 0;
  // Пересчитываем только сумму строки и общий итог, чтобы не перерисовывать всю таблицу (не терять фокус)
  const sum = (
    supplyItems[index].quantity * supplyItems[index].costPrice
  ).toFixed(2);
  document.getElementById(`rowSum-${index}`).innerText = sum;

  let total = supplyItems.reduce(
    (acc, item) => acc + item.quantity * item.costPrice,
    0,
  );
  document.getElementById("confirmTotalAmount").innerText = total.toFixed(2);
}

function removeFromModal(index) {
  supplyItems.splice(index, 1);
  document.getElementById("cartCount").innerText = supplyItems.length;
  if (supplyItems.length === 0) {
    closeConfirmModal();
  } else {
    renderModalTable();
  }
}

function closeConfirmModal() {
  document.getElementById("confirmSupplyModal").style.display = "none";
}

async function executeSubmit() {
  const payload = {
    supplierId: parseInt(document.getElementById("supplySupplier").value),
    items: supplyItems.map((i) => ({
      productId: i.id,
      quantity: i.quantity,
      costPrice: i.costPrice,
    })),
  };

  try {
    const res = await fetch(`${API_BASE}/supply`, {
      method: "POST",
      headers: getHeaders(),
      body: JSON.stringify(payload),
    });

    if (res.ok) {
      showAlert(
        "Приход успешно оформлен и товары зачислены!",
        "Успех",
        "success",
      );
      supplyItems = [];
      document.getElementById("cartCount").innerText = "0";
      closeConfirmModal();
      fetchProducts(); // Обновим остатки в каталоге
    } else {
      const err = await res.json();
      showAlert(err.message || "Ошибка при сохранении", "Ошибка", "error");
    }
  } catch (e) {
    showAlert("Ошибка связи с сервером", "Ошибка", "error");
  }
}

function filterCatalog(query) {
  const q = query.toLowerCase();
  renderCatalog(
    allProducts.filter(
      (p) =>
        p.name.toLowerCase().includes(q) ||
        (p.barcode && p.barcode.includes(q)),
    ),
  );
}

init();
