package restaurante.menu.view;

import java.util.ArrayList;
import javax.swing.table.AbstractTableModel;
import restaurante.menu.model.Combinacion;

/**
 * Le dice a un JTable qué mostrar de cada combinación. En el GUI Builder se pone
 * en model, opción Custom code: new ModeloTablaCombinaciones()
 */
public class ModeloTablaCombinaciones extends AbstractTableModel {

    private String[] columnas = {"#", "Combinación", "Calorías", "Precio (₡)"};
    private ArrayList<Combinacion> combinaciones = new ArrayList<>();

    public void setCombinaciones(ArrayList<Combinacion> combinaciones) {
        this.combinaciones = combinaciones;
        fireTableDataChanged();
    }

    public void limpiar() {
        setCombinaciones(new ArrayList<>());
    }

    @Override
    public int getRowCount() {
        return combinaciones.size();
    }

    @Override
    public int getColumnCount() {
        return columnas.length;
    }

    @Override
    public String getColumnName(int columna) {
        return columnas[columna];
    }

    /** Con esto la tabla ordena bien las columnas de números. */
    @Override
    public Class<?> getColumnClass(int columna) {
        if (columna == 0 || columna == 2) {
            return Integer.class;
        }
        if (columna == 3) {
            return java.math.BigDecimal.class;
        }
        return String.class;
    }

    @Override
    public Object getValueAt(int fila, int columna) {
        Combinacion combinacion = combinaciones.get(fila);
        switch (columna) {
            case 0:
                return fila + 1;
            case 1:
                return combinacion.descripcion();
            case 2:
                return combinacion.getTotalCalorias();
            default:
                return combinacion.getTotalPrecio();
        }
    }
}
