package com.splitbill.paymentservice.client;

import com.splitbill.paymentservice.dto.response.SettlementResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class SettlementClient {

    private final RestClient restClient;

    public SettlementClient(@Value("${services.settlement.url:http://localhost:8085}") String settlementServiceUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(settlementServiceUrl)
                .build();
    }

    public SettlementResponse getSettlement(Long settlementId) {

        return restClient.get()
                .uri("/api/v1/settlements/{id}", settlementId)
                .retrieve()
                .body(SettlementResponse.class);
    }
}