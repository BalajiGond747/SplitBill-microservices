package com.splitbill.settlementservice.client;

import com.splitbill.settlementservice.dto.request.BalanceSettlementRequest;
import com.splitbill.settlementservice.dto.response.BalanceResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class BalanceClient {

    private final RestClient restClient;

    public BalanceClient(@Value("${services.balance.url:http://localhost:8084}") String balanceServiceUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(balanceServiceUrl)
                .build();
    }

    public BalanceResponse getBalance(Long groupId, Long fromUserId, Long toUserId) {

        return restClient.get()
                .uri("/api/v1/balances/internal/group/{groupId}/from/{fromUserId}/to/{toUserId}", groupId, fromUserId, toUserId)
                .retrieve()
                .body(BalanceResponse.class);
    }

    public BalanceResponse settleBalance(BalanceSettlementRequest request) {

        return restClient.post()
                .uri("/api/v1/balances/internal/settle")
                .body(request)
                .retrieve()
                .body(BalanceResponse.class);
    }
}