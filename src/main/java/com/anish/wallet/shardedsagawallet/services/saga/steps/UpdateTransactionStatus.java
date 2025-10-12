package com.anish.wallet.shardedsagawallet.services.saga.steps;

import com.anish.wallet.shardedsagawallet.entity.Transaction;
import com.anish.wallet.shardedsagawallet.entity.TransactionStatus;
import com.anish.wallet.shardedsagawallet.repositories.TransactionRepository;
import com.anish.wallet.shardedsagawallet.services.saga.SagaContext;
import com.anish.wallet.shardedsagawallet.services.saga.SagaStep;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UpdateTransactionStatus implements SagaStep {

    private final TransactionRepository transactionRepository;

    @Override
    @Transactional
    public boolean execute(SagaContext context) {
        Long transactionId = context.getLong("transactionId");
        log.info("updating transaction status for {}", transactionId);

        Transaction transaction = transactionRepository.findById(transactionId).orElseThrow(() -> new RuntimeException("Transaction not found"));

        context.put("originalTransactionStatus", transaction.getStatus());
        transaction.setStatus(TransactionStatus.SUCCESS);
        transactionRepository.save(transaction);

        log.info("Transaction status updated for transaction {}", transactionId);

        context.put("transactionStatusAfterUpdate", transaction.getStatus());

        log.info("Update transaction status step executed successfully");

        return true;
    }

    @Override
    @Transactional
    public boolean compensate(SagaContext sagaContext) {
        Long transactionId = sagaContext.getLong("transactionId");

        TransactionStatus status = TransactionStatus.valueOf(sagaContext.getString("originalTransactionStatus"));

        log.info("Compensating transaction status for transaction {}", transactionId);

        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        transaction.setStatus(status);
        transactionRepository.save(transaction);

        log.info("Transaction status compensated for transaction {}", transactionId);

        return true;
    }

    @Override
    public String getStepName() {
        return "UpdateTransactionStatus";
    }
}
