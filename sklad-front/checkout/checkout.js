const API_BASE = "http://194.163.157.81:9090/employee";
let cart = [];
let allCurrencies = [];
let warehouseBaseCurrency = "";

async function init() {
  try {
    const whRes = await fetch(`${API_BASE}/warehouse/current`, {
      headers: getHeaders(),
    });
    if (!whRes.ok) throw new Error("Не удалось узнать валюту склада");

    const whData = await whRes.json();
    warehouseBaseCurrency = whData.baseCurrencyCode;
    document.getElementById("warehouseCurrencyLabel").innerText =
      warehouseBaseCurrency;

    await loadClients();
    await loadCurrencies();
  } catch (e) {
    console.error("Ошибка старта:", e);
    showAlert(
      "Ошибка инициализации кассы! Проверьте соединение.",
      "Ошибка",
      "error",
    );
  }
}

async function loadCurrencies() {
  try {
    const res = await fetch(`${API_BASE}/currency`, { headers: getHeaders() });
    if (!res.ok) throw new Error();
    allCurrencies = await res.json();

    const select = document.getElementById("payCurrency");
    select.innerHTML = "";

    allCurrencies.forEach((c) => {
      const isSelected = c.code === warehouseBaseCurrency ? "selected" : "";
      select.innerHTML += `<option value="${c.code}" ${isSelected}>${c.code} - ${c.name}</option>`;
    });

    changePaymentCurrency();
  } catch (e) {
    showAlert("Не удалось загрузить валюты", "Ошибка", "error");
  }
}
function changePaymentCurrency() {
  const selectedCode = document.getElementById("payCurrency").value;
  const rateInput = document.getElementById("exchangeRate");
  const rateContainer = document.getElementById("rateContainer");
  const currencyData = allCurrencies.find((c) => c.code === selectedCode);

  if (selectedCode === warehouseBaseCurrency) {
    rateContainer.style.display = "none";
    rateInput.value = 1;
  } else {
    rateContainer.style.display = "block";
    rateInput.value = currencyData ? currencyData.rate : 1.0;
  }

  recalcForeignAmount();
}
function recalcForeignAmount() {
  const totalBase =
    parseFloat(document.getElementById("totalBaseDisplay").innerText) || 0;
  const rate = parseFloat(document.getElementById("exchangeRate").value) || 1;
  const payInput = document.getElementById("payAmount");

  if (totalBase === 0) {
    payInput.value = "";
    return;
  }
  const neededAmount = (totalBase / rate).toFixed(2);
  payInput.value = neededAmount;

  calculateChange();
}
function calculateChange() {
  // Сколько нужно оплатить (цифра в поле input, которую мы сами туда вписали или кассир поменял)
  const amountEntered =
    parseFloat(document.getElementById("payAmount").value) || 0;

  // Переводим введенную сумму обратно в базовую валюту, чтобы понять масштаб
  const rate = parseFloat(document.getElementById("exchangeRate").value) || 1;
  const totalBaseNeeded =
    parseFloat(document.getElementById("totalBaseDisplay").innerText) || 0;

  // Сколько клиент дает в пересчете на базу
  const valueInBase = amountEntered * rate;

  const diff = valueInBase - totalBaseNeeded;
  const resultDiv = document.getElementById("changeResult");
  const currencyCode = document.getElementById("payCurrency").value;

  if (amountEntered === 0) {
    resultDiv.innerHTML = "";
    return;
  }
  if (Math.abs(diff) < 0.05) {
    resultDiv.innerHTML = `<span style="color:green">Без сдачи</span>`;
  } else if (diff > 0) {
    const changeInForeign = (diff / rate).toFixed(2);
    resultDiv.innerHTML = `<span style="color:green">Сдача: ${changeInForeign} ${currencyCode}</span>`;
  } else {
    const debtInForeign = (Math.abs(diff) / rate).toFixed(2);
    resultDiv.innerHTML = `<span style="color:red">Не хватает: ${debtInForeign} ${currencyCode}</span>`;
  }
}
async function loadClients() {
  const res = await fetch(`${API_BASE}/client`, { headers: getHeaders() });
  const data = await res.json();
  const select = document.getElementById("selectClient");
  data.forEach(
    (c) =>
      (select.innerHTML += `<option value="${c.id}">${c.fullName}</option>`),
  );
}
async function handleBarcode(e) {
  if (e.key === "Enter") {
    const query = e.target.value.trim().toLowerCase();
    if (!query) return;

    try {
      const res = await fetch(`${API_BASE}/product`, { headers: getHeaders() });
      const products = await res.json();

      const product = products.find(
        (p) => p.barcode === query || p.name.toLowerCase().includes(query),
      );

      if (product) {
        addToCart(product);
        e.target.value = "";
      } else {
        showAlert("Товар не найден", "Внимание", "error");
      }
    } catch (e) {
      showAlert("Ошибка при поиске", "Ошибка", "error");
    }
  }
}

