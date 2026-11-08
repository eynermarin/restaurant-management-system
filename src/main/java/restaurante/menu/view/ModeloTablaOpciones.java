package restaurante.menu.view;

import java.util.ArrayList;
import javax.swing.table.AbstractTableModel;
import restaurante.menu.model.OpcionMenu;

/**
 * Le dice a un JTable qué mostrar de cada opción. En el GUI Builder se pone en
 * model, opción Custom code: new ModeloTablaOpciones()
 */
public class ModeloTablaOpciones extends AbstractTableModel {

    private String[] columnas = {"ID", "Nombre", "Categoría", "Descripción", "Calorías", "Precio (₡)", "Disponible"};
    private ArrayList<OpcionMenu> opciones = new ArrayList<>();

    public void setOpciones(ArrayList<OpcionMenu> opciones) {
        this.opciones = opciones;
        fireTableDataChanged(); // avisa a la tabla que se vuelva a dibujar
    }

    public OpcionMenu getOpcion(int fila) {
        return opciones.get(fila);
    }

    @Override
    public int getRowCount() {
        return opciones.size();
    }

    @Override
    public int getColumnCount() {
        return columnas.length;
    }

    @Override
    public String getColumnName(int columna) {
        return columnas[columna];
    }

    /** Con esto la tabla ordena bien los números y muestra un check en "Disponible". */
    @Override
    public Class<?> getColumnClass(int columna) {
        if (columna == 0 || columna == 4) {
            return Integer.class;
        }
        if (columna == 5) {
            return java.math.BigDecimal.class;
        }
        if (columna == 6) {
            return Boolean.class;
        }
        return String.class;
    }

    @Override
    public Object getValueAt(int fila, int columna) {
        OpcionMenu opcion = opciones.get(fila);
        switch (columna) {
            case 0:
                return opcion.getId();
            case 1:
                return opcion.getNombre();
            case 2:
                return opcion.getCategoria().getNombre();
            case 3:
                return opcion.getDescripcion();
            case 4:
                return opcion.getCalorias();
            case 5:
                return opcion.getPrecio();
            default:
                return opcion.isDisponible();
        }
    }
}
