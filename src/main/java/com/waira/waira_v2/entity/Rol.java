package com.waira.waira_v2.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Rol {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idRol;

    @Column(nullable = false, unique = true, length = 50)
    private String nombreRol;
}