function renderCart() {
  const body = document.getElementById("cartBody");
  body.innerHTML = "";
  let totalBase = 0;

  if (cart.length === 0) {
    body.innerHTML =
      '<tr><td colspan="5" style="text-align:center">Корзина пуста</td></tr>';
  }

  cart.forEach((item, index) => {
    const sum = item.salePrice * item.quantity;
    totalBase += sum;
    body.innerHTML += `
            <tr>
                <td data-label="Товар">${item.name}</td>
                <td data-label="Цена">${item.salePrice}</td>
                <td data-label="Кол-во">
                    <input type="number" value="${item.quantity}" 
                           style="width:70px; padding: 5px;" 
                           onchange="updateQty(${index}, this.value)">
                </td>
                <td data-label="Сумма">${sum.toFixed(2)}</td>
                <td data-label="Удалить">
                    <button class="btn-delete" onclick="removeItem(${index})">×</button>
                </td>
            </tr>`;
  });

  document.getElementById("totalBaseDisplay").innerText = totalBase.toFixed(2);
  recalcForeignAmount();
}

function addToCart(p) {
  const existing = cart.find((item) => item.id === p.id);
  if (existing) existing.quantity++;
  else cart.push({ ...p, quantity: 1 });
  renderCart();
}

function updateQty(index, val) {
  if (val <= 0) removeItem(index);
  else cart[index].quantity = parseFloat(val);
  renderCart();
}

function removeItem(index) {
  cart.splice(index, 1);
  renderCart();
}

