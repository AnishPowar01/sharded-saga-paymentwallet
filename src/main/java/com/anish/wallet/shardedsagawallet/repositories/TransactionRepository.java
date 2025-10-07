package com.anish.wallet.shardedsagawallet.repositories;

import com.anish.wallet.shardedsagawallet.entity.Transaction;
import com.anish.wallet.shardedsagawallet.entity.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByFromWalletId(Long fromWalletId);

    List<Transaction> findByTomWalletId(Long toWalletId);

    @Query("Select t from Transaction t where t.fromWalletId = :walletId OR t.toWalletId = :walletId")
    List<Transaction> findByWalletId(@Param("walletId") Long walletId);

    List<Transaction> findByStatus(TransactionStatus status);

    List<Transaction> findBySagaInstanceId(Long sagaInstanceId);
}
