package restaurante.atencion.service;

import java.time.LocalDate;
import java.util.ArrayList;
import restaurante.atencion.estadistica.ArticulosMasSolicitados;
import restaurante.atencion.estadistica.ConsultaEstadistica;
import restaurante.atencion.estadistica.ResultadoEstadistica;
import restaurante.atencion.estadistica.VentasCobradas;
import restaurante.atencion.estadistica.VentasPorMesero;

/**
 * Corre las consultas estadísticas sobre el historial de visitas cerradas de
 * ServicioAtencion. Solo lee: nunca modifica visitas, pedidos ni pagos.
 */
public class ServicioEstadisticas {

    private ServicioAtencion atencion;
    private ArrayList<ConsultaEstadistica> consultas = new ArrayList<>();

    public ServicioEstadisticas(ServicioAtencion atencion) {
        if (atencion == null) {
            throw new IllegalArgumentException("El servicio de atención no puede ser nulo.");
        }
        this.atencion = atencion;
        consultas.add(new ArticulosMasSolicitados());
        consultas.add(new VentasCobradas());
        consultas.add(new VentasPorMesero());
    }

    public ArrayList<ConsultaEstadistica> getConsultas() {
        return new ArrayList<>(consultas);
    }

    /** Calcula una consulta entre dos fechas, ambas incluidas. */
    public ResultadoEstadistica calcular(ConsultaEstadistica consulta, LocalDate desde, LocalDate hasta) {
        if (consulta == null) {
            throw new IllegalArgumentException("Elija una consulta.");
        }
        if (desde == null || hasta == null) {
            throw new IllegalArgumentException("Indique las dos fechas del rango.");
        }
        if (desde.isAfter(hasta)) {
            throw new IllegalArgumentException("La fecha inicial no puede ser posterior a la final.");
        }
        return consulta.calcular(atencion.getHistorial(), desde, hasta);
    }
}
