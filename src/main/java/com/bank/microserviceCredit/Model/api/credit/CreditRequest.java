package com.bank.microserviceCredit.Model.api.credit;

import lombok.Data;

@Data
public class CreditRequest {
    private String customerId;
    private String type; // "PERSONAL", "BUSINESS", o "CREDIT_CARD"
    private Double creditLimit;
    private Double balance;
    private boolean active; // Indica si el crédito o tarjeta está activa

}
