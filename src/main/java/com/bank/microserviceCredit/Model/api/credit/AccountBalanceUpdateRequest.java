package com.bank.microserviceCredit.Model.api.credit;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountBalanceUpdateRequest {
    private Double newBalance;

}
