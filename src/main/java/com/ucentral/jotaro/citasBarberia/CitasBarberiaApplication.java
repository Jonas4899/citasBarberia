package com.ucentral.jotaro.citasBarberia;

import com.ucentral.jotaro.citasBarberia.service.CitaService;
import com.ucentral.jotaro.citasBarberia.service.ClienteService;
import com.ucentral.jotaro.citasBarberia.service.ServicioService;
import com.ucentral.jotaro.citasBarberia.ui.VentanaPrincipalSwing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import javax.swing.*;

@SpringBootApplication
public class CitasBarberiaApplication {

	public static void main(String[] args) {
		ConfigurableApplicationContext context = new SpringApplicationBuilder(CitasBarberiaApplication.class)
				.headless(false)
				.web(org.springframework.boot.WebApplicationType.NONE)
				.run(args);

		SwingUtilities.invokeLater(() -> {
			// Obtén tu servicio de citas del contexto de Spring
			// Asumiremos que tienes un CitaService bean (lo crearemos en el paso 3)
			CitaService citaService = context.getBean(CitaService.class);
			ClienteService clienteService = context.getBean(ClienteService.class); // Asumiendo que lo tienes
			ServicioService servicioService = context.getBean(ServicioService.class); // Asumiendo

			// Pasa el servicio a tu ventana principal
			VentanaPrincipalSwing frame = new VentanaPrincipalSwing(citaService, clienteService, servicioService);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setSize(800, 600);
			frame.setLocationRelativeTo(null);
			frame.setVisible(true);
		});
	}

}
