package restaurante.menu.busqueda;

import restaurante.menu.model.Categoria;
import restaurante.menu.model.OpcionMenu;

/** Deja pasar solo las opciones de una categoría. */
public class CriterioPorCategoria implements CriterioBusqueda {

    private Categoria categoria;

    public CriterioPorCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    @Override
    public boolean cumple(OpcionMenu opcion) {
        return opcion.getCategoria() == categoria;
    }

    @Override
    public String describir() {
        return "categoría " + categoria.getNombre();
    }
}
