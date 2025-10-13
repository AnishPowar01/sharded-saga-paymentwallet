package com.anish.wallet.shardedsagawallet.services.saga;

public interface SagaStepInterface {

    boolean execute(SagaContext context);

    boolean compensate(SagaContext sagaContext);

    String getStepName();
}
