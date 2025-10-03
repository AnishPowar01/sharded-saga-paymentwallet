package com.anish.wallet.shardedsagawallet.services.saga;

public interface SagaStep {

    boolean execute(SagaContext context);

    boolean compensate(SagaContext sagaContext);

    String getStepName();
}
