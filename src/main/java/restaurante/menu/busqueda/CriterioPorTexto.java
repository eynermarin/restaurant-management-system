package restaurante.menu.busqueda;

import restaurante.menu.model.OpcionMenu;

/** Busca un texto dentro del nombre o la descripción, sin importar mayúsculas. */
public class CriterioPorTexto implements CriterioBusqueda {

    private String texto;

    public CriterioPorTexto(String texto) {
        this.texto = texto.trim().toLowerCase();
    }

    @Override
    public boolean cumple(OpcionMenu opcion) {
        String nombre = opcion.getNombre().toLowerCase();
        String descripcion = opcion.getDescripcion().toLowerCase();
        return nombre.contains(texto) || descripcion.contains(texto);
    }

    @Override
    public String describir() {
        return "texto \"" + texto + "\"";
    }
}
