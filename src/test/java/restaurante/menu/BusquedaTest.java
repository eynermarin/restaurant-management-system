package restaurante.menu;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import restaurante.menu.control.ControladorMenu;
import restaurante.menu.exception.DatoMenuInvalidoException;
import restaurante.menu.model.Categoria;
import restaurante.menu.model.OpcionMenu;

class BusquedaTest {

    private ControladorMenu controlador;

    @BeforeEach
    void preparar() {
        controlador = new ControladorMenu(); // trae el menú de ejemplo
    }

    private Categoria buscarCategoria(String nombre) {
        for (Categoria c : controlador.categorias()) {
            if (c.getNombre().equals(nombre)) {
                return c;
            }
        }
        throw new IllegalArgumentException(nombre);
    }

    @Test
    void sinFiltrosDevuelveTodoElMenu() throws Exception {
        ArrayList<OpcionMenu> todas = controlador.buscar("Todas", "", "", "", "", "", false);
        assertEquals(controlador.listarOpciones().size(), todas.size());
    }

    @Test
    void filtraPorCategoria() throws Exception {
        Categoria postres = buscarCategoria("Postres");
        ArrayList<OpcionMenu> resultado = controlador.buscar(postres, "", "", "", "", "", false);
        assertEquals(4, resultado.size());
        for (OpcionMenu opcion : resultado) {
            assertEquals(postres, opcion.getCategoria());
        }
    }

    @Test
    void textoIgnoraMayusculas() throws Exception {
        ArrayList<OpcionMenu> resultado = controlador.buscar("Todas", "ARROZ", "", "", "", "", false);
        assertEquals(1, resultado.size());
        assertEquals("Arroz blanco", resultado.get(0).getNombre());
    }

    @Test
    void combinaRangoDePrecioYCalorias() throws Exception {
        ArrayList<OpcionMenu> resultado = controlador.buscar("Todas", "", "", "1000", "", "100", false);
        assertTrue(resultado.size() > 0);
        for (OpcionMenu opcion : resultado) {
            assertTrue(opcion.getCalorias() <= 100);
            assertTrue(opcion.getPrecio().intValue() <= 1000);
        }
    }

    @Test
    void soloDisponiblesOcultaLasDesactivadas() throws Exception {
        assertEquals(1, controlador.buscar("Todas", "caprese", "", "", "", "", false).size());
        assertEquals(0, controlador.buscar("Todas", "caprese", "", "", "", "", true).size());
    }

    @Test
    void rangosInvalidos() {
        assertThrows(DatoMenuInvalidoException.class, () -> controlador.buscar("Todas", "", "", "", "500", "100", false));
        assertThrows(DatoMenuInvalidoException.class, () -> controlador.buscar("Todas", "", "abc", "", "", "", false));
        assertThrows(DatoMenuInvalidoException.class, () -> controlador.buscar("Todas", "", "", "", "-10", "", false));
    }
}
