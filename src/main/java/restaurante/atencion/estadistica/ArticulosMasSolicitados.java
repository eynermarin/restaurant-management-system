package restaurante.atencion.estadistica;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import restaurante.atencion.model.ItemPedido;
import restaurante.atencion.model.Visita;

/**
 * Ranking de los artículos más pedidos. Usa la fecha de cada pedido y no cuenta
 * los pedidos anulados. La columna Categoría permite verlo por categoría
 * (clic en el encabezado para ordenar).
 */
public class ArticulosMasSolicitados implements ConsultaEstadistica {

    /** Lo acumulado de un artículo mientras se recorren los pedidos. */
    private static class Acumulado {
        int idOpcion;
        String nombre;
        String categoria;
        int cantidad;
        BigDecimal monto = BigDecimal.ZERO;
    }

    @Override
    public String getNombre() {
        return "Artículos más solicitados";
    }

    /** Lo que muestra el JComboBox de la pantalla. */
    @Override
    public String toString() {
        return getNombre();
    }

    @Override
    public String explicar() {
        return "Suma las cantidades pedidas de cada artículo en las visitas cerradas, según la fecha "
                + "del pedido. Los pedidos anulados no cuentan. Se ordena de más a menos pedido.";
    }

    @Override
    public ResultadoEstadistica calcular(ArrayList<Visita> historial, LocalDate desde, LocalDate hasta) {
        ArrayList<Acumulado> articulos = new ArrayList<>();
        int unidades = 0;

        for (Visita visita : historial) {
            for (ItemPedido pedido : visita.getPedidos()) {
                LocalDate fecha = pedido.getFechaPedido().toLocalDate();
                if (pedido.isAnulado() || fecha.isBefore(desde) || fecha.isAfter(hasta)) {
                    continue;
                }
                Acumulado a = buscar(articulos, pedido.getIdOpcion());
                if (a == null) {
                    a = new Acumulado();
                    a.idOpcion = pedido.getIdOpcion();
                    a.nombre = pedido.getNombreOpcion();
                    a.categoria = pedido.getCategoria();
                    articulos.add(a);
                }
                a.cantidad = a.cantidad + pedido.getCantidad();
                a.monto = a.monto.add(pedido.calcularSubtotal());
                unidades = unidades + pedido.getCantidad();
            }
        }

        // De más a menos pedido; si empatan, por nombre
        articulos.sort((x, y) -> {
            if (x.cantidad != y.cantidad) {
                return y.cantidad - x.cantidad;
            }
            return x.nombre.compareToIgnoreCase(y.nombre);
        });

        ArrayList<Object[]> filas = new ArrayList<>();
        for (int i = 0; i < articulos.size(); i++) {
            Acumulado a = articulos.get(i);
            filas.add(new Object[]{i + 1, a.nombre, a.categoria, a.cantidad, a.monto});
        }

        String resumen = articulos.size() + " artículos distintos, " + unidades + " unidades pedidas.";
        return new ResultadoEstadistica(
                new String[]{"#", "Artículo", "Categoría", "Cantidad", "Monto (₡)"}, filas, resumen);
    }

    private Acumulado buscar(ArrayList<Acumulado> articulos, int idOpcion) {
        for (Acumulado a : articulos) {
            if (a.idOpcion == idOpcion) {
                return a;
            }
        }
        return null;
    }
}
