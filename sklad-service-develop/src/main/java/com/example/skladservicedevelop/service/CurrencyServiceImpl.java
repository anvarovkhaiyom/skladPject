package com.example.skladservicedevelop.service;

import com.example.skladservicedevelop.database.model.CurrencyModel;
import com.example.skladservicedevelop.database.repository.CurrencyRepository;
import com.example.skladservicedevelop.dto.request.CurrencyRequest;
import com.example.skladservicedevelop.dto.response.CurrencyResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CurrencyServiceImpl implements CurrencyService {

    private final CurrencyRepository currencyRepository;

    @Override
    public CurrencyResponse createCurrency(CurrencyRequest request) {
        CurrencyModel model = new CurrencyModel();
        model.setCode(request.getCode());
        model.setName(request.getName());
        model.setRate(request.getRate() != null ? request.getRate() : 1.0);
        model.setCreatedAt(LocalDateTime.now());
        return toResponse(currencyRepository.save(model));
    }
    @Transactional
    public CurrencyResponse updateCurrency(Integer id, CurrencyRequest request) {
        CurrencyModel model = currencyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Валюта не найдена"));
        model.setName(request.getName());
        model.setRate(request.getRate()); // Обновление курса
        return toResponse(currencyRepository.save(model));
    }
    @Override
    public List<CurrencyResponse> getAllCurrencies() {
        return currencyRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteCurrency(Integer id) {
        currencyRepository.deleteById(id);
    }

    private CurrencyResponse toResponse(CurrencyModel model) {
        CurrencyResponse response = new CurrencyResponse();
        response.setId(model.getId());
        response.setCode(model.getCode());
        response.setName(model.getName());
        response.setRate(model.getRate());
        response.setCreatedAt(model.getCreatedAt());
        return response;
    }
}
