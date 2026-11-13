package restaurante.atencion;

import org.junit.jupiter.api.Test;

/**
 * Corre con JUnit (clic derecho → Test) las pruebas del módulo de atención,
 * que están escritas como programas con main(). Si alguna comprobación falla,
 * esa prueba lanza AssertionError y JUnit la marca como fallida.
 * PruebaInterfaz no se incluye porque abre una ventana.
 */
class PruebasAtencionTest {

    private final String[] sinArgumentos = new String[0];

    @Test
    void mesa() {
        PruebaMesa.main(sinArgumentos);
    }

    @Test
    void estados() {
        PruebaEstados.main(sinArgumentos);
    }

    @Test
    void servicio() {
        PruebaServicio.main(sinArgumentos);
    }

    @Test
    void pagos() {
        PruebaPagos.main(sinArgumentos);
    }

    @Test
    void anulaciones() {
        PruebaAnulaciones.main(sinArgumentos);
    }

    @Test
    void precioCongelado() {
        PruebaPrecioCongelado.main(sinArgumentos);
    }

    @Test
    void observer() {
        PruebaObserver.main(sinArgumentos);
    }

    @Test
    void validaciones() {
        PruebaValidaciones.main(sinArgumentos);
    }
}
