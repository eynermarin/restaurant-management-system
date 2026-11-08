package restaurante.menu.service;

import java.util.ArrayList;
import java.util.Collections;
import restaurante.menu.exception.DatoMenuInvalidoException;
import restaurante.menu.exception.SinCombinacionesException;
import restaurante.menu.model.Categoria;
import restaurante.menu.model.Combinacion;
import restaurante.menu.model.OpcionMenu;
import restaurante.menu.model.ResultadoCombinaciones;

/**
 * Filtro calórico. Recibe una proteína y un máximo X de calorías, y arma todas
 * las comidas completas: la proteína + UNA opción de cada categoría activa
 * adicional, cuya suma de calorías sea menor o igual a X.
 *
 * Algoritmo (backtracking con poda):
 *  1. Se juntan las categorías activas que no son de proteína, cada una con sus
 *     opciones disponibles. Si alguna está vacía, no hay comida completa posible.
 *  2. Las opciones de cada categoría se ordenan de menos a más calorías.
 *  3. minimoQueFalta[i] = suma de la opción más liviana de cada categoría desde
 *     la i hasta la última. Es lo mínimo que todavía falta sumar.
 *  4. Se elige una opción de la categoría 0, luego una de la 1, etc. Antes de
 *     elegir se revisa: acumulado + opción + minimoQueFalta[i + 1] <= X.
 *     Si no se cumple, esa opción no cabe, y las siguientes tampoco (pesan
 *     igual o más), así que se deja de probar esa categoría (poda).
 *
 * Por qué es correcto: solo se descarta lo que con seguridad pasa de X, porque
 * se compara contra el mínimo real de lo que falta. No hay repetidas porque cada
 * combinación toma una sola opción por categoría, siempre en el mismo orden.
 */
public class GeneradorCombinaciones {

    private CatalogoMenu catalogo;

    // Datos de la búsqueda en curso (se reinician en cada llamada a generar).
    private ArrayList<ArrayList<OpcionMenu>> opcionesPorCategoria;
    private int[] minimoQueFalta;
    private OpcionMenu[] elegidas;
    private OpcionMenu proteina;
    private int maximo;
    private ArrayList<Combinacion> encontradas;
    private int pasosExplorados;
    private int ramasPodadas;

    public GeneradorCombinaciones(CatalogoMenu catalogo) {
        this.catalogo = catalogo;
    }

    public ResultadoCombinaciones generar(OpcionMenu proteina, int maximoCalorias)
            throws DatoMenuInvalidoException, SinCombinacionesException {
        validarEntrada(proteina, maximoCalorias);
        this.proteina = proteina;
        this.maximo = maximoCalorias;

        // Paso 1 y 2: categorías requeridas con sus opciones, de menos a más calorías.
        opcionesPorCategoria = new ArrayList<>();
        for (Categoria categoria : catalogo.getCategoriasActivas()) {
            if (categoria.esProteina()) {
                continue;
            }
            ArrayList<OpcionMenu> opciones = catalogo.opcionesDe(categoria, true);
            if (opciones.isEmpty()) {
                throw new SinCombinacionesException("La categoría " + categoria.getNombre()
                        + " no tiene opciones disponibles, así que no se puede armar una comida completa. "
                        + "Active alguna de sus opciones o desactive la categoría.");
            }
            opciones.sort((a, b) -> a.getCalorias() - b.getCalorias());
            opcionesPorCategoria.add(opciones);
        }

        // Paso 3: lo mínimo que falta sumar desde cada categoría hasta el final.
        int n = opcionesPorCategoria.size();
        minimoQueFalta = new int[n + 1];
        minimoQueFalta[n] = 0;
        for (int i = n - 1; i >= 0; i--) {
            int masLiviana = opcionesPorCategoria.get(i).get(0).getCalorias();
            minimoQueFalta[i] = minimoQueFalta[i + 1] + masLiviana;
        }

        int comidaMasLiviana = proteina.getCalorias() + minimoQueFalta[0];
        if (comidaMasLiviana > maximoCalorias) {
            throw new SinCombinacionesException("La comida completa más liviana con " + proteina.getNombre()
                    + " suma " + comidaMasLiviana + " kcal y el máximo indicado es " + maximoCalorias
                    + " kcal. Suba el máximo a " + comidaMasLiviana + " kcal o elija otra proteína.");
        }

        // Paso 4: probar opciones categoría por categoría.
        elegidas = new OpcionMenu[n];
        encontradas = new ArrayList<>();
        pasosExplorados = 0;
        ramasPodadas = 0;
        explorar(0, proteina.getCalorias());

        // Orden de presentación: calorías ascendentes y luego nombre (ver Combinacion.compareTo).
        Collections.sort(encontradas);
        return new ResultadoCombinaciones(encontradas, pasosExplorados, ramasPodadas);
    }

    /** Elige una opción de la categoría indicada y sigue con la siguiente. */
    private void explorar(int categoria, int caloriasAcumuladas) {
        if (categoria == elegidas.length) {
            guardarCombinacion();
            return;
        }
        ArrayList<OpcionMenu> opciones = opcionesPorCategoria.get(categoria);
        for (int i = 0; i < opciones.size(); i++) {
            OpcionMenu opcion = opciones.get(i);
            pasosExplorados++;
            int total = caloriasAcumuladas + opcion.getCalorias();
            if (total + minimoQueFalta[categoria + 1] > maximo) {
                // Poda: esta opción y las siguientes (más calóricas) ya no caben.
                ramasPodadas = ramasPodadas + (opciones.size() - i);
                break;
            }
            elegidas[categoria] = opcion;
            explorar(categoria + 1, total);
        }
    }

    private void guardarCombinacion() {
        ArrayList<OpcionMenu> comida = new ArrayList<>();
        comida.add(proteina);
        for (OpcionMenu opcion : elegidas) {
            comida.add(opcion);
        }
        encontradas.add(new Combinacion(comida));
    }

    private void validarEntrada(OpcionMenu proteina, int maximoCalorias) throws DatoMenuInvalidoException {
        if (proteina == null) {
            throw new DatoMenuInvalidoException("Elija una proteína.");
        }
        if (!proteina.getCategoria().esProteina()) {
            throw new DatoMenuInvalidoException(proteina.getNombre() + " no es una proteína.");
        }
        if (!proteina.isDisponible() || !proteina.getCategoria().isActiva()) {
            throw new DatoMenuInvalidoException(proteina.getNombre() + " no está disponible.");
        }
        if (maximoCalorias < 0) {
            throw new DatoMenuInvalidoException("El máximo de calorías no puede ser negativo.");
        }
    }
}
