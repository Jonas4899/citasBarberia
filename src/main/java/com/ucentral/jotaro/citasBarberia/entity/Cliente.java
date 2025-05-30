package com.ucentral.jotaro.citasBarberia.entity;
import jakarta.persistence.*;
import lombok.Data; 
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data // Genera los getters y setters
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "clientes")
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCliente;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 100)
    private String apellido;

    @Column(nullable = false, unique = true, length = 150)
    private String correoElectronico;

    @Column(length = 20)
    private String telefono;

    @OneToMany(mappedBy = "cliente")
    private List<Reserva> reservas;
    
    @Override
    public String toString() {
        return nombre + " " + apellido + (correoElectronico != null && !correoElectronico.isEmpty() ? " (" + correoElectronico + ")" : "");
    }
}
