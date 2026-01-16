package com.wralonzo.detail_shop.modules.auth.domain.jpa.entities;

import java.util.Set;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "roles", schema = "auth")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_role")
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @Column()
    private String note;

    // En tu clase Role.java
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "roles_permissions", schema = "auth", joinColumns = @JoinColumn(name = "role_id"), inverseJoinColumns = @JoinColumn(name = "permission_id"))
    private Set<Permission> permissions;
}