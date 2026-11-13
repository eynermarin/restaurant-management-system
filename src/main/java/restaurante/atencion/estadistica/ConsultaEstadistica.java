package restaurante.atencion.estadistica;

import java.time.LocalDate;
import java.util.ArrayList;
import restaurante.atencion.model.Visita;

/**
 * Una consulta estadística sobre el historial de visitas cerradas.
 * Cada consulta es una clase distinta (patrón Strategy): ServicioEstadisticas y
 * la pantalla las usan todas igual, sin saber cuál es cuál.
 */
public interface ConsultaEstadistica {

    /** Nombre que se muestra en el combo de la pantalla. */
    String getNombre();

    /** Explica cómo se calcula y qué fecha se usa para el rango. */
    String explicar();

    /** Calcula la consulta con las visitas cerradas entre desde y hasta (ambos incluidos). */
    ResultadoEstadistica calcular(ArrayList<Visita> historial, LocalDate desde, LocalDate hasta);
}
