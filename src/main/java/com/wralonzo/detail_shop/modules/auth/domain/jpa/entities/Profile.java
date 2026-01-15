package com.wralonzo.detail_shop.modules.auth.domain.jpa.entities;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.*;
import jakarta.validation.constraints.Past;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "profile", schema = "auth")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
public class Profile {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id_profile")
  private Long id;

  @Column(name = "full_name")
  private String fullName;

  @Column(name = "phone", length = 20)
  private String phone;

  @Column(length = 100)
  private String email;

  @Column(name = "address")
  private String address;

  @Column(name = "avatar", nullable = true)
  private String avatar;

  @Past(message = "La fecha de nacimiento debe ser una fecha pasada")
  @Column(name = "birth_date")
  @JsonFormat(pattern = "yyyy-MM-dd")
  private LocalDate birthDate;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, columnDefinition = "TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW()")
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "update_at", updatable = false)
  private LocalDateTime updateAt;

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;
}
