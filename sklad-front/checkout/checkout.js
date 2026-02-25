const API_BASE = "http://194.163.157.81:9090/employee";
let cart = [];
let allCurrencies = [];
let allProducts = [];
let warehouseBaseCurrency = "";

async function init() {
  try {
    const today = new Date().toISOString().split("T")[0];
    const proxyDateEl = document.getElementById("proxyDate");
    if (proxyDateEl) proxyDateEl.value = today;

    const whRes = await fetch(`${API_BASE}/warehouse/current`, {
      headers: getHeaders(),
    });
    const whData = await whRes.json();
    warehouseBaseCurrency = whData.baseCurrencyCode;

    document
      .querySelectorAll("#warehouseCurrencyLabel")
      .forEach((el) => (el.innerText = warehouseBaseCurrency));

    await loadClients();
    await loadCurrencies();
    await fetchProducts();
    await loadDrivers();

    loadFromLocalStorage();
    updateCartCounter();

    [
      "carNumber",
      "proxyDate",
      "selectClient",
      "driverName",
      "proxyNumber",
      "carMark",
    ].forEach((id) => {
      const el = document.getElementById(id);
      if (el) el.addEventListener("change", saveToLocalStorage);
    });

    const alertBtn = document.getElementById("alertCloseBtn");
    if (alertBtn)
      alertBtn.onclick = () =>
        (document.getElementById("alertModal").style.display = "none");
  } catch (e) {
    console.error("Ошибка старта:", e);
    showAlert("Не удалось подключиться к серверу", "Ошибка", "error");
  }
}
let allDrivers = []; // Храним тут всех водителей

async function loadDrivers() {
  try {
    const res = await fetch(`${API_BASE}/driver`, { headers: getHeaders() });
    allDrivers = await res.json();
    const select = document.getElementById("selectDriver");
    if (!select) return;

    select.innerHTML = '<option value="">Выберите водителя...</option>';
    allDrivers.forEach((d) => {
      // Отображаем Имя и Номер авто для удобства выбора
      select.innerHTML += `<option value="${d.id}">${d.fullName} (${d.carNumber || "---"})</option>`;
    });

    select.addEventListener("change", (e) => {
      const driverId = e.target.value;
      const selectedDriver = allDrivers.find((d) => d.id == driverId);

      // Находим наши скрытые поля
      const driverNameInput = document.getElementById("driverName");
      const carNumberInput = document.getElementById("carNumber");
      const carMarkInput = document.getElementById("carMark");

      if (selectedDriver) {
        // Заполняем данными из БД
        if (driverNameInput)
          driverNameInput.value = selectedDriver.fullName || "";
        if (carNumberInput)
          carNumberInput.value = selectedDriver.carNumber || "";
        if (carMarkInput) carMarkInput.value = selectedDriver.carMark || "";
      } else {
        // Если сбросили выбор - очищаем
        [driverNameInput, carNumberInput, carMarkInput].forEach((el) => {
          if (el) el.value = "";
        });
      }
      saveToLocalStorage();
    });
  } catch (e) {
    console.error("Ошибка загрузки водителей:", e);
  }
}
function saveToLocalStorage() {
  localStorage.setItem("checkout_cart", JSON.stringify(cart));

  // Сохраняем все видимые и скрытые поля формы
  const form = {
    carNumber: document.getElementById("carNumber")?.value || "",
    clientId: document.getElementById("selectClient")?.value || "",
    proxyDate: document.getElementById("proxyDate")?.value || "",
    docNumber: document.getElementById("docNumber")?.value || "",
    carMark: document.getElementById("carMark")?.value || "",
    driverName: document.getElementById("driverName")?.value || "",
    proxyNumber: document.getElementById("proxyNumber")?.value || "",
  };
  localStorage.setItem("checkout_form", JSON.stringify(form));
}

