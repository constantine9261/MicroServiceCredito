package com.bank.microserviceCredit.business.repository;

import com.bank.microserviceCredit.Model.entity.CreditEntity;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface ICreditRepository extends
        ReactiveMongoRepository<CreditEntity, Long> {

}
