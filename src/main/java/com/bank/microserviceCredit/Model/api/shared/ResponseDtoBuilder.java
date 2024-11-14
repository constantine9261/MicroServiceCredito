package com.bank.microserviceCredit.Model.api.shared;

public class ResponseDtoBuilder {
    /**
     * Crea un ResponseDto con datos, mensaje y estado de éxito.
     *
     * @param data    Los datos que se incluirán en el ResponseDto.
     * @param message Mensaje informativo sobre la operación.
     * @param <T>     Tipo de los datos incluidos en el ResponseDto.
     * @return ResponseDto con el estado "SUCCESS".
     */
    public static <T> ResponseDto<T> success(T data, String message) {
        return ResponseDto.<T>builder()
                .data(data)
                .message(message)
                .status("SUCCESS")
                .build();
    }

    /**
     * Crea un ResponseDto con datos, mensaje y estado de "NOT_FOUND".
     *
     * @param message Mensaje informativo sobre la operación.
     * @param <T>     Tipo de los datos incluidos en el ResponseDto.
     * @return ResponseDto con el estado "NOT_FOUND".
     */
    public static <T> ResponseDto<T> notFound(String message) {
        return ResponseDto.<T>builder()
                .data(null)
                .message(message)
                .status("NOT_FOUND")
                .build();
    }

    /**
     * Crea un ResponseDto con datos, mensaje y un estado de error.
     *
     * @param message Mensaje de error.
     * @param <T>     Tipo de los datos incluidos en el ResponseDto.
     * @return ResponseDto con el estado "ERROR".
     */
    public static <T> ResponseDto<T> error(String message) {
        return ResponseDto.<T>builder()
                .data(null)
                .message(message)
                .status("ERROR")
                .build();
    }
}