function loadFromLocalStorage() {
  // Загружаем корзину
  const savedCart = localStorage.getItem("checkout_cart");
  if (savedCart) {
    cart = JSON.parse(savedCart);
  }

  // Загружаем данные формы и расставляем их по input-ам
  const savedForm = localStorage.getItem("checkout_form");
  if (savedForm) {
    const form = JSON.parse(savedForm);
    for (const key in form) {
      const el = document.getElementById(key);
      if (el) el.value = form[key];
    }
  }
}
function resetAllData() {
  // 1. Очищаем переменные в памяти
  cart = [];

  // 2. Удаляем данные из браузера
  localStorage.removeItem("checkout_cart");
  localStorage.removeItem("checkout_form");

  // 3. Очищаем визуально поля формы (включая скрытые)
  const fields = [
    "carNumber",
    "selectClient",
    "docNumber",
    "carMark",
    "driverName",
    "proxyNumber",
  ];
  fields.forEach((id) => {
    const el = document.getElementById(id);
    if (el) el.value = "";
  });

  // Сбрасываем дату на "сегодня"
  const today = new Date().toISOString().split("T")[0];
  const proxyDateEl = document.getElementById("proxyDate");
  if (proxyDateEl) proxyDateEl.value = today;

  // 4. Обновляем интерфейс
  updateCartCounter();
  renderCatalog(allProducts); // чтобы вернулись остатки в таблицу
}
// --- РАБОТА С ТОВАРАМИ ---
async function fetchProducts() {
  try {
    const res = await fetch(`${API_BASE}/product`, { headers: getHeaders() });
    allProducts = await res.json();
    renderCatalog(allProducts);
  } catch (e) {
    console.error("Ошибка загрузки товаров", e);
  }
}
function filterCatalog(query) {
  const q = query.toLowerCase();
  const filtered = allProducts.filter(
    (p) =>
      p.name.toLowerCase().includes(q) || (p.barcode && p.barcode.includes(q)),
  );
  renderCatalog(filtered);
}

function renderCatalog(products) {
  const body = document.getElementById("catalogBody");
  if (!body) return;
  body.innerHTML = "";

  products
    .filter((p) => !p.deleted)
    .forEach((p) => {
      const inCart = cart.find((item) => item.id === p.id);
      const qtyInCart = inCart ? inCart.quantity : 0;
      const available = p.stockQuantity - qtyInCart;

      body.innerHTML += `
        <tr>
            <td data-label="Товар"><b>${p.name}</b><br><small style="color:gray">${p.barcode || "---"}</small></td>
            <td data-label="В наличии"><b style="color: ${available > 0 ? "#28a745" : "#dc3545"}">${available} ${p.unit}</b></td>
            <td data-label="Цена">${p.salePrice.toLocaleString()} ₸</td>
            <td data-label="Действие" style="text-align: right;">
                <button class="btn-add-cart" 
                    style="background: ${available > 0 ? "#28a745" : "#ccc"}; color: white; padding: 8px 15px; border:none; border-radius:4px; cursor:pointer; width: 100%; max-width: 150px;"
                    onclick='addToCart(${JSON.stringify(p)})'
                    ${available <= 0 ? "disabled" : ""}>
                    + Добавить
                </button>
            </td>
        </tr>`;
    });
}

function addToCart(p) {
  const existing = cart.find((item) => item.id === p.id);
  const qtyInCart = existing ? existing.quantity : 0;

  if (qtyInCart + 1 > p.stockQuantity) {
    return showAlert(
      `Недостаточно товара! На складе: ${p.stockQuantity}`,
      "Ошибка",
      "warning",
    );
  }

  if (existing) {
    existing.quantity++;
  } else {
    cart.push({ ...p, quantity: 1 });
  }

  updateCartCounter();
  renderCatalog(allProducts); // Обновляем остатки в таблице
  saveToLocalStorage();
}

function updateQty(index, val, max) {
  val = parseFloat(val);

  // Если ввели не число или меньше 0
  if (isNaN(val) || val < 0) val = 1;

  if (val > max) {
    showAlert(`Максимум доступно: ${max}`, "Внимание", "warning");
    val = max; // Принудительно ставим максимум
  }

  if (val === 0) {
    // Если 0 - удаляем, но можно оставить и 1, как тебе удобнее
    cart.splice(index, 1);
  } else {
    cart[index].quantity = val;
  }

  updateCartCounter();
  renderCatalog(allProducts);
  saveToLocalStorage();
  renderCartInModal();
}

