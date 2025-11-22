package com.waira.waira_v2.entity;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.util.StringUtils;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "direcciones")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Direccion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idDireccion;


    private String tipoVia;
    private String numero;
    private String complemento;
    private String barrio;
    private String ciudad;

    public String getDireccionFormateada() {
        String resultado = Stream.of(tipoVia, numero, complemento, barrio, ciudad)
                .filter(StringUtils::hasText)
                .collect(Collectors.joining(" "));
        return resultado.isBlank() ? "" : resultado;
    }
}
