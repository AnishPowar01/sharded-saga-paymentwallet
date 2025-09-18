package com.anish.wallet.shardedsagawallet.repositories;

import com.anish.wallet.shardedsagawallet.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {

    List<User> findByName(String name);
}
