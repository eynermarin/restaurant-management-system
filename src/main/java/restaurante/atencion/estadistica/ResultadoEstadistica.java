package restaurante.atencion.estadistica;

import java.util.ArrayList;

/**
 * Resultado de una consulta: los títulos de las columnas, las filas de la tabla
 * y un resumen corto (por ejemplo, el total). Si no hay datos, las filas vienen vacías.
 */
public class ResultadoEstadistica {

    private String[] columnas;
    private ArrayList<Object[]> filas;
    private String resumen;

    public ResultadoEstadistica(String[] columnas, ArrayList<Object[]> filas, String resumen) {
        this.columnas = columnas.clone();
        this.filas = new ArrayList<>(filas);
        this.resumen = resumen;
    }

    public String[] getColumnas() {
        return columnas.clone();
    }

    public ArrayList<Object[]> getFilas() {
        return new ArrayList<>(filas);
    }

    public String getResumen() {
        return resumen;
    }

    public boolean estaVacio() {
        return filas.isEmpty();
    }
}
