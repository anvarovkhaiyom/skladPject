package com.example.skladservicedevelop.service;

import com.example.skladservicedevelop.dto.request.CurrencyRequest;
import com.example.skladservicedevelop.dto.response.CurrencyResponse;

import java.util.List;

public interface CurrencyService {
    CurrencyResponse createCurrency(CurrencyRequest request);
    List<CurrencyResponse> getAllCurrencies();
    void deleteCurrency(Integer id);
    CurrencyResponse updateCurrency(Integer id, CurrencyRequest request);
}
