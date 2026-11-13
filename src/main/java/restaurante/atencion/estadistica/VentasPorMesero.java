package restaurante.atencion.estadistica;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import restaurante.atencion.model.Pago;
import restaurante.atencion.model.Visita;

/**
 * Desempeño de cada mesero: cuántas visitas atendió, a cuántas personas y
 * cuánto se cobró. Usa la fecha de cierre de la visita.
 */
public class VentasPorMesero implements ConsultaEstadistica {

    /** Lo acumulado de un mesero. */
    private static class Mesero {
        String nombre;
        int visitas;
        int personas;
        BigDecimal cobrado = BigDecimal.ZERO;
    }

    @Override
    public String getNombre() {
        return "Ventas por mesero";
    }

    /** Lo que muestra el JComboBox de la pantalla. */
    @Override
    public String toString() {
        return getNombre();
    }

    @Override
    public String explicar() {
        return "Para cada mesero cuenta las visitas que cerró en el rango (según la fecha de cierre), "
                + "las personas atendidas y el total cobrado. Promedio = total / visitas.";
    }

    @Override
    public ResultadoEstadistica calcular(ArrayList<Visita> historial, LocalDate desde, LocalDate hasta) {
        ArrayList<Mesero> meseros = new ArrayList<>();

        for (Visita visita : historial) {
            if (visita.getFechaCierre() == null) {
                continue;
            }
            LocalDate fecha = visita.getFechaCierre().toLocalDate();
            if (fecha.isBefore(desde) || fecha.isAfter(hasta)) {
                continue;
            }
            Mesero m = buscar(meseros, visita.getMesero());
            if (m == null) {
                m = new Mesero();
                m.nombre = visita.getMesero();
                meseros.add(m);
            }
            m.visitas++;
            m.personas = m.personas + visita.getComensales().size();
            for (Pago pago : visita.getPagos()) {
                m.cobrado = m.cobrado.add(pago.getMonto());
            }
        }

        // El que más cobró primero
        meseros.sort((x, y) -> y.cobrado.compareTo(x.cobrado));

        ArrayList<Object[]> filas = new ArrayList<>();
        int visitas = 0;
        for (Mesero m : meseros) {
            BigDecimal promedio = m.cobrado.divide(new BigDecimal(m.visitas), 2, RoundingMode.HALF_UP);
            filas.add(new Object[]{m.nombre, m.visitas, m.personas, m.cobrado, promedio});
            visitas = visitas + m.visitas;
        }

        String resumen = meseros.size() + " meseros, " + visitas + " visitas cerradas.";
        return new ResultadoEstadistica(
                new String[]{"Mesero", "Visitas", "Personas", "Total cobrado (₡)", "Promedio por visita (₡)"},
                filas, resumen);
    }

    private Mesero buscar(ArrayList<Mesero> meseros, String nombre) {
        for (Mesero m : meseros) {
            if (m.nombre.equalsIgnoreCase(nombre)) {
                return m;
            }
        }
        return null;
    }
}
