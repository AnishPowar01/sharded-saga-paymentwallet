package com.anish.wallet.shardedsagawallet.services.saga.steps;

import com.anish.wallet.shardedsagawallet.entity.Wallet;
import com.anish.wallet.shardedsagawallet.repositories.WalletRepository;
import com.anish.wallet.shardedsagawallet.services.saga.SagaContext;
import com.anish.wallet.shardedsagawallet.services.saga.SagaStep;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreditDestinationWalletStep implements SagaStep {

    private final WalletRepository walletRepository;

    @Override
    @Transactional
    public boolean execute(SagaContext context) {
        Long toWalletId = context.getLong("toWalletId");
        BigDecimal amount = context.getBigDecimal("amount");

        log.info("Crediting destination wallet {} with amount of {}", toWalletId, amount);

        Wallet wallet = walletRepository.findByIdWithLock(toWalletId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        context.put("originalToWalletBalance", wallet.getBalance());

        wallet.credit(amount);
        walletRepository.save(wallet);

        context.put("toWalletBalanceAfterCredit", wallet.getBalance());

        return true;
    }

    @Override
    @Transactional
    public boolean compensate(SagaContext sagaContext) {
        Long toWalletId = sagaContext.getLong("toWalletId");
        BigDecimal amount = sagaContext.getBigDecimal("amount");

        log.info("Compensating credit of destination wallet {} with amount of {}", toWalletId, amount);

        Wallet wallet = walletRepository.findByIdWithLock(toWalletId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        wallet.debit(amount);
        walletRepository.save(wallet);
        sagaContext.put("toWalletBalanceAfterCreditCompensation", wallet.getBalance());

        return true;
    }

    @Override
    public String getStepName() {
        return "CreditDestinationWalletStep";
    }
}
