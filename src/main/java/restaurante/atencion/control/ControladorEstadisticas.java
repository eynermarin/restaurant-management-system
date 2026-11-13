package restaurante.atencion.control;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import restaurante.atencion.estadistica.ConsultaEstadistica;
import restaurante.atencion.estadistica.ResultadoEstadistica;
import restaurante.atencion.service.ServicioAtencion;
import restaurante.atencion.service.ServicioEstadisticas;

/**
 * Única clase que usa la pantalla de estadísticas. Convierte lo que viene de
 * los componentes (lo seleccionado en el combo, las fechas de los JSpinner)
 * y llama a ServicioEstadisticas.
 */
public class ControladorEstadisticas {

    private ServicioEstadisticas servicio;

    public ControladorEstadisticas(ServicioAtencion atencion) {
        this.servicio = new ServicioEstadisticas(atencion);
    }

    public ArrayList<ConsultaEstadistica> consultas() {
        return servicio.getConsultas();
    }

    /** consulta es lo seleccionado en el JComboBox; desde y hasta, los valores de los JSpinner de fecha. */
    public ResultadoEstadistica calcular(Object consulta, Date desde, Date hasta) {
        if (!(consulta instanceof ConsultaEstadistica)) {
            throw new IllegalArgumentException("Elija una consulta.");
        }
        return servicio.calcular((ConsultaEstadistica) consulta, aFecha(desde), aFecha(hasta));
    }

    /** Explicación de cómo se calcula la consulta seleccionada, para mostrarla en pantalla. */
    public String explicar(Object consulta) {
        if (!(consulta instanceof ConsultaEstadistica)) {
            return "";
        }
        return ((ConsultaEstadistica) consulta).explicar();
    }

    /** Un JSpinner de fecha entrega java.util.Date; las consultas usan LocalDate. */
    private LocalDate aFecha(Date fecha) {
        if (fecha == null) {
            return null;
        }
        return fecha.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }
}
