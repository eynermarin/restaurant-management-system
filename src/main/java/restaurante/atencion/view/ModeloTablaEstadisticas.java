package restaurante.atencion.view;

import java.util.ArrayList;
import javax.swing.table.AbstractTableModel;
import restaurante.atencion.estadistica.ResultadoEstadistica;

/**
 * Le dice al JTable qué mostrar de un resultado estadístico. Las columnas
 * cambian según la consulta. En el GUI Builder se pone en model, opción
 * Custom code: new ModeloTablaEstadisticas()
 */
public class ModeloTablaEstadisticas extends AbstractTableModel {

    private String[] columnas = new String[0];
    private ArrayList<Object[]> filas = new ArrayList<>();

    public void setResultado(ResultadoEstadistica resultado) {
        columnas = resultado.getColumnas();
        filas = resultado.getFilas();
        fireTableStructureChanged(); // cambian las columnas, no solo los datos
    }

    public void limpiar() {
        columnas = new String[0];
        filas = new ArrayList<>();
        fireTableStructureChanged();
    }

    @Override
    public int getRowCount() {
        return filas.size();
    }

    @Override
    public int getColumnCount() {
        return columnas.length;
    }

    @Override
    public String getColumnName(int columna) {
        return columnas[columna];
    }

    /** Con esto la tabla ordena bien los números al hacer clic en el encabezado. */
    @Override
    public Class<?> getColumnClass(int columna) {
        if (filas.isEmpty() || filas.get(0)[columna] == null) {
            return Object.class;
        }
        return filas.get(0)[columna].getClass();
    }

    @Override
    public Object getValueAt(int fila, int columna) {
        return filas.get(fila)[columna];
    }
}
