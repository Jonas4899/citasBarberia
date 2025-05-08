package com.ucentral.jotaro.citasBarberia.ui;

import com.ucentral.jotaro.citasBarberia.entity.Cliente;
import com.ucentral.jotaro.citasBarberia.entity.Servicio;
import com.ucentral.jotaro.citasBarberia.service.ClienteService;
import com.ucentral.jotaro.citasBarberia.service.ServicioService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.math.BigDecimal;
import java.util.List;

public class AdminPanelSwing extends JFrame {
    private final ClienteService clienteService;
    private final ServicioService servicioService;

    // UI Components
    private JTabbedPane tabbedPane;
    private JPanel serviciosPanel;
    private JPanel clientesPanel;
    
    // Servicios components
    private JTable serviciosTable;
    private DefaultTableModel serviciosTableModel;
    private JTextField txtNombreServicio;
    private JTextField txtDescripcionServicio;
    private JTextField txtDuracionServicio;
    private JTextField txtPrecioServicio;
    
    // Clientes components
    private JTable clientesTable;
    private DefaultTableModel clientesTableModel;
    private JTextField txtNombreCliente;
    private JTextField txtApellidoCliente;
    private JTextField txtEmailCliente;
    private JTextField txtTelefonoCliente;

    public AdminPanelSwing(ClienteService clienteService, ServicioService servicioService) {
        super("Panel de Administración - Barbería");
        this.clienteService = clienteService;
        this.servicioService = servicioService;

        initComponents();
        loadInitialData();
    }

    private void initComponents() {
        // Main Layout
        setLayout(new BorderLayout());
        tabbedPane = new JTabbedPane();
        
        // Initialize panels
        initServiciosPanel();
        initClientesPanel();
        
        // Add panels to tabbed pane
        tabbedPane.addTab("Servicios", serviciosPanel);
        tabbedPane.addTab("Clientes", clientesPanel);
        
        add(tabbedPane, BorderLayout.CENTER);
        
        // Add a refresh button at the top
        JPanel toolbarPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnRefresh = new JButton("Actualizar Datos");
        btnRefresh.addActionListener(e -> loadInitialData());
        toolbarPanel.add(btnRefresh);
        
        add(toolbarPanel, BorderLayout.NORTH);
        
        // Set frame properties
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // DISPOSE_ON_CLOSE to not close the main application
    }
    
