package com.anish.wallet.shardedsagawallet.services.saga;

import com.anish.wallet.shardedsagawallet.entity.SagaInstance;
import com.anish.wallet.shardedsagawallet.entity.SagaStatus;
import com.anish.wallet.shardedsagawallet.entity.SagaStep;
import com.anish.wallet.shardedsagawallet.entity.StepStatus;
import com.anish.wallet.shardedsagawallet.repositories.SagaInstanceRepository;
import com.anish.wallet.shardedsagawallet.repositories.SagaStepRepository;
import com.anish.wallet.shardedsagawallet.services.saga.steps.SagaStepFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class SagaOrchestratorImpl implements SagaOrchestrator {

    private final ObjectMapper objectMapper;
    private final SagaInstanceRepository sagaInstanceRepository;
    private final SagaStepRepository sagaStepRepository;

    @Override
    public Long startSaga(SagaContext sagaContext) {
        try {
            String contextJson = objectMapper.writeValueAsString(sagaContext);
            SagaInstance sagaInstance = SagaInstance.builder().context(contextJson)
                    .status(SagaStatus.STARTED)
                    .build();

            sagaInstance = sagaInstanceRepository.save(sagaInstance);

            log.info("Started saga with id {}", sagaInstance.getId());

            return sagaInstance.getId();

        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error starting saga", e);
        }
    }

    @Override
    public Boolean executeStep(Long sagaInstanceId, String stepName) {
        SagaInstance sagaInstance = sagaInstanceRepository.findById(sagaInstanceId).orElseThrow(() -> new RuntimeException("Not found"));
        SagaStepInterface step = SagaStepFactory.getSagaStep(stepName);
        if (step == null) {
            log.error("Saga Step not found");
            throw new RuntimeException("saga Step not found");
        }

        SagaStep sagaStepDB  = sagaStepRepository.findBySagaInstanceIdAndStatus(sagaInstanceId, StepStatus.PENDING).
                stream().filter(s -> s.getStepName().equals(stepName))
                .findFirst().orElse(SagaStep.builder()
                        .sagaInstanceId(sagaInstanceId).stepName(stepName).stepStatus(StepStatus.PENDING).build());

        if(sagaStepDB.getId() == null)
        {
            sagaStepDB = sagaStepRepository.save(sagaStepDB);
        }

        try
        {
            SagaContext context = objectMapper.readValue(sagaInstance.getContext(), SagaContext.class);

        }catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }


        return null;
    }

    @Override
    public Boolean compensateStep(Long sagaInstanceId, String stepName) {
        return null;
    }

    @Override
    public SagaInstance getSagaInstance(Long sagaInstanceId) {
        return null;
    }

    @Override
    public void compensateSaga(Long sagaInstanceId) {

    }

    @Override
    public void failedSaga(Long sagaInstanceId) {

    }

    @Override
    public void completeSaga(Long sagaInstanceId) {

    }
}
