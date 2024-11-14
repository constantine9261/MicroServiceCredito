package com.bank.microserviceCredit.Model.entity;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.time.LocalDateTime;
@Data
@Builder
@Document(collection = "credits")
public class CreditEntity implements Serializable {

    @Id
    private String id;
    private String customerId;
    private String type; // "PERSONAL", "BUSINESS", o "CREDIT_CARD"
    private Double creditLimit;
    private Double balance;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
