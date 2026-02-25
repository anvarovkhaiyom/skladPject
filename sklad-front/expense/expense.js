let allExpenses = []; // Кэш всех расходов для быстрой фильтрации

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

  titleElem.innerText = type === "error" ? "Ошибка" : "Успешно";
  titleElem.style.color = type === "error" ? "#e74c3c" : "#27ae60";
  msgElem.innerText = message;
  modal.style.display = "flex";

  document.getElementById("alertCloseBtn").onclick = () => {
    modal.style.display = "none";
  };
}
// Функция для получения даты в формате YYYY-MM-DD
function getTodayDate() {
  return new Date().toISOString().split("T")[0];
}

// Вызываем при загрузке страницы
document.addEventListener("DOMContentLoaded", () => {
  // Устанавливаем сегодняшнюю дату в инпуты
  const today = getTodayDate();
  const fromInput = document.getElementById("filterFrom");
  const toInput = document.getElementById("filterTo");

  if (fromInput && toInput) {
    fromInput.value = today;
    toInput.value = today;
  }

  // Загружаем данные
  loadExpenses();
});

// Обнови функцию очистки, чтобы она тоже возвращала к "Сегодня"
function clearFilters() {
  const today = getTodayDate();
  document.getElementById("filterFrom").value = today;
  document.getElementById("filterTo").value = today;
  applyFilters();
}
// Загрузка данных с сервера
async function loadExpenses() {
  try {
    const res = await fetch("http://194.163.157.81:9090/admin/expenses", {
      headers: getHeaders(),
    });
    if (res.ok) {
      allExpenses = await res.json();
      applyFilters(); // Вызываем фильтрацию (она же отрисует таблицу)
    }
  } catch (e) {
    console.error("Ошибка сети", e);
  }
}

// Логика фильтрации и сортировки
function applyFilters() {
  const fromDate = document.getElementById("filterFrom").value;
  const toDate = document.getElementById("filterTo").value;

  let filtered = [...allExpenses];

  if (fromDate || toDate) {
    filtered = filtered.filter((e) => {
      // Формат даты из БД в YYYY-MM-DD для сравнения с инпутом
      const d = new Date(e.expenseDate).toISOString().split("T")[0];
      if (fromDate && d < fromDate) return false;
      if (toDate && d > toDate) return false;
      return true;
    });
  }

  // Сортировка: новые сверху
  filtered.sort((a, b) => new Date(b.expenseDate) - new Date(a.expenseDate));

  renderExpenseTable(filtered);
}
function renderExpenseTable(data) {
  const body = document.getElementById("expenseBody");
  if (!body) return;

  if (data.length === 0) {
    body.innerHTML =
      '<tr><td colspan="4" style="text-align:center">Ничего не найдено</td></tr>';
    return;
  }

  body.innerHTML = data
    .map(
      (e) => `
        <tr>
            <td data-label="Дата">${new Date(e.expenseDate).toLocaleDateString()}</td>
            <td data-label="Категория">${e.category}</td>
            <td data-label="Сумма" style="color:red; font-weight:bold;">-${e.amount.toLocaleString()} ₸</td>
            <td data-label="Комментарий">${e.description || "—"}</td>
        </tr>
    `,
    )
    .join("");
}

async function saveExpense() {
  const amountInput = document.getElementById("expAmount");
  const categoryInput = document.getElementById("expCategory");
  const descInput = document.getElementById("expDesc");

  const amount = parseFloat(amountInput.value);
  if (isNaN(amount) || amount <= 0)
    return showAlert("Введите корректную сумму", "error");

  const payload = {
    category: categoryInput.value,
    amount: amount,
    description: descInput.value.trim(),
  };

  try {
    const res = await fetch("http://194.163.157.81:9090/admin/expense", {
      method: "POST",
      headers: getHeaders(),
      body: JSON.stringify(payload),
    });

    if (res.ok) {
      showAlert("Расход сохранен", "success");
      closeExpenseModal();
      amountInput.value = "";
      descInput.value = "";
      loadExpenses(); // Перезагружаем кэш и таблицу
    } else {
      showAlert("Ошибка сохранения", "error");
    }
  } catch (e) {
    showAlert("Ошибка сервера", "error");
  }
}

function openExpenseModal() {
  document.getElementById("expenseModal").style.display = "flex";
}
function closeExpenseModal() {
  document.getElementById("expenseModal").style.display = "none";
}

document.addEventListener("DOMContentLoaded", loadExpenses);
