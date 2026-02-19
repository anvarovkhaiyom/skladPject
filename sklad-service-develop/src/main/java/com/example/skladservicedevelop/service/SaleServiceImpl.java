package com.example.skladservicedevelop.service;

import com.example.skladservicedevelop.config.SecurityHelper;
import com.example.skladservicedevelop.database.model.*;
import com.example.skladservicedevelop.database.repository.ClientRepository;
import com.example.skladservicedevelop.database.repository.CurrencyRepository;
import com.example.skladservicedevelop.database.repository.ProductRepository;
import com.example.skladservicedevelop.database.repository.SaleRepository;
import com.example.skladservicedevelop.dto.request.PaymentRequest;
import com.example.skladservicedevelop.dto.request.SaleItemRequest;
import com.example.skladservicedevelop.dto.request.SaleRequest;
import com.example.skladservicedevelop.dto.response.PaymentResponse;
import com.example.skladservicedevelop.dto.response.SaleItemResponse;
import com.example.skladservicedevelop.dto.response.SaleResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SaleServiceImpl implements SaleService {

    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;
    private final ClientRepository clientRepository;

    private final CurrencyRepository currencyRepository;
    private final SecurityHelper securityHelper;

    private SaleResponse toResponse(SaleModel sale) {
        SaleResponse resp = new SaleResponse();
        resp.setId(sale.getId());
        resp.setSaleDate(sale.getSaleDate());
        resp.setStatus(sale.getStatus());
        resp.setTotalAmount(sale.getTotalAmount());
        resp.setChangeAmount(sale.getChangeAmount());
        resp.setDocumentNumber(sale.getDocumentNumber());
        if (sale.getClient() != null) {
            resp.setClientId(sale.getClient().getId());
            resp.setClientName(sale.getClient().getFullName());
        }
        resp.setClientId(sale.getClient() != null ? sale.getClient().getId() : null);
        resp.setEmployeeId(sale.getEmployee() != null ? sale.getEmployee().getId() : null);

        List<SaleItemResponse> itemResponses = new ArrayList<>();
        if (sale.getItems() != null) {
            for (SaleItemModel si : sale.getItems()) {
                SaleItemResponse ir = new SaleItemResponse();
                ir.setProductId(si.getProduct().getId());
                ir.setProductName(si.getProduct().getName());
                ir.setQuantity(si.getQuantity());
                ir.setUnitPrice(si.getUnitPrice());
                ir.setTotalPrice(si.getTotalPrice());
                ir.setBoxCount(si.getBoxCount());
                itemResponses.add(ir);
            }
        }
        resp.setItems(itemResponses);

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
        resp.setPayments(paymentResponses);

        return resp;
    }

    @Override
    @Transactional
    public SaleResponse createSale(SaleRequest request) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new RuntimeException("Нет товаров в продаже");
        }

        // Достаем текущего сотрудника и его склад через наш новый Helper
        EmployeeModel currentEmployee = securityHelper.getCurrentEmployee();
        WarehouseModel warehouse = currentEmployee.getWarehouse();

        if (warehouse == null && !"SUPER_ADMIN".equals(currentEmployee.getRole())) {
            throw new RuntimeException("Ошибка: Сотрудник не привязан к складу!");
        }

        SaleModel sale = new SaleModel();
        sale.setSaleDate(LocalDateTime.now());
        sale.setStatus("Оплачено");
        sale.setEmployee(currentEmployee);
        sale.setWarehouse(warehouse); // ПРИВЯЗКА К СКЛАДУ

        // Нумерация документов ВНУТРИ конкретного склада
        Integer lastId = saleRepository.findMaxIdByWarehouseId(warehouse != null ? warehouse.getId() : null);
        int nextId = (lastId == null) ? 1 : lastId + 1;
        sale.setDocumentNumber(String.format("%07d", nextId));

        // Данные о машине и водителе
        sale.setCarMark(request.getCarMark());
        sale.setCarNumber(request.getCarNumber());
        sale.setDriverName(request.getDriverName());
        sale.setProxyNumber(request.getProxyNumber());
        sale.setProxyDate(request.getProxyDate());

        if (request.getClientId() != null) {
            // Проверяем клиента (чтобы не продать клиенту чужого склада)
            sale.setClient(clientRepository.findById(request.getClientId())
                    .orElseThrow(() -> new RuntimeException("Клиент не найден")));
        }

        BigDecimal totalAmountBase = BigDecimal.ZERO;
        List<SaleItemModel> items = new ArrayList<>();

        for (SaleItemRequest itemReq : request.getItems()) {
            // ВАЖНО: Ищем товар только на СВОЕМ складе
            ProductModel product = productRepository.findByBarcodeAndWarehouseId(
                    itemReq.getBarcode(),
                    warehouse != null ? warehouse.getId() : null
            ).orElseThrow(() -> new RuntimeException("Товар с баркодом " + itemReq.getBarcode() + " не найден на вашем складе!"));

            BigDecimal qty = itemReq.getQuantity();
            BigDecimal itemTotal = product.getSalePrice().multiply(qty);

            SaleItemModel si = new SaleItemModel();
            si.setSale(sale);
            si.setProduct(product);
            si.setQuantity(qty);
            si.setUnitPrice(product.getSalePrice());
            si.setTotalPrice(itemTotal);

            BigDecimal inBox = product.getItemsInBox() != null ? product.getItemsInBox() : BigDecimal.ONE;
            si.setBoxCount(qty.divide(inBox, 2, java.math.RoundingMode.HALF_UP));
            si.setItemsPerBoxAtSale(inBox);

            items.add(si);
            totalAmountBase = totalAmountBase.add(itemTotal);

            // Списание остатков
            product.setStockQuantity(product.getStockQuantity().subtract(qty));
            productRepository.save(product);
        }

        sale.setItems(items);
        sale.setTotalAmount(totalAmountBase);

        // Логика платежей
        BigDecimal totalPaidInBase = BigDecimal.ZERO;
        List<SalePaymentModel> payments = new ArrayList<>();

        if (request.getPayments() != null) {
            for (PaymentRequest pReq : request.getPayments()) {
                CurrencyModel currency = currencyRepository.findByCode(pReq.getCurrency())
                        .orElseThrow(() -> new RuntimeException("Валюта не найдена: " + pReq.getCurrency()));

                BigDecimal rate = (pReq.getRate() != null) ? pReq.getRate() : BigDecimal.ONE;
                totalPaidInBase = totalPaidInBase.add(pReq.getAmount().multiply(rate));

                SalePaymentModel payment = new SalePaymentModel();
                payment.setSale(sale);
                payment.setMethod(pReq.getMethod());
                payment.setAmount(pReq.getAmount());
                payment.setCurrency(currency);
                payment.setExchangeRate(rate);
                payments.add(payment);
            }
        }

        sale.setPayments(payments);
        sale.setChangeAmount(totalPaidInBase.subtract(totalAmountBase));

        if (totalPaidInBase.compareTo(totalAmountBase) < 0) {
            throw new RuntimeException("Недостаточно средств. Итого: " + totalAmountBase + ", получено: " + totalPaidInBase);
        }

        return toResponse(saleRepository.save(sale));
    }

    @Override
    @Transactional
    public SaleResponse getById(Integer id) {
        SaleModel sale = saleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sale not found"));

        SaleResponse resp = new SaleResponse();
        resp.setId(sale.getId());
        resp.setSaleDate(sale.getSaleDate());
        resp.setStatus(sale.getStatus());
        resp.setTotalAmount(sale.getTotalAmount());
        resp.setChangeAmount(sale.getChangeAmount());
        resp.setClientId(sale.getClient() != null ? sale.getClient().getId() : null);
        resp.setEmployeeId(sale.getEmployee() != null ? sale.getEmployee().getId() : null);
        if (sale.getClient() != null) {
            resp.setClientId(sale.getClient().getId());
            resp.setClientName(sale.getClient().getFullName());
        } else {
            resp.setClientName("Розничный покупатель");
        }
        List<SaleItemResponse> items = new ArrayList<>();
        if (sale.getItems() != null) {
            for (SaleItemModel si : sale.getItems()) {
                SaleItemResponse ir = new SaleItemResponse();
                ir.setProductId(si.getProduct().getId());
                ir.setProductName(si.getProduct().getName());
                ir.setQuantity(si.getQuantity());
                ir.setUnitPrice(si.getUnitPrice());
                ir.setTotalPrice(si.getTotalPrice());
                items.add(ir);
            }
        }
        resp.setItems(items);
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
        resp.setPayments(paymentResponses);

        return resp;
    }

    @Override
    @Transactional
    public List<SaleResponse> getAll() {
        return saleRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<SaleResponse> findSalesByFilters(LocalDateTime start, LocalDateTime end, String client, String employee, Integer warehouseId) {

        Integer finalWhId = warehouseId;

        if (finalWhId == null) {
            EmployeeModel currentUser = securityHelper.getCurrentEmployee();
            if (!"ROLE_SUPER_ADMIN".equals(currentUser.getRole()) && !"SUPER_ADMIN".equals(currentUser.getRole())) {
                if (currentUser.getWarehouse() != null) {
                    finalWhId = currentUser.getWarehouse().getId();
                }
            }
        }

        List<SaleModel> sales = saleRepository.findSalesWithFilters(
                start, end,
                finalWhId,
                client == null ? "" : client,
                employee == null ? "" : employee
        );

        return sales.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }}
