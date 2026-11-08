package restaurante.menu.model;

import java.math.BigDecimal;
import restaurante.menu.exception.DatoMenuInvalidoException;

/**
 * Una opción individual del menú (por ejemplo "Arroz blanco").
 * No tiene setters sueltos: los datos solo cambian con actualizar(), que primero
 * valida todo y solo después asigna. Así la opción nunca queda a medio cambiar.
 */
public class OpcionMenu {

    private int id;
    private String nombre;
    private Categoria categoria;
    private String descripcion;
    private int calorias;
    private BigDecimal precio;
    private boolean disponible;

    public OpcionMenu(int id, String nombre, Categoria categoria, String descripcion,
            int calorias, BigDecimal precio) throws DatoMenuInvalidoException {
        this.id = id;
        this.disponible = true;
        actualizar(nombre, categoria, descripcion, calorias, precio);
    }

    /** Cambia los datos de la opción. Si alguno es inválido, no cambia nada. */
    public void actualizar(String nombre, Categoria categoria, String descripcion,
            int calorias, BigDecimal precio) throws DatoMenuInvalidoException {
        validar(nombre, categoria, calorias, precio);
        this.nombre = nombre.trim();
        this.categoria = categoria;
        this.descripcion = (descripcion == null) ? "" : descripcion.trim();
        this.calorias = calorias;
        this.precio = precio;
    }

    private static void validar(String nombre, Categoria categoria, int calorias, BigDecimal precio)
            throws DatoMenuInvalidoException {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new DatoMenuInvalidoException("El nombre de la opción es obligatorio.");
        }
        if (categoria == null) {
            throw new DatoMenuInvalidoException("Debe elegir una categoría.");
        }
        if (calorias < 0) {
            throw new DatoMenuInvalidoException("Las calorías no pueden ser negativas.");
        }
        if (precio == null) {
            throw new DatoMenuInvalidoException("El precio es obligatorio.");
        }
        if (precio.compareTo(BigDecimal.ZERO) < 0) {
            throw new DatoMenuInvalidoException("El precio no puede ser negativo.");
        }
    }

    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public int getCalorias() {
        return calorias;
    }

    /** Precio exacto en colones. Se usa BigDecimal y no double para no tener errores de redondeo. */
    public BigDecimal getPrecio() {
        return precio;
    }

    public boolean isDisponible() {
        return disponible;
    }

    /** Es lo que muestra un JComboBox o un JTree. */
    @Override
    public String toString() {
        return nombre + " (" + calorias + " kcal)";
    }
}
