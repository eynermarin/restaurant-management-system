package restaurante.menu.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Lo que devuelve el filtro calórico: las combinaciones encontradas y dos
 * contadores que sirven para demostrar (en pantalla y en el video) que la poda
 * ahorra trabajo.
 */
public class ResultadoCombinaciones {

    private ArrayList<Combinacion> combinaciones;
    private int pasosExplorados;
    private int ramasPodadas;

    public ResultadoCombinaciones(List<Combinacion> combinaciones, int pasosExplorados, int ramasPodadas) {
        this.combinaciones = new ArrayList<>(combinaciones);
        this.pasosExplorados = pasosExplorados;
        this.ramasPodadas = ramasPodadas;
    }

    /** Ya vienen ordenadas por calorías y luego por nombre. */
    public ArrayList<Combinacion> getCombinaciones() {
        return new ArrayList<>(combinaciones);
    }

    public int getPasosExplorados() {
        return pasosExplorados;
    }

    public int getRamasPodadas() {
        return ramasPodadas;
    }

    /** Texto corto para una etiqueta de la pantalla. */
    public String resumen() {
        return combinaciones.size() + " combinaciones encontradas, "
                + pasosExplorados + " pasos explorados, "
                + ramasPodadas + " opciones descartadas por la poda";
    }
}
