package com.ucentral.jotaro.citasBarberia.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.ucentral.jotaro.citasBarberia.entity.Reserva;
import java.util.List;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {
    
    @Query("SELECT r FROM Reserva r JOIN FETCH r.cliente JOIN FETCH r.servicio")
    List<Reserva> findAllWithClienteAndServicio();
}
