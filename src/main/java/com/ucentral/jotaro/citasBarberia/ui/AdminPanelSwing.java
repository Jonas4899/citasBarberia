package com.ucentral.jotaro.citasBarberia.ui;

import com.ucentral.jotaro.citasBarberia.entity.Cliente;
import com.ucentral.jotaro.citasBarberia.entity.Servicio;
import com.ucentral.jotaro.citasBarberia.entity.Reserva;
import com.ucentral.jotaro.citasBarberia.entity.EstadoReserva;
import com.ucentral.jotaro.citasBarberia.service.ClienteService;
import com.ucentral.jotaro.citasBarberia.service.ServicioService;
import com.ucentral.jotaro.citasBarberia.repository.ReservaRepository;
import com.ucentral.jotaro.citasBarberia.listener.EstadisticasListener;
import com.ucentral.jotaro.citasBarberia.service.CitaService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class AdminPanelSwing extends JFrame {
    private final ClienteService clienteService;
    private final ServicioService servicioService;
    private final ReservaRepository reservaRepository;
    private final EstadisticasListener estadisticasListener;
    private final CitaService citaService;

    // UI Components
    private JTabbedPane tabbedPane;

    // Servicios components
    private JTable serviciosTable;
    private DefaultTableModel serviciosTableModel;
    private JTextField txtNombreServicio, txtDescripcionServicio, txtDuracionServicio, txtPrecioServicio;
    private JButton btnGuardarServicio, btnEliminarServicio, btnLimpiarServicio;
    private Long currentServicioId = null; // Para saber si estamos editando

    // Clientes components
    private JTable clientesTable;
    private DefaultTableModel clientesTableModel;
    private JTextField txtNombreCliente, txtApellidoCliente, txtEmailCliente, txtTelefonoCliente;
    private JButton btnGuardarCliente, btnEliminarCliente, btnLimpiarCliente;
    private Long currentClienteId = null; // Para saber si estamos editando

    // Reservas components
    private JTable reservasTable;
    private DefaultTableModel reservasTableModel;
    private JComboBox<EstadoReserva> comboEstadoReserva;
    private JButton btnActualizarEstado, btnRefrescarReservas;

    // Estadísticas components
    private JPanel estadisticasPanel;
    private JLabel lblTotalReservas;
    private JTable estadisticasServicioTable;
    private DefaultTableModel estadisticasServicioTableModel;
    private JTable estadisticasEstadoTable;
    private DefaultTableModel estadisticasEstadoTableModel;
    private JTable estadisticasFechaTable;
    private DefaultTableModel estadisticasFechaTableModel;
    private JButton btnRefrescarEstadisticas;

    public AdminPanelSwing(ClienteService clienteService, ServicioService servicioService, 
                         ReservaRepository reservaRepository, EstadisticasListener estadisticasListener) {
        super("Panel de Administración - Barbería");
        this.clienteService = clienteService;
        this.servicioService = servicioService;
        this.reservaRepository = reservaRepository;
        this.estadisticasListener = estadisticasListener;
        this.citaService = null;

        initComponents();
        loadInitialData();
        pack(); // Ajustar tamaño
        setLocationRelativeTo(null); // Centrar
    }

    public AdminPanelSwing(ClienteService clienteService, ServicioService servicioService, 
                         ReservaRepository reservaRepository, EstadisticasListener estadisticasListener,
                         CitaService citaService) {
        super("Panel de Administración - Barbería");
        this.clienteService = clienteService;
        this.servicioService = servicioService;
        this.reservaRepository = reservaRepository;
        this.estadisticasListener = estadisticasListener;
        this.citaService = citaService;

        initComponents();
        loadInitialData();
        pack(); // Ajustar tamaño
        setLocationRelativeTo(null); // Centrar
    }

    private void initComponents() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // No cerrar toda la app
        setLayout(new BorderLayout(10, 10)); // Espaciado general
        getRootPane().setBorder(BorderFactory.createEmptyBorder(10,10,10,10));


        tabbedPane = new JTabbedPane();

        initServiciosPanel();
        initClientesPanel();
        initReservasPanel();
        initEstadisticasPanel();

        tabbedPane.addTab("Gestión de Servicios", createScrollablePanel(serviciosPanel));
        tabbedPane.addTab("Gestión de Clientes", createScrollablePanel(clientesPanel));
        tabbedPane.addTab("Citas Agendadas", createScrollablePanel(reservasPanel));
        tabbedPane.addTab("Estadísticas", createScrollablePanel(estadisticasPanel));

        add(tabbedPane, BorderLayout.CENTER);

        // No es necesario el botón de actualizar si las tablas se refrescan tras cada operación
        // Si se desea, se puede añadir.
    }

    // Helper para hacer paneles scrolleables si el contenido es muy grande
    private JScrollPane createScrollablePanel(JPanel panel) {
        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder()); // Quitar borde del scrollpane si el panel ya tiene
        return scrollPane;
    }


    private JPanel serviciosPanel; // Declarar como miembro de la clase
    private void initServiciosPanel() {
        serviciosPanel = new JPanel(new BorderLayout(10, 10));
        serviciosPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        // --- Tabla de Servicios ---
        String[] columnNamesServicios = {"ID", "Nombre", "Descripción", "Duración (min)", "Precio"};
        serviciosTableModel = new DefaultTableModel(columnNamesServicios, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        serviciosTable = new JTable(serviciosTableModel);
        serviciosTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        serviciosTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    cargarServicioSeleccionadoParaEdicion();
                }
            }
        });
        serviciosPanel.add(new JScrollPane(serviciosTable), BorderLayout.CENTER);

        // --- Formulario de Servicios ---
        JPanel formServiciosPanel = new JPanel(new GridBagLayout());
        formServiciosPanel.setBorder(BorderFactory.createTitledBorder("Detalles del Servicio"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int y = 0;
        // Nombre
        gbc.gridx = 0; gbc.gridy = y;
        formServiciosPanel.add(new JLabel("Nombre:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        txtNombreServicio = new JTextField(25);
        formServiciosPanel.add(txtNombreServicio, gbc);
        gbc.weightx = 0;

        // Descripción
        y++; gbc.gridx = 0; gbc.gridy = y;
        formServiciosPanel.add(new JLabel("Descripción:"), gbc);
        gbc.gridx = 1;
        txtDescripcionServicio = new JTextField(25);
        formServiciosPanel.add(txtDescripcionServicio, gbc);

        // Duración
        y++; gbc.gridx = 0; gbc.gridy = y;
        formServiciosPanel.add(new JLabel("Duración (min):"), gbc);
        gbc.gridx = 1;
        txtDuracionServicio = new JTextField(5);
        formServiciosPanel.add(txtDuracionServicio, gbc);

        // Precio
        y++; gbc.gridx = 0; gbc.gridy = y;
        formServiciosPanel.add(new JLabel("Precio:"), gbc);
        gbc.gridx = 1;
        txtPrecioServicio = new JTextField(10);
        formServiciosPanel.add(txtPrecioServicio, gbc);

        // Botones
        y++; gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER; gbc.fill = GridBagConstraints.NONE;
        JPanel botonesServicioPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        btnGuardarServicio = new JButton("Guardar");
        btnGuardarServicio.addActionListener(this::accionGuardarServicio);
        btnEliminarServicio = new JButton("Eliminar");
        btnEliminarServicio.addActionListener(this::accionEliminarServicio);
        btnEliminarServicio.setEnabled(false); // Habilitar solo cuando se selecciona
        btnLimpiarServicio = new JButton("Limpiar Campos");
        btnLimpiarServicio.addActionListener(e -> limpiarFormularioServicio());

        botonesServicioPanel.add(btnGuardarServicio);
        botonesServicioPanel.add(btnEliminarServicio);
        botonesServicioPanel.add(btnLimpiarServicio);
        formServiciosPanel.add(botonesServicioPanel, gbc);

        serviciosPanel.add(formServiciosPanel, BorderLayout.SOUTH);
    }

    private JPanel clientesPanel; // Declarar como miembro de la clase
    private void initClientesPanel() {
        clientesPanel = new JPanel(new BorderLayout(10, 10));
        clientesPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        // --- Tabla de Clientes ---
        String[] columnNamesClientes = {"ID", "Nombre", "Apellido", "Email", "Teléfono"};
        clientesTableModel = new DefaultTableModel(columnNamesClientes, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        clientesTable = new JTable(clientesTableModel);
        clientesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        clientesTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    cargarClienteSeleccionadoParaEdicion();
                }
            }
        });
        clientesPanel.add(new JScrollPane(clientesTable), BorderLayout.CENTER);

        // --- Formulario de Clientes ---
        JPanel formClientesPanel = new JPanel(new GridBagLayout());
        formClientesPanel.setBorder(BorderFactory.createTitledBorder("Detalles del Cliente"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int y = 0;
        // Nombre
        gbc.gridx = 0; gbc.gridy = y;
        formClientesPanel.add(new JLabel("Nombre:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        txtNombreCliente = new JTextField(25);
        formClientesPanel.add(txtNombreCliente, gbc);
        gbc.weightx = 0;

        // Apellido
        y++; gbc.gridx = 0; gbc.gridy = y;
        formClientesPanel.add(new JLabel("Apellido:"), gbc);
        gbc.gridx = 1;
        txtApellidoCliente = new JTextField(25);
        formClientesPanel.add(txtApellidoCliente, gbc);

        // Email
        y++; gbc.gridx = 0; gbc.gridy = y;
        formClientesPanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        txtEmailCliente = new JTextField(25);
        formClientesPanel.add(txtEmailCliente, gbc);

        // Teléfono
        y++; gbc.gridx = 0; gbc.gridy = y;
        formClientesPanel.add(new JLabel("Teléfono:"), gbc);
        gbc.gridx = 1;
        txtTelefonoCliente = new JTextField(15);
        formClientesPanel.add(txtTelefonoCliente, gbc);

        // Botones
        y++; gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER; gbc.fill = GridBagConstraints.NONE;
        JPanel botonesClientePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        btnGuardarCliente = new JButton("Guardar");
        btnGuardarCliente.addActionListener(this::accionGuardarCliente);
        btnEliminarCliente = new JButton("Eliminar");
        btnEliminarCliente.addActionListener(this::accionEliminarCliente);
        btnEliminarCliente.setEnabled(false); // Habilitar solo cuando se selecciona
        btnLimpiarCliente = new JButton("Limpiar Campos");
        btnLimpiarCliente.addActionListener(e -> limpiarFormularioCliente());

        botonesClientePanel.add(btnGuardarCliente);
        botonesClientePanel.add(btnEliminarCliente);
        botonesClientePanel.add(btnLimpiarCliente);
        formClientesPanel.add(botonesClientePanel, gbc);

        clientesPanel.add(formClientesPanel, BorderLayout.SOUTH);
    }

    private JPanel reservasPanel;
    private void initReservasPanel() {
        reservasPanel = new JPanel(new BorderLayout(10, 10));
        reservasPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        // --- Tabla de Reservas ---
        String[] columnNamesReservas = {"ID", "Cliente", "Servicio", "Fecha y Hora", "Estado", "Fecha Creación"};
        reservasTableModel = new DefaultTableModel(columnNamesReservas, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        reservasTable = new JTable(reservasTableModel);
        reservasTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        reservasPanel.add(new JScrollPane(reservasTable), BorderLayout.CENTER);

        // --- Panel de Acciones ---
        JPanel accionesPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        accionesPanel.setBorder(BorderFactory.createTitledBorder("Acciones"));
        
        // Combo para cambiar estado
        accionesPanel.add(new JLabel("Cambiar Estado:"));
        comboEstadoReserva = new JComboBox<>(EstadoReserva.values());
        accionesPanel.add(comboEstadoReserva);
        
        // Botón para actualizar estado
        btnActualizarEstado = new JButton("Actualizar Estado");
        btnActualizarEstado.addActionListener(this::accionActualizarEstadoReserva);
        btnActualizarEstado.setEnabled(false); // Se habilita cuando hay selección
        accionesPanel.add(btnActualizarEstado);
        
        // Botón para refrescar lista
        btnRefrescarReservas = new JButton("Refrescar Lista");
        btnRefrescarReservas.addActionListener(e -> loadReservas());
        accionesPanel.add(btnRefrescarReservas);
        
        reservasPanel.add(accionesPanel, BorderLayout.SOUTH);
        
        // Añadir listener para selección en tabla
        reservasTable.getSelectionModel().addListSelectionListener(e -> {
            int selectedRow = reservasTable.getSelectedRow();
            btnActualizarEstado.setEnabled(selectedRow != -1);
            if (selectedRow != -1) {
                String estadoActual = (String) reservasTableModel.getValueAt(selectedRow, 4);
                for (EstadoReserva estado : EstadoReserva.values()) {
                    if (estado.name().equals(estadoActual)) {
                        comboEstadoReserva.setSelectedItem(estado);
                        break;
                    }
                }
            }
        });
    }

    private void initEstadisticasPanel() {
        estadisticasPanel = new JPanel(new BorderLayout(10, 10));
        estadisticasPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        
        // Panel superior con total de reservas
        JPanel totalReservasPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        totalReservasPanel.setBorder(BorderFactory.createTitledBorder("Total de Reservas"));
        lblTotalReservas = new JLabel("Total de reservas: 0");
        lblTotalReservas.setFont(new Font("Arial", Font.BOLD, 16));
        totalReservasPanel.add(lblTotalReservas);
        
        // Panel central con tablas de estadísticas
        JPanel tablasEstadisticasPanel = new JPanel(new GridLayout(1, 3, 10, 10));
        
        // Tabla de reservas por servicio
        JPanel serviciosEstadisticasPanel = new JPanel(new BorderLayout());
        serviciosEstadisticasPanel.setBorder(BorderFactory.createTitledBorder("Reservas por Servicio"));
        String[] columnNamesServiciosEstadisticas = {"Servicio", "Cantidad"};
        estadisticasServicioTableModel = new DefaultTableModel(columnNamesServiciosEstadisticas, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        estadisticasServicioTable = new JTable(estadisticasServicioTableModel);
        serviciosEstadisticasPanel.add(new JScrollPane(estadisticasServicioTable), BorderLayout.CENTER);
        
        // Tabla de reservas por estado
        JPanel estadosEstadisticasPanel = new JPanel(new BorderLayout());
        estadosEstadisticasPanel.setBorder(BorderFactory.createTitledBorder("Reservas por Estado"));
        String[] columnNamesEstadosEstadisticas = {"Estado", "Cantidad"};
        estadisticasEstadoTableModel = new DefaultTableModel(columnNamesEstadosEstadisticas, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        estadisticasEstadoTable = new JTable(estadisticasEstadoTableModel);
        estadosEstadisticasPanel.add(new JScrollPane(estadisticasEstadoTable), BorderLayout.CENTER);
        
        // Tabla de reservas por fecha
        JPanel fechasEstadisticasPanel = new JPanel(new BorderLayout());
        fechasEstadisticasPanel.setBorder(BorderFactory.createTitledBorder("Reservas por Fecha"));
        String[] columnNamesFechasEstadisticas = {"Fecha", "Cantidad"};
        estadisticasFechaTableModel = new DefaultTableModel(columnNamesFechasEstadisticas, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        estadisticasFechaTable = new JTable(estadisticasFechaTableModel);
        fechasEstadisticasPanel.add(new JScrollPane(estadisticasFechaTable), BorderLayout.CENTER);
        
        tablasEstadisticasPanel.add(serviciosEstadisticasPanel);
        tablasEstadisticasPanel.add(estadosEstadisticasPanel);
        tablasEstadisticasPanel.add(fechasEstadisticasPanel);
        
        // Panel de acciones
        JPanel accionesEstadisticasPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnRefrescarEstadisticas = new JButton("Refrescar Estadísticas");
        btnRefrescarEstadisticas.addActionListener(e -> cargarEstadisticas());
        accionesEstadisticasPanel.add(btnRefrescarEstadisticas);
        
        // Añadir todos los paneles al panel principal
        estadisticasPanel.add(totalReservasPanel, BorderLayout.NORTH);
        estadisticasPanel.add(tablasEstadisticasPanel, BorderLayout.CENTER);
        estadisticasPanel.add(accionesEstadisticasPanel, BorderLayout.SOUTH);
    }

    private void loadInitialData() {
        loadServicios();
        loadClientes();
        loadReservas();
        cargarEstadisticas();
    }

    private void loadServicios() {
        serviciosTableModel.setRowCount(0); // Limpiar tabla
        List<Servicio> servicios = servicioService.listarTodosLosServicios();
        for (Servicio s : servicios) {
            serviciosTableModel.addRow(new Object[]{s.getIdServicio(), s.getNombre(), s.getDescripcion(), s.getDuracionEstimadaMinutos(), s.getPrecio()});
        }
    }
    private void loadClientes() {
        clientesTableModel.setRowCount(0); // Limpiar tabla
        List<Cliente> clientes = clienteService.listarTodosLosClientes();
        for (Cliente c : clientes) {
            clientesTableModel.addRow(new Object[]{c.getIdCliente(), c.getNombre(), c.getApellido(), c.getCorreoElectronico(), c.getTelefono()});
        }
    }

    private void limpiarFormularioServicio() {
        currentServicioId = null;
        txtNombreServicio.setText("");
        txtDescripcionServicio.setText("");
        txtDuracionServicio.setText("");
        txtPrecioServicio.setText("");
        btnEliminarServicio.setEnabled(false);
        serviciosTable.clearSelection();
        txtNombreServicio.requestFocus();
    }

    private void limpiarFormularioCliente() {
        currentClienteId = null;
        txtNombreCliente.setText("");
        txtApellidoCliente.setText("");
        txtEmailCliente.setText("");
        txtTelefonoCliente.setText("");
        btnEliminarCliente.setEnabled(false);
        clientesTable.clearSelection();
        txtNombreCliente.requestFocus();
    }

    private void cargarServicioSeleccionadoParaEdicion() {
        int selectedRow = serviciosTable.getSelectedRow();
        if (selectedRow != -1) {
            currentServicioId = (Long) serviciosTableModel.getValueAt(selectedRow, 0);
            servicioService.obtenerServicioPorId(currentServicioId).ifPresent(s -> {
                txtNombreServicio.setText(s.getNombre());
                txtDescripcionServicio.setText(s.getDescripcion());
                txtDuracionServicio.setText(s.getDuracionEstimadaMinutos() != null ? s.getDuracionEstimadaMinutos().toString() : "");
                txtPrecioServicio.setText(s.getPrecio() != null ? s.getPrecio().toPlainString() : "");
                btnEliminarServicio.setEnabled(true);
            });
        }
    }
    private void cargarClienteSeleccionadoParaEdicion() {
        int selectedRow = clientesTable.getSelectedRow();
        if (selectedRow != -1) {
            currentClienteId = (Long) clientesTableModel.getValueAt(selectedRow, 0);
            clienteService.obtenerClientePorId(currentClienteId).ifPresent(c -> {
                txtNombreCliente.setText(c.getNombre());
                txtApellidoCliente.setText(c.getApellido());
                txtEmailCliente.setText(c.getCorreoElectronico());
                txtTelefonoCliente.setText(c.getTelefono());
                btnEliminarCliente.setEnabled(true);
            });
        }
    }

    private void accionGuardarServicio(ActionEvent e) {
        String nombre = txtNombreServicio.getText().trim();
        if (nombre.isEmpty()) {
            JOptionPane.showMessageDialog(this, "El nombre del servicio es obligatorio.", "Error de Validación", JOptionPane.ERROR_MESSAGE);
            return;
        }
        Servicio servicio = (currentServicioId != null) ?
                servicioService.obtenerServicioPorId(currentServicioId).orElse(new Servicio()) :
                new Servicio();

        servicio.setNombre(nombre);
        servicio.setDescripcion(txtDescripcionServicio.getText().trim());
        try {
            String duracionStr = txtDuracionServicio.getText().trim();
            if (!duracionStr.isEmpty()) servicio.setDuracionEstimadaMinutos(Integer.parseInt(duracionStr)); else servicio.setDuracionEstimadaMinutos(null);

            String precioStr = txtPrecioServicio.getText().trim();
            if (!precioStr.isEmpty()) servicio.setPrecio(new BigDecimal(precioStr)); else servicio.setPrecio(null);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Duración debe ser un número entero y Precio un número decimal.", "Error de Formato", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            servicioService.guardarServicio(servicio);
            JOptionPane.showMessageDialog(this, "Servicio guardado exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            loadServicios();
            limpiarFormularioServicio();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al guardar el servicio: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void accionEliminarServicio(ActionEvent e) {
        if (currentServicioId == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un servicio de la tabla para eliminar.", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Está seguro de eliminar el servicio '" + txtNombreServicio.getText() + "'?",
                "Confirmar Eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                servicioService.eliminarServicio(currentServicioId);
                JOptionPane.showMessageDialog(this, "Servicio eliminado.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                loadServicios();
                limpiarFormularioServicio();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al eliminar servicio: " + ex.getMessage() + "\nAsegúrese que no tenga reservas asociadas.", "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

    private void accionGuardarCliente(ActionEvent e) {
        String nombre = txtNombreCliente.getText().trim();
        String email = txtEmailCliente.getText().trim();
        if (nombre.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nombre y Email del cliente son obligatorios.", "Error de Validación", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Cliente cliente = (currentClienteId != null) ?
                clienteService.obtenerClientePorId(currentClienteId).orElse(new Cliente()) :
                new Cliente();

        cliente.setNombre(nombre);
        cliente.setApellido(txtApellidoCliente.getText().trim());
        cliente.setCorreoElectronico(email);
        cliente.setTelefono(txtTelefonoCliente.getText().trim());

        try {
            clienteService.guardarCliente(cliente);
            JOptionPane.showMessageDialog(this, "Cliente guardado exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            loadClientes();
            limpiarFormularioCliente();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al guardar el cliente: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void accionEliminarCliente(ActionEvent e) {
        if (currentClienteId == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un cliente de la tabla para eliminar.", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Está seguro de eliminar el cliente '" + txtNombreCliente.getText() + "'?",
                "Confirmar Eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                clienteService.eliminarCliente(currentClienteId);
                JOptionPane.showMessageDialog(this, "Cliente eliminado.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                loadClientes();
                limpiarFormularioCliente();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al eliminar cliente: " + ex.getMessage() + "\nAsegúrese que no tenga reservas asociadas.", "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

    private void loadReservas() {
        reservasTableModel.setRowCount(0); // Limpiar tabla
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        
        List<Reserva> reservas = reservaRepository.findAllWithClienteAndServicio();
        for (Reserva r : reservas) {
            reservasTableModel.addRow(new Object[]{
                r.getIdReserva(),
                r.getCliente().getNombre() + " " + r.getCliente().getApellido(),
                r.getServicio().getNombre(),
                r.getFechaHoraInicio().format(formatter),
                r.getEstado().toString(),
                r.getFechaCreacion().format(formatter)
            });
        }
    }

    private void accionActualizarEstadoReserva(ActionEvent e) {
        int selectedRow = reservasTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione una reserva de la tabla.", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Long idReserva = (Long) reservasTableModel.getValueAt(selectedRow, 0);
        EstadoReserva nuevoEstado = (EstadoReserva) comboEstadoReserva.getSelectedItem();
        
        try {
            if (this.citaService != null) {
                citaService.actualizarEstadoReserva(idReserva, nuevoEstado);
            } else {
                reservaRepository.findById(idReserva).ifPresent(reserva -> {
                    reserva.setEstado(nuevoEstado);
                    reservaRepository.save(reserva);
                });
            }
            
            JOptionPane.showMessageDialog(this, "Estado de reserva actualizado.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            loadReservas();
            
            cargarEstadisticas();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al actualizar el estado: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void cargarEstadisticas() {
        // Limpiar tablas
        estadisticasServicioTableModel.setRowCount(0);
        estadisticasEstadoTableModel.setRowCount(0);
        estadisticasFechaTableModel.setRowCount(0);
        
        // Actualizar total de reservas
        int totalReservas = estadisticasListener.getTotalReservas();
        lblTotalReservas.setText("Total de reservas: " + totalReservas);
        
        // Cargar estadísticas por servicio
        Map<String, Integer> reservasPorServicio = estadisticasListener.getReservasPorServicio();
        reservasPorServicio.forEach((servicio, cantidad) -> {
            estadisticasServicioTableModel.addRow(new Object[]{servicio, cantidad});
        });
        
        // Cargar estadísticas por estado
        Map<String, Integer> reservasPorEstado = estadisticasListener.getReservasPorEstado();
        reservasPorEstado.forEach((estado, cantidad) -> {
            estadisticasEstadoTableModel.addRow(new Object[]{estado, cantidad});
        });
        
        // Cargar estadísticas por fecha
        Map<String, Integer> reservasPorFecha = estadisticasListener.getReservasPorFecha();
        reservasPorFecha.forEach((fecha, cantidad) -> {
            estadisticasFechaTableModel.addRow(new Object[]{fecha, cantidad});
        });
    }
}