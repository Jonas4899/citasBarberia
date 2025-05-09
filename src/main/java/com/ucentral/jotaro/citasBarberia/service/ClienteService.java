package com.ucentral.jotaro.citasBarberia.service;

import com.ucentral.jotaro.citasBarberia.entity.Cliente;
import java.util.List;
import java.util.Optional;

public interface ClienteService {
    Cliente guardarCliente(Cliente cliente);
    Optional<Cliente> obtenerClientePorId(Long id);
    List<Cliente> listarTodosLosClientes();
    void eliminarCliente(Long id);
}