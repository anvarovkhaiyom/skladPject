package com.example.skladservicedevelop.service;

import com.example.skladservicedevelop.database.model.*;
import com.example.skladservicedevelop.database.repository.SaleRepository;
import com.example.skladservicedevelop.dto.response.PaymentResponse;
import com.example.skladservicedevelop.dto.response.SalesReportResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class SalesReportServiceImpl implements SalesReportService {

    private final SaleRepository saleRepository;

    public SalesReportServiceImpl(SaleRepository saleRepository) {
        this.saleRepository = saleRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SalesReportResponse> getSalesReport(LocalDateTime start, LocalDateTime end) {
        List<SaleModel> sales = saleRepository.findAllBySaleDateBetween(start, end);

        if (sales.isEmpty()) {
            return Collections.emptyList();
        }

        sales.sort(Comparator.comparing(SaleModel::getSaleDate));

        List<SalesReportResponse> report = new ArrayList<>();

        for (SaleModel sale : sales) {
            SalesReportResponse r = new SalesReportResponse();
            r.setSaleId(sale.getId());
            r.setSaleDate(sale.getSaleDate());
            r.setEmployeeName(
                    sale.getEmployee() != null ? sale.getEmployee().getFullName() : "Без сотрудника"
            );
            r.setClientName(
                    sale.getClient() != null ? sale.getClient().getFullName() : "Без клиента"
            );
            r.setTotalAmount(sale.getTotalAmount());

            List<SalesReportResponse.SalesReportItem> items = new ArrayList<>();
            BigDecimal totalProfit = BigDecimal.ZERO;

            if (sale.getItems() != null) {
                for (SaleItemModel si : sale.getItems()) {
                    SalesReportResponse.SalesReportItem item = new SalesReportResponse.SalesReportItem();
                    item.setProductName(si.getProduct().getName());
                    item.setCategoryName(
                            si.getProduct().getCategory() != null ? si.getProduct().getCategory().getName() : "-"
                    );
                    item.setQuantity(si.getQuantity());
                    item.setUnitPrice(si.getUnitPrice());
                    item.setTotalPrice(si.getTotalPrice());

                    BigDecimal cost = Optional.ofNullable(si.getProduct().getCostPrice()).orElse(BigDecimal.ZERO);
                    BigDecimal unitPrice = Optional.ofNullable(si.getUnitPrice()).orElse(BigDecimal.ZERO);
                    BigDecimal quantity = Optional.ofNullable(si.getQuantity()).orElse(BigDecimal.ZERO);

                    BigDecimal profit = unitPrice.subtract(cost).multiply(quantity);
                    item.setProfit(profit);
                    totalProfit = totalProfit.add(profit);

                    items.add(item);
                }
            }
            r.setProfit(totalProfit);
            r.setItems(items);
            List<PaymentResponse> paymentResponses = new ArrayList<>();
            if (sale.getPayments() != null) {
                for (SalePaymentModel pay : sale.getPayments()) {
                    PaymentResponse pr = new PaymentResponse();
                    pr.setMethod(pay.getMethod());
                    pr.setAmount(pay.getAmount());
                    pr.setCurrencyCode(pay.getCurrency() != null ? pay.getCurrency().getCode() : null);
                    pr.setExchangeRate(pay.getExchangeRate());
                    paymentResponses.add(pr);
                }
            }
            r.setPayments(paymentResponses);

            report.add(r);
        }

        return report;
    }

}
