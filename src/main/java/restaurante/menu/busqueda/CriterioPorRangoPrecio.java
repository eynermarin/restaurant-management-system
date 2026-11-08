package restaurante.menu.busqueda;

import java.math.BigDecimal;
import restaurante.menu.exception.DatoMenuInvalidoException;
import restaurante.menu.model.OpcionMenu;

/** Opciones cuyo precio está dentro del rango (los límites cuentan). */
public class CriterioPorRangoPrecio extends CriterioRango {

    public CriterioPorRangoPrecio(BigDecimal minimo, BigDecimal maximo) throws DatoMenuInvalidoException {
        super("precio", "colones", minimo, maximo);
    }

    @Override
    protected BigDecimal valorDe(OpcionMenu opcion) {
        return opcion.getPrecio();
    }
}
