package restaurante.menu;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import restaurante.menu.exception.DatoMenuInvalidoException;
import restaurante.menu.exception.SinCombinacionesException;
import restaurante.menu.model.Categoria;
import restaurante.menu.model.Combinacion;
import restaurante.menu.model.OpcionMenu;
import restaurante.menu.service.CatalogoMenu;
import restaurante.menu.service.DatosMenuIniciales;
import restaurante.menu.service.GeneradorCombinaciones;

/**
 * Menú pequeño de prueba:
 *   Proteína: Pollo 300
 *   Guarniciones: Ensalada 50, Arroz 200, Papas 400
 *   Bebidas: Agua 0, Gaseosa 150
 */
class GeneradorCombinacionesTest {

    private CatalogoMenu catalogo;
    private GeneradorCombinaciones generador;
    private Categoria guarniciones;
    private Categoria bebidas;
    private OpcionMenu pollo;

    @BeforeEach
    void preparar() throws Exception {
        catalogo = new CatalogoMenu();
        Categoria proteinas = catalogo.agregarCategoria("Proteínas", true);
        guarniciones = catalogo.agregarCategoria("Guarniciones", false);
        bebidas = catalogo.agregarCategoria("Bebidas", false);
        pollo = catalogo.agregarOpcion("Pollo", proteinas, "", 300, BigDecimal.TEN);
        catalogo.agregarOpcion("Arroz", guarniciones, "", 200, BigDecimal.TEN);
        catalogo.agregarOpcion("Ensalada", guarniciones, "", 50, BigDecimal.TEN);
        catalogo.agregarOpcion("Papas", guarniciones, "", 400, BigDecimal.TEN);
        catalogo.agregarOpcion("Agua", bebidas, "", 0, BigDecimal.TEN);
        catalogo.agregarOpcion("Gaseosa", bebidas, "", 150, BigDecimal.TEN);
        generador = new GeneradorCombinaciones(catalogo);
    }

    @Test
    void encuentraSoloLasQueCaben() throws Exception {
        // Con máximo 600 caben: Ensalada+Agua (350), Arroz+Agua (500), Ensalada+Gaseosa (500).
        ArrayList<Combinacion> resultado = generador.generar(pollo, 600).getCombinaciones();
        assertEquals(3, resultado.size());
        for (Combinacion c : resultado) {
            assertTrue(c.getTotalCalorias() <= 600);
            assertEquals(3, c.getOpciones().size()); // proteína + 1 guarnición + 1 bebida
        }
    }

    @Test
    void ordenPorCaloriasYLuegoNombre() throws Exception {
        ArrayList<Combinacion> resultado = generador.generar(pollo, 600).getCombinaciones();
        assertEquals("Pollo + Ensalada + Agua", resultado.get(0).descripcion());
        assertEquals("Pollo + Arroz + Agua", resultado.get(1).descripcion());      // 500, "Arroz" va antes
        assertEquals("Pollo + Ensalada + Gaseosa", resultado.get(2).descripcion()); // 500
    }

    @Test
    void elMaximoCuentaComoValido() throws Exception {
        assertEquals(1, generador.generar(pollo, 350).getCombinaciones().size());
    }

    @Test
    void avisaCuandoNadaCabe() {
        SinCombinacionesException e = assertThrows(SinCombinacionesException.class,
                () -> generador.generar(pollo, 349));
        assertTrue(e.getMessage().contains("350")); // le dice al usuario el mínimo que necesita
    }

    @Test
    void categoriaSinOpcionesNoPermiteCombinaciones() throws Exception {
        for (OpcionMenu bebida : catalogo.opcionesDe(bebidas, true)) {
            catalogo.cambiarDisponibilidad(bebida.getId(), false);
        }
        assertThrows(SinCombinacionesException.class, () -> generador.generar(pollo, 5000));
    }

    @Test
    void categoriaDesactivadaYaNoEsRequerida() throws Exception {
        catalogo.cambiarEstadoCategoria(bebidas, false);
        ArrayList<Combinacion> resultado = generador.generar(pollo, 5000).getCombinaciones();
        assertEquals(3, resultado.size());
        for (Combinacion c : resultado) {
            assertEquals(2, c.getOpciones().size()); // proteína + guarnición
        }
    }

    @Test
    void datosDeEntradaInvalidos() throws Exception {
        OpcionMenu arroz = catalogo.opcionesDe(guarniciones, true).get(0);
        assertThrows(DatoMenuInvalidoException.class, () -> generador.generar(arroz, 1000)); // no es proteína
        assertThrows(DatoMenuInvalidoException.class, () -> generador.generar(pollo, -1));
        assertThrows(DatoMenuInvalidoException.class, () -> generador.generar(null, 1000));
    }

    @Test
    void laPodaAhorraPasos() throws Exception {
        // Sin poda serían 3 guarniciones x 2 bebidas = 6 combinaciones completas por revisar.
        int pasos = generador.generar(pollo, 350).getPasosExplorados();
        assertTrue(pasos < 3 + 3 * 2, "pasos explorados: " + pasos);
    }

    /** Prueba contra fuerza bruta (probar TODAS las combinaciones) en el menú de ejemplo. */
    @Test
    void daLoMismoQueFuerzaBruta() throws Exception {
        CatalogoMenu menu = new CatalogoMenu();
        DatosMenuIniciales.cargar(menu);
        GeneradorCombinaciones gen = new GeneradorCombinaciones(menu);

        ArrayList<Categoria> adicionales = new ArrayList<>();
        ArrayList<OpcionMenu> proteinas = new ArrayList<>();
        for (Categoria c : menu.getCategoriasActivas()) {
            if (c.esProteina()) {
                proteinas.addAll(menu.opcionesDe(c, true));
            } else {
                adicionales.add(c);
            }
        }

        int[] maximos = {600, 800, 1000, 1400};
        for (OpcionMenu proteina : proteinas) {
            for (int maximo : maximos) {
                HashSet<String> esperadas = new HashSet<>();
                ArrayList<OpcionMenu> actual = new ArrayList<>();
                actual.add(proteina);
                fuerzaBruta(menu, adicionales, 0, actual, maximo, esperadas);

                ArrayList<String> obtenidas = new ArrayList<>();
                try {
                    for (Combinacion c : gen.generar(proteina, maximo).getCombinaciones()) {
                        obtenidas.add(c.descripcion());
                    }
                } catch (SinCombinacionesException e) {
                    // no hay resultados: obtenidas queda vacía
                }
                assertEquals(esperadas.size(), obtenidas.size(), "repetidas o faltantes con máximo " + maximo);
                assertEquals(esperadas, new HashSet<>(obtenidas));
            }
        }
    }

    private void fuerzaBruta(CatalogoMenu menu, ArrayList<Categoria> categorias, int i,
            ArrayList<OpcionMenu> actual, int maximo, HashSet<String> salida) {
        if (i == categorias.size()) {
            Combinacion c = new Combinacion(actual);
            if (c.getTotalCalorias() <= maximo) {
                salida.add(c.descripcion());
            }
            return;
        }
        for (OpcionMenu opcion : menu.opcionesDe(categorias.get(i), true)) {
            actual.add(opcion);
            fuerzaBruta(menu, categorias, i + 1, actual, maximo, salida);
            actual.remove(actual.size() - 1);
        }
    }
}
