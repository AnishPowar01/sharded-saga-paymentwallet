package com.anish.wallet.shardedsagawallet.repositories;

import com.anish.wallet.shardedsagawallet.entity.SagaInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SagaInstanceRepository extends JpaRepository<SagaInstance, Long> {
}
