package restaurante.menu.busqueda;

import java.math.BigDecimal;
import restaurante.menu.exception.DatoMenuInvalidoException;
import restaurante.menu.model.OpcionMenu;

/** Opciones cuyas calorías están dentro del rango (los límites cuentan). */
public class CriterioPorRangoCalorias extends CriterioRango {

    public CriterioPorRangoCalorias(BigDecimal minimo, BigDecimal maximo) throws DatoMenuInvalidoException {
        super("calorías", "kcal", minimo, maximo);
    }

    @Override
    protected BigDecimal valorDe(OpcionMenu opcion) {
        return new BigDecimal(opcion.getCalorias());
    }
}
