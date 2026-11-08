package restaurante.menu.busqueda;

import restaurante.menu.model.OpcionMenu;

/**
 * Patrón Strategy: cada criterio de búsqueda es una estrategia intercambiable.
 * BuscadorMenu solo conoce esta interfaz, así que para agregar un criterio nuevo
 * basta con crear otra clase que la implemente, sin tocar el buscador ni la vista.
 */
public interface CriterioBusqueda {

    boolean cumple(OpcionMenu opcion);

    /** Explicación legible del filtro, para mostrarla en pantalla. */
    String describir();
}
