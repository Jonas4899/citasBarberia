package com.ucentral.jotaro.citasBarberia;

import com.ucentral.jotaro.citasBarberia.repository.ReservaRepository;
import com.ucentral.jotaro.citasBarberia.service.CitaService;
import com.ucentral.jotaro.citasBarberia.service.ClienteService;
import com.ucentral.jotaro.citasBarberia.service.ServicioService;
import com.ucentral.jotaro.citasBarberia.ui.AdminPanelSwing;
import com.ucentral.jotaro.citasBarberia.ui.VentanaPrincipalSwing;
import com.ucentral.jotaro.citasBarberia.listener.EstadisticasListener;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import javax.swing.*;
import java.util.Arrays;

@SpringBootApplication
public class CitasBarberiaApplication {

	public static void main(String[] args) {
		try {
			for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
			// Opcional: Ajustar fuentes globales para Nimbus si es necesario
			// setGlobalFont(new Font("Segoe UI", Font.PLAIN, 13)); // Ejemplo de fuente global
		} catch (Exception e) {
			System.err.println("Nimbus LaF not available, using default.");
			// Puedes optar por el System LaF como fallback:
			// try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
			// catch (Exception ex) { ex.printStackTrace(); }
		}

		ConfigurableApplicationContext context = new SpringApplicationBuilder(CitasBarberiaApplication.class)
				.headless(false)
				.web(org.springframework.boot.WebApplicationType.NONE)
				.run(args);

		SwingUtilities.invokeLater(() -> {
			// Obtener servicios del contexto de Spring
			CitaService citaService = context.getBean(CitaService.class);
			ClienteService clienteService = context.getBean(ClienteService.class);
			ServicioService servicioService = context.getBean(ServicioService.class);
			ReservaRepository reservaRepository = context.getBean(ReservaRepository.class);
			EstadisticasListener estadisticasListener = context.getBean(EstadisticasListener.class);

			// Verificar si se solicitó abrir directamente el panel de administración
			boolean abrirAdmin = Arrays.stream(args).anyMatch(arg -> arg.equals("--admin"));
			
			// Siempre mostrar la ventana principal
			VentanaPrincipalSwing frame = new VentanaPrincipalSwing(citaService, clienteService, servicioService, estadisticasListener);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setTitle("Reserva de Citas - Barbería Jotaro");
			frame.setLocationRelativeTo(null);
			// pack() es mejor llamarlo después de que todos los componentes estén listos.
			// Lo moveremos al final de los constructores de las ventanas.
			frame.setVisible(true);
			
			// Si se solicitó, abrir también el panel de administración
			if (abrirAdmin) {
				AdminPanelSwing adminPanel = new AdminPanelSwing(clienteService, servicioService, reservaRepository, estadisticasListener, citaService);
				adminPanel.setVisible(true);
			}
		});
	}

}
