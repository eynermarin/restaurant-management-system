package restaurante.menu;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import restaurante.menu.exception.DatoMenuInvalidoException;
import restaurante.menu.exception.OpcionEnUsoException;
import restaurante.menu.model.Categoria;
import restaurante.menu.model.OpcionMenu;
import restaurante.menu.service.CatalogoMenu;

class CatalogoMenuTest {

    private CatalogoMenu catalogo;
    private Categoria postres;

    @BeforeEach
    void preparar() throws Exception {
        catalogo = new CatalogoMenu();
        catalogo.agregarCategoria("Proteínas", true);
        postres = catalogo.agregarCategoria("Postres", false);
    }

    @Test
    void agregaOpcionValida() throws Exception {
        OpcionMenu flan = catalogo.agregarOpcion("Flan", postres, "Casero", 300, new BigDecimal("1800.50"));
        assertEquals(new BigDecimal("1800.50"), flan.getPrecio());
        assertTrue(flan.isDisponible());
        assertEquals(1, catalogo.getOpciones().size());
    }

    @Test
    void rechazaCaloriasNegativasSinAgregarNada() {
        assertThrows(DatoMenuInvalidoException.class,
                () -> catalogo.agregarOpcion("Flan", postres, "", -1, BigDecimal.ONE));
        assertTrue(catalogo.getOpciones().isEmpty());
    }

    @Test
    void rechazaPrecioNegativoYNombreVacio() {
        assertThrows(DatoMenuInvalidoException.class,
                () -> catalogo.agregarOpcion("Flan", postres, "", 10, new BigDecimal("-5")));
        assertThrows(DatoMenuInvalidoException.class,
                () -> catalogo.agregarOpcion("   ", postres, "", 10, BigDecimal.ONE));
    }

    @Test
    void rechazaNombreRepetidoSinImportarMayusculas() throws Exception {
        catalogo.agregarOpcion("Flan", postres, "", 300, BigDecimal.TEN);
        assertThrows(DatoMenuInvalidoException.class,
                () -> catalogo.agregarOpcion("  FLAN ", postres, "", 200, BigDecimal.ONE));
        assertEquals(1, catalogo.getOpciones().size());
    }

    @Test
    void rechazaCategoriaRepetida() {
        assertThrows(DatoMenuInvalidoException.class, () -> catalogo.agregarCategoria("postres", false));
    }

    @Test
    void noSePuedeDesactivarLaCategoriaDeProteinas() throws Exception {
        Categoria proteinas = catalogo.getCategorias().get(0);
        assertThrows(DatoMenuInvalidoException.class, () -> catalogo.cambiarEstadoCategoria(proteinas, false));
        assertTrue(proteinas.isActiva());
    }

    @Test
    void modificarConservaIdYDisponibilidad() throws Exception {
        int id = catalogo.agregarOpcion("Flan", postres, "", 300, BigDecimal.TEN).getId();
        catalogo.cambiarDisponibilidad(id, false);
        catalogo.modificarOpcion(id, "Flan de coco", postres, "Nuevo", 320, BigDecimal.ONE);
        OpcionMenu editada = catalogo.buscarOpcion(id);
        assertEquals("Flan de coco", editada.getNombre());
        assertFalse(editada.isDisponible());
    }

    @Test
    void modificarConDatoInvalidoNoCambiaNada() throws Exception {
        int id = catalogo.agregarOpcion("Flan", postres, "", 300, BigDecimal.TEN).getId();
        assertThrows(DatoMenuInvalidoException.class,
                () -> catalogo.modificarOpcion(id, "Flan nuevo", postres, "", -50, BigDecimal.ONE));
        assertEquals("Flan", catalogo.buscarOpcion(id).getNombre());
        assertEquals(300, catalogo.buscarOpcion(id).getCalorias());
    }

    @Test
    void opcionDesactivadaNoSeOfrece() throws Exception {
        int id = catalogo.agregarOpcion("Flan", postres, "", 300, BigDecimal.TEN).getId();
        catalogo.cambiarDisponibilidad(id, false);
        assertTrue(catalogo.opcionesDisponibles().isEmpty());
        assertEquals(1, catalogo.getOpciones().size());
    }

    @Test
    void noEliminaOpcionUsadaEnPedidos() throws Exception {
        int id = catalogo.agregarOpcion("Flan", postres, "", 300, BigDecimal.TEN).getId();
        catalogo.setVerificadorUso(idOpcion -> idOpcion == id); // simula que atención ya la pidió
        assertThrows(OpcionEnUsoException.class, () -> catalogo.eliminarOpcion(id));
        assertEquals(1, catalogo.getOpciones().size());
    }

    @Test
    void eliminaOpcionNoUsada() throws Exception {
        int id = catalogo.agregarOpcion("Flan", postres, "", 300, BigDecimal.TEN).getId();
        catalogo.eliminarOpcion(id);
        assertThrows(DatoMenuInvalidoException.class, () -> catalogo.buscarOpcion(id));
    }
}
