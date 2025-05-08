package com.ucentral.jotaro.citasBarberia;

import com.ucentral.jotaro.citasBarberia.service.CitaService;
import com.ucentral.jotaro.citasBarberia.service.ClienteService;
import com.ucentral.jotaro.citasBarberia.service.ServicioService;
import com.ucentral.jotaro.citasBarberia.ui.AdminPanelSwing;
import com.ucentral.jotaro.citasBarberia.ui.VentanaPrincipalSwing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import javax.swing.*;
import java.util.Arrays;

@SpringBootApplication
public class CitasBarberiaApplication {

	public static void main(String[] args) {
		ConfigurableApplicationContext context = new SpringApplicationBuilder(CitasBarberiaApplication.class)
				.headless(false)
				.web(org.springframework.boot.WebApplicationType.NONE)
				.run(args);

		SwingUtilities.invokeLater(() -> {
			// Obtener servicios del contexto de Spring
			CitaService citaService = context.getBean(CitaService.class);
			ClienteService clienteService = context.getBean(ClienteService.class);
			ServicioService servicioService = context.getBean(ServicioService.class);

			// Verificar si se solicitó abrir directamente el panel de administración
			boolean abrirAdmin = Arrays.stream(args).anyMatch(arg -> arg.equals("--admin"));
			
			// Siempre mostrar la ventana principal
			VentanaPrincipalSwing frame = new VentanaPrincipalSwing(citaService, clienteService, servicioService);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setSize(800, 600);
			frame.setLocationRelativeTo(null);
			frame.setVisible(true);
			
			// Si se solicitó, abrir también el panel de administración
			if (abrirAdmin) {
				AdminPanelSwing adminPanel = new AdminPanelSwing(clienteService, servicioService);
				adminPanel.setVisible(true);
			}
		});
	}

}
