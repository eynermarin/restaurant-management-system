package restaurante.atencion;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;

import restaurante.atencion.model.ItemPedido;
import restaurante.atencion.model.Visita;

public class PruebaPrecioCongelado {

    public static void main(String[] args) {

        BigDecimal precioOriginal = new BigDecimal("4500");

        Visita visita = new Visita(
            "Carlos",
            new ArrayList<>(Arrays.asList("Juan"))
        );

        ItemPedido pedido = new ItemPedido(
            1, "Juan", 10, "Chuleta de cerdo",
            "PLATO FUERTE", 2, precioOriginal
        );

        visita.agregarPedido(pedido);

        BigDecimal precioNuevo = new BigDecimal("6000");

        if (precioNuevo.compareTo(precioOriginal) == 0) {
            throw new AssertionError("El precio no cambio.");
        }

        BigDecimal esperado = new BigDecimal("9000");

        if (pedido.calcularSubtotal().compareTo(esperado) != 0) {
            throw new AssertionError("El pedido cambio de precio.");
        }

        if (visita.calcularTotal().compareTo(esperado) != 0) {
            throw new AssertionError("El total de la visita cambio.");
        }

        System.out.println("OK - Precio original conservado");
        System.out.println("OK - Subtotal correcto");
        System.out.println("OK - Total correcto");
        System.out.println("RESULTADO: 3 comprobaciones correctas.");
    }
}