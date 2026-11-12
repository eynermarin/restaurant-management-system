package restaurante.atencion;

import javax.swing.*;

import restaurante.atencion.control.ControladorAtencion;
import restaurante.atencion.view.*;
import restaurante.menu.control.ControladorMenu;

public class PruebaInterfaz {

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            ControladorMenu menu = new ControladorMenu();

            ControladorAtencion controlador =
                new ControladorAtencion(menu.getConsultaMenu());

            menu.setVerificadorUso(controlador.getServicio());

            JFrame ventana = new JFrame(
                "Restaurant Management System - Atencion"
            );

            ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            JTabbedPane pestanas = new JTabbedPane();

            PanelMesas panelMesas = new PanelMesas();
            PanelPedidos panelPedidos = new PanelPedidos();
            PanelCuenta panelCuenta = new PanelCuenta();

            panelMesas.setControlador(controlador);
            panelPedidos.setControlador(controlador);
            panelCuenta.setControlador(controlador);

            pestanas.addTab("Mesas", panelMesas);
            pestanas.addTab("Pedidos", panelPedidos);
            pestanas.addTab("Cuentas", panelCuenta);

            pestanas.addChangeListener(e -> {
                panelMesas.refrescar();
                panelPedidos.refrescar();
                panelCuenta.refrescar();
            });

            ventana.add(pestanas);
            ventana.setSize(1000, 750);
            ventana.setLocationRelativeTo(null);
            ventana.setVisible(true);
        });
    }
}