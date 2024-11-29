package com.bank.microserviceCredit.business.service;


import com.bank.microserviceCredit.Model.api.credit.CreditDto;
import com.bank.microserviceCredit.Model.api.credit.CreditRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ICreditService {

    Mono<CreditDto> createCredit(CreditRequest request);

    Mono<CreditDto> findById(String id);

    Flux<CreditDto> findAll();

    Mono<CreditDto> updateCredit(String id, CreditRequest request);

    Mono<Void> deleteById(String id);

    Mono<Boolean> hasActiveCreditCard(String customerId);

    Mono<Boolean> hasOverdueDebts(String customerId);

    Mono<List<CreditDto>> generateReport(String startDate, String endDate);

    Flux<CreditDto> findByCustomerId(String customerId);

}