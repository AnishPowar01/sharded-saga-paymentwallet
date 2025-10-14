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

import java.util.List;

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

        // SagaStep sagaStepDB  = sagaStepRepository.findBySagaInstanceIdAndStatus(sagaInstanceId, StepStatus.PENDING).
        //         stream().filter(s -> s.getStepName().equals(stepName))
        //         .findFirst().orElse(SagaStep.builder()
        //                 .sagaInstanceId(sagaInstanceId).stepName(stepName).stepStatus(StepStatus.PENDING).build());

        SagaStep sagaStepDB = sagaStepRepository.findBySagaInstanceIdAndStepNameAndStatus(sagaInstanceId, stepName, StepStatus.PENDING).orElse(SagaStep.builder()
                .sagaInstanceId(sagaInstanceId).stepName(stepName).stepStatus(StepStatus.PENDING).build());

        if (sagaStepDB.getId() == null) {
            sagaStepDB = sagaStepRepository.save(sagaStepDB);
        }

        try {
            SagaContext context = objectMapper.readValue(sagaInstance.getContext(), SagaContext.class);

            sagaStepDB.setStepStatus(StepStatus.RUNNING);
            sagaStepRepository.save(sagaStepDB);

            boolean success = step.execute(context);

            if (success) {
                sagaStepDB.setStepStatus(StepStatus.COMPLETED);
                sagaStepRepository.save(sagaStepDB);

                sagaInstance.setCurrentStep(stepName);
                sagaInstance.setStatus(SagaStatus.RUNNING);
                sagaInstanceRepository.save(sagaInstance);

                return true;

            } else {
                sagaStepDB.setStepStatus(StepStatus.FAILED);
                sagaStepRepository.save(sagaStepDB);
                return false;
            }

        } catch (JsonProcessingException e) {

            sagaStepDB.setStepStatus(StepStatus.FAILED);
            sagaStepRepository.save(sagaStepDB);

            throw new RuntimeException(e);
        }


    }

    @Override
    public Boolean compensateStep(Long sagaInstanceId, String stepName) {
        SagaInstance sagaInstance = sagaInstanceRepository.findById(sagaInstanceId).orElseThrow(() -> new RuntimeException("Not found"));
        SagaStepInterface step = SagaStepFactory.getSagaStep(stepName);

        if (step == null) {
            log.error("Saga step not found");
            throw new RuntimeException("Saga step not found");
        }

        SagaStep sagaStep_DB = sagaStepRepository.findBySagaInstanceIdAndStepNameAndStatus(sagaInstanceId, stepName, StepStatus.COMPLETED).orElse(
                null
        );

        if (sagaStep_DB == null) {
            log.info("Step not found in the db");
            return true;
        }

        try {
            SagaContext sagaContext = objectMapper.readValue(sagaInstance.getContext(), SagaContext.class);
            sagaStep_DB.setStepStatus(StepStatus.COMPENSATING);
            sagaStepRepository.save(sagaStep_DB);

            boolean compensated = step.compensate(sagaContext);

            if (compensated) {
                sagaStep_DB.setStepStatus(StepStatus.COMPENSATED);
                sagaStepRepository.save(sagaStep_DB);
                return true;

            } else {
                sagaStep_DB.setStepStatus(StepStatus.FAILED);
                sagaStepRepository.save(sagaStep_DB);
                return false;
            }
        } catch (JsonProcessingException e) {
            sagaStep_DB.setStepStatus(StepStatus.FAILED);
            sagaStepRepository.save(sagaStep_DB);
            throw new RuntimeException(e);
        }

    }

    @Override
    public SagaInstance getSagaInstance(Long sagaInstanceId) {
        return sagaInstanceRepository.findById(sagaInstanceId).orElseThrow(() -> new RuntimeException("Saga Instance not found"));
    }

    @Override
    public void compensateSaga(Long sagaInstanceId) {
        SagaInstance sagaInstance = sagaInstanceRepository.findById(sagaInstanceId).orElseThrow(() -> new RuntimeException("Saga Instance not found"));
        sagaInstance.setStatus(SagaStatus.COMPENSATING);
        sagaInstanceRepository.save(sagaInstance);

    }

    @Override
    public void failedSaga(Long sagaInstanceId) {
        SagaInstance sagaInstance = sagaInstanceRepository.findById(sagaInstanceId).orElseThrow(() -> new RuntimeException("Saga Instance not found"));
        sagaInstance.setStatus(SagaStatus.FAILED);
        sagaInstanceRepository.save(sagaInstance);

//        get all saga steps which is completed or compensated

        List<SagaStep> sagaStepList = sagaStepRepository.findCompletedOrCompensatedStepsBySagaInstanceId(sagaInstanceId);

        boolean isAllCompensated = true;

        for (SagaStep sagaStep : sagaStepList) {
            boolean compensated = this.compensateStep(sagaInstanceId, sagaStep.getStepName());
            if (!compensated) {
                isAllCompensated = false;
            }
        }

        if (isAllCompensated) {
            sagaInstance.setStatus(SagaStatus.COMPENSATED);
            sagaInstanceRepository.save(sagaInstance);
        } else {
            sagaInstance.setStatus(SagaStatus.FAILED);
            sagaInstanceRepository.save(sagaInstance);
        }
    }

    @Override
    public void completeSaga(Long sagaInstanceId) {
        SagaInstance sagaInstance = sagaInstanceRepository.findById(sagaInstanceId).orElseThrow(() -> new RuntimeException("Saga Instance not found"));
        sagaInstance.setStatus(SagaStatus.COMPLETED);
        sagaInstanceRepository.save(sagaInstance);

    }
}
