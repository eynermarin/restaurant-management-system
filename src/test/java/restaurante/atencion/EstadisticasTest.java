package restaurante.atencion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import restaurante.atencion.estadistica.ArticulosMasSolicitados;
import restaurante.atencion.estadistica.ResultadoEstadistica;
import restaurante.atencion.estadistica.VentasCobradas;
import restaurante.atencion.estadistica.VentasPorMesero;
import restaurante.atencion.model.ItemPedido;
import restaurante.atencion.model.ModalidadCuenta;
import restaurante.atencion.model.Pago;
import restaurante.atencion.model.Visita;
import restaurante.atencion.service.DatosAtencionIniciales;
import restaurante.atencion.service.ServicioAtencion;
import restaurante.atencion.service.ServicioEstadisticas;
import restaurante.menu.control.ControladorMenu;
import restaurante.menu.model.OpcionMenu;

/**
 * Historial de prueba (menú de ejemplo: Pechuga id 1 = 3800, Agua id 20 = 800):
 *   1 de nov: Daniela, Pechuga x2 + Agua x1 = 8400, pago en efectivo
 *   3 de nov: Marco, Pechuga x1 + Agua x1 (anulada) = 3800, pago con tarjeta
 *  20 de nov: Daniela, Agua x5 = 4000 (fuera del rango 1 al 10)
 */
class EstadisticasTest {

    private static final LocalDate DESDE = LocalDate.of(2026, 11, 1);
    private static final LocalDate HASTA = LocalDate.of(2026, 11, 10);

    private ControladorMenu menu;
    private ServicioAtencion atencion;
    private ServicioEstadisticas estadisticas;

    @BeforeEach
    void preparar() throws Exception {
        menu = new ControladorMenu();
        atencion = new ServicioAtencion(menu.getConsultaMenu());
        estadisticas = new ServicioEstadisticas(atencion);

        atencion.registrarVisitaHistorica(visita("Daniela", LocalDateTime.of(2026, 11, 1, 12, 0),
                new int[][]{{1, 2}, {20, 1}}, "EFECTIVO", -1));
        atencion.registrarVisitaHistorica(visita("Marco", LocalDateTime.of(2026, 11, 3, 19, 0),
                new int[][]{{1, 1}, {20, 1}}, "TARJETA", 2));
        atencion.registrarVisitaHistorica(visita("Daniela", LocalDateTime.of(2026, 11, 20, 13, 0),
                new int[][]{{20, 5}}, "EFECTIVO", -1));
    }

    /** pedidos = {id de opción, cantidad}; anular = id del pedido a anular o -1. */
    private Visita visita(String mesero, LocalDateTime fecha, int[][] pedidos, String metodo, int anular)
            throws Exception {
        ArrayList<String> alias = new ArrayList<>();
        alias.add("Cliente");
        Visita v = new Visita(mesero, alias, fecha);
        for (int i = 0; i < pedidos.length; i++) {
            OpcionMenu o = menu.buscarOpcion(pedidos[i][0]);
            v.agregarPedido(new ItemPedido(i + 1, "Cliente", o.getId(), o.getNombre(),
                    o.getCategoria().getNombre(), pedidos[i][1], o.getPrecio(), fecha.plusMinutes(5)));
        }
        if (anular > 0) {
            v.anularPedido(anular, "Prueba");
        }
        v.solicitarCuenta(ModalidadCuenta.CONJUNTA);
        v.registrarPago(new Pago(1, metodo, v.saldoPendiente(), "REF-1", null, fecha.plusMinutes(60)));
        v.cerrarVisita(fecha.plusMinutes(65));
        return v;
    }

    @Test
    void articulosOrdenadosYSinAnulados() {
        ResultadoEstadistica r = estadisticas.calcular(new ArticulosMasSolicitados(), DESDE, HASTA);
        ArrayList<Object[]> filas = r.getFilas();
        assertEquals(2, filas.size());
        assertEquals("Pechuga de pollo a la plancha", filas.get(0)[1]);
        assertEquals(3, filas.get(0)[3]);                                    // 2 + 1
        assertEquals(0, new BigDecimal("11400").compareTo((BigDecimal) filas.get(0)[4]));
        assertEquals("Agua", filas.get(1)[1]);
        assertEquals(1, filas.get(1)[3]);                                    // la anulada no cuenta
    }

    @Test
    void ventasCobradasPorDiaYMetodo() {
        ArrayList<Object[]> filas = estadisticas.calcular(new VentasCobradas(), DESDE, HASTA).getFilas();
        assertEquals(2, filas.size());
        assertEquals("01/11/2026", filas.get(0)[0]);
        assertEquals(0, new BigDecimal("8400").compareTo((BigDecimal) filas.get(0)[2]));   // efectivo
        assertEquals("03/11/2026", filas.get(1)[0]);
        assertEquals(0, new BigDecimal("3800").compareTo((BigDecimal) filas.get(1)[3]));   // tarjeta
    }

    @Test
    void ventasPorMesero() {
        ArrayList<Object[]> filas = estadisticas.calcular(new VentasPorMesero(), DESDE, HASTA).getFilas();
        assertEquals(2, filas.size());
        assertEquals("Daniela", filas.get(0)[0]);                            // la del 20 queda fuera
        assertEquals(1, filas.get(0)[1]);
        assertEquals(0, new BigDecimal("8400").compareTo((BigDecimal) filas.get(0)[3]));
    }

    @Test
    void rangoSinDatosDaResultadoVacio() {
        LocalDate dia = LocalDate.of(2026, 11, 15);
        assertTrue(estadisticas.calcular(new ArticulosMasSolicitados(), dia, dia).estaVacio());
        assertTrue(estadisticas.calcular(new VentasCobradas(), dia, dia).estaVacio());
        assertTrue(estadisticas.calcular(new VentasPorMesero(), dia, dia).estaVacio());
    }

    @Test
    void rangoInvalido() {
        assertThrows(IllegalArgumentException.class,
                () -> estadisticas.calcular(new VentasCobradas(), HASTA, DESDE));
        assertThrows(IllegalArgumentException.class,
                () -> estadisticas.calcular(null, DESDE, HASTA));
    }

    @Test
    void soloAceptaVisitasCerradasEnElHistorial() {
        ArrayList<String> alias = new ArrayList<>();
        alias.add("Ana");
        Visita abierta = new Visita("Daniela", alias);
        assertThrows(IllegalArgumentException.class, () -> atencion.registrarVisitaHistorica(abierta));
        assertEquals(3, atencion.getHistorial().size());
    }

    @Test
    void datosDeEjemplo() {
        ServicioAtencion nuevo = new ServicioAtencion(menu.getConsultaMenu());
        DatosAtencionIniciales.cargar(nuevo, menu.getConsultaMenu());
        assertEquals(10, nuevo.getHistorial().size());
        for (Visita v : nuevo.getHistorial()) {
            assertTrue(v.isCerrada());
        }
        assertTrue(nuevo.opcionUsadaEnPedidos(1));
        assertTrue(new ServicioEstadisticas(nuevo).calcular(new VentasCobradas(),
                LocalDate.now().minusDays(30), LocalDate.now()).getFilas().size() > 0);
    }
}
