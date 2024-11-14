package com.bank.microserviceCredit.controller;


import com.bank.microserviceCredit.Model.api.credit.CreditDto;
import com.bank.microserviceCredit.Model.api.credit.CreditRequest;
import com.bank.microserviceCredit.Model.api.shared.ResponseDto;
import com.bank.microserviceCredit.Model.api.shared.ResponseDtoBuilder;
import com.bank.microserviceCredit.business.service.ICreditService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/credits")
@RequiredArgsConstructor
public class CreditController {

    private final ICreditService creditService;

    @PostMapping
    public Mono<ResponseDto<CreditDto>> createCredit(@RequestBody CreditRequest request) {
        return creditService.createCredit(request)
                .map(credit -> ResponseDtoBuilder.success(credit, "Crédito creado con éxito"))
                .onErrorResume(e -> Mono.just(
                        ResponseDtoBuilder.error("Error al crear el crédito: " + e.getMessage())
                ));
    }

    @GetMapping("/{id}")
    public Mono<ResponseDto<CreditDto>> getCreditById(@PathVariable String id) {
        return creditService.findById(id)
                .map(credit -> ResponseDtoBuilder.success(credit, "Crédito encontrado"))
                .switchIfEmpty(Mono.just(ResponseDtoBuilder.notFound("Crédito no encontrado")));
    }

    @GetMapping
    public Mono<ResponseDto<List<CreditDto>>> getAllCredits() {
        return creditService.findAll()  // Obtiene el Flux<CreditDto>
                .collectList()  // Convierte el Flux a Mono<List<CreditDto>>
                .map(creditList -> ResponseDtoBuilder.success(creditList, "Lista de créditos obtenida con éxito"));
    }

    @PutMapping("/{id}")
    public Mono<ResponseDto<CreditDto>> updateCredit(@PathVariable String id, @RequestBody CreditRequest request) {
        return creditService.updateCredit(id, request)
                .map(credit -> ResponseDtoBuilder.success(credit, "Crédito actualizado con éxito"))
                .switchIfEmpty(Mono.just(ResponseDtoBuilder.notFound("Crédito no encontrado")));
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseDto<Object>> deleteCredit(@PathVariable String id) {
        return creditService.deleteById(id)
                .then(Mono.just(ResponseDtoBuilder.success(null, "Crédito eliminado con éxito")))
                .switchIfEmpty(Mono.just(ResponseDtoBuilder.notFound("Crédito no encontrado")));
    }
}
