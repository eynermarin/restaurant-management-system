package restaurante.menu.control;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import restaurante.compartido.ConsultaMenu;
import restaurante.compartido.VerificadorUsoOpcion;
import restaurante.menu.busqueda.CriterioBusqueda;
import restaurante.menu.busqueda.CriterioPorCategoria;
import restaurante.menu.busqueda.CriterioPorDisponibilidad;
import restaurante.menu.busqueda.CriterioPorRangoCalorias;
import restaurante.menu.busqueda.CriterioPorRangoPrecio;
import restaurante.menu.busqueda.CriterioPorTexto;
import restaurante.menu.exception.DatoMenuInvalidoException;
import restaurante.menu.exception.OpcionEnUsoException;
import restaurante.menu.exception.SinCombinacionesException;
import restaurante.menu.model.Categoria;
import restaurante.menu.model.OpcionMenu;
import restaurante.menu.model.ResultadoCombinaciones;
import restaurante.menu.service.BuscadorMenu;
import restaurante.menu.service.CatalogoMenu;
import restaurante.menu.service.DatosMenuIniciales;
import restaurante.menu.service.GeneradorCombinaciones;

/**
 * Única clase que usan las pantallas del menú. Recibe los valores tal como
 * vienen de los componentes (textos, números, lo seleccionado), los convierte y
 * llama al servicio que corresponde. Las pantallas no tienen reglas: solo llaman
 * a estos métodos y muestran el resultado o el mensaje de la excepción.
 */
public class ControladorMenu {

    private CatalogoMenu catalogo;
    private BuscadorMenu buscador;
    private GeneradorCombinaciones generador;
    private String descripcionUltimaBusqueda = "sin filtros (menú completo)";

    /** Crea el menú con los datos de ejemplo ya cargados. */
    public ControladorMenu() {
        catalogo = new CatalogoMenu();
        DatosMenuIniciales.cargar(catalogo);
        buscador = new BuscadorMenu(catalogo);
        generador = new GeneradorCombinaciones(catalogo);
    }

    // ===================== Integración con atención (Persona 1) =====================

    /** Lo que el módulo de atención usa para ver el menú. */
    public ConsultaMenu getConsultaMenu() {
        return catalogo;
    }

    /** Atención se registra aquí para que el menú sepa qué opciones ya se pidieron. */
    public void setVerificadorUso(VerificadorUsoOpcion verificador) {
        catalogo.setVerificadorUso(verificador);
    }

    // ===================== Categorías =====================

    public ArrayList<Categoria> categorias() {
        return catalogo.getCategorias();
    }

    public Categoria agregarCategoria(String nombre, boolean esProteina) throws DatoMenuInvalidoException {
        return catalogo.agregarCategoria(nombre, esProteina);
    }

    public void cambiarEstadoCategoria(Categoria categoria, boolean activa) throws DatoMenuInvalidoException {
        catalogo.cambiarEstadoCategoria(categoria, activa);
    }

    // ===================== Administración de opciones =====================

    public ArrayList<OpcionMenu> listarOpciones() {
        return catalogo.getOpciones();
    }

    public OpcionMenu buscarOpcion(int id) throws DatoMenuInvalidoException {
        return catalogo.buscarOpcion(id);
    }

    /** categoria es lo seleccionado en el JComboBox; precioTexto lo escrito en el campo de precio. */
    public OpcionMenu agregarOpcion(String nombre, Object categoria, String descripcion, int calorias,
            String precioTexto) throws DatoMenuInvalidoException {
        return catalogo.agregarOpcion(nombre, comoCategoria(categoria), descripcion, calorias,
                leerPrecioObligatorio(precioTexto));
    }

    public OpcionMenu modificarOpcion(int id, String nombre, Object categoria, String descripcion, int calorias,
            String precioTexto) throws DatoMenuInvalidoException {
        return catalogo.modificarOpcion(id, nombre, comoCategoria(categoria), descripcion, calorias,
                leerPrecioObligatorio(precioTexto));
    }

    public OpcionMenu cambiarDisponibilidad(int id, boolean disponible) throws DatoMenuInvalidoException {
        return catalogo.cambiarDisponibilidad(id, disponible);
    }

