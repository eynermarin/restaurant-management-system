package restaurante.atencion;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;

import restaurante.atencion.exception.*;
import restaurante.atencion.model.*;
import restaurante.atencion.service.ServicioAtencion;
import restaurante.menu.control.ControladorMenu;
import restaurante.menu.model.OpcionMenu;

public class PruebaValidaciones {

    private static int pruebasCorrectas = 0;

    public static void main(String[] args) {

        ControladorMenu controladorMenu = new ControladorMenu();

        ServicioAtencion servicio = new ServicioAtencion(
            controladorMenu.getConsultaMenu()
        );

        System.out.println("=== PRUEBAS DE VALIDACIONES ===");

        // 1. Alias repetidos
        comprobarExcepcion(
            "Rechazar alias repetidos",
            IllegalArgumentException.class,
            () -> servicio.abrirVisita(
                1, "Carlos", alias("Juan", "juan")
            )
        );

        verificar(
            "Mesa sin cambios tras alias repetidos",
            servicio.buscarMesa(1).estaDisponible()
        );

        // Abrir una visita valida
        servicio.abrirVisita(
            1, "Carlos", alias("Juan", "Pedro")
        );

        Visita visita = servicio.buscarMesa(1).getVisitaActual();

        // 2. Mesa ocupada
        comprobarExcepcion(
            "Rechazar apertura de mesa ocupada",
            MesaNoDisponibleException.class,
            () -> servicio.abrirVisita(
                1, "Ana", alias("Maria")
            )
        );

        verificar(
            "Conservar visita original",
            servicio.buscarMesa(1).getVisitaActual() == visita
        );

        // Buscar una opcion real del menu
        ArrayList<OpcionMenu> opciones = servicio.obtenerMenu();

        if (opciones.isEmpty()) {
            throw new AssertionError(
                "El menu no tiene opciones disponibles."
            );
        }

        OpcionMenu opcion = opciones.get(0);

        verificar(
            "Precio del producto positivo",
            opcion.getPrecio().compareTo(BigDecimal.ZERO) > 0
        );

        servicio.agregarPedido(
            1, "Juan", opcion.getId(), 1
        );

        BigDecimal total = servicio.obtenerTotal(1);

        // 3. Cierre sin pagar
        comprobarExcepcion(
            "Rechazar cierre sin solicitar cuenta",
            IllegalStateException.class,
            () -> servicio.cerrarVisita(1)
        );

        servicio.solicitarCuenta(
            1, ModalidadCuenta.CONJUNTA
        );

        comprobarExcepcion(
            "Rechazar cierre con saldo pendiente",
            IllegalStateException.class,
            () -> servicio.cerrarVisita(1)
        );

        verificar(
            "Mesa sigue pendiente de pago",
            !servicio.buscarMesa(1).estaDisponible()
        );

        // 4. Pedido despues de solicitar cuenta
        int cantidadPedidos = visita.getPedidos().size();

        comprobarExcepcion(
            "Rechazar pedido despues de solicitar cuenta",
            PedidoEnVisitaCerradaException.class,
            () -> servicio.agregarPedido(
                1, "Juan", opcion.getId(), 1
            )
        );

        verificar(
            "Cantidad de pedidos sin cambios",
            visita.getPedidos().size() == cantidadPedidos
        );

        int idPedido = visita.getPedidos().get(0).getId();

        comprobarExcepcion(
            "Rechazar anulacion despues de solicitar cuenta",
            PedidoEnVisitaCerradaException.class,
            () -> servicio.anularPedido(
                1, idPedido, "Prueba"
            )
        );

        verificar(
            "Pedido original sigue activo",
            !visita.getPedidos().get(0).isAnulado()
        );

        // 5. Pagos invalidos
        comprobarPagoInvalido(
            servicio, "Pago de cero",
            BigDecimal.ZERO
        );

        comprobarPagoInvalido(
            servicio, "Pago negativo",
            new BigDecimal("-100")
        );

        comprobarPagoInvalido(
            servicio, "Pago mayor al saldo",
            total.add(BigDecimal.ONE)
        );

        BigDecimal saldoAntes = servicio.obtenerSaldo(1);

        comprobarExcepcion(
            "Rechazar tarjeta sin referencia",
            PagoInvalidoException.class,
            () -> servicio.registrarPago(
                1, null, "TARJETA",
                total, ""
            )
        );

        verificar(
            "Saldo sin cambios tras tarjeta invalida",
            servicio.obtenerSaldo(1).compareTo(saldoAntes) == 0
        );

        // 6. Pago valido y cierre
        servicio.registrarPago(
            1, null, "EFECTIVO", total, ""
        );

        verificar(
            "Saldo final igual a cero",
            servicio.obtenerSaldo(1).compareTo(BigDecimal.ZERO) == 0
        );

        servicio.cerrarVisita(1);

        verificar(
            "Mesa disponible despues del cierre",
            servicio.buscarMesa(1).estaDisponible()
        );

        verificar(
            "Visita guardada en historial",
            servicio.getHistorial().size() == 1
        );

        verificar(
            "Visita marcada como cerrada",
            servicio.getHistorial().get(0).isCerrada()
        );

        System.out.println();
        System.out.println(
            "RESULTADO: " + pruebasCorrectas +
            " comprobaciones correctas."
        );
    }

    private static ArrayList<String> alias(String... nombres) {
        return new ArrayList<>(Arrays.asList(nombres));
    }

    private static void verificar(
        String descripcion, boolean condicion
    ) {
        if (!condicion) {
            throw new AssertionError("FALLO: " + descripcion);
        }

        pruebasCorrectas++;
        System.out.println("OK - " + descripcion);
    }

    private static void comprobarExcepcion(
        String descripcion,
        Class<? extends Throwable> esperada,
        Runnable operacion
    ) {
        try {
            operacion.run();
        } catch (Throwable e) {
            if (!esperada.isInstance(e)) {
                throw new AssertionError(
                    descripcion + ": excepcion incorrecta: " +
                    e.getClass().getSimpleName(), e
                );
            }

            verificar(descripcion, true);
            return;
        }

        throw new AssertionError(
            "FALLO: " + descripcion +
            " (no se lanzo ninguna excepcion)"
        );
    }

    private static void comprobarPagoInvalido(
        ServicioAtencion servicio,
        String descripcion,
        BigDecimal monto
    ) {
        BigDecimal saldoAntes = servicio.obtenerSaldo(1);

        comprobarExcepcion(
            descripcion,
            PagoInvalidoException.class,
            () -> servicio.registrarPago(
                1, null, "EFECTIVO", monto, ""
            )
        );

        verificar(
            "Saldo conservado: " + descripcion,
            servicio.obtenerSaldo(1).compareTo(saldoAntes) == 0
        );
    }
}