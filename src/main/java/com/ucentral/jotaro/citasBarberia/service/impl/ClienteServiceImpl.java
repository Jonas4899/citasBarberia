package com.ucentral.jotaro.citasBarberia.service.impl;

import com.ucentral.jotaro.citasBarberia.entity.Cliente;
import com.ucentral.jotaro.citasBarberia.repository.ClienteRepository;
import com.ucentral.jotaro.citasBarberia.service.ClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Importante para la gestión de transacciones

import java.util.List;
import java.util.Optional;

@Service // Marca esta clase como un bean de servicio gestionado por Spring
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository clienteRepository;

    @Autowired // Spring inyectará una instancia de ClienteRepository aquí
    public ClienteServiceImpl(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @Override
    @Transactional // Las operaciones que modifican datos deben ser transaccionales
    public Cliente guardarCliente(Cliente cliente) {
        // Aquí podrías añadir validaciones o lógica de negocio antes de guardar
        return clienteRepository.save(cliente);
    }

    @Override
    @Transactional(readOnly = true) // Para operaciones de solo lectura, optimiza la transacción
    public Optional<Cliente> obtenerClientePorId(Long id) {
        return clienteRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cliente> listarTodosLosClientes() {
        return clienteRepository.findAll();
    }

    @Override
    @Transactional
    public void eliminarCliente(Long id) {
        // Podrías verificar si el cliente existe antes de intentar borrarlo
        // if(clienteRepository.existsById(id)) {
        //     clienteRepository.deleteById(id);
        // } else {
        //     throw new RuntimeException("Cliente no encontrado con ID: " + id);
        // }
        clienteRepository.deleteById(id);
    }
}