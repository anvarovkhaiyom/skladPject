package com.example.skladservicedevelop.service;

import com.example.skladservicedevelop.dto.request.ClientRequest;
import com.example.skladservicedevelop.dto.response.ClientResponse;

import java.util.List;

public interface ClientService {
    ClientResponse create(ClientRequest request);
    ClientResponse update(Integer id, ClientRequest request);
    void delete(Integer id);
    ClientResponse getById(Integer id);
    List<ClientResponse> getAll(Integer warehouseId);
}
