package com.bank.microserviceCredit.Model.api.shared;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResponseDto<T> {

    private T data; // Datos de la respuesta
    private String message; // Mensaje sobre el estado de la operación
    private String status; // Estado de la respuesta (ej. "SUCCESS" o "ERROR")
}
