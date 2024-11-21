package com.bank.microserviceCredit.controller;


import com.bank.microserviceCredit.Model.api.credit.CreditDto;
import com.bank.microserviceCredit.Model.api.credit.CreditRequest;
import com.bank.microserviceCredit.Model.api.shared.ResponseDto;
import com.bank.microserviceCredit.Model.api.shared.ResponseDtoBuilder;
import com.bank.microserviceCredit.business.service.ICreditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    @Operation(summary = "Crear crédito", description = "Crea un nuevo crédito con los datos proporcionados")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Crédito creado con éxito",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PostMapping
    public Mono<ResponseDto<CreditDto>> createCredit(@RequestBody CreditRequest request) {
        return creditService.createCredit(request)
                .map(credit -> ResponseDtoBuilder.success(credit, "Crédito creado con éxito"))
                .onErrorResume(e -> Mono.just(
                        ResponseDtoBuilder.error("Error al crear el crédito: " + e.getMessage())
                ));
    }
    @Operation(summary = "Obtener crédito por ID", description = "Obtiene los detalles de un crédito específico por su ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Crédito encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Crédito no encontrado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/{id}")
    public Mono<ResponseDto<CreditDto>> getCreditById(@PathVariable String id) {
        return creditService.findById(id)
                .map(credit -> ResponseDtoBuilder.success(credit, "Crédito encontrado"))
                .switchIfEmpty(Mono.just(ResponseDtoBuilder.notFound("Crédito no encontrado")));
    }
    @Operation(summary = "Obtener todos los créditos", description = "Obtiene una lista de todos los créditos disponibles")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de créditos obtenida con éxito",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping
    public Mono<ResponseDto<List<CreditDto>>> getAllCredits() {
        return creditService.findAll()  // Obtiene el Flux<CreditDto>
                .collectList()  // Convierte el Flux a Mono<List<CreditDto>>
                .map(creditList -> ResponseDtoBuilder.success(creditList, "Lista de créditos obtenida con éxito"));
    }
    @Operation(summary = "Actualizar crédito", description = "Actualiza los datos de un crédito existente por su ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Crédito actualizado con éxito",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Crédito no encontrado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PutMapping("/{id}")
    public Mono<ResponseDto<CreditDto>> updateCredit(@PathVariable String id, @RequestBody CreditRequest request) {
        return creditService.updateCredit(id, request)
                .map(credit -> ResponseDtoBuilder.success(credit, "Crédito actualizado con éxito"))
                .switchIfEmpty(Mono.just(ResponseDtoBuilder.notFound("Crédito no encontrado")));
    }
    @Operation(summary = "Eliminar crédito", description = "Elimina un crédito específico por su ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Crédito eliminado con éxito",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Crédito no encontrado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @DeleteMapping("/{id}")
    public Mono<ResponseDto<Object>> deleteCredit(@PathVariable String id) {
        return creditService.deleteById(id)
                .then(Mono.just(ResponseDtoBuilder.success(null, "Crédito eliminado con éxito")))
                .switchIfEmpty(Mono.just(ResponseDtoBuilder.notFound("Crédito no encontrado")));
    }

    @GetMapping("/customer/{customerId}/has-active-card")
    public Mono<Boolean> hasActiveCreditCard(@PathVariable String customerId) {
        return creditService.hasActiveCreditCard(customerId);
    }

}
