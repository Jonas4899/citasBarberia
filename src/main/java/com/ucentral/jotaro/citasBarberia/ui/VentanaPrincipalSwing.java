package com.ucentral.jotaro.citasBarberia.ui;

import com.ucentral.jotaro.citasBarberia.entity.Cliente;
import com.ucentral.jotaro.citasBarberia.entity.Servicio;
import com.ucentral.jotaro.citasBarberia.service.CitaService;
import com.ucentral.jotaro.citasBarberia.service.ClienteService;
import com.ucentral.jotaro.citasBarberia.service.ServicioService;
import com.ucentral.jotaro.citasBarberia.listener.EstadisticasListener;
import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class VentanaPrincipalSwing extends JFrame {
    private final CitaService citaService;
    private final ClienteService clienteService;
    private final ServicioService servicioBarberiaService;
    private final EstadisticasListener estadisticasListener;

    private JComboBox<Cliente> comboClientes;
    private JComboBox<Servicio> comboServicios;
    private JDateChooser dateChooser;
    private JSpinner timeSpinner;
    private JButton btnSolicitarReserva;
    private JButton btnNuevoCliente;

    private JDialog nuevoClienteDialog;
    private JTextField txtNombreNuevoCliente;
    private JTextField txtApellidoNuevoCliente;
    private JTextField txtEmailNuevoCliente;
    private JTextField txtTelefonoNuevoCliente;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE; // YYYY-MM-DD
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_TIME; // HH:MM o HH:MM:SS

    public VentanaPrincipalSwing(CitaService citaService, ClienteService clienteService, 
                               ServicioService servicioBarberiaService, EstadisticasListener estadisticasListener) {
        super("Gestión de Citas Barbería");
        this.citaService = citaService;
        this.clienteService = clienteService;
        this.servicioBarberiaService = servicioBarberiaService;
        this.estadisticasListener = estadisticasListener;

        initComponents();
        cargarDatosIniciales();
        pack();
        setLocationRelativeTo(null);
    }

    private void initComponents() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // --- Menú ---
        JMenuBar menuBar = new JMenuBar();
        JMenu menuAdmin = new JMenu("Administración");
        JMenuItem menuItemAdmin = new JMenuItem("Panel de Administración");
        menuItemAdmin.addActionListener(e -> abrirPanelAdmin());
        menuAdmin.add(menuItemAdmin);
        menuBar.add(menuAdmin);
        setJMenuBar(menuBar);

        // --- Panel Principal de Reservas ---
        JPanel panelContenedorReservas = new JPanel(new BorderLayout(10,10));
        panelContenedorReservas.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15)); // Padding general

        // --- Formulario de Reservas (Usando GridBagLayout) ---
        JPanel panelFormularioReservas = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // Espaciado entre componentes
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Fila Cliente
        gbc.gridx = 0;
        gbc.gridy = 0;
        panelFormularioReservas.add(new JLabel("Cliente:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        comboClientes = new JComboBox<>();
        panelFormularioReservas.add(comboClientes, gbc);
        gbc.weightx = 0;

        gbc.gridx = 2;
        btnNuevoCliente = new JButton("Nuevo Cliente");
        btnNuevoCliente.setToolTipText("Registrar un nuevo cliente");
        btnNuevoCliente.addActionListener(this::mostrarDialogoNuevoCliente);
        panelFormularioReservas.add(btnNuevoCliente, gbc);

        // Fila Servicio
        gbc.gridx = 0;
        gbc.gridy = 1;
        panelFormularioReservas.add(new JLabel("Servicio:"), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        comboServicios = new JComboBox<>();
        panelFormularioReservas.add(comboServicios, gbc);
        gbc.gridwidth = 1;

        // Fila Fecha
        gbc.gridx = 0;
        gbc.gridy = 2;
        panelFormularioReservas.add(new JLabel("Fecha:"), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        dateChooser = new JDateChooser();
        dateChooser.setDateFormatString("yyyy-MM-dd");
        dateChooser.setDate(new Date());
        panelFormularioReservas.add(dateChooser, gbc);
        gbc.gridwidth = 1;

        // Fila Hora
        gbc.gridx = 0;
        gbc.gridy = 3;
        panelFormularioReservas.add(new JLabel("Hora:"), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        
        // Configurar spinner para hora
        SpinnerDateModel timeModel = new SpinnerDateModel();
        timeSpinner = new JSpinner(timeModel);
        
        // Formato del spinner de tiempo HH:mm
        JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(timeSpinner, "HH:mm");
        timeSpinner.setEditor(timeEditor);
        
        // Establecer hora predeterminada a las 12:00 PM
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 12);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        timeSpinner.setValue(calendar.getTime());
        
        panelFormularioReservas.add(timeSpinner, gbc);
        gbc.gridwidth = 1;

        // Panel para el botón de solicitar reserva
        JPanel panelBotonSolicitar = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnSolicitarReserva = new JButton("Solicitar Reserva");
        panelBotonSolicitar.add(btnSolicitarReserva);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 3;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        panelFormularioReservas.add(panelBotonSolicitar, gbc);

        btnSolicitarReserva.addActionListener(this::accionSolicitarReserva);

        panelContenedorReservas.add(panelFormularioReservas, BorderLayout.NORTH);
        add(panelContenedorReservas, BorderLayout.CENTER);

        crearDialogoNuevoCliente();
    }


    private void cargarDatosIniciales() {
        cargarClientes();
        cargarServicios();

        comboClientes.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Cliente) {
                    Cliente cliente = (Cliente) value;
                    setText(cliente.getNombre() + " " + (cliente.getApellido() != null ? cliente.getApellido() : ""));
                } else if (value == null && list.getModel().getSize() == 0) { // Placeholder
                    setText("Cargue o cree un cliente");
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
                } else if (value == null && list.getModel().getSize() == 0) { // Placeholder
                    setText("No hay servicios disponibles");
                }
                return this;
            }
        });
    }

    private void cargarClientes() {
        List<Cliente> clientes = clienteService.listarTodosLosClientes();
        comboClientes.removeAllItems();
        if (clientes.isEmpty()) {
        } else {
            for (Cliente cliente : clientes) {
                comboClientes.addItem(cliente);
            }
        }
    }

    private void cargarServicios() {
        List<Servicio> servicios = servicioBarberiaService.listarTodosLosServicios();
        comboServicios.removeAllItems();
        if (servicios.isEmpty()) {
            // comboServicios.addItem(new Servicio(null, "No hay servicios (use Admin)", "", 0, BigDecimal.ZERO));
            // comboServicios.setEnabled(false);
        } else {
            for (Servicio servicio : servicios) {
                comboServicios.addItem(servicio);
            }
            // comboServicios.setEnabled(true);
        }
    }

    private void crearDialogoNuevoCliente() {
        nuevoClienteDialog = new JDialog(this, "Registrar Nuevo Cliente", true); // true para modal
        nuevoClienteDialog.setLayout(new BorderLayout(10,10));
        nuevoClienteDialog.getRootPane().setBorder(BorderFactory.createEmptyBorder(10,10,10,10));


        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Nombre
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Nombre:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        txtNombreNuevoCliente = new JTextField(20);
        formPanel.add(txtNombreNuevoCliente, gbc);
        gbc.weightx = 0;

        // Apellido
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Apellido:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        txtApellidoNuevoCliente = new JTextField(20);
        formPanel.add(txtApellidoNuevoCliente, gbc);
        gbc.weightx = 0;

        // Email
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        txtEmailNuevoCliente = new JTextField(20);
        formPanel.add(txtEmailNuevoCliente, gbc);
        gbc.weightx = 0;

        // Teléfono
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Teléfono:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        txtTelefonoNuevoCliente = new JTextField(15);
        formPanel.add(txtTelefonoNuevoCliente, gbc);
        gbc.weightx = 0;

        nuevoClienteDialog.add(formPanel, BorderLayout.CENTER);

        // Botones del diálogo
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnGuardarCliente = new JButton("Guardar");
        btnGuardarCliente.addActionListener(this::guardarNuevoClienteDesdeDialogo);
        JButton btnCancelarCliente = new JButton("Cancelar");
        btnCancelarCliente.addActionListener(e -> nuevoClienteDialog.setVisible(false));

        buttonPanel.add(btnGuardarCliente);
        buttonPanel.add(btnCancelarCliente);
        nuevoClienteDialog.add(buttonPanel, BorderLayout.SOUTH);

        nuevoClienteDialog.pack();
        nuevoClienteDialog.setLocationRelativeTo(this);
    }

    private void mostrarDialogoNuevoCliente(ActionEvent e) {
        // Limpiar campos antes de mostrar
        txtNombreNuevoCliente.setText("");
        txtApellidoNuevoCliente.setText("");
        txtEmailNuevoCliente.setText("");
        txtTelefonoNuevoCliente.setText("");
        nuevoClienteDialog.setVisible(true);
    }

    private void guardarNuevoClienteDesdeDialogo(ActionEvent e) {
        try {
            String nombre = txtNombreNuevoCliente.getText().trim();
            String apellido = txtApellidoNuevoCliente.getText().trim();
            String email = txtEmailNuevoCliente.getText().trim();
            String telefono = txtTelefonoNuevoCliente.getText().trim();

            if (nombre.isEmpty() || email.isEmpty()) {
                JOptionPane.showMessageDialog(nuevoClienteDialog, "El nombre y el email son obligatorios.", "Error de Validación", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Cliente nuevoCliente = new Cliente();
            nuevoCliente.setNombre(nombre);
            nuevoCliente.setApellido(apellido);
            nuevoCliente.setCorreoElectronico(email);
            nuevoCliente.setTelefono(telefono);

            Cliente clienteGuardado = clienteService.guardarCliente(nuevoCliente);
            nuevoClienteDialog.setVisible(false);
            cargarClientes(); // Recargar la lista de clientes

            // Seleccionar el nuevo cliente en el combobox
            for (int i = 0; i < comboClientes.getItemCount(); i++) {
                Cliente c = comboClientes.getItemAt(i);
                if (c.getIdCliente() != null && c.getIdCliente().equals(clienteGuardado.getIdCliente())) {
                    comboClientes.setSelectedIndex(i);
                    break;
                }
            }
            JOptionPane.showMessageDialog(this, "Cliente guardado exitosamente.", "Cliente Guardado", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(nuevoClienteDialog, "Error al guardar el cliente: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void accionSolicitarReserva(ActionEvent e) {
        Cliente clienteSeleccionado = (Cliente) comboClientes.getSelectedItem();
        Servicio servicioSeleccionado = (Servicio) comboServicios.getSelectedItem();

        if (clienteSeleccionado == null || clienteSeleccionado.getIdCliente() == null) {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione un cliente válido.", "Error de Selección", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (servicioSeleccionado == null || servicioSeleccionado.getIdServicio() == null) {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione un servicio válido.", "Error de Selección", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            // Obtener fecha del JDateChooser
            Date selectedDate = dateChooser.getDate();
            if (selectedDate == null) {
                JOptionPane.showMessageDialog(this, "Por favor, seleccione una fecha válida.", "Error de Selección", JOptionPane.ERROR_MESSAGE);
                return;
            }
            LocalDate fecha = selectedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            
            // Obtener hora del JSpinner
            Date selectedTime = (Date) timeSpinner.getValue();
            LocalTime hora = selectedTime.toInstant().atZone(ZoneId.systemDefault()).toLocalTime();
            
            LocalDateTime fechaHora = LocalDateTime.of(fecha, hora);

            citaService.solicitarReserva(clienteSeleccionado.getIdCliente(), servicioSeleccionado.getIdServicio(), fechaHora);
            JOptionPane.showMessageDialog(this, "Solicitud de reserva enviada exitosamente.", "Reserva Solicitada", JOptionPane.INFORMATION_MESSAGE);
            
            // Resetear la fecha y hora a valores por defecto en lugar de limpiarlos
            dateChooser.setDate(new Date());
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, 12);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            timeSpinner.setValue(calendar.getTime());
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al solicitar la reserva: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void abrirPanelAdmin() {
        // Asegurarse de que solo haya una instancia del panel de admin o manejarlo adecuadamente
        AdminPanelSwing adminPanel = new AdminPanelSwing(this.clienteService, this.servicioBarberiaService, 
                                                        citaService.getReservaRepository(), this.estadisticasListener, 
                                                        this.citaService);
        adminPanel.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                cargarDatosIniciales(); // Recargar datos cuando se cierre el panel de admin
            }
        });
        adminPanel.setVisible(true);
    }
}