package com.example.skladservicedevelop.service;

import com.example.skladservicedevelop.config.SecurityHelper;
import com.example.skladservicedevelop.database.model.ProductModel;
import com.example.skladservicedevelop.database.model.SaleItemModel;
import com.example.skladservicedevelop.database.model.SaleModel;
import com.example.skladservicedevelop.database.repository.ProductRepository;
import com.example.skladservicedevelop.database.repository.SaleRepository;
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
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;
    private final SecurityHelper securityHelper;

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
            createTableHeader(sheet, 6, headers); // Сдвинул на 6 строку, чтобы влезла доверенность

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
        // 1. Сначала проверяем, пришел ли ID из фильтра (от Супер-Админа)
        // 2. Если нет, пытаемся взять ID текущего пользователя (через SecurityHelper)
        Integer finalWhId = (warehouseId != null) ? warehouseId : securityHelper.getCurrentWarehouseId();

        LocalDateTime startDT = start.atStartOfDay();
        LocalDateTime endDT = end.atTime(LocalTime.MAX);

        List<SaleModel> sales;
        // Если finalWhId всё еще null, значит это Супер-Админ смотрит ГЛОБАЛЬНЫЙ отчет
        if (finalWhId == null) {
            sales = saleRepository.findAllBySaleDateBetween(startDT, endDT);
        } else {
            sales = saleRepository.findAllByWarehouseIdAndSaleDateBetween(finalWhId, startDT, endDT);
        }

        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal totalCost = BigDecimal.ZERO;
        List<SalesDataRow> details = new ArrayList<>();

        for (SaleModel sale : sales) {
            BigDecimal saleRevenueFromCategory = BigDecimal.ZERO;
            BigDecimal saleCostFromCategory = BigDecimal.ZERO;
            boolean hasTargetProducts = false;

            for (SaleItemModel item : sale.getItems()) {
                // Фильтрация по категории, если она указана
                if (categoryId == null || (item.getProduct().getCategory() != null && item.getProduct().getCategory().getId().equals(categoryId))) {
                    BigDecimal itemRevenue = item.getTotalPrice();
                    BigDecimal itemCost = (item.getProduct().getCostPrice() != null ? item.getProduct().getCostPrice() : BigDecimal.ZERO)
                            .multiply(item.getQuantity());

                    saleRevenueFromCategory = saleRevenueFromCategory.add(itemRevenue);
                    saleCostFromCategory = saleCostFromCategory.add(itemCost);
                    hasTargetProducts = true;
                }
            }

            if (hasTargetProducts) {
                totalRevenue = totalRevenue.add(saleRevenueFromCategory);
                totalCost = totalCost.add(saleCostFromCategory);
                details.add(new SalesDataRow(
                        sale.getSaleDate().format(formatter),
                        getDocNumber(sale),
                        getClientName(sale),
                        saleRevenueFromCategory,
                        saleCostFromCategory,
                        saleRevenueFromCategory.subtract(saleCostFromCategory)
                ));
            }
        }

        return new SalesSummaryResponse(totalRevenue, totalCost, totalRevenue.subtract(totalCost), (long) details.size(), details);
    }

    @Override
    public byte[] generateGeneralSalesReportExcel(LocalDate start, LocalDate end, Integer categoryId, Integer warehouseId) {
        // Теперь передаем warehouseId, чтобы summary строился по выбранному складу
        SalesSummaryResponse summary = getSalesSummary(start, end, categoryId, warehouseId);
        try (Workbook workbook = new XSSFWorkbook()) {
            initStyles(workbook);
            Sheet sheet = workbook.createSheet("Отчет по продажам");

            createTitle(sheet, 0, "Общий отчет по продажам: " + start.format(formatter) + " - " + end.format(formatter), 5);

            createRowKeyValue(sheet, 2, "Итого Выручка:", summary.getTotalRevenue().toString() + " ₸");
            createRowKeyValue(sheet, 3, "Итого Себестоимость:", summary.getTotalCost().toString() + " ₸");
            createRowKeyValue(sheet, 4, "Чистая Прибыль:", summary.getTotalProfit().toString() + " ₸");

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
        // Логика: если warehouseId пришел из фильтра — берем его,
        // если нет — берем привязанный склад текущего пользователя.
        Integer finalWhId = (warehouseId != null) ? warehouseId : securityHelper.getCurrentWarehouseId();

        List<ProductModel> products = (finalWhId == null)
                ? productRepository.findAll()
                : productRepository.findAllByWarehouseId(finalWhId);

        BigDecimal totalItems = BigDecimal.ZERO;
        BigDecimal totalCost = BigDecimal.ZERO;
        BigDecimal totalSale = BigDecimal.ZERO;

        for (ProductModel p : products) {
            BigDecimal qty = p.getStockQuantity() != null ? p.getStockQuantity() : BigDecimal.ZERO;
            totalItems = totalItems.add(qty);
            totalCost = totalCost.add((p.getCostPrice() != null ? p.getCostPrice() : BigDecimal.ZERO).multiply(qty));
            totalSale = totalSale.add((p.getSalePrice() != null ? p.getSalePrice() : BigDecimal.ZERO).multiply(qty));
        }

        return new StockSummaryResponse(totalItems, totalCost, totalSale, totalSale.subtract(totalCost));
    }
    @Override
    public byte[] generateStockReportExcel(Integer warehouseId) {
        // Определяем склад (приоритет фильтру, затем безопасности)
        Integer finalWhId = (warehouseId != null) ? warehouseId : securityHelper.getCurrentWarehouseId();

        // Получаем товары только выбранного склада (или все, если ID null)
        List<ProductModel> products = (finalWhId == null)
                ? productRepository.findAll()
                : productRepository.findAllByWarehouseId(finalWhId);

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

    // --- ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ---

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
        // Сделаем ключи жирными
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