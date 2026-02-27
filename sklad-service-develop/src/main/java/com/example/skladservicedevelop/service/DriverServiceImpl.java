package com.example.skladservicedevelop.service;

import com.example.skladservicedevelop.database.model.DriverModel;
import com.example.skladservicedevelop.database.repository.DriverRepository;
import com.example.skladservicedevelop.database.repository.WarehouseRepository;
import com.example.skladservicedevelop.dto.request.DriverRequest;
import com.example.skladservicedevelop.dto.response.DriverResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DriverServiceImpl implements DriverService {

    private final DriverRepository driverRepository;
    private final WarehouseRepository warehouseRepository;

    @Override
    public List<DriverResponse> findAllNotDeleted(Integer warehouseId) {
        List<DriverModel> drivers;
        if (warehouseId != null) {
            drivers = driverRepository.findAllByDeletedFalseAndWarehouseId(warehouseId);
        } else {
            drivers = driverRepository.findAllByDeletedFalse();
        }

        return drivers.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void save(DriverRequest request) {
        validateNewDriver(request);

        DriverModel model = new DriverModel();
        updateModelFromRequest(model, request);
        driverRepository.save(model);
    }

    @Override
    @Transactional
    public void update(Long id, DriverRequest request) {
        DriverModel model = driverRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Водитель не найден"));

        validateUpdateDriver(id, request);

        updateModelFromRequest(model, request);
        driverRepository.save(model);
    }

    private void validateNewDriver(DriverRequest request) {
        if (request.getFullName() == null || request.getFullName().isEmpty())
            throw new RuntimeException("ФИО не может быть пустым");
        if (request.getPhone() == null || request.getPhone().isEmpty())
            throw new RuntimeException("Телефон не может быть пустым");
        if (request.getCarNumber() == null || request.getCarNumber().isEmpty())
            throw new RuntimeException("Гос. номер не может быть пустым");
        if (driverRepository.existsByPhone(request.getPhone())) {
            throw new RuntimeException("Водитель с таким телефоном уже существует");
        }
        if (driverRepository.existsByCarNumber(request.getCarNumber())) {
            throw new RuntimeException("Машина с таким гос. номером уже зарегистрирована");
        }
    }

    private void validateUpdateDriver(Long id, DriverRequest request) {
        if (driverRepository.existsByPhoneAndIdNot(request.getPhone(), id)) {
            throw new RuntimeException("Этот телефон уже используется другим водителем");
        }
        if (driverRepository.existsByCarNumberAndIdNot(request.getCarNumber(), id)) {
            throw new RuntimeException("Этот гос. номер уже закреплен за другим водителем");
        }
    }

    @Override
    public void delete(Long id) {
        DriverModel model = driverRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Водитель с id " + id + " не найден"));
        model.setDeleted(true);
        driverRepository.save(model);
    }
    private DriverResponse mapToResponse(DriverModel model) {
        return DriverResponse.builder()
                .id(model.getId())
                .fullName(model.getFullName())
                .phone(model.getPhone())
                .carMark(model.getCarMark())
                .carNumber(model.getCarNumber())
                .additionalInfo(model.getAdditionalInfo())
                .salary(model.getSalary())
                .warehouseId(model.getWarehouse() != null ? model.getWarehouse().getId() : null)
                .warehouseName(model.getWarehouse() != null ? model.getWarehouse().getName() : "Глобальный")
                .build();
    }

    private void updateModelFromRequest(DriverModel model, DriverRequest request) {
        model.setFullName(request.getFullName());
        model.setPhone(request.getPhone());
        model.setCarMark(request.getCarMark());
        model.setCarNumber(request.getCarNumber());
        model.setAdditionalInfo(request.getAdditionalInfo());
        model.setSalary(request.getSalary());
        if (request.getWarehouseId() != null) {
            model.setWarehouse(warehouseRepository.findById(request.getWarehouseId()).orElse(null));
        }
    }
}