package com.example.skladservicedevelop.service;

import com.example.skladservicedevelop.config.SecurityHelper;
import com.example.skladservicedevelop.database.model.ClientModel;
import com.example.skladservicedevelop.database.model.EmployeeModel;
import com.example.skladservicedevelop.database.repository.ClientRepository;
import com.example.skladservicedevelop.dto.request.ClientRequest;
import com.example.skladservicedevelop.dto.response.ClientResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;
    private final SecurityHelper securityHelper;


    @Override
    public ClientResponse create(ClientRequest request) {
        EmployeeModel current = securityHelper.getCurrentEmployee();
        ClientModel client = new ClientModel();
        client.setFullName(request.getFullName());
        client.setContacts(request.getContacts());
        client.setWarehouse(current.getWarehouse());
        clientRepository.save(client);
        return toResponse(client);
    }

    @Override
    public ClientResponse update(Integer id, ClientRequest request) {
        ClientModel client = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client not found"));
        client.setFullName(request.getFullName());
        client.setContacts(request.getContacts());
        clientRepository.save(client);
        return toResponse(client);
    }

    @Override
    public void delete(Integer id) {
        clientRepository.deleteById(id);
    }

    @Override
    public ClientResponse getById(Integer id) {
        ClientModel client = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client not found"));
        return toResponse(client);
    }

    @Override
    public List<ClientResponse> getAll(Integer warehouseId) {
        EmployeeModel current = securityHelper.getCurrentEmployee();
        String role = current.getRole();

        List<ClientModel> clients;

        if (role.contains("SUPER_ADMIN")) {
            if (warehouseId != null) {
                clients = clientRepository.findAllByWarehouseId(warehouseId);
            } else {
                clients = clientRepository.findAll();
            }
        } else {
            clients = clientRepository.findAllByWarehouseId(current.getWarehouse().getId());
        }

        return clients.stream().map(this::toResponse).collect(Collectors.toList());
    }

    private ClientResponse toResponse(ClientModel client) {
        ClientResponse response = new ClientResponse();
        response.setId(client.getId());
        response.setFullName(client.getFullName());
        response.setContacts(client.getContacts());
        if (client.getWarehouse() != null) {
            response.setWarehouseId(client.getWarehouse().getId());
            response.setWarehouseName(client.getWarehouse().getName());
        }
        return response;
    }
}
