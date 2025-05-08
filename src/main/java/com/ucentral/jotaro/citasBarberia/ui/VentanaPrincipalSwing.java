package com.ucentral.jotaro.citasBarberia.ui;

import com.ucentral.jotaro.citasBarberia.service.CitaService;
import com.ucentral.jotaro.citasBarberia.service.ClienteService; // Para obtener clientes
import com.ucentral.jotaro.citasBarberia.service.ServicioService; // Para obtener servicios
// Importa tus entidades si las vas a listar
import com.ucentral.jotaro.citasBarberia.entity.Cliente;
import com.ucentral.jotaro.citasBarberia.entity.Servicio;


import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.ArrayList;


public class VentanaPrincipalSwing extends JFrame {
    private final CitaService citaService;
    private final ClienteService clienteService;
    private final ServicioService servicioBarberiaService;

    // Componentes de UI
    private JComboBox<Cliente> comboClientes;
    private JComboBox<Servicio> comboServicios;
    private JTextField txtFecha; // Podrías usar un JDatePicker si añades la librería
    private JTextField txtHora; // Formato HH:mm
    private JButton btnSolicitarReserva;

    public VentanaPrincipalSwing(CitaService citaService, ClienteService clienteService, ServicioService servicioBarberiaService) {
        super("Gestión de Citas Barbería");
        this.citaService = citaService;
        this.clienteService = clienteService;
        this.servicioBarberiaService = servicioBarberiaService;

        initComponents();
        cargarDatosIniciales();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        JPanel panelFormulario = new JPanel(new GridLayout(5, 2, 10, 10)); // 5 filas, 2 columnas

        panelFormulario.add(new JLabel("Cliente:"));
        comboClientes = new JComboBox<>();
        panelFormulario.add(comboClientes);

        panelFormulario.add(new JLabel("Servicio:"));
        comboServicios = new JComboBox<>();
        panelFormulario.add(comboServicios);

        panelFormulario.add(new JLabel("Fecha (YYYY-MM-DD):"));
        txtFecha = new JTextField();
        panelFormulario.add(txtFecha);

        panelFormulario.add(new JLabel("Hora (HH:MM):"));
        txtHora = new JTextField();
        panelFormulario.add(txtHora);

        btnSolicitarReserva = new JButton("Solicitar Reserva");
        btnSolicitarReserva.addActionListener(this::accionSolicitarReserva);
        panelFormulario.add(new JLabel()); // Espacio en blanco
        panelFormulario.add(btnSolicitarReserva);

        add(panelFormulario, BorderLayout.CENTER);
        // Aquí puedes añadir más paneles, menús, etc.
    }

    private void cargarDatosIniciales() {
        // Cargar clientes (necesitarás un método en ClienteService para listarlos)
        // List<Cliente> clientes = clienteService.listarTodos();
        // clientes.forEach(comboClientes::addItem);
        // Mock por ahora:
        comboClientes.addItem(new Cliente(1L, "Cliente", "Ejemplo", "cliente@example.com", "12345", new ArrayList<>()));


        // Cargar servicios (necesitarás un método en ServicioBarberiaService para listarlos)
        // List<Servicio> servicios = servicioBarberiaService.listarTodos();
        // servicios.forEach(comboServicios::addItem);
        // Mock por ahora:
        comboServicios.addItem(new Servicio(1L, "Corte de Cabello", "Corte sencillo", 30, new java.math.BigDecimal("15.00")));


        // Para que los JComboBox muestren el nombre y no el toString() del objeto:
        // Deberías sobrescribir el método toString() en Cliente y Servicio,
        // o usar un ComboBoxRenderer personalizado.
        // Por simplicidad, asegúrate que el toString() de Cliente y Servicio devuelva algo legible.
    }

    private void accionSolicitarReserva(ActionEvent e) {
        try {
            Cliente clienteSeleccionado = (Cliente) comboClientes.getSelectedItem();
            Servicio servicioSeleccionado = (Servicio) comboServicios.getSelectedItem();
            LocalDate fecha = LocalDate.parse(txtFecha.getText()); // Añadir validación de formato
            LocalTime hora = LocalTime.parse(txtHora.getText());   // Añadir validación de formato
            LocalDateTime fechaHora = LocalDateTime.of(fecha, hora);

            if (clienteSeleccionado == null || servicioSeleccionado == null) {
                JOptionPane.showMessageDialog(this, "Por favor, seleccione cliente y servicio.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Llamar al servicio para solicitar la reserva
            citaService.solicitarReserva(clienteSeleccionado.getIdCliente(), servicioSeleccionado.getIdServicio(), fechaHora);

            JOptionPane.showMessageDialog(this, "Solicitud de reserva enviada exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            // Limpiar formulario o actualizar UI
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Formato de fecha u hora incorrecto. Use YYYY-MM-DD y HH:MM.", "Error de Formato", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al solicitar la reserva: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace(); // Para depuración
        }
    }
}