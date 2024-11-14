package com.bank.microserviceCredit.business.repository;

import com.bank.microserviceCredit.Model.entity.CreditEntity;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface ICreditRepository extends
        ReactiveMongoRepository<CreditEntity, String> {
    Mono<Boolean> existsByCustomerIdAndType(String customerId, String type);

}
