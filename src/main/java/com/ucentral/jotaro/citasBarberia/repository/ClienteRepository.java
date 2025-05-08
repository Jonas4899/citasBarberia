package com.ucentral.jotaro.citasBarberia.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.ucentral.jotaro.citasBarberia.entity.Cliente;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
}
