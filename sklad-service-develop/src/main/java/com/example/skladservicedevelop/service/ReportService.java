package com.example.skladservicedevelop.service;

import com.example.skladservicedevelop.dto.ArchiveDocumentDto;
import com.example.skladservicedevelop.dto.MovementDto;
import com.example.skladservicedevelop.dto.response.SalesSummaryResponse;
import com.example.skladservicedevelop.dto.response.StockSummaryResponse;

import java.time.LocalDate;
import java.util.List;

public interface ReportService {
    byte[] generatePickingListExcel(Integer saleId);

    byte[] generateInvoiceWithBoxesExcel(Integer saleId);

    byte[] generateZ2Report(Integer saleId);

    byte[] generateTTN(Integer saleId);

    SalesSummaryResponse getSalesSummary(LocalDate start, LocalDate end, Integer categoryId, Integer warehouseId);

    byte[] generateGeneralSalesReportExcel(LocalDate start, LocalDate end, Integer categoryId, Integer warehouseId);

    StockSummaryResponse getStockSummary(Integer warehouseId);

    byte[] generateStockReportExcel(Integer warehouseId);

    byte[] generateSupplyHistoryExcel(LocalDate start, LocalDate end, Integer warehouseId);
    List<MovementDto> getMovementJournal(LocalDate start, LocalDate end, Integer warehouseId);

    byte[] generateMovementJournalExcel(LocalDate start, LocalDate end, Integer warehouseId);
    List<ArchiveDocumentDto> getDocumentArchive(LocalDate start, LocalDate end, Integer warehouseId, String clientName, String employeeName);
}