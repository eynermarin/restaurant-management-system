package restaurante.menu.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import restaurante.menu.busqueda.CriterioBusqueda;
import restaurante.menu.model.Categoria;
import restaurante.menu.model.OpcionMenu;

/**
 * Aplica criterios de búsqueda al catálogo. Solo conoce la interfaz
 * CriterioBusqueda (patrón Strategy): no sabe qué filtros concretos existen.
 */
public class BuscadorMenu {

    private CatalogoMenu catalogo;

    public BuscadorMenu(CatalogoMenu catalogo) {
        this.catalogo = catalogo;
    }

    /**
     * Devuelve las opciones que cumplen TODOS los criterios.
     * Si la lista de criterios está vacía, devuelve el menú completo.
     */
    public ArrayList<OpcionMenu> buscar(ArrayList<CriterioBusqueda> criterios) {
        ArrayList<OpcionMenu> resultado = new ArrayList<>();
        for (OpcionMenu opcion : catalogo.getOpciones()) {
            boolean cumpleTodos = true;
            for (CriterioBusqueda criterio : criterios) {
                if (!criterio.cumple(opcion)) {
                    cumpleTodos = false;
                }
            }
            if (cumpleTodos) {
                resultado.add(opcion);
            }
        }
        return resultado;
    }

    /** Agrupa opciones por categoría, en el orden de las categorías del menú. */
    public LinkedHashMap<Categoria, ArrayList<OpcionMenu>> agruparPorCategoria(ArrayList<OpcionMenu> opciones) {
        LinkedHashMap<Categoria, ArrayList<OpcionMenu>> grupos = new LinkedHashMap<>();
        for (Categoria categoria : catalogo.getCategorias()) {
            ArrayList<OpcionMenu> deEsta = new ArrayList<>();
            for (OpcionMenu opcion : opciones) {
                if (opcion.getCategoria() == categoria) {
                    deEsta.add(opcion);
                }
            }
            if (!deEsta.isEmpty()) {
                grupos.put(categoria, deEsta);
            }
        }
        return grupos;
    }

    /** Texto con los filtros usados, por ejemplo: "categoría Postres y calorías hasta 300 kcal". */
    public static String describir(ArrayList<CriterioBusqueda> criterios) {
        if (criterios.isEmpty()) {
            return "sin filtros (menú completo)";
        }
        String texto = "";
        for (int i = 0; i < criterios.size(); i++) {
            if (i > 0) {
                texto = texto + " y ";
            }
            texto = texto + criterios.get(i).describir();
        }
        return texto;
    }
}
