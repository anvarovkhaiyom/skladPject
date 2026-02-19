const API_BASE = "http://localhost:9090/employee";
let supplyItems = [];

function getHeaders() {
  return {
    "Content-Type": "application/json",
    Authorization: "Bearer " + localStorage.getItem("token"),
  };
}

async function init() {
  await loadSuppliers();
  const warehouseId = localStorage.getItem("warehouseId");
  console.log("Работаем со складом ID:", warehouseId);
}

async function loadSuppliers() {
  try {
    const res = await fetch(`${API_BASE}/supplier`, { headers: getHeaders() });
    if (res.ok) {
      const data = await res.json();
      const select = document.getElementById("supplySupplier");
      select.innerHTML = '<option value="">Выберите поставщика</option>';
      data.forEach((s) => {
        select.innerHTML += `<option value="${s.id}">${s.fullName}</option>`;
      });
    }
  } catch (e) {
    showAlert("Ошибка загрузки поставщиков", "Ошибка", "error");
  }
}

async function handleSearch(e) {
  if (e.key === "Enter") {
    const query = e.target.value.trim();
    if (!query) return;

    try {
      const res = await fetch(`${API_BASE}/product`, { headers: getHeaders() });
      const products = await res.json();

      const product = products.find(
        (p) => p.barcode === query || p.sku === query,
      );

      if (product) {
        addItem(product);
        e.target.value = "";
      } else {
        showAlert(
          "Товар не найден! Проверьте артикул или штрих-код.",
          "Внимание",
          "error",
        );
      }
    } catch (e) {
      showAlert("Ошибка при поиске товара", "Ошибка", "error");
    }
  }
}

function addItem(p) {
  const existing = supplyItems.find((item) => item.id === p.id);
  if (existing) {
    existing.quantity++;
  } else {
    supplyItems.push({
      id: p.id,
      name: p.name,
      sku: p.sku,
      barcode: p.barcode,
      quantity: 1,
      costPrice: p.costPrice || 0,
    });
  }
  renderTable();
}

function renderTable() {
  const body = document.getElementById("supplyBody");
  body.innerHTML = "";

  if (supplyItems.length === 0) {
    body.innerHTML =
      '<tr><td colspan="5" style="text-align:center">Список пуст</td></tr>';
    return;
  }

  supplyItems.forEach((item, index) => {
    body.innerHTML += `
            <tr>
                <td data-label="Товар"><strong>${item.name}</strong></td>
                <td data-label="Код">${item.sku || item.barcode || "—"}</td>
                <td data-label="Кол-во">
                    <input type="number" step="any" value="${item.quantity}" 
                           style="width:80px; padding:5px" 
                           onchange="updateQty(${index}, this.value)">
                </td>
                <td data-label="Закуп. цена">
                    <input type="number" step="any" value="${item.costPrice}" 
                           style="width:100px; padding:5px" 
                           onchange="updatePrice(${index}, this.value)">
                </td>
                <td data-label="Действие">
                    <button class="btn-delete" onclick="removeItem(${index})">🗑️</button>
                </td>
            </tr>`;
  });
}

function updateQty(idx, val) {
  const num = parseFloat(val);
  supplyItems[idx].quantity = isNaN(num) ? 0 : num;
}

function updatePrice(idx, val) {
  const num = parseFloat(val);
  supplyItems[idx].costPrice = isNaN(num) ? 0 : num;
}

function removeItem(idx) {
  supplyItems.splice(idx, 1);
  renderTable();
}

async function submitSupply() {
  if (supplyItems.length === 0) {
    return showAlert(
      "Добавьте товары для оформления поступления",
      "Внимание",
      "error",
    );
  }

  const supplierId = document.getElementById("supplySupplier").value;
  if (!supplierId) {
    return showAlert("Необходимо выбрать поставщика", "Ошибка", "error");
  }

  const payload = {
    supplierId: parseInt(supplierId),
    items: supplyItems.map((i) => ({
      barcode: i.barcode,
      sku: i.sku,
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
      showAlert("Поступление успешно оформлено!", "Успех", "success");
      // Вместо перезагрузки просто очищаем форму
      supplyItems = [];
      renderTable();
      document.getElementById("supplySupplier").value = "";
    } else {
      const errData = await res.json();
      showAlert(
        errData.message || "Не удалось сохранить поступление",
        "Ошибка",
        "error",
      );
    }
  } catch (e) {
    showAlert("Ошибка связи с сервером", "Ошибка", "error");
  }
}

init();
