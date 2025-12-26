package com.wralonzo.detail_shop.domain.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "warehouses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Warehouse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_warehouse")
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 100)
    private String boss;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(nullable = false, length = 100)
    private String address;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(nullable = true, length = 100)
    private String notes;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW()")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "update_at", nullable = true, updatable = false)
    private LocalDateTime updateAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @OneToMany(mappedBy = "warehouse", cascade = CascadeType.ALL)
    private List<Product> products;

    @OneToMany(mappedBy = "warehouse", cascade = CascadeType.ALL)
    private List<Client> client;

    @OneToMany(mappedBy = "warehouse", cascade = CascadeType.ALL)
    private List<Employee> employee;

    @OneToMany(mappedBy = "warehouse", cascade = CascadeType.ALL)
    private List<Reservation> reservation;
}
