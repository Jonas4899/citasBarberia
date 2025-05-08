package com.ucentral.jotaro.citasBarberia.service.impl;

import com.ucentral.jotaro.citasBarberia.entity.Servicio;
import com.ucentral.jotaro.citasBarberia.repository.ServicioRepository;
import com.ucentral.jotaro.citasBarberia.service.ServicioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ServicioServiceImpl implements ServicioService {

    private final ServicioRepository servicioRepository;

    @Autowired
    public ServicioServiceImpl(ServicioRepository servicioRepository) {
        this.servicioRepository = servicioRepository;
    }

    @Override
    @Transactional
    public Servicio guardarServicio(Servicio servicio) {
        return servicioRepository.save(servicio);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Servicio> obtenerServicioPorId(Long id) {
        return servicioRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Servicio> listarTodosLosServicios() {
        return servicioRepository.findAll();
    }

    @Override
    @Transactional
    public void eliminarServicio(Long id) {
        servicioRepository.deleteById(id);
    }
}