    public void eliminarOpcion(int id) throws DatoMenuInvalidoException, OpcionEnUsoException {
        catalogo.eliminarOpcion(id);
    }

    // ===================== Búsqueda =====================

    /**
     * Arma un criterio por cada campo que el usuario llenó; los vacíos no filtran.
     * categoria puede ser "Todas" (un texto) para no filtrar por categoría.
     */
    public ArrayList<OpcionMenu> buscar(Object categoria, String texto, String precioMinimo, String precioMaximo,
            String caloriasMinimo, String caloriasMaximo, boolean soloDisponibles) throws DatoMenuInvalidoException {
        ArrayList<CriterioBusqueda> criterios = new ArrayList<>();

        if (categoria instanceof Categoria) {
            criterios.add(new CriterioPorCategoria((Categoria) categoria));
        }
        if (!estaVacio(texto)) {
            criterios.add(new CriterioPorTexto(texto));
        }
        if (!estaVacio(precioMinimo) || !estaVacio(precioMaximo)) {
            criterios.add(new CriterioPorRangoPrecio(leerNumero(precioMinimo, "precio mínimo"),
                    leerNumero(precioMaximo, "precio máximo")));
        }
        if (!estaVacio(caloriasMinimo) || !estaVacio(caloriasMaximo)) {
            criterios.add(new CriterioPorRangoCalorias(leerNumero(caloriasMinimo, "calorías mínimas"),
                    leerNumero(caloriasMaximo, "calorías máximas")));
        }
        if (soloDisponibles) {
            criterios.add(new CriterioPorDisponibilidad());
        }

        descripcionUltimaBusqueda = BuscadorMenu.describir(criterios);
        return buscador.buscar(criterios);
    }

    /** Explicación de los filtros de la última búsqueda, para mostrar en una etiqueta. */
    public String descripcionUltimaBusqueda() {
        return descripcionUltimaBusqueda;
    }

    /** Menú disponible agrupado por categoría (para el JTree). */
    public LinkedHashMap<Categoria, ArrayList<OpcionMenu>> menuPorCategoria() {
        return buscador.agruparPorCategoria(catalogo.opcionesDisponibles());
    }

    // ===================== Filtro calórico =====================

    /** Proteínas que se pueden elegir en el filtro calórico. */
    public ArrayList<OpcionMenu> proteinasDisponibles() {
        ArrayList<OpcionMenu> proteinas = new ArrayList<>();
        for (OpcionMenu opcion : catalogo.opcionesDisponibles()) {
            if (opcion.getCategoria().esProteina()) {
                proteinas.add(opcion);
            }
        }
        return proteinas;
    }

    /** proteina es lo seleccionado en el JComboBox de proteínas. */
    public ResultadoCombinaciones generarCombinaciones(Object proteina, int maximoCalorias)
            throws DatoMenuInvalidoException, SinCombinacionesException {
        if (!(proteina instanceof OpcionMenu)) {
            throw new DatoMenuInvalidoException("Elija una proteína.");
        }
        return generador.generar((OpcionMenu) proteina, maximoCalorias);
    }

    // ===================== Conversión de lo que escribe el usuario =====================

    private Categoria comoCategoria(Object seleccion) throws DatoMenuInvalidoException {
        if (!(seleccion instanceof Categoria)) {
            throw new DatoMenuInvalidoException("Debe elegir una categoría.");
        }
        return (Categoria) seleccion;
    }

    private BigDecimal leerPrecioObligatorio(String texto) throws DatoMenuInvalidoException {
        if (estaVacio(texto)) {
            throw new DatoMenuInvalidoException("El precio es obligatorio.");
        }
        return leerNumero(texto, "precio");
    }

    /** Convierte un texto como "2500" o "2500,50" en número. Si está vacío devuelve null. */
    private BigDecimal leerNumero(String texto, String campo) throws DatoMenuInvalidoException {
        if (estaVacio(texto)) {
            return null;
        }
        try {
            return new BigDecimal(texto.trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            throw new DatoMenuInvalidoException("El campo " + campo + " debe ser un número, por ejemplo 2500.");
        }
    }

    private boolean estaVacio(String texto) {
        return texto == null || texto.trim().isEmpty();
    }
}
