package com.wralonzo.detail_shop.modules.organization.domain.jpa.entities;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "branches", schema = "organization")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Branch {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String name; // Ej: "Sucursal Norte", "Sede Central"

  @Column(length = 50)
  private String code;

  @Column(length = 100)
  private String address;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "company_id", nullable = false)
  private Company company;

  @OneToMany(mappedBy = "branch", cascade = CascadeType.ALL)
  private List<Warehouse> warehouses;
}