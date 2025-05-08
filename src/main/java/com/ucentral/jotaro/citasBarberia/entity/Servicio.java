package com.ucentral.jotaro.citasBarberia.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "servicios")
public class Servicio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idServicio;

    @Column(nullable = false, unique = true, length = 150)
    private String nombre;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column
    private Integer duracionEstimadaMinutos;

    @Column(precision = 10, scale = 2)
    private BigDecimal precio;
    
    @Override
    public String toString() {
        return nombre + (precio != null ? " - $" + precio : "") + 
               (duracionEstimadaMinutos != null ? " (" + duracionEstimadaMinutos + " min)" : "");
    }
}