    private void initServiciosPanel() {
        serviciosPanel = new JPanel(new BorderLayout());
        
        // Table setup
        String[] columnNames = {"ID", "Nombre", "Descripción", "Duración (min)", "Precio"};
        serviciosTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table read-only
            }
        };
        serviciosTable = new JTable(serviciosTableModel);
        JScrollPane scrollPane = new JScrollPane(serviciosTable);
        serviciosPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Add double-click listener to load service for editing
        serviciosTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    int row = serviciosTable.getSelectedRow();
                    if (row != -1) {
                        Long idServicio = (Long) serviciosTableModel.getValueAt(row, 0);
                        cargarServicioParaEdicion(idServicio);
                    }
                }
            }
        });
        
        // Form panel
        JPanel formPanel = new JPanel(new GridLayout(6, 2, 5, 5));
        formPanel.setBorder(BorderFactory.createTitledBorder("Agregar/Editar Servicio"));
        
        formPanel.add(new JLabel("Nombre:"));
        txtNombreServicio = new JTextField();
        formPanel.add(txtNombreServicio);
        
        formPanel.add(new JLabel("Descripción:"));
        txtDescripcionServicio = new JTextField();
        formPanel.add(txtDescripcionServicio);
        
        formPanel.add(new JLabel("Duración (minutos):"));
        txtDuracionServicio = new JTextField();
        formPanel.add(txtDuracionServicio);
        
        formPanel.add(new JLabel("Precio:"));
        txtPrecioServicio = new JTextField();
        formPanel.add(txtPrecioServicio);
        
        JButton btnAgregarServicio = new JButton("Guardar Servicio");
        btnAgregarServicio.addActionListener(this::agregarServicio);
        formPanel.add(btnAgregarServicio);
        
        JButton btnEliminarServicio = new JButton("Eliminar Servicio");
        btnEliminarServicio.addActionListener(this::eliminarServicio);
        formPanel.add(btnEliminarServicio);
        
        serviciosPanel.add(formPanel, BorderLayout.SOUTH);
    }
    
    private void initClientesPanel() {
        clientesPanel = new JPanel(new BorderLayout());
        
        // Table setup
        String[] columnNames = {"ID", "Nombre", "Apellido", "Email", "Teléfono"};
        clientesTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table read-only
            }
        };
        clientesTable = new JTable(clientesTableModel);
        JScrollPane scrollPane = new JScrollPane(clientesTable);
        clientesPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Add double-click listener to load client for editing
        clientesTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    int row = clientesTable.getSelectedRow();
                    if (row != -1) {
                        Long idCliente = (Long) clientesTableModel.getValueAt(row, 0);
                        cargarClienteParaEdicion(idCliente);
                    }
                }
            }
        });
        
        // Form panel
        JPanel formPanel = new JPanel(new GridLayout(6, 2, 5, 5));
        formPanel.setBorder(BorderFactory.createTitledBorder("Agregar/Editar Cliente"));
        
        formPanel.add(new JLabel("Nombre:"));
        txtNombreCliente = new JTextField();
        formPanel.add(txtNombreCliente);
        
        formPanel.add(new JLabel("Apellido:"));
        txtApellidoCliente = new JTextField();
        formPanel.add(txtApellidoCliente);
        
        formPanel.add(new JLabel("Email:"));
        txtEmailCliente = new JTextField();
        formPanel.add(txtEmailCliente);
        
        formPanel.add(new JLabel("Teléfono:"));
        txtTelefonoCliente = new JTextField();
        formPanel.add(txtTelefonoCliente);
        
        JButton btnAgregarCliente = new JButton("Guardar Cliente");
        btnAgregarCliente.addActionListener(this::agregarCliente);
        formPanel.add(btnAgregarCliente);
        
        JButton btnEliminarCliente = new JButton("Eliminar Cliente");
        btnEliminarCliente.addActionListener(this::eliminarCliente);
        formPanel.add(btnEliminarCliente);
        
        clientesPanel.add(formPanel, BorderLayout.SOUTH);
    }

    private void loadInitialData() {
        loadServicios();
        loadClientes();
    }
    
    private void loadServicios() {
        // Clear existing data
        serviciosTableModel.setRowCount(0);
        
        // Load services from database
        List<Servicio> servicios = servicioService.listarTodosLosServicios();
        for (Servicio servicio : servicios) {
            Object[] row = {
                servicio.getIdServicio(),
                servicio.getNombre(),
                servicio.getDescripcion(),
                servicio.getDuracionEstimadaMinutos(),
                servicio.getPrecio()
            };
            serviciosTableModel.addRow(row);
        }
    }
    
    private void loadClientes() {
        // Clear existing data
        clientesTableModel.setRowCount(0);
        
        // Load clients from database
        List<Cliente> clientes = clienteService.listarTodosLosClientes();
        for (Cliente cliente : clientes) {
            Object[] row = {
                cliente.getIdCliente(),
                cliente.getNombre(),
                cliente.getApellido(),
                cliente.getCorreoElectronico(),
                cliente.getTelefono()
            };
            clientesTableModel.addRow(row);
        }
    }
    
    private void cargarServicioParaEdicion(Long idServicio) {
        try {
            servicioService.obtenerServicioPorId(idServicio).ifPresent(servicio -> {
                txtNombreServicio.setText(servicio.getNombre());
                txtDescripcionServicio.setText(servicio.getDescripcion());
                if (servicio.getDuracionEstimadaMinutos() != null) {
                    txtDuracionServicio.setText(servicio.getDuracionEstimadaMinutos().toString());
                }
                if (servicio.getPrecio() != null) {
                    txtPrecioServicio.setText(servicio.getPrecio().toString());
                }
                
                // Store the service ID for later update
                txtNombreServicio.putClientProperty("idServicio", idServicio);
            });
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al cargar el servicio: " + e.getMessage(), 
                                         "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private void cargarClienteParaEdicion(Long idCliente) {
        try {
            clienteService.obtenerClientePorId(idCliente).ifPresent(cliente -> {
                txtNombreCliente.setText(cliente.getNombre());
                txtApellidoCliente.setText(cliente.getApellido());
                txtEmailCliente.setText(cliente.getCorreoElectronico());
                txtTelefonoCliente.setText(cliente.getTelefono());
                
                // Store the client ID for later update
                txtNombreCliente.putClientProperty("idCliente", idCliente);
            });
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al cargar el cliente: " + e.getMessage(), 
                                         "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private void agregarServicio(ActionEvent event) {
        try {
            String nombre = txtNombreServicio.getText().trim();
            String descripcion = txtDescripcionServicio.getText().trim();
            String duracionStr = txtDuracionServicio.getText().trim();
            String precioStr = txtPrecioServicio.getText().trim();
            
            // Validate inputs
            if (nombre.isEmpty()) {
                JOptionPane.showMessageDialog(this, "El nombre del servicio es obligatorio", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Parse numeric values
            Integer duracion = null;
            BigDecimal precio = null;
            
            try {
                if (!duracionStr.isEmpty()) {
                    duracion = Integer.parseInt(duracionStr);
                }
                
                if (!precioStr.isEmpty()) {
                    precio = new BigDecimal(precioStr);
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "La duración debe ser un número entero y el precio un número decimal", 
                                             "Error de Formato", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Check if we're updating or creating
            Long idServicio = (Long) txtNombreServicio.getClientProperty("idServicio");
            Servicio servicio;
            
            if (idServicio != null) {
                // Update existing service
                servicio = servicioService.obtenerServicioPorId(idServicio)
                    .orElseThrow(() -> new RuntimeException("No se encontró el servicio a actualizar"));
            } else {
                // Create new service
                servicio = new Servicio();
            }
            
            servicio.setNombre(nombre);
            servicio.setDescripcion(descripcion);
            servicio.setDuracionEstimadaMinutos(duracion);
            servicio.setPrecio(precio);
            
            servicioService.guardarServicio(servicio);
            
            // Clear form and ID property
            txtNombreServicio.setText("");
            txtDescripcionServicio.setText("");
            txtDuracionServicio.setText("");
            txtPrecioServicio.setText("");
            txtNombreServicio.putClientProperty("idServicio", null);
            
            // Reload table
            loadServicios();
            
            JOptionPane.showMessageDialog(this, "Servicio guardado correctamente", "Éxito", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al guardar el servicio: " + e.getMessage(), 
                                         "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private void eliminarServicio(ActionEvent event) {
        int selectedRow = serviciosTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione un servicio para eliminar", 
                                         "Selección requerida", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Long idServicio = (Long) serviciosTableModel.getValueAt(selectedRow, 0);
        
        int confirmacion = JOptionPane.showConfirmDialog(this, 
                "¿Está seguro de eliminar este servicio? Esta acción no se puede deshacer.", 
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION);
        
        if (confirmacion == JOptionPane.YES_OPTION) {
            try {
                servicioService.eliminarServicio(idServicio);
                loadServicios();
                JOptionPane.showMessageDialog(this, "Servicio eliminado correctamente", 
                                             "Éxito", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error al eliminar el servicio: " + e.getMessage(), 
                                             "Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }
    
    private void agregarCliente(ActionEvent event) {
        try {
            String nombre = txtNombreCliente.getText().trim();
            String apellido = txtApellidoCliente.getText().trim();
            String email = txtEmailCliente.getText().trim();
            String telefono = txtTelefonoCliente.getText().trim();
            
            // Validate inputs
            if (nombre.isEmpty() || email.isEmpty()) {
                JOptionPane.showMessageDialog(this, "El nombre y el email son obligatorios", 
                                             "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Check if we're updating or creating
            Long idCliente = (Long) txtNombreCliente.getClientProperty("idCliente");
            Cliente cliente;
            
            if (idCliente != null) {
                // Update existing client
                cliente = clienteService.obtenerClientePorId(idCliente)
                    .orElseThrow(() -> new RuntimeException("No se encontró el cliente a actualizar"));
            } else {
                // Create new client
                cliente = new Cliente();
            }
            
            cliente.setNombre(nombre);
            cliente.setApellido(apellido);
            cliente.setCorreoElectronico(email);
            cliente.setTelefono(telefono);
            
            clienteService.guardarCliente(cliente);
            
            // Clear form and ID property
            txtNombreCliente.setText("");
            txtApellidoCliente.setText("");
            txtEmailCliente.setText("");
            txtTelefonoCliente.setText("");
            txtNombreCliente.putClientProperty("idCliente", null);
            
            // Reload table
            loadClientes();
            
            JOptionPane.showMessageDialog(this, "Cliente guardado correctamente", 
                                         "Éxito", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al guardar el cliente: " + e.getMessage(), 
                                         "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private void eliminarCliente(ActionEvent event) {
        int selectedRow = clientesTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione un cliente para eliminar", 
                                         "Selección requerida", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Long idCliente = (Long) clientesTableModel.getValueAt(selectedRow, 0);
        
        int confirmacion = JOptionPane.showConfirmDialog(this, 
                "¿Está seguro de eliminar este cliente? Esta acción no se puede deshacer.", 
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION);
        
        if (confirmacion == JOptionPane.YES_OPTION) {
            try {
                clienteService.eliminarCliente(idCliente);
                loadClientes();
                JOptionPane.showMessageDialog(this, "Cliente eliminado correctamente", 
                                             "Éxito", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error al eliminar el cliente: " + e.getMessage(), 
                                             "Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }
} 