package restaurante.atencion.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import restaurante.atencion.model.ItemPedido;
import restaurante.atencion.model.ModalidadCuenta;
import restaurante.atencion.model.Pago;
import restaurante.atencion.model.Visita;
import restaurante.compartido.ConsultaMenu;
import restaurante.menu.exception.DatoMenuInvalidoException;
import restaurante.menu.model.OpcionMenu;

/**
 * Carga 10 visitas ya cerradas en los últimos 12 días, para que las
 * estadísticas tengan datos desde que se abre la aplicación. Las fechas son
 * relativas a hoy, así siempre caen dentro del rango por defecto (últimos 30 días).
 */
public final class DatosAtencionIniciales {

    private DatosAtencionIniciales() {
    }

    public static void cargar(ServicioAtencion servicio, ConsultaMenu menu) {
        // Cada pedido es {comensal (posición en los alias), id de la opción del menú, cantidad}
        visita(servicio, menu, 12, 12, "Daniela", new String[]{"Ana", "Luis"},
                new int[][]{{0, 1, 1}, {0, 20, 1}, {1, 2, 1}, {1, 22, 1}, {1, 17, 1}}, "EFECTIVO", -1);
        visita(servicio, menu, 11, 19, "Marco", new String[]{"Sofía"},
                new int[][]{{0, 4, 1}, {0, 8, 1}, {0, 24, 1}}, "TARJETA", -1);
        visita(servicio, menu, 10, 13, "Daniela", new String[]{"Pedro", "Laura", "Tomás"},
                new int[][]{{0, 1, 1}, {1, 3, 1}, {2, 1, 1}, {0, 6, 1}, {1, 12, 1}, {2, 21, 2}}, "TARJETA", 4);
        visita(servicio, menu, 8, 20, "Valeria", new String[]{"Diego", "Paula"},
                new int[][]{{0, 2, 2}, {1, 5, 1}, {0, 13, 1}, {1, 23, 1}, {0, 16, 2}}, "EFECTIVO", -1);
        visita(servicio, menu, 6, 12, "Marco", new String[]{"Andrés"},
                new int[][]{{0, 1, 1}, {0, 11, 1}, {0, 20, 1}}, "EFECTIVO", -1);
        visita(servicio, menu, 5, 19, "Valeria", new String[]{"Camila", "Jorge", "Elena", "Mario"},
                new int[][]{{0, 3, 1}, {1, 4, 1}, {2, 1, 1}, {3, 2, 1}, {0, 7, 1}, {1, 8, 1},
                    {2, 6, 1}, {3, 10, 1}, {0, 17, 2}, {2, 19, 1}, {1, 22, 2}, {3, 21, 1}}, "TARJETA", -1);
        visita(servicio, menu, 3, 13, "Daniela", new String[]{"Rosa", "Iván"},
                new int[][]{{0, 5, 1}, {1, 1, 1}, {0, 14, 1}, {1, 21, 2}}, "TARJETA", -1);
        visita(servicio, menu, 2, 20, "Marco", new String[]{"Lucía", "Pablo"},
                new int[][]{{0, 2, 1}, {1, 4, 1}, {0, 18, 1}, {1, 16, 1}, {0, 23, 1}, {1, 24, 1}}, "EFECTIVO", -1);
        visita(servicio, menu, 1, 12, "Valeria", new String[]{"Nico"},
                new int[][]{{0, 1, 2}, {0, 9, 1}, {0, 20, 2}}, "EFECTIVO", -1);
        visita(servicio, menu, 1, 19, "Daniela", new String[]{"Marta", "Raúl", "Bea"},
                new int[][]{{0, 3, 1}, {1, 1, 1}, {2, 2, 1}, {0, 12, 1}, {1, 6, 1}, {2, 8, 1},
                    {0, 17, 1}, {1, 17, 1}, {2, 22, 1}}, "TARJETA", -1);
    }

    /**
     * Arma una visita de hace diasAtras días a la hora indicada, la cobra completa
     * en cuenta conjunta y la cierra. Si anular no es -1, anula ese pedido antes de cobrar.
     */
    private static void visita(ServicioAtencion servicio, ConsultaMenu menu, int diasAtras, int hora,
            String mesero, String[] alias, int[][] pedidos, String metodo, int anular) {
        try {
            LocalDateTime apertura = LocalDate.now().minusDays(diasAtras).atTime(hora, 0);
            ArrayList<String> comensales = new ArrayList<>();
            for (String a : alias) {
                comensales.add(a);
            }
            Visita visita = new Visita(mesero, comensales, apertura);

            for (int i = 0; i < pedidos.length; i++) {
                OpcionMenu opcion = menu.buscarOpcion(pedidos[i][1]);
                visita.agregarPedido(new ItemPedido(i + 1, alias[pedidos[i][0]], opcion.getId(),
                        opcion.getNombre(), String.valueOf(opcion.getCategoria()), pedidos[i][2],
                        opcion.getPrecio(), apertura.plusMinutes(10 + i)));
            }
            if (anular >= 0) {
                visita.anularPedido(anular + 1, "El cliente cambió de opinión");
            }

            visita.solicitarCuenta(ModalidadCuenta.CONJUNTA);
            String referencia = metodo.equals("TARJETA") ? "AUT-" + (1000 + diasAtras * 10 + hora) : "EF-EJEMPLO";
            visita.registrarPago(new Pago(1, metodo, visita.saldoPendiente(), referencia, null,
                    apertura.plusMinutes(70)));
            visita.cerrarVisita(apertura.plusMinutes(75));
            servicio.registrarVisitaHistorica(visita);
        } catch (DatoMenuInvalidoException e) {
            // Los ids de arriba son fijos y existen en el menú de ejemplo; si falla es un error de programación.
            throw new IllegalStateException("Datos de ejemplo inválidos: " + e.getMessage(), e);
        }
    }
}
