package restaurante.menu.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import restaurante.compartido.ConsultaMenu;
import restaurante.compartido.VerificadorUsoOpcion;
import restaurante.menu.exception.DatoMenuInvalidoException;
import restaurante.menu.exception.OpcionEnUsoException;
import restaurante.menu.model.Categoria;
import restaurante.menu.model.OpcionMenu;

/**
 * Guarda las categorías y opciones del menú. Todas las altas, cambios y bajas
 * pasan por aquí, porque aquí están las reglas: nombres sin repetir y no
 * eliminar opciones que ya se usaron en pedidos.
 */
public class CatalogoMenu implements ConsultaMenu {

    private ArrayList<Categoria> categorias = new ArrayList<>();
    private ArrayList<OpcionMenu> opciones = new ArrayList<>();
    private int siguienteId = 1;
    // Lo pone el módulo de atención (Persona 1). Mientras sea null, ninguna opción se ha pedido.
    private VerificadorUsoOpcion verificadorUso = null;

    public void setVerificadorUso(VerificadorUsoOpcion verificadorUso) {
        this.verificadorUso = verificadorUso;
    }

    // ===================== Categorías =====================

    public Categoria agregarCategoria(String nombre, boolean esProteina) throws DatoMenuInvalidoException {
        Categoria nueva = new Categoria(nombre, esProteina);
        for (Categoria c : categorias) {
            if (c.getNombre().equalsIgnoreCase(nueva.getNombre())) {
                throw new DatoMenuInvalidoException("Ya existe la categoría " + c.getNombre() + ".");
            }
        }
        categorias.add(nueva);
        return nueva;
    }

    /** Copia de la lista, en el orden en que se crearon. */
    public ArrayList<Categoria> getCategorias() {
        return new ArrayList<>(categorias);
    }

    public ArrayList<Categoria> getCategoriasActivas() {
        ArrayList<Categoria> activas = new ArrayList<>();
        for (Categoria c : categorias) {
            if (c.isActiva()) {
                activas.add(c);
            }
        }
        return activas;
    }

    public void cambiarEstadoCategoria(Categoria categoria, boolean activa) throws DatoMenuInvalidoException {
        validarCategoria(categoria);
        if (!activa && categoria.esProteina()) {
            throw new DatoMenuInvalidoException("La categoría " + categoria.getNombre()
                    + " no se puede desactivar: el filtro calórico necesita elegir una proteína.");
        }
        if (activa) {
            categoria.activar();
        } else {
            categoria.desactivar();
        }
    }

    // ===================== Opciones =====================

    public OpcionMenu agregarOpcion(String nombre, Categoria categoria, String descripcion,
            int calorias, BigDecimal precio) throws DatoMenuInvalidoException {
        validarCategoria(categoria);
        validarNombreLibre(nombre, 0);
        OpcionMenu nueva = new OpcionMenu(siguienteId, nombre, categoria, descripcion, calorias, precio);
        opciones.add(nueva);
        siguienteId++;
        return nueva;
    }

    public OpcionMenu modificarOpcion(int id, String nombre, Categoria categoria, String descripcion,
            int calorias, BigDecimal precio) throws DatoMenuInvalidoException {
        OpcionMenu opcion = buscarOpcion(id);
        validarCategoria(categoria);
        validarNombreLibre(nombre, id);
        opcion.actualizar(nombre, categoria, descripcion, calorias, precio);
        return opcion;
    }

    public OpcionMenu cambiarDisponibilidad(int id, boolean disponible) throws DatoMenuInvalidoException {
        OpcionMenu opcion = buscarOpcion(id);
        opcion.setDisponible(disponible);
        return opcion;
    }

    /**
     * Elimina una opción solo si nunca se ha pedido. Si ya está en pedidos se
     * rechaza sin tocar nada; en ese caso lo correcto es desactivarla.
     */
    public void eliminarOpcion(int id) throws DatoMenuInvalidoException, OpcionEnUsoException {
        OpcionMenu opcion = buscarOpcion(id);
        if (verificadorUso != null && verificadorUso.opcionUsadaEnPedidos(id)) {
            throw new OpcionEnUsoException(opcion.getNombre() + " ya aparece en pedidos y no se puede eliminar. "
                    + "Puede desactivarla para que ya no se ofrezca.");
        }
        opciones.remove(opcion);
    }

    @Override
    public OpcionMenu buscarOpcion(int id) throws DatoMenuInvalidoException {
        for (OpcionMenu opcion : opciones) {
            if (opcion.getId() == id) {
                return opcion;
            }
        }
        throw new DatoMenuInvalidoException("No se encontró la opción. Seleccione una opción de la lista.");
    }

    /** Todas las opciones, agrupadas por categoría y ordenadas por nombre. */
    public ArrayList<OpcionMenu> getOpciones() {
        ArrayList<OpcionMenu> resultado = new ArrayList<>();
        for (Categoria categoria : categorias) {
            resultado.addAll(opcionesDe(categoria, false));
        }
        return resultado;
    }

    /** Lo que se puede pedir ahora: opciones disponibles de categorías activas. */
    @Override
    public ArrayList<OpcionMenu> opcionesDisponibles() {
        ArrayList<OpcionMenu> resultado = new ArrayList<>();
        for (Categoria categoria : categorias) {
            if (categoria.isActiva()) {
                resultado.addAll(opcionesDe(categoria, true));
            }
        }
        return resultado;
    }

    /** Opciones de una categoría ordenadas por nombre; si soloDisponibles, omite las desactivadas. */
    public ArrayList<OpcionMenu> opcionesDe(Categoria categoria, boolean soloDisponibles) {
        ArrayList<OpcionMenu> resultado = new ArrayList<>();
        for (OpcionMenu opcion : opciones) {
            if (opcion.getCategoria() == categoria && (opcion.isDisponible() || !soloDisponibles)) {
                resultado.add(opcion);
            }
        }
        resultado.sort((a, b) -> a.getNombre().compareToIgnoreCase(b.getNombre()));
        return resultado;
    }

    // ===================== Validaciones =====================

    private void validarCategoria(Categoria categoria) throws DatoMenuInvalidoException {
        if (categoria == null) {
            throw new DatoMenuInvalidoException("Debe elegir una categoría.");
        }
        if (!categorias.contains(categoria)) {
            throw new DatoMenuInvalidoException("La categoría " + categoria + " no existe en el menú.");
        }
    }

    /** idPropio es 0 al agregar; al modificar, la opción puede conservar su propio nombre. */
    private void validarNombreLibre(String nombre, int idPropio) throws DatoMenuInvalidoException {
        if (nombre == null) {
            return; // OpcionMenu se encarga de avisar que el nombre es obligatorio
        }
        for (OpcionMenu otra : opciones) {
            if (otra.getId() != idPropio && otra.getNombre().equalsIgnoreCase(nombre.trim())) {
                throw new DatoMenuInvalidoException("Ya existe una opción llamada " + otra.getNombre() + ".");
            }
        }
    }
}
