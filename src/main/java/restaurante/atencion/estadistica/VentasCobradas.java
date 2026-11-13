package restaurante.atencion.estadistica;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.TreeMap;
import restaurante.atencion.model.Pago;
import restaurante.atencion.model.Visita;

/**
 * Dinero cobrado por día. Usa la fecha de cada pago, así que cuenta lo que
 * realmente entró en ese rango, separado por efectivo y tarjeta.
 */
public class VentasCobradas implements ConsultaEstadistica {

    /** Lo acumulado de un día. */
    private static class Dia {
        int pagos;
        BigDecimal efectivo = BigDecimal.ZERO;
        BigDecimal tarjeta = BigDecimal.ZERO;
    }

    @Override
    public String getNombre() {
        return "Ventas cobradas por día";
    }

    /** Lo que muestra el JComboBox de la pantalla. */
    @Override
    public String toString() {
        return getNombre();
    }

    @Override
    public String explicar() {
        return "Suma los pagos registrados en las visitas cerradas, según la fecha del pago, "
                + "y los agrupa por día y por método de pago.";
    }

    @Override
    public ResultadoEstadistica calcular(ArrayList<Visita> historial, LocalDate desde, LocalDate hasta) {
        // TreeMap mantiene los días ordenados de forma automática
        TreeMap<LocalDate, Dia> dias = new TreeMap<>();

        for (Visita visita : historial) {
            for (Pago pago : visita.getPagos()) {
                LocalDate fecha = pago.getFechaPago().toLocalDate();
                if (fecha.isBefore(desde) || fecha.isAfter(hasta)) {
                    continue;
                }
                Dia dia = dias.get(fecha);
                if (dia == null) {
                    dia = new Dia();
                    dias.put(fecha, dia);
                }
                dia.pagos++;
                if (pago.getMetodo().equalsIgnoreCase("TARJETA")) {
                    dia.tarjeta = dia.tarjeta.add(pago.getMonto());
                } else {
                    dia.efectivo = dia.efectivo.add(pago.getMonto());
                }
            }
        }

        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        ArrayList<Object[]> filas = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        int totalPagos = 0;

        for (LocalDate fecha : dias.keySet()) {
            Dia dia = dias.get(fecha);
            BigDecimal totalDia = dia.efectivo.add(dia.tarjeta);
            filas.add(new Object[]{fecha.format(formato), dia.pagos, dia.efectivo, dia.tarjeta, totalDia});
            total = total.add(totalDia);
            totalPagos = totalPagos + dia.pagos;
        }

        String resumen = "Total cobrado: ₡" + total.toPlainString() + " en " + totalPagos + " pagos.";
        return new ResultadoEstadistica(
                new String[]{"Fecha", "Pagos", "Efectivo (₡)", "Tarjeta (₡)", "Total (₡)"}, filas, resumen);
    }
}
