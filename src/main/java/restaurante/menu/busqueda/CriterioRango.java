package restaurante.menu.busqueda;

import java.math.BigDecimal;
import restaurante.menu.exception.DatoMenuInvalidoException;
import restaurante.menu.model.OpcionMenu;

/**
 * Clase abstracta para los filtros de rango (precio y calorías).
 * Aquí está lo que comparten: el mínimo, el máximo, la validación del rango y la
 * comparación. Cada subclase solo dice qué valor de la opción se compara.
 * Un mínimo o máximo en null significa "sin límite" de ese lado.
 */
public abstract class CriterioRango implements CriterioBusqueda {

    private String medida;
    private String unidad;
    private BigDecimal minimo;
    private BigDecimal maximo;

    protected CriterioRango(String medida, String unidad, BigDecimal minimo, BigDecimal maximo)
            throws DatoMenuInvalidoException {
        if (minimo == null && maximo == null) {
            throw new DatoMenuInvalidoException("Indique al menos un mínimo o un máximo de " + medida + ".");
        }
        if (esNegativo(minimo) || esNegativo(maximo)) {
            throw new DatoMenuInvalidoException("El rango de " + medida + " no admite números negativos.");
        }
        if (minimo != null && maximo != null && minimo.compareTo(maximo) > 0) {
            throw new DatoMenuInvalidoException("En " + medida + ", el mínimo no puede ser mayor que el máximo.");
        }
        this.medida = medida;
        this.unidad = unidad;
        this.minimo = minimo;
        this.maximo = maximo;
    }

    /** El valor de la opción que compara este filtro (su precio, sus calorías...). */
    protected abstract BigDecimal valorDe(OpcionMenu opcion);

    @Override
    public boolean cumple(OpcionMenu opcion) {
        BigDecimal valor = valorDe(opcion);
        if (minimo != null && valor.compareTo(minimo) < 0) {
            return false;
        }
        if (maximo != null && valor.compareTo(maximo) > 0) {
            return false;
        }
        return true;
    }

    @Override
    public String describir() {
        if (minimo == null) {
            return medida + " hasta " + maximo + " " + unidad;
        }
        if (maximo == null) {
            return medida + " desde " + minimo + " " + unidad;
        }
        return medida + " entre " + minimo + " y " + maximo + " " + unidad;
    }

    private static boolean esNegativo(BigDecimal numero) {
        return numero != null && numero.compareTo(BigDecimal.ZERO) < 0;
    }
}
