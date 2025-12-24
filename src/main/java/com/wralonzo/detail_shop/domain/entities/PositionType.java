package com.wralonzo.detail_shop.domain.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Table(name = "position_type")
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PositionType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_position_type")
    private Long id;

    @Column(name = "name", length = 100)
    private String name;

    private int level;

    @OneToOne(mappedBy = "positionType")
    private Employee employee;
}
