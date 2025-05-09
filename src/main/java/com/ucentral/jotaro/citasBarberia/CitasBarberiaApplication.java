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
		} catch (Exception e) {
			System.err.println("Nimbus LaF not available, using default.");
		}

		ConfigurableApplicationContext context = new SpringApplicationBuilder(CitasBarberiaApplication.class)
				.headless(false)
				.web(org.springframework.boot.WebApplicationType.NONE)
				.run(args);

		SwingUtilities.invokeLater(() -> {
			CitaService citaService = context.getBean(CitaService.class);
			ClienteService clienteService = context.getBean(ClienteService.class);
			ServicioService servicioService = context.getBean(ServicioService.class);
			ReservaRepository reservaRepository = context.getBean(ReservaRepository.class);
			EstadisticasListener estadisticasListener = context.getBean(EstadisticasListener.class);

			boolean abrirAdmin = Arrays.stream(args).anyMatch(arg -> arg.equals("--admin"));
			
			VentanaPrincipalSwing frame = new VentanaPrincipalSwing(citaService, clienteService, servicioService, estadisticasListener);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setTitle("Reserva de Citas - Barbería Jotaro");
			frame.setLocationRelativeTo(null);
			frame.setVisible(true);
			
			if (abrirAdmin) {
				AdminPanelSwing adminPanel = new AdminPanelSwing(clienteService, servicioService, reservaRepository, estadisticasListener, citaService);
				adminPanel.setVisible(true);
			}
		});
	}

}
