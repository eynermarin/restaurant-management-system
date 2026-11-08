package restaurante.menu.model;

import restaurante.menu.exception.DatoMenuInvalidoException;

/**
 * Categoría del menú (proteínas, guarniciones, ensaladas, ...).
 * Una categoría activa es obligatoria en las combinaciones del filtro calórico.
 */
public class Categoria {

    private String nombre;
    private boolean esProteina;
    private boolean activa;

    public Categoria(String nombre, boolean esProteina) throws DatoMenuInvalidoException {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new DatoMenuInvalidoException("La categoría debe tener un nombre.");
        }
        this.nombre = nombre.trim();
        this.esProteina = esProteina;
        this.activa = true;
    }

    public String getNombre() {
        return nombre;
    }

    /** Las proteínas son las que el cliente elige en el filtro calórico. */
    public boolean esProteina() {
        return esProteina;
    }

    public boolean isActiva() {
        return activa;
    }

    public void activar() {
        activa = true;
    }

    public void desactivar() {
        activa = false;
    }

    /** Es lo que muestra un JComboBox o un JList. */
    @Override
    public String toString() {
        return nombre;
    }
}