function removeItem(index) {
  cart.splice(index, 1);
  updateCartCounter();
  renderCatalog(allProducts);
  saveToLocalStorage();
  renderCartInModal();
  if (cart.length === 0) closeCheckoutModal();
}
function updateCartCounter() {
  const counter = document.getElementById("cartCount");
  if (counter) counter.innerText = cart.length;
}

// --- ОПЛАТА И ВАЛЮТЫ ---
async function loadCurrencies() {
  const res = await fetch(`${API_BASE}/currency`, { headers: getHeaders() });
  allCurrencies = await res.json();
  const select = document.getElementById("payCurrency");
  if (!select) return;

  select.innerHTML = allCurrencies
    .map(
      (c) =>
        `<option value="${c.code}" ${c.code === warehouseBaseCurrency ? "selected" : ""}>${c.code} - ${c.name}</option>`,
    )
    .join("");

  select.addEventListener("change", changePaymentCurrency);
  changePaymentCurrency();
}

function changePaymentCurrency() {
  const code = document.getElementById("payCurrency").value;
  const currency = allCurrencies.find((c) => c.code === code);
  const rateInput = document.getElementById("exchangeRate");
  const container = document.getElementById("rateContainer");

  if (code === warehouseBaseCurrency) {
    if (container) container.style.display = "none";
    if (rateInput) rateInput.value = 1;
  } else {
    if (container) container.style.display = "block";
    if (rateInput) rateInput.value = currency ? currency.rate : 1;
  }
  recalcForeignAmount();
}

function recalcForeignAmount() {
  const totalBase =
    parseFloat(document.getElementById("totalBaseDisplay")?.innerText) || 0;
  const rate = parseFloat(document.getElementById("exchangeRate")?.value) || 1;
  const payInput = document.getElementById("payAmount");
  if (payInput) {
    payInput.value = (totalBase / rate).toFixed(2);
    calculateChange();
  }
}

function calculateChange() {
  const entered = parseFloat(document.getElementById("payAmount").value) || 0;
  const rate = parseFloat(document.getElementById("exchangeRate").value) || 1;
  const totalBase =
    parseFloat(document.getElementById("totalBaseDisplay").innerText) || 0;
  const diff = entered * rate - totalBase;
  const resDiv = document.getElementById("changeResult");
  const curr = document.getElementById("payCurrency").value;

  if (!resDiv) return;

  if (Math.abs(diff) < 0.1)
    resDiv.innerHTML = '<b style="color:green">Без сдачи</b>';
  else if (diff > 0)
    resDiv.innerHTML = `<b style="color:green">Сдача: ${(diff / rate).toFixed(2)} ${curr}</b>`;
  else
    resDiv.innerHTML = `<b style="color:red">Не хватает: ${(Math.abs(diff) / rate).toFixed(2)} ${curr}</b>`;
}

// --- МОДАЛКИ И ОТРИСОВКА ---
function openCheckoutModal() {
  if (cart.length === 0) return showAlert("Корзина пуста", "Внимание", "info");
  renderCartInModal();
  document.getElementById("checkoutModal").style.display = "block";
}

function closeCheckoutModal() {
  document.getElementById("checkoutModal").style.display = "none";
}

function renderCartInModal() {
  const body = document.getElementById("cartModalBody");
  if (!body) return;
  body.innerHTML = "";
  let total = 0;

  cart.forEach((item, index) => {
    const sum = item.salePrice * item.quantity;
    total += sum;
    body.innerHTML += `
        <tr>
            <td data-label="Товар">${item.name}</td>
            <td data-label="Кол-во">
                <input type="number" 
                       value="${item.quantity}" 
                       min="1" 
                       max="${item.stockQuantity}" 
                       style="width:70px; padding: 5px;"
                       onchange="updateQty(${index}, this.value, ${item.stockQuantity})">
            </td>
            <td data-label="Сумма">${sum.toFixed(2)} ₸</td>
            <td class="actions">
                <button onclick="removeItem(${index})" style="background:#ff4757; color:white; border:none; border-radius:4px; padding: 5px 10px; cursor:pointer;">×</button>
            </td>
        </tr>`;
  });

  const totalDisplay = document.getElementById("totalBaseDisplay");
  if (totalDisplay) totalDisplay.innerText = total.toFixed(2);
  recalcForeignAmount();
}
function showAlert(message, title = "Уведомление", type = "info") {
  const modal = document.getElementById("alertModal");
  document.getElementById("alertTitle").innerText = title;
  document.getElementById("alertMessage").innerText = message;

  const titleEl = document.getElementById("alertTitle");
  titleEl.style.color =
    type === "error" ? "red" : type === "success" ? "green" : "black";

  modal.style.display = "block";
}

