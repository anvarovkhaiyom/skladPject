package com.example.skladservicedevelop.service;

import com.example.skladservicedevelop.config.SecurityHelper;
import com.example.skladservicedevelop.database.model.*;
import com.example.skladservicedevelop.database.repository.*;
import com.example.skladservicedevelop.dto.ArchiveDocumentDto;
import com.example.skladservicedevelop.dto.MovementDto;
import com.example.skladservicedevelop.dto.response.SalesDataRow;
import com.example.skladservicedevelop.dto.response.SalesSummaryResponse;
import com.example.skladservicedevelop.dto.response.StockSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;
    private final SecurityHelper securityHelper;
    private final SupplyHistoryRepository supplyHistoryRepository;
    private final ExpenseRepository expenseRepository;
    private final WriteOffHistoryRepository writeOffRepository;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private final DateTimeFormatter longFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", new Locale("ru"));

    private CellStyle headerStyle;
    private CellStyle dataStyle;
    private CellStyle titleStyle;
    private CellStyle currencyStyle;

    @Override
    public byte[] generatePickingListExcel(Integer saleId) {
        SaleModel sale = getSale(saleId);
        try (Workbook workbook = new XSSFWorkbook()) {
            initStyles(workbook);
            Sheet sheet = workbook.createSheet("Лист сборки");
            createTitle(sheet, 0, "Лист сборки № " + getDocNumber(sale), 5);
            createRowKeyValue(sheet, 2, "Клиент:", getClientName(sale));
            createRowKeyValue(sheet, 3, "Дата:", sale.getSaleDate().format(formatter));

            String[] headers = {"№", "SKU", "Название", "Коробок", "Штук", "Ед"};
            createTableHeader(sheet, 5, headers);

            int rowIdx = 6;
            int npp = 1;
            for (SaleItemModel item : sale.getItems()) {
                Row row = sheet.createRow(rowIdx++);
                createCell(row, 0, npp++, dataStyle);
                createCell(row, 1, item.getProduct().getSku(), dataStyle);
                createCell(row, 2, item.getProduct().getName(), dataStyle);
                createCell(row, 3, item.getBoxCount(), dataStyle);
                createCell(row, 4, item.getQuantity(), dataStyle);
                createCell(row, 5, item.getProduct().getUnit(), dataStyle);
            }
            autoSizeColumns(sheet, headers.length);
            return workbookToBytes(workbook);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка Листа сборки", e);
        }
    }

    @Override
    public byte[] generateInvoiceWithBoxesExcel(Integer saleId) {
        SaleModel sale = getSale(saleId);
        try (Workbook workbook = new XSSFWorkbook()) {
            initStyles(workbook);
            Sheet sheet = workbook.createSheet("Накладная");
            String title = String.format("Расходная накладная № %s от %s", getDocNumber(sale), sale.getSaleDate().format(longFormatter));
            createTitle(sheet, 0, title, 7);

            createRowKeyValue(sheet, 2, "Покупатель:", getClientName(sale));

            String driverInfo = (sale.getDriverName() != null) ? sale.getDriverName() : "---";
            createRowKeyValue(sheet, 3, "Водитель:", driverInfo);

            if (sale.getProxyNumber() != null && !sale.getProxyNumber().isEmpty()) {
                String proxy = "№ " + sale.getProxyNumber() + (sale.getProxyDate() != null ? " от " + sale.getProxyDate().format(formatter) : "");
                createRowKeyValue(sheet, 4, "Доверенность:", proxy);
            }

            String[] headers = {"№", "Код", "Товар", "В коробке", "Коробок", "Штук", "Цена", "Сумма"};
            createTableHeader(sheet, 6, headers);

            int rowIdx = 7;
            int npp = 1;
            for (SaleItemModel item : sale.getItems()) {
                Row row = sheet.createRow(rowIdx++);
                createCell(row, 0, npp++, dataStyle);
                createCell(row, 1, item.getProduct().getSku(), dataStyle);
                createCell(row, 2, item.getProduct().getName(), dataStyle);
                createCell(row, 3, item.getItemsPerBoxAtSale(), dataStyle);
                createCell(row, 4, item.getBoxCount(), dataStyle);
                createCell(row, 5, item.getQuantity(), dataStyle);
                createCell(row, 6, item.getUnitPrice(), currencyStyle);
                createCell(row, 7, item.getTotalPrice(), currencyStyle);
            }

            Row totalRow = sheet.createRow(rowIdx + 1);
            createCell(totalRow, 6, "ИТОГО:", headerStyle);
            createCell(totalRow, 7, sale.getTotalAmount(), currencyStyle);

            autoSizeColumns(sheet, headers.length);
            return workbookToBytes(workbook);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка Накладной", e);
        }
    }

    @Override
    public byte[] generateZ2Report(Integer saleId) {
        SaleModel sale = getSale(saleId);
        try (Workbook workbook = new XSSFWorkbook()) {
            initStyles(workbook);
            Sheet sheet = workbook.createSheet("З-2");
            createTitle(sheet, 0, "НАКЛАДНАЯ З-2 № " + getDocNumber(sale), 6);
            createRowKeyValue(sheet, 2, "Получатель:", getClientName(sale));

            String[] headers = {"№", "Наименование", "SKU", "Ед", "Кол-во", "Цена", "Сумма"};
            createTableHeader(sheet, 4, headers);

            int rowIdx = 5;
            int npp = 1;
            for (SaleItemModel item : sale.getItems()) {
                Row row = sheet.createRow(rowIdx++);
                createCell(row, 0, npp++, dataStyle);
                createCell(row, 1, item.getProduct().getName(), dataStyle);
                createCell(row, 2, item.getProduct().getSku(), dataStyle);
                createCell(row, 3, item.getProduct().getUnit(), dataStyle);
                createCell(row, 4, item.getQuantity(), dataStyle);
                createCell(row, 5, item.getUnitPrice(), currencyStyle);
                createCell(row, 6, item.getTotalPrice(), currencyStyle);
            }
            autoSizeColumns(sheet, headers.length);
            return workbookToBytes(workbook);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка З-2", e);
        }
    }

    @Override
    public byte[] generateTTN(Integer saleId) {
        SaleModel sale = getSale(saleId);
        try (Workbook workbook = new XSSFWorkbook()) {
            initStyles(workbook);
            Sheet sheet = workbook.createSheet("ТТН");
            createTitle(sheet, 0, "ТОВАРНО-ТРАНСПОРТНАЯ НАКЛАДНАЯ № " + getDocNumber(sale), 7);

            String carInfo = ((sale.getCarMark() != null ? sale.getCarMark() : "") + " " +
                    (sale.getCarNumber() != null ? sale.getCarNumber() : "")).trim();
            createRowKeyValue(sheet, 2, "Автомобиль:", carInfo.isEmpty() ? "---" : carInfo);
            createRowKeyValue(sheet, 3, "Водитель:", sale.getDriverName() != null ? sale.getDriverName() : "---");

            if (sale.getProxyNumber() != null) {
                createRowKeyValue(sheet, 4, "Основание:", "Доверенность " + sale.getProxyNumber());
            }

            String[] headers = {"№", "Код", "Наименование", "Ед", "Мест", "Масса", "Кол-во", "Сумма"};
            createTableHeader(sheet, 6, headers);

            int rowIdx = 7;
            int npp = 1;
            for (SaleItemModel item : sale.getItems()) {
                Row row = sheet.createRow(rowIdx++);
                createCell(row, 0, npp++, dataStyle);
                createCell(row, 1, item.getProduct().getSku(), dataStyle);
                createCell(row, 2, item.getProduct().getName(), dataStyle);
                createCell(row, 3, item.getProduct().getUnit(), dataStyle);
                createCell(row, 4, item.getBoxCount(), dataStyle);

                double weight = (item.getProduct().getWeightBrutto() != null)
                        ? item.getProduct().getWeightBrutto().doubleValue() * item.getQuantity().doubleValue()
                        : 0.0;
                createCell(row, 5, weight, dataStyle);
                createCell(row, 6, item.getQuantity(), dataStyle);
                createCell(row, 7, item.getTotalPrice(), currencyStyle);
            }
            autoSizeColumns(sheet, headers.length);
            return workbookToBytes(workbook);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка ТТН", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public SalesSummaryResponse getSalesSummary(LocalDate start, LocalDate end, Integer categoryId, Integer warehouseId) {
        Integer finalWhId = (warehouseId != null) ? warehouseId : securityHelper.getCurrentWarehouseId();
        LocalDateTime startDT = start.atStartOfDay();
        LocalDateTime endDT = end.atTime(LocalTime.MAX);

        List<SaleModel> sales = (finalWhId == null)
                ? saleRepository.findAllBySaleDateBetween(startDT, endDT)
                : saleRepository.findAllByWarehouseIdAndSaleDateBetween(finalWhId, startDT, endDT);

        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal totalCostOfSales = BigDecimal.ZERO;
        List<SalesDataRow> details = new ArrayList<>();

        for (SaleModel sale : sales) {
            BigDecimal saleRev = BigDecimal.ZERO;
            BigDecimal saleCost = BigDecimal.ZERO;
            boolean hasMatch = false;

            for (SaleItemModel item : sale.getItems()) {
                if (categoryId == null || (item.getProduct().getCategory() != null && item.getProduct().getCategory().getId().equals(categoryId))) {
                    saleRev = saleRev.add(item.getTotalPrice());
                    saleCost = saleCost.add((item.getProduct().getCostPrice() != null ? item.getProduct().getCostPrice() : BigDecimal.ZERO).multiply(item.getQuantity()));
                    hasMatch = true;
                }
            }
            if (hasMatch) {
                totalRevenue = totalRevenue.add(saleRev);
                totalCostOfSales = totalCostOfSales.add(saleCost);
                details.add(new SalesDataRow(sale.getSaleDate().format(formatter), getDocNumber(sale), getClientName(sale), saleRev, saleCost, saleRev.subtract(saleCost)));
            }
        }

        BigDecimal totalExpenses = BigDecimal.ZERO;
        if (categoryId == null) {
            List<ExpenseModel> expenses = (finalWhId == null)
                    ? expenseRepository.findAllByExpenseDateBetween(startDT, endDT)
                    : expenseRepository.findAllByWarehouseIdAndExpenseDateBetween(finalWhId, startDT, endDT);
            totalExpenses = expenses.stream().map(ExpenseModel::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        BigDecimal totalWriteOffCost = BigDecimal.ZERO;
        List<WriteOffHistoryModel> writeOffs = (finalWhId == null)
                ? writeOffRepository.findAllByWriteOffDateBetween(startDT, endDT)
                : writeOffRepository.findAllByWarehouseIdAndWriteOffDateBetween(finalWhId, startDT, endDT);

        for (WriteOffHistoryModel wo : writeOffs) {
            if (categoryId == null || (wo.getProduct().getCategory() != null && wo.getProduct().getCategory().getId().equals(categoryId))) {
                BigDecimal cost = (wo.getProduct().getCostPrice() != null ? wo.getProduct().getCostPrice() : BigDecimal.ZERO).multiply(wo.getQuantity());
                totalWriteOffCost = totalWriteOffCost.add(cost);
            }
        }

        BigDecimal netProfit = totalRevenue.subtract(totalCostOfSales).subtract(totalExpenses).subtract(totalWriteOffCost);

        return new SalesSummaryResponse(
                totalRevenue,
                totalCostOfSales,
                netProfit,
                (long) details.size(),
                details,
                totalExpenses,
                totalWriteOffCost
        );
    }

    @Override
    public byte[] generateGeneralSalesReportExcel(LocalDate start, LocalDate end, Integer categoryId, Integer warehouseId) {
        SalesSummaryResponse summary = getSalesSummary(start, end, categoryId, warehouseId);
        try (Workbook workbook = new XSSFWorkbook()) {
            initStyles(workbook);
            Sheet sheet = workbook.createSheet("Отчет по продажам");

            createTitle(sheet, 0, "Общий отчет по продажам: " + start.format(formatter) + " - " + end.format(formatter), 5);

            createRowKeyValue(sheet, 2, "Итого Выручка:", summary.getTotalRevenue().toString() + " ₸");
            createRowKeyValue(sheet, 3, "Себестоимость продаж:", summary.getTotalCost().toString() + " ₸");
            createRowKeyValue(sheet, 4, "Операционные расходы:", summary.getTotalExpenses().toString() + " ₸");
            createRowKeyValue(sheet, 5, "Потери (Списания):", summary.getTotalWriteOffCost().toString() + " ₸");
            createRowKeyValue(sheet, 6, "ЧИСТАЯ ПРИБЫЛЬ:", summary.getTotalProfit().toString() + " ₸");

            String[] headers = {"Дата", "Документ", "Клиент", "Выручка", "Себестоимость", "Прибыль"};
            createTableHeader(sheet, 6, headers);

            int rowIdx = 7;
            for (SalesDataRow rowData : summary.getDetails()) {
                Row row = sheet.createRow(rowIdx++);
                createCell(row, 0, rowData.getDate(), dataStyle);
                createCell(row, 1, rowData.getDocumentNumber(), dataStyle);
                createCell(row, 2, rowData.getClientName(), dataStyle);
                createCell(row, 3, rowData.getRevenue(), currencyStyle);
                createCell(row, 4, rowData.getCost(), currencyStyle);
                createCell(row, 5, rowData.getProfit(), currencyStyle);
            }

            autoSizeColumns(sheet, headers.length);
            return workbookToBytes(workbook);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка экспорта продаж", e);
        }
    }

    @Override
    public StockSummaryResponse getStockSummary(Integer warehouseId) {
        Integer finalWhId = (warehouseId != null) ? warehouseId : securityHelper.getCurrentWarehouseId();

        List<ProductModel> products = (finalWhId == null)
                ? productRepository.findAll()
                : productRepository.findAllByWarehouseId(finalWhId);

        List<ProductModel> activeProducts = products.stream()
                .filter(p -> !p.isDeleted())
                .collect(Collectors.toList());

        BigDecimal totalItems = BigDecimal.ZERO;
        BigDecimal totalCost = BigDecimal.ZERO;
        BigDecimal totalSale = BigDecimal.ZERO;

        for (ProductModel p : products) {
            if (p.isDeleted()) continue;

            BigDecimal qty = p.getStockQuantity() != null ? p.getStockQuantity() : BigDecimal.ZERO;
            totalItems = totalItems.add(qty);
            totalCost = totalCost.add((p.getCostPrice() != null ? p.getCostPrice() : BigDecimal.ZERO).multiply(qty));
            totalSale = totalSale.add((p.getSalePrice() != null ? p.getSalePrice() : BigDecimal.ZERO).multiply(qty));
        }
        return new StockSummaryResponse(totalItems, totalCost, totalSale, totalSale.subtract(totalCost));
    }

    @Override
    public byte[] generateStockReportExcel(Integer warehouseId) {
        Integer finalWhId = (warehouseId != null) ? warehouseId : securityHelper.getCurrentWarehouseId();

        List<ProductModel> products = (finalWhId == null)
                ? productRepository.findAll()
                : productRepository.findAllByWarehouseId(finalWhId);
        products = products.stream().filter(p -> !p.isDeleted()).collect(Collectors.toList());
        try (Workbook workbook = new XSSFWorkbook()) {
            initStyles(workbook);
            Sheet sheet = workbook.createSheet("Остатки на складе");
            createTitle(sheet, 0, "Инвентаризационный отчет от " + LocalDateTime.now().format(formatter), 6);

            String[] headers = {"SKU", "Товар", "Остаток", "Ед", "Закуп (ед)", "Продажа (ед)", "Общий закуп"};
            createTableHeader(sheet, 2, headers);

            int rowIdx = 3;
            for (ProductModel p : products) {
                Row row = sheet.createRow(rowIdx++);
                BigDecimal qty = p.getStockQuantity() != null ? p.getStockQuantity() : BigDecimal.ZERO;
                createCell(row, 0, p.getSku(), dataStyle);
                createCell(row, 1, p.getName(), dataStyle);
                createCell(row, 2, qty, dataStyle);
                createCell(row, 3, p.getUnit(), dataStyle);
                createCell(row, 4, p.getCostPrice(), currencyStyle);
                createCell(row, 5, p.getSalePrice(), currencyStyle);
                createCell(row, 6, (p.getCostPrice() != null ? p.getCostPrice() : BigDecimal.ZERO).multiply(qty), currencyStyle);
            }

            autoSizeColumns(sheet, headers.length);
            return workbookToBytes(workbook);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка экспорта остатков", e);
        }
    }

    @Override
    public byte[] generateSupplyHistoryExcel(LocalDate start, LocalDate end, Integer warehouseId) {
        List<SupplyHistoryModel> supplies = supplyHistoryRepository.findAllBySupplyDateBetween(
                start.atStartOfDay(), end.atTime(LocalTime.MAX));

        if (warehouseId != null) {
            supplies = supplies.stream()
                    .filter(s -> s.getWarehouse().getId().equals(warehouseId))
                    .collect(Collectors.toList());
        }

        try (Workbook workbook = new XSSFWorkbook()) {
            initStyles(workbook);

            Sheet sheet = workbook.createSheet("История поставок");

            createTitle(sheet, 0, "История поставок: " + start.format(formatter) + " - " + end.format(formatter), 7);

            String[] headers = {"Дата", "Поставщик", "Товар", "Кол-во", "Цена закупа", "Сумма", "Принял", "Склад"};
            createTableHeader(sheet, 2, headers);

            int rowIdx = 3;
            for (SupplyHistoryModel s : supplies) {
                Row row = sheet.createRow(rowIdx++);
                createCell(row, 0, s.getSupplyDate().format(formatter), dataStyle);
                createCell(row, 1, s.getSupplier().getFullName(), dataStyle);
                createCell(row, 2, s.getProduct().getName(), dataStyle);
                createCell(row, 3, s.getQuantity(), dataStyle);
                createCell(row, 4, s.getCostPrice(), currencyStyle);
                createCell(row, 5, s.getCostPrice().multiply(s.getQuantity()), currencyStyle);
                createCell(row, 6, s.getEmployee().getFullName(), dataStyle);
                createCell(row, 7, s.getWarehouse().getName(), dataStyle);
            }

            autoSizeColumns(sheet, headers.length);
            return workbookToBytes(workbook);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка экспорта поставок: " + e.getMessage(), e);
        }
    }

    @Override
    public List<MovementDto> getMovementJournal(LocalDate start, LocalDate end, Integer warehouseId) {
        LocalDateTime s = start.atStartOfDay();
        LocalDateTime e = end.atTime(LocalTime.MAX);
        List<MovementDto> journal = new ArrayList<>();

        saleRepository.findAllBySaleDateBetween(s, e).forEach(sale -> {
            if (warehouseId == null || sale.getWarehouse().getId().equals(warehouseId)) {
                journal.add(new MovementDto(sale.getSaleDate(), "Продажа",  sale.getDocumentNumber(),
                        getClientName(sale), sale.getEmployee().getFullName(), sale.getTotalAmount(), sale.getWarehouse().getName()));
            }
        });

        supplyHistoryRepository.findAllBySupplyDateBetween(s, e).forEach(supply -> {
            if (warehouseId == null || supply.getWarehouse().getId().equals(warehouseId)) {
                journal.add(new MovementDto(supply.getSupplyDate(), "Приход",  supply.getDocumentNumber(),
                        supply.getSupplier().getFullName(), supply.getEmployee().getFullName(), supply.getCostPrice().multiply(supply.getQuantity()), supply.getWarehouse().getName()));
            }
        });

        writeOffRepository.findAllByWriteOffDateBetween(s, e).forEach(wo -> {
            if (warehouseId == null || wo.getWarehouse().getId().equals(warehouseId)) {
                BigDecimal cost = (wo.getProduct().getCostPrice() != null ? wo.getProduct().getCostPrice() : BigDecimal.ZERO)
                        .multiply(wo.getQuantity());

                journal.add(new MovementDto(
                        wo.getWriteOffDate(),
                        "Списание",
                        wo.getDocumentNumber(),
                        wo.getProduct().getName(),
                        wo.getEmployee().getFullName(),
                        cost.negate(),
                        wo.getWarehouse().getName()
                ));
            }
        });

        expenseRepository.findAllByExpenseDateBetween(s, e).forEach(exp -> {
            if (warehouseId == null || exp.getWarehouse().getId().equals(warehouseId)) {
                journal.add(new MovementDto(
                        exp.getExpenseDate(),
                        "Расход",
                        exp.getDocumentNumber(),
                        exp.getCategory() + ": " + exp.getDescription(),
                        exp.getEmployee().getFullName(),
                        exp.getAmount().negate(),
                        exp.getWarehouse().getName()
                ));
            }
        });

        return journal.stream()
                .sorted(Comparator.comparing(MovementDto::getDate).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public byte[] generateMovementJournalExcel(LocalDate start, LocalDate end, Integer warehouseId) {
        List<MovementDto> journal = getMovementJournal(start, end, warehouseId);

        BigDecimal totalSales = BigDecimal.ZERO;
        BigDecimal totalSupplies = BigDecimal.ZERO;
        BigDecimal totalWriteOffs = BigDecimal.ZERO;
        BigDecimal totalExpenses = BigDecimal.ZERO;

        for (MovementDto m : journal) {
            switch (m.getType()) {
                case "Продажа":
                    totalSales = totalSales.add(m.getAmount());
                    break;
                case "Приход":
                    totalSupplies = totalSupplies.add(m.getAmount());
                    break;
                case "Списание":
                    totalWriteOffs = totalWriteOffs.add(m.getAmount().abs());
                    break;
                case "Расход":
                    totalExpenses = totalExpenses.add(m.getAmount().abs());
                    break;
            }
        }
        BigDecimal netResult = totalSales.subtract(totalWriteOffs).subtract(totalExpenses);

        try (Workbook workbook = new XSSFWorkbook()) {
            initStyles(workbook);
            Sheet sheet = workbook.createSheet("Журнал движений");

            createTitle(sheet, 0, "Журнал движения документов: " + start + " - " + end, 6);
            String[] headers = {"Дата", "Тип", "№ Документа", "Контрагент/Товар", "Сотрудник", "Сумма", "Склад"};
            createTableHeader(sheet, 2, headers);

            int rowIdx = 3;
            for (MovementDto m : journal) {
                Row row = sheet.createRow(rowIdx++);
                String formattedDate = m.getDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));

                createCell(row, 0, formattedDate, dataStyle);
                createCell(row, 1, m.getType(), dataStyle);
                createCell(row, 2, m.getDocNumber(), dataStyle);
                createCell(row, 3, m.getCounterparty(), dataStyle);
                createCell(row, 4, m.getEmployee(), dataStyle);
                createCell(row, 5, m.getAmount(), currencyStyle);
                createCell(row, 6, m.getWarehouse(), dataStyle);
            }

            rowIdx++;
            createRowSummary(sheet, rowIdx++, "Итого продаж:", totalSales);
            createRowSummary(sheet, rowIdx++, "Итого приход:", totalSupplies);
            createRowSummary(sheet, rowIdx++, "Итого списано:", totalWriteOffs);
            createRowSummary(sheet, rowIdx++, "Расходы:", totalExpenses);

            Row resultRow = sheet.createRow(rowIdx);
            createCell(resultRow, 4, "Чистая прибыль:", headerStyle);
            createCell(resultRow, 5, netResult, currencyStyle);

            autoSizeColumns(sheet, headers.length);
            return workbookToBytes(workbook);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка генерации Excel", e);
        }
    }

    private void createRowSummary(Sheet sheet, int rowIdx, String label, BigDecimal value) {
        Row row = sheet.createRow(rowIdx);
        createCell(row, 4, label, dataStyle);
        createCell(row, 5, value, currencyStyle);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ArchiveDocumentDto> getDocumentArchive(LocalDate start, LocalDate end, Integer warehouseId, String clientName, String employeeName) {
        LocalDateTime startDT = start.atStartOfDay();
        LocalDateTime endDT = end.atTime(LocalTime.MAX);
        List<ArchiveDocumentDto> archive = new ArrayList<>();


        saleRepository.findAllBySaleDateBetween(startDT, endDT).stream()
                .filter(sale -> warehouseId == null || sale.getWarehouse().getId().equals(warehouseId))
                .filter(sale -> clientName == null || clientName.isEmpty() || getClientName(sale).contains(clientName))
                .filter(sale -> employeeName == null || employeeName.isEmpty() || (sale.getEmployee() != null && sale.getEmployee().getFullName().contains(employeeName)))
                .forEach(sale -> archive.add(new ArchiveDocumentDto(
                        sale.getId(), "Отпуск", sale.getDocumentNumber(), sale.getSaleDate(), getClientName(sale), sale.getTotalAmount()
                )));

        supplyHistoryRepository.findAllBySupplyDateBetween(startDT, endDT).stream()
                .filter(sup -> warehouseId == null || sup.getWarehouse().getId().equals(warehouseId))
                .filter(sup -> clientName == null || clientName.isEmpty() || (sup.getSupplier() != null && sup.getSupplier().getFullName().contains(clientName)))
                .forEach(sup -> archive.add(new ArchiveDocumentDto(
                        sup.getId(), "Приход", sup.getDocumentNumber(), sup.getSupplyDate(),
                        sup.getSupplier() != null ? sup.getSupplier().getFullName() : "Поставщик",
                        sup.getCostPrice().multiply(sup.getQuantity())
                )));

        writeOffRepository.findAllByWriteOffDateBetween(startDT, endDT).stream()
                .filter(wo -> warehouseId == null || wo.getWarehouse().getId().equals(warehouseId))
                .filter(wo -> employeeName == null || employeeName.isEmpty() || (wo.getEmployee() != null && wo.getEmployee().getFullName().contains(employeeName)))
                .forEach(wo -> archive.add(new ArchiveDocumentDto(
                        wo.getId(), "Списание", wo.getDocumentNumber(), wo.getWriteOffDate(), wo.getProduct().getName(),
                        wo.getProduct().getCostPrice().multiply(wo.getQuantity()).negate()
                )));

        expenseRepository.findAllByExpenseDateBetween(startDT, endDT).stream()
                .filter(exp -> warehouseId == null || exp.getWarehouse().getId().equals(warehouseId))
                .filter(exp -> employeeName == null || employeeName.isEmpty() || (exp.getEmployee() != null && exp.getEmployee().getFullName().contains(employeeName)))
                .forEach(exp -> archive.add(new ArchiveDocumentDto(
                        exp.getId(), "Расход", exp.getDocumentNumber(), exp.getExpenseDate(), exp.getCategory(), exp.getAmount().negate()
                )));

        return archive.stream()
                .sorted(Comparator.comparing(ArchiveDocumentDto::getDate).reversed())
                .collect(Collectors.toList());
    }

    private void initStyles(Workbook wb) {
        // Заголовок таблицы
        headerStyle = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        headerStyle.setFont(font);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        dataStyle = wb.createCellStyle();
        dataStyle.setBorderBottom(BorderStyle.THIN);
        dataStyle.setBorderTop(BorderStyle.THIN);
        dataStyle.setBorderRight(BorderStyle.THIN);
        dataStyle.setBorderLeft(BorderStyle.THIN);
        titleStyle = wb.createCellStyle();
        Font titleFont = wb.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 14);
        titleStyle.setFont(titleFont);
        currencyStyle = wb.createCellStyle();
        currencyStyle.cloneStyleFrom(dataStyle);
        currencyStyle.setDataFormat(wb.createDataFormat().getFormat("#,##0.00"));
    }
    private void createTitle(Sheet sheet, int rowIdx, String text, int lastCol) {
        Row row = sheet.createRow(rowIdx);
        Cell cell = row.createCell(0);
        cell.setCellValue(text);
        cell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowIdx, rowIdx, 0, lastCol));
    }

    private void createRowKeyValue(Sheet sheet, int rowIdx, String key, String value) {
        Row row = sheet.createRow(rowIdx);
        Cell k = row.createCell(0);
        k.setCellValue(key);
        CellStyle bold = sheet.getWorkbook().createCellStyle();
        Font f = sheet.getWorkbook().createFont();
        f.setBold(true);
        bold.setFont(f);
        k.setCellStyle(bold);

        row.createCell(1).setCellValue(value != null ? value : "");
    }

    private void createTableHeader(Sheet sheet, int rowIdx, String[] headers) {
        Row row = sheet.createRow(rowIdx);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
    }

    private void createCell(Row row, int colIdx, Object value, CellStyle style) {
        Cell cell = row.createCell(colIdx);
        cell.setCellStyle(style);
        if (value instanceof Number) cell.setCellValue(((Number) value).doubleValue());
        else cell.setCellValue(value != null ? value.toString() : "");
    }

    private void autoSizeColumns(Sheet sheet, int count) {
        for (int i = 0; i < count; i++) sheet.autoSizeColumn(i);
    }

    private SaleModel getSale(Integer id) {
        return saleRepository.findById(id).orElseThrow(() -> new RuntimeException("Продажа не найдена"));
    }

    private String getClientName(SaleModel sale) {
        return (sale.getClient() != null) ? sale.getClient().getFullName() : "Розничный покупатель";
    }

    private String getDocNumber(SaleModel sale) {
        return (sale.getDocumentNumber() != null) ? sale.getDocumentNumber() : String.valueOf(sale.getId());
    }

    private byte[] workbookToBytes(Workbook wb) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        wb.write(out);
        return out.toByteArray();
    }
}