// ЛОГИКА ВАЛЮТ
function updateRateDisplay() {
  const curr = document.getElementById("payCurrency").value;
  const rateContainer = document.getElementById("rateContainer");
  const rateInput = document.getElementById("exchangeRate");

  if (curr === BASE_CURRENCY) {
    rateContainer.style.display = "none";
    rateInput.value = 1;
  } else {
    rateContainer.style.display = "block";
    // Ищем сохраненный курс валюты
    const currencyObj = currencies.find((c) => c.code === curr);
    rateInput.value = currencyObj && currencyObj.rate ? currencyObj.rate : "";
  }

  // Сразу считаем и вписываем сумму
  autoCalculateNeededAmount();
}
function autoCalculateNeededAmount() {
  const totalNeededKzt =
    parseFloat(document.getElementById("totalBase").innerText) || 0;
  const rate = parseFloat(document.getElementById("exchangeRate").value) || 0;
  const payAmountInput = document.getElementById("payAmount");
  const curr = document.getElementById("payCurrency").value;

  // Если итоговая сумма 0, очищаем поле
  if (totalNeededKzt === 0) {
    payAmountInput.value = "";
    document.getElementById("conversionResult").innerHTML = "";
    return;
  }

  if (curr === BASE_CURRENCY) {
    // Если тенге — просто копируем сумму
    payAmountInput.value = totalNeededKzt;
  } else {
    // Если валюта и курс указан — делим тенге на курс
    if (rate > 0) {
      const needed = (totalNeededKzt / rate).toFixed(2); // Округляем до копеек/центов
      payAmountInput.value = needed;
    }
  }

  // Обновляем текст про сдачу (внизу)
  calculateConversion();
}
function calculateConversion() {
  const payAmount = parseFloat(document.getElementById("payAmount").value) || 0;
  const rate = parseFloat(document.getElementById("exchangeRate").value) || 0;
  const totalNeededKzt =
    parseFloat(document.getElementById("totalBase").innerText) || 0;
  const resultDiv = document.getElementById("conversionResult");
  const curr = document.getElementById("payCurrency").value;

  // Сколько это в тенге
  const totalInBase = (payAmount * rate).toFixed(2);

  if (totalNeededKzt > 0 && payAmount > 0) {
    let infoHtml = `К зачислению: ${totalInBase} ${BASE_CURRENCY}`;

    // Если переплата
    if (parseFloat(totalInBase) > totalNeededKzt) {
      const change = (totalInBase - totalNeededKzt).toFixed(2);
      infoHtml += ` <br><span style="color:green; font-weight:bold;">Сдача: ${change} ${BASE_CURRENCY}</span>`;
    }
    // Если недоплата
    else if (parseFloat(totalInBase) < totalNeededKzt) {
      const debt = (totalNeededKzt - totalInBase).toFixed(2);
      infoHtml += ` <br><span style="color:red;">Недостаточно: ${debt} ${BASE_CURRENCY}</span>`;
    }

    resultDiv.innerHTML = infoHtml;
  } else {
    resultDiv.innerHTML = "";
  }
}
function clearForm() {
  cart = [];
  renderCart();

  const fieldsToClear = [
    "docNumber",
    "carMark",
    "carNumber",
    "driverName",
    "proxyNumber",
    "proxyDate",
    "payAmount",
  ];

  fieldsToClear.forEach((id) => {
    const el = document.getElementById(id);
    if (el) el.value = "";
  });

  const selectClient = document.getElementById("selectClient");
  if (selectClient) selectClient.selectedIndex = 0;

  const payMethod = document.getElementById("payMethod");
  if (payMethod) payMethod.selectedIndex = 0;

  // 4. Очищаем блок сдачи
  const changeDiv = document.getElementById("changeResult");
  if (changeDiv) changeDiv.innerHTML = "";

  console.log("Форма полностью очищена");
}
async function processSale() {
  if (cart.length === 0) {
    return showAlert(
      "Добавьте хотя бы один товар в корзину",
      "Внимание",
      "error",
    );
  }

  const totalBaseNeeded =
    parseFloat(document.getElementById("totalBaseDisplay").innerText) || 0;
  const rawAmount = document.getElementById("payAmount").value;
  const payAmount = rawAmount ? parseFloat(rawAmount) : totalBaseNeeded;
  const rate = parseFloat(document.getElementById("exchangeRate").value) || 1;

  if (payAmount * rate < totalBaseNeeded - 0.01) {
    return showAlert(
      "Введенной суммы недостаточно для оплаты",
      "Ошибка оплаты",
      "error",
    );
  }

  const payload = {
    documentNumber: document.getElementById("docNumber").value,
    carMark: document.getElementById("carMark").value,
    carNumber: document.getElementById("carNumber").value,
    driverName: document.getElementById("driverName").value,
    proxyNumber: document.getElementById("proxyNumber").value,
    proxyDate: document.getElementById("proxyDate").value || null,
    clientId: document.getElementById("selectClient").value || null,
    items: cart.map((i) => ({
      productId: i.id,
      quantity: i.quantity,
      price: i.salePrice,
      barcode: i.barcode,
    })),
    payments: [
      {
        amount: payAmount,
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
      const result = await res.json();
      const change = parseFloat(result.changeAmount || 0).toFixed(2);
      const changeMessage =
        parseFloat(change) > 0
          ? ` Сдача: ${change} ${warehouseBaseCurrency}`
          : "";

      showAlert(
        `Продажа №${result.id} успешно завершена!${changeMessage}`,
        "Успех",
        "success",
      );

      clearForm();
      cart = [];
      renderCart();
      document.getElementById("payAmount").value = "";
      document.getElementById("changeResult").innerHTML = "";
    } else {
      const err = await res.json();
      showAlert(
        "Ошибка сервера: " + (err.message || "Не удалось провести продажу"),
        "Ошибка",
        "error",
      );
    }
  } catch (e) {
    showAlert(
      "Ошибка сети: проверьте соединение с сервером",
      "Ошибка",
      "error",
    );
  }
}
function getHeaders() {
  return {
    "Content-Type": "application/json",
    Authorization: "Bearer " + localStorage.getItem("token"),
  };
}

init();
