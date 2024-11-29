package com.bank.microserviceCredit.business.service.impl;
import com.bank.microserviceCredit.Model.api.credit.AccountBalanceUpdateRequest;
import com.bank.microserviceCredit.Model.api.credit.CreditDto;
import com.bank.microserviceCredit.Model.api.credit.CreditRequest;
import com.bank.microserviceCredit.Model.entity.CreditEntity;
import com.bank.microserviceCredit.business.repository.ICreditRepository;
import com.bank.microserviceCredit.business.service.ICreditService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
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
                .dueDate(entity.getDueDate())
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
        return creditRepository.findById(id)  // Step 1: Find the existing credit by ID
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Crédito no encontrado con ID: " + id)))  // Step 2: Handle "not found" scenario
                .flatMap(existingCredit -> {  // Step 3: If credit found, proceed with updates
                    if (request.getBalance() < 0) {  // Step 4: Validate the input (balance can't be negative)
                        return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "El saldo no puede ser negativo."));  // Step 5: Handle validation error
                    }

                    // Step 6: Update fields of the credit object
                    existingCredit.setCreditLimit(request.getCreditLimit());
                    existingCredit.setBalance(request.getBalance());
                    existingCredit.setUpdatedAt(LocalDateTime.now());  // Mark the update time

                    // Step 7: Special handling for CREDIT_CARD type
                    if ("CREDIT_CARD".equals(existingCredit.getType())) {
                        return updateCreditCardBalance(existingCredit.getId(), request.getBalance())  // Step 8: If it's a credit card, update balance
                                .then(creditRepository.save(existingCredit).map(this::convertToDto));  // Save updated credit and return as DTO
                    }

                    // Step 9: Save and return updated credit
                    return creditRepository.save(existingCredit).map(this::convertToDto);
                })
                .onErrorResume(e -> {
                    // Step 10: Global error handler if any exception occurs
                    return Mono.error(e);
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


    @Override
    public Mono<Boolean> hasOverdueDebts(String customerId) {
        return creditRepository.findByCustomerId(customerId)
                .any(credit -> credit.getDueDate().isBefore(LocalDateTime.now()) && credit.getBalance() > 0)
                .doOnNext(hasDebts -> {
                    if (hasDebts) {
                        log.info("El cliente con ID {} tiene deudas vencidas.", customerId);
                    } else {
                        log.info("El cliente con ID {} no tiene deudas vencidas.", customerId);
                    }
                })
                .onErrorResume(error -> {
                    log.error("Error al verificar deudas vencidas para cliente ID {}: {}", customerId, error.getMessage());
                    return Mono.just(false);
                });
    }

    @Override
    public Mono<List<CreditDto>> generateReport(String startDate, String endDate) {
        LocalDateTime start = LocalDateTime.parse(startDate);
        LocalDateTime end = LocalDateTime.parse(endDate);

        return creditRepository.findAllByCreatedAtBetween(start, end)
                .map(this::convertToDto)
                .collectList();
    }

    @Override
    public Flux<CreditDto> findByCustomerId(String customerId) {
        return creditRepository.findByCustomerId(customerId)
                .map(this::convertToDto);
    }

}
