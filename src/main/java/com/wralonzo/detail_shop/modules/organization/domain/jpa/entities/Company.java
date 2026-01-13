package com.wralonzo.detail_shop.modules.organization.domain.jpa.entities;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
  name = "companies",
  schema = "organization",
  uniqueConstraints = {
    @UniqueConstraint(name = "uk_company_tax_id", columnNames = "tax_id")
  })
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Company {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "business_name", nullable = false, length = 50)
  private String businessName; // Nombre legal

  @Column(name = "tax_id", unique = true, nullable = false, length = 10)
  private String taxId; // NIT, RUC, RFC, etc.

  @Column(length = 100)
  private String address;

  @Column(length = 50)
  private String phone;

  @Column(length = 50)
  private String email;

  @OneToMany(mappedBy = "company", cascade = CascadeType.ALL)
  private List<Branch> branches;

  @CreationTimestamp
  @Column(updatable = false)
  private LocalDateTime createdAt;
}