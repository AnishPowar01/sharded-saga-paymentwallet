package com.anish.wallet.shardedsagawallet.services.saga.steps;

import com.anish.wallet.shardedsagawallet.services.saga.SagaStepInterface;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class SagaStepFactory {

    private static final Map<String, SagaStepInterface> sagaStepMap = Map.of();

    public static enum SagaStepType
    {
        DEBIT_SOURCE_WALLET_STEP,
        CREDIT_DESTINATION_WALLET_STEP,
        UPDATE_TRANSACTION_STATUS_STEP
    }

    public static SagaStepInterface getSagaStep(String stepName)
    {
        return sagaStepMap.get(stepName);
    }
}
