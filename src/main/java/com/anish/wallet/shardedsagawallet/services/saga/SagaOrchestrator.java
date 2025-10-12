package com.anish.wallet.shardedsagawallet.services.saga;

import com.anish.wallet.shardedsagawallet.entity.SagaInstance;

public interface SagaOrchestrator {

    Long startSaga(SagaContext sagaContext);

    Boolean executeStep(Long sagaInstanceId, String stepName);

    Boolean compensateStep(Long sagaInstanceId, String stepName);

    SagaInstance getSagaInstance(Long sagaInstanceId);

    void compensateSaga(Long sagaInstanceId);

    void failedSaga(Long sagaInstanceId);

    void completeSaga(Long sagaInstanceId);
}
