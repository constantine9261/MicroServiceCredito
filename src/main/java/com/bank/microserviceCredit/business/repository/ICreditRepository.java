package com.bank.microserviceCredit.business.repository;

import com.bank.microserviceCredit.Model.entity.CreditEntity;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

public interface ICreditRepository extends
        ReactiveMongoRepository<CreditEntity, String> {
    Mono<Boolean> existsByCustomerIdAndType(String customerId, String type);
    Flux<CreditEntity> findByCustomerId(String customerId);

    // Encuentra todos los créditos entre fechas específicas
    Flux<CreditEntity> findAllByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

}
