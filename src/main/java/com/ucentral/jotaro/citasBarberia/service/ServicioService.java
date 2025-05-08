package com.ucentral.jotaro.citasBarberia.service;

import com.ucentral.jotaro.citasBarberia.entity.Servicio;
import java.util.List;
import java.util.Optional;

public interface ServicioService { // Nombre consistence con la entidad Servicio
    Servicio guardarServicio(Servicio servicio);
    Optional<Servicio> obtenerServicioPorId(Long id);
    List<Servicio> listarTodosLosServicios();
    void eliminarServicio(Long id);
}
