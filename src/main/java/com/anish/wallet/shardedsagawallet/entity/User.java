package com.anish.wallet.shardedsagawallet.entity;

import jakarta.persistence.*;
import lombok.*;
import jakarta.persistence.Id;


@Entity
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Table(name = "user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(nullable = false, unique = true)
    private String email;
}
