package com.anish.wallet.shardedsagawallet.config;

import com.anish.wallet.shardedsagawallet.services.saga.SagaStepInterface;
import com.anish.wallet.shardedsagawallet.services.saga.steps.CreditDestinationWalletStep;
import com.anish.wallet.shardedsagawallet.services.saga.steps.DebitSourceWalletStep;
import com.anish.wallet.shardedsagawallet.services.saga.steps.SagaStepFactory;
import com.anish.wallet.shardedsagawallet.services.saga.steps.UpdateTransactionStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class SagaConfiguration {

    @Bean
    public Map<String, SagaStepInterface> sagaStepMap(
            DebitSourceWalletStep debitSourceWalletStep,
            CreditDestinationWalletStep creditDestinationWalletStep,
            UpdateTransactionStatus updateTransactionStatus
    ){
        Map<String, SagaStepInterface> StepMap = new HashMap<>();
        StepMap.put(SagaStepFactory.SagaStepType.DEBIT_SOURCE_WALLET_STEP.toString(), debitSourceWalletStep);
        StepMap.put(SagaStepFactory.SagaStepType.CREDIT_DESTINATION_WALLET_STEP.toString(), creditDestinationWalletStep);
        StepMap.put(SagaStepFactory.SagaStepType.UPDATE_TRANSACTION_STATUS_STEP.toString(), updateTransactionStatus);
        return StepMap;
    }
}
