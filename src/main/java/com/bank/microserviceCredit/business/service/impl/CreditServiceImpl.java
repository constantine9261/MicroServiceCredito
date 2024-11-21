package com.bank.microserviceCredit.business.service.impl;
import com.bank.microserviceCredit.Model.api.credit.AccountBalanceUpdateRequest;
import com.bank.microserviceCredit.Model.api.credit.CreditDto;
import com.bank.microserviceCredit.Model.api.credit.CreditRequest;
import com.bank.microserviceCredit.Model.entity.CreditEntity;
import com.bank.microserviceCredit.business.repository.ICreditRepository;
import com.bank.microserviceCredit.business.service.ICreditService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class CreditServiceImpl implements ICreditService {

    private final ICreditRepository creditRepository;
    private final WebClient customerWebClient;
    private final WebClient accountWebClient;

    private CreditEntity convertToEntity(CreditRequest request) {
        return CreditEntity.builder()
                .customerId(request.getCustomerId())
                .type(request.getType())
                .creditLimit(request.getCreditLimit())
                .balance(request.getBalance())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .active(request.isActive()) // Mapea el atributo active desde el request
                .build();
    }

    private CreditDto convertToDto(CreditEntity entity) {
        return CreditDto.builder()
                .id(entity.getId())
                .customerId(entity.getCustomerId())
                .type(entity.getType())
                .creditLimit(entity.getCreditLimit())
                .balance(entity.getBalance())
                .active(entity.getActive() != null ? entity.getActive() : false) // Asigna false si es nulo
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private Mono<Boolean> verifyCustomerExists(String customerId) {
        return customerWebClient.get()
                .uri("/{id}", customerId)
                .retrieve()
                .bodyToMono(Object.class) // Usa un DTO apropiado si el CustomerService tiene una estructura de respuesta específica
                .map(response -> true)
                .onErrorResume(error -> Mono.just(false));
    }

    private Mono<Void> updateCreditCardBalance(String accountId, Double newBalance) {
        return accountWebClient.put()
                .uri("/accounts/{id}", accountId)
                .bodyValue(new AccountBalanceUpdateRequest(newBalance))
                .retrieve()
                .bodyToMono(Void.class);
    }

    @Override
    public Mono<CreditDto> createCredit(CreditRequest request) {
        return verifyCustomerExists(request.getCustomerId())
                .flatMap(customerExists -> {
                    if (!customerExists) {
                        return Mono.error(new IllegalArgumentException("Cliente no válido"));
                    }
                    if ("PERSONAL".equals(request.getType())) {
                        return creditRepository.existsByCustomerIdAndType(request.getCustomerId(), "PERSONAL")
                                .flatMap(exists -> {
                                    if (exists) {
                                        return Mono.error(new IllegalArgumentException("Cliente ya tiene un crédito personal"));
                                    }
                                    CreditEntity creditEntity = convertToEntity(request);
                                    return creditRepository.save(creditEntity).map(this::convertToDto);
                                });
                    } else {
                        CreditEntity creditEntity = convertToEntity(request);
                        return creditRepository.save(creditEntity).map(this::convertToDto);
                    }
                });
    }

    @Override
    public Mono<CreditDto> findById(String id) {
        return creditRepository.findById(id).map(this::convertToDto);
    }

    public Flux<CreditDto> findAll() {
        return creditRepository.findAll()
                .map(this::convertToDto)
                .doOnError(error -> log.error("Error al obtener créditos: {}", error.getMessage()))
                .onErrorResume(error -> Flux.empty()); // Devuelve un flujo vacío si ocurre un error
    }

    @Override
    public Mono<CreditDto> updateCredit(String id, CreditRequest request) {
        return creditRepository.findById(id)
                .flatMap(existingCredit -> {
                    existingCredit.setCreditLimit(request.getCreditLimit());
                    existingCredit.setBalance(request.getBalance());
                    existingCredit.setUpdatedAt(LocalDateTime.now());
                    if ("CREDIT_CARD".equals(existingCredit.getType())) {
                        return updateCreditCardBalance(existingCredit.getId(), request.getBalance())
                                .then(creditRepository.save(existingCredit).map(this::convertToDto));
                    }
                    return creditRepository.save(existingCredit).map(this::convertToDto);
                });
    }

    @Override
    public Mono<Void> deleteById(String id) {
        return creditRepository.findById(id).flatMap(creditRepository::delete);
    }

    @Override
    public Mono<Boolean> hasActiveCreditCard(String customerId) {
        return creditRepository.findByCustomerId(customerId)
                .any(credit -> "CREDIT_CARD".equalsIgnoreCase(credit.getType()) && Boolean.TRUE.equals(credit.getActive()));
    }
}
