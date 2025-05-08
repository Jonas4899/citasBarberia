package com.ucentral.jotaro.citasBarberia.ui;

import com.ucentral.jotaro.citasBarberia.service.CitaService;
import com.ucentral.jotaro.citasBarberia.service.ClienteService;
import com.ucentral.jotaro.citasBarberia.service.ServicioService;
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
    private JTextField txtFecha;
    private JTextField txtHora;
    private JButton btnSolicitarReserva;
    private JButton btnNuevoCliente;
    private JPanel panelNuevoCliente;
    private JTextField txtNombre;
    private JTextField txtApellido;
    private JTextField txtEmail;
    private JTextField txtTelefono;

    public VentanaPrincipalSwing(CitaService citaService, ClienteService clienteService, ServicioService servicioBarberiaService) {
        super("Gestión de Citas Barbería");
        this.citaService = citaService;
        this.clienteService = clienteService;
        this.servicioBarberiaService = servicioBarberiaService;

        initComponents();
        cargarDatosIniciales();
    }

    private void initComponents() {
        // Configurar menú
        JMenuBar menuBar = new JMenuBar();
        JMenu menuAdmin = new JMenu("Administración");
        JMenuItem menuItemAdmin = new JMenuItem("Panel de Administración");
        
        menuItemAdmin.addActionListener(e -> abrirPanelAdmin());
        menuAdmin.add(menuItemAdmin);
        menuBar.add(menuAdmin);
        setJMenuBar(menuBar);

        // Panel principal
        setLayout(new BorderLayout());
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // Panel de reservas
        JPanel panelReservas = new JPanel(new BorderLayout());
        JPanel panelFormulario = new JPanel(new GridLayout(6, 2, 10, 10)); // 6 filas, 2 columnas

        panelFormulario.add(new JLabel("Cliente:"));
        comboClientes = new JComboBox<>();
        panelFormulario.add(comboClientes);

        btnNuevoCliente = new JButton("Nuevo Cliente");
        btnNuevoCliente.addActionListener(this::mostrarFormularioNuevoCliente);
        panelFormulario.add(new JLabel()); 
        panelFormulario.add(btnNuevoCliente);

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

        panelReservas.add(panelFormulario, BorderLayout.NORTH);
        
        // Agregar formulario de nuevo cliente (inicialmente oculto)
        panelNuevoCliente = new JPanel(new GridLayout(5, 2, 10, 10));
        panelNuevoCliente.setBorder(BorderFactory.createTitledBorder("Nuevo Cliente"));
        
        panelNuevoCliente.add(new JLabel("Nombre:"));
        txtNombre = new JTextField();
        panelNuevoCliente.add(txtNombre);
        
        panelNuevoCliente.add(new JLabel("Apellido:"));
        txtApellido = new JTextField();
        panelNuevoCliente.add(txtApellido);
        
        panelNuevoCliente.add(new JLabel("Email:"));
        txtEmail = new JTextField();
        panelNuevoCliente.add(txtEmail);
        
        panelNuevoCliente.add(new JLabel("Teléfono:"));
        txtTelefono = new JTextField();
        panelNuevoCliente.add(txtTelefono);
        
        JButton btnGuardarCliente = new JButton("Guardar Cliente");
        btnGuardarCliente.addActionListener(this::guardarNuevoCliente);
        panelNuevoCliente.add(new JLabel());
        panelNuevoCliente.add(btnGuardarCliente);
        
        panelNuevoCliente.setVisible(false);
        panelReservas.add(panelNuevoCliente, BorderLayout.CENTER);
        
        tabbedPane.addTab("Reservar Cita", panelReservas);
        add(tabbedPane, BorderLayout.CENTER);
    }

    private void cargarDatosIniciales() {
        // Cargar clientes
        cargarClientes();

        // Cargar servicios
        cargarServicios();
        
        // Configurar renderers personalizados para mostrar los nombres
        comboClientes.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Cliente) {
                    Cliente cliente = (Cliente) value;
                    setText(cliente.getNombre() + " " + cliente.getApellido());
                }
                return this;
            }
        });
        
        comboServicios.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Servicio) {
                    Servicio servicio = (Servicio) value;
                    setText(servicio.getNombre());
                }
                return this;
            }
        });
    }
    
    private void cargarClientes() {
        comboClientes.removeAllItems();
        List<Cliente> clientes = clienteService.listarTodosLosClientes();
        
        if (clientes.isEmpty()) {
            // Si no hay clientes, agregar un cliente de ejemplo
            comboClientes.addItem(new Cliente(null, "Seleccione o cree", "un cliente", "", "", new ArrayList<>()));
        } else {
            for (Cliente cliente : clientes) {
                comboClientes.addItem(cliente);
            }
        }
    }
    
    private void cargarServicios() {
        comboServicios.removeAllItems();
        List<Servicio> servicios = servicioBarberiaService.listarTodosLosServicios();
        
        if (servicios.isEmpty()) {
            // Si no hay servicios, agregar un servicio de ejemplo
            comboServicios.addItem(new Servicio(null, "No hay servicios disponibles", "Utilice el panel de administración para agregar servicios", 0, new java.math.BigDecimal("0.00")));
        } else {
            for (Servicio servicio : servicios) {
                comboServicios.addItem(servicio);
            }
        }
    }

    private void accionSolicitarReserva(ActionEvent e) {
        try {
            Cliente clienteSeleccionado = (Cliente) comboClientes.getSelectedItem();
            Servicio servicioSeleccionado = (Servicio) comboServicios.getSelectedItem();
            
            // Validar que hay un cliente y servicio válido seleccionado
            if (clienteSeleccionado == null || clienteSeleccionado.getIdCliente() == null) {
                JOptionPane.showMessageDialog(this, "Por favor, seleccione un cliente válido.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (servicioSeleccionado == null || servicioSeleccionado.getIdServicio() == null) {
                JOptionPane.showMessageDialog(this, "Por favor, seleccione un servicio válido.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            LocalDate fecha = LocalDate.parse(txtFecha.getText()); // Añadir validación de formato
            LocalTime hora = LocalTime.parse(txtHora.getText());   // Añadir validación de formato
            LocalDateTime fechaHora = LocalDateTime.of(fecha, hora);

            // Llamar al servicio para solicitar la reserva
            citaService.solicitarReserva(clienteSeleccionado.getIdCliente(), servicioSeleccionado.getIdServicio(), fechaHora);

            JOptionPane.showMessageDialog(this, "Solicitud de reserva enviada exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            // Limpiar formulario
            txtFecha.setText("");
            txtHora.setText("");
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Formato de fecha u hora incorrecto. Use YYYY-MM-DD y HH:MM.", "Error de Formato", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al solicitar la reserva: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace(); // Para depuración
        }
    }
    
    private void mostrarFormularioNuevoCliente(ActionEvent e) {
        panelNuevoCliente.setVisible(true);
        pack(); // Ajustar tamaño de la ventana
    }
    
    private void guardarNuevoCliente(ActionEvent e) {
        try {
            String nombre = txtNombre.getText().trim();
            String apellido = txtApellido.getText().trim();
            String email = txtEmail.getText().trim();
            String telefono = txtTelefono.getText().trim();
            
            // Validar campos obligatorios
            if (nombre.isEmpty() || email.isEmpty()) {
                JOptionPane.showMessageDialog(this, "El nombre y el email son obligatorios", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Crear y guardar el cliente
            Cliente cliente = new Cliente();
            cliente.setNombre(nombre);
            cliente.setApellido(apellido);
            cliente.setCorreoElectronico(email);
            cliente.setTelefono(telefono);
            
            Cliente clienteGuardado = clienteService.guardarCliente(cliente);
            
            // Limpiar formulario y ocultarlo
            txtNombre.setText("");
            txtApellido.setText("");
            txtEmail.setText("");
            txtTelefono.setText("");
            panelNuevoCliente.setVisible(false);
            
            // Recargar la lista de clientes y seleccionar el nuevo cliente
            cargarClientes();
            
            // Seleccionar el nuevo cliente en el combobox
            for (int i = 0; i < comboClientes.getItemCount(); i++) {
                Cliente c = comboClientes.getItemAt(i);
                if (c.getIdCliente() != null && c.getIdCliente().equals(clienteGuardado.getIdCliente())) {
                    comboClientes.setSelectedIndex(i);
                    break;
                }
            }
            
            pack(); // Ajustar tamaño de la ventana
            
            JOptionPane.showMessageDialog(this, "Cliente guardado exitosamente", "Éxito", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al guardar el cliente: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
    
    private void abrirPanelAdmin() {
        SwingUtilities.invokeLater(() -> {
            AdminPanelSwing adminPanel = new AdminPanelSwing(clienteService, servicioBarberiaService);
            adminPanel.setVisible(true);
            
            // Agregar listener para actualizar datos cuando se cierre el panel admin
            adminPanel.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosed(java.awt.event.WindowEvent windowEvent) {
                    cargarDatosIniciales(); // Actualizar datos al cerrar panel de admin
                }
            });
        });
    }
}