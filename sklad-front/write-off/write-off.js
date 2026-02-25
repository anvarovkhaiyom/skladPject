let selectedProduct = null;
let allProducts = [];

// 1. Загрузка всех товаров склада
async function loadAllProducts() {
  try {
    const res = await fetch("http://194.163.157.81:9090/admin/product", {
      headers: getHeaders(),
    });
    if (res.ok) {
      allProducts = await res.json();
    }
  } catch (e) {
    showAlert("Ошибка загрузки товаров", "error");
  }
}

// 2. Умный поиск (Имя, Штрих-код, Артикул/SKU)
function searchProducts(query) {
  const resultsDiv = document.getElementById("searchResults");
  resultsDiv.innerHTML = "";

  if (!query || query.length < 2) return; // Начинаем поиск от 2-х символов

  const q = query.toLowerCase();

  const filtered = allProducts.filter((p) => {
    return (
      p.name.toLowerCase().includes(q) ||
      (p.barcode && p.barcode.includes(q)) ||
      (p.sku && p.sku.toLowerCase().includes(q))
    );
  });

  // Если найден ровно один товар и введен точный штрих-код/артикул — выбираем его сразу
  if (
    filtered.length === 1 &&
    (filtered[0].barcode === query || filtered[0].sku === query)
  ) {
    selectProductForWriteOff(filtered[0]);
    return;
  }

  filtered.forEach((p) => {
    const item = document.createElement("div");
    item.className = "search-item";
    item.style =
      "padding:12px; cursor:pointer; border-bottom:1px solid #eee; background:#fff;";

    item.innerHTML = `
        <div style="display:flex; justify-content:space-between;">
            <strong>${p.name}</strong>
            <span style="color:#27ae60;">${p.stockQuantity} ${p.unit || "шт"}</span>
        </div>
        <div style="font-size:0.85em; color:#7f8c8d;">
            ${p.sku ? "Арт: " + p.sku : ""} ${p.barcode ? " | Штрих: " + p.barcode : ""}
        </div>
    `;

    item.onclick = () => selectProductForWriteOff(p);
    resultsDiv.appendChild(item);
  });
}

// 3. Выбор товара
function selectProductForWriteOff(product) {
  selectedProduct = product;

  // Обновляем заголовок формы
  document.getElementById("selectedProdName").innerHTML = `
    <span style="color:#7f8c8d; font-weight:normal;">Выбран товар:</span><br>
    ${product.name} <b style="color:#e67e22;">(Остаток: ${product.stockQuantity} ${product.unit || "шт"})</b>
  `;

  // Показываем форму и прячем поиск
  document.getElementById("writeOffForm").style.display = "block";
  document.getElementById("searchResults").innerHTML = "";
  document.getElementById("prodSearch").value = "";

  // Автоматически ставим курсор на поле ввода количества
  setTimeout(() => {
    document.getElementById("woQty").focus();
  }, 100);
}

// 4. Отправка данных на сервер
async function submitWriteOff() {
  const qtyInput = document.getElementById("woQty");
  const qty = parseFloat(qtyInput.value);
  const reason = document.getElementById("woReason").value;

  if (!selectedProduct) return showAlert("Сначала выберите товар!", "error");
  if (isNaN(qty) || qty <= 0) return showAlert("Введите количество!", "error");

  if (qty > selectedProduct.stockQuantity) {
    return showAlert(
      `На складе нет такого количества! (Макс: ${selectedProduct.stockQuantity})`,
      "error",
    );
  }

  const payload = {
    productId: selectedProduct.id,
    quantity: qty,
    reason: reason,
  };

  try {
    const res = await fetch("http://194.163.157.81:9090/admin/write-off", {
      method: "POST",
      headers: getHeaders(),
      body: JSON.stringify(payload),
    });

    if (res.ok) {
      showAlert("Списание успешно оформлено!", "success");

      // Сбрасываем форму
      document.getElementById("writeOffForm").style.display = "none";
      qtyInput.value = "";
      selectedProduct = null;

      // Обновляем список товаров (чтобы остатки были актуальны)
      await loadAllProducts();
    } else {
      const err = await res.json();
      showAlert(err.message || "Ошибка сервера", "error");
    }
  } catch (e) {
    showAlert("Сетевая ошибка!", "error");
  }
}

// --- ВСПОМОГАТЕЛЬНЫЕ ФУНКЦИИ ---
function getHeaders() {
  return {
    "Content-Type": "application/json",
    Authorization: "Bearer " + localStorage.getItem("token"),
  };
}

function showAlert(message, type = "info") {
  const modal = document.getElementById("alertModal");
  const msgElem = document.getElementById("alertMessage");
  const titleElem = document.getElementById("alertTitle");

  if (!modal) return alert(message);

  titleElem.innerText = type === "error" ? "Ошибка" : "Успешно";
  titleElem.style.color = type === "error" ? "#e74c3c" : "#27ae60";
  msgElem.innerText = message;
  modal.style.display = "flex";

  document.getElementById("alertCloseBtn").onclick = () => {
    modal.style.display = "none";
  };
}

// Запуск при загрузке
document.addEventListener("DOMContentLoaded", loadAllProducts);