function getHeaders() {
  return {
    "Content-Type": "application/json",
    Authorization: "Bearer " + localStorage.getItem("token"),
  };
}
async function processSale() {
  // 1. Проверка на пустую корзину
  if (cart.length === 0) {
    showAlert("Корзина пуста!", "Ошибка", "error");
    return; // ОСТАНАВЛИВАЕМ выполнение здесь
  }

  // 2. ЖЕСТКАЯ ПРОВЕРКА ОСТАТКОВ
  // Используем обычный цикл, чтобы было проще выйти через return
  for (let i = 0; i < cart.length; i++) {
    const item = cart[i];
    if (item.quantity > item.stockQuantity) {
      showAlert(
        `Ошибка: ${item.name}. В наличии ${item.stockQuantity}, а вы ввели ${item.quantity}`,
        "Превышение остатка",
        "error",
      );
      return; // ВАЖНО: Прекращаем выполнение, запрос к API не уйдет!
    }
  }

  // 3. Расчеты
  const totalBase =
    parseFloat(document.getElementById("totalBaseDisplay").innerText) || 0;
  const entered = parseFloat(document.getElementById("payAmount").value) || 0;
  const rate = parseFloat(document.getElementById("exchangeRate").value) || 1;

  // 4. Проверка суммы оплаты
  if (entered * rate < totalBase - 0.1) {
    showAlert("Сумма оплаты меньше итоговой суммы!", "Ошибка", "error");
    return; // ОСТАНАВЛИВАЕМ
  }

  // Если дошли сюда — значит всё честно, формируем данные
  const payload = {
    proxyDate: document.getElementById("proxyDate").value,
    carNumber: document.getElementById("carNumber")?.value || "",
    carMark: document.getElementById("carMark")?.value || "",
    driverName: document.getElementById("driverName")?.value || "",
    proxyDate: document.getElementById("proxyDate").value,
    clientId: document.getElementById("selectClient").value || null,
    docNumber: document.getElementById("docNumber")?.value || "",
    proxyNumber: document.getElementById("proxyNumber")?.value || "",
    items: cart.map((i) => ({
      productId: i.id,
      quantity: i.quantity,
      price: i.salePrice,
    })),
    payments: [
      {
        amount: entered,
        currency: document.getElementById("payCurrency").value,
        rate: rate,
        method: document.getElementById("payMethod").value,
      },
    ],
  };

  try {
    const res = await fetch(`${API_BASE}/sale`, {
      method: "POST",
      headers: getHeaders(),
      body: JSON.stringify(payload),
    });

    if (res.ok) {
      showAlert("Продажа успешно оформлена!", "Успех", "success");
      resetAllData();
      closeCheckoutModal();
      await fetchProducts();
    } else {
      const err = await res.json();
      showAlert(err.message || "Ошибка при сохранении", "Ошибка", "error");
    }
  } catch (e) {
    showAlert("Ошибка сети", "Ошибка", "error");
  }
}

async function loadClients() {
  const res = await fetch(`${API_BASE}/client`, { headers: getHeaders() });
  const data = await res.json();
  const select = document.getElementById("selectClient");
  if (!select) return;
  select.innerHTML = '<option value="">Выберите клиента...</option>';
  data.forEach((c) => {
    select.innerHTML += `<option value="${c.id}">${c.fullName}</option>`;
  });
}

init();
