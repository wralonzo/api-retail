package com.wralonzo.detail_shop.domain.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.wralonzo.detail_shop.domain.enums.ProviderRegister;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "user", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "provider", "provider_id" })
})
@EntityListeners(AuditingEntityListener.class)
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "username", length = 100)
    private String username;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "address", length = 100)
    private String address;

    @Column(name = "avatar", nullable = true)
    private String avatar;

    @Column(name = "password")
    private String password;

    @Column(name = "init_password")
    private String passwordInit;

    @Enumerated(EnumType.ORDINAL)
    private ProviderRegister provider;

    private String providerId;

    @Builder.Default
    private boolean enabled = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW()")
    private LocalDateTime createdAt;

    @CreationTimestamp
    @Column(name = "last_login_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW()")
    private LocalDateTime lastLoginAt;

    @UpdateTimestamp
    @LastModifiedDate
    @Column(name = "update_at")
    private LocalDateTime updateAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "employee_id")
    public Employee employee;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "client_id")
    public Client client;

    @ManyToMany(fetch = FetchType.EAGER) // EAGER para cargar roles al autenticar
    @JoinTable(name = "users_roles", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toList());
    }
}