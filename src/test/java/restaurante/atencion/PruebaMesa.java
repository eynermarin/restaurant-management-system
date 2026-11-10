package restaurante.atencion;

import java.util.ArrayList;
import java.math.BigDecimal;
import restaurante.atencion.model.*;
import restaurante.atencion.exception.MesaNoDisponibleException;

public class PruebaMesa {

    public static void main(String[] args) {

        Mesa mesa = new Mesa(1);

        System.out.println("Estado inicial: " + mesa.getEstado());

        ArrayList<String> clientes = new ArrayList<>();
        clientes.add("Carlos");
        clientes.add("Maria");

        mesa.abrirVisita("Daniel", clientes);
        System.out.println("Despues de abrir: " + mesa.getEstado());

        try {
            mesa.abrirVisita("Otro mesero", clientes);
            throw new AssertionError("Se permitio abrir una mesa ocupada.");
        } catch (MesaNoDisponibleException e) {
            System.out.println("Mesa ocupada rechazada correctamente.");
        }

        ItemPedido pedido = new ItemPedido(
            1, "Carlos", 10, "Hamburguesa",
            "PROTEINAS", 2, new BigDecimal("3500")
        );

        mesa.agregarPedido(pedido);

        System.out.println("Total: " +
            mesa.getVisitaActual().calcularTotal());

        mesa.solicitarCuenta(ModalidadCuenta.CONJUNTA);
        System.out.println("Cuenta solicitada: " + mesa.getEstado());

        Pago pago = new Pago(
            1, "EFECTIVO", new BigDecimal("7000"),
            "EF-001", null
        );

        mesa.registrarPago(pago);
        mesa.cerrarVisita();

        System.out.println("Estado final: " + mesa.getEstado());

        if (!mesa.estaDisponible()) {
            throw new AssertionError("La mesa no fue liberada.");
        }

        System.out.println("PRUEBA COMPLETADA CORRECTAMENTE");
    }
}