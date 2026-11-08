package restaurante.menu.busqueda;

import restaurante.menu.model.OpcionMenu;

/** Deja pasar solo las opciones disponibles. */
public class CriterioPorDisponibilidad implements CriterioBusqueda {

    @Override
    public boolean cumple(OpcionMenu opcion) {
        return opcion.isDisponible();
    }

    @Override
    public String describir() {
        return "solo disponibles";
    }
}
