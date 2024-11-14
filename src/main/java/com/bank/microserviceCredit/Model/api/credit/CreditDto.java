package com.bank.microserviceCredit.Model.api.credit;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CreditDto {
    private String id;
    private String customerId;
    private String type;
    private Double creditLimit;
    private Double balance;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
