package com.anish.wallet.shardedsagawallet.services.saga.steps;

import com.anish.wallet.shardedsagawallet.entity.Wallet;
import com.anish.wallet.shardedsagawallet.repositories.WalletRepository;
import com.anish.wallet.shardedsagawallet.services.saga.SagaContext;
import com.anish.wallet.shardedsagawallet.services.saga.SagaStep;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class DebitSourceWalletStep implements SagaStep {

    private final WalletRepository walletRepository;
    @Override
    @Transactional
    public boolean execute(SagaContext context) {
        Long fromWalletId = context.getLong("fromWalletId");
        BigDecimal amount = context.getBigDecimal("amount");

        Wallet wallet = walletRepository.findByIdWithLock(fromWalletId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        if(!wallet.hasSufficientBalance(amount))
        {
            throw new RuntimeException("Insufficient Balance");
        }
        context.put("originalSourceWalletBalance", wallet.getBalance());

        wallet.debit(amount);
        walletRepository.save(wallet);

        context.put("sourceWalletBalanceAfterDebit", wallet.getBalance());
        return true;
    }

    @Override
    public boolean compensate(SagaContext context) {
        Long fromWalletId = context.getLong("fromWalletId");
        BigDecimal amount = context.getBigDecimal("amount");

        Wallet wallet = walletRepository.findByIdWithLock(fromWalletId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        wallet.credit(amount);
        walletRepository.save(wallet);

        context.put("sourceWalletBalanceAfterDebitCompensation", wallet.getBalance());
        return true;
    }

    @Override
    public String getStepName() {
        return "DebitSourceWalletStep";
    }
}
