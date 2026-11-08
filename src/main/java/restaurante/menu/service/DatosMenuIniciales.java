package restaurante.menu.service;

import java.math.BigDecimal;
import restaurante.menu.exception.DatoMenuInvalidoException;
import restaurante.menu.model.Categoria;

/**
 * Menú de ejemplo que se carga al iniciar la aplicación (el enunciado pide
 * opciones precargadas). Precios en colones.
 */
public final class DatosMenuIniciales {

    private DatosMenuIniciales() {
    }

    public static void cargar(CatalogoMenu catalogo) {
        try {
            Categoria proteinas = catalogo.agregarCategoria("Proteínas", true);
            Categoria guarniciones = catalogo.agregarCategoria("Guarniciones", false);
            Categoria ensaladas = catalogo.agregarCategoria("Ensaladas", false);
            Categoria postres = catalogo.agregarCategoria("Postres", false);
            Categoria bebidas = catalogo.agregarCategoria("Bebidas", false);

            catalogo.agregarOpcion("Pechuga de pollo a la plancha", proteinas, "Pechuga sazonada con hierbas", 280, precio("3800"));
            catalogo.agregarOpcion("Lomito de res", proteinas, "Corte de 200 g a término medio", 350, precio("5200"));
            catalogo.agregarOpcion("Filete de tilapia", proteinas, "Al ajillo con limón", 220, precio("4200"));
            catalogo.agregarOpcion("Chuleta de cerdo", proteinas, "Chuleta ahumada a la parrilla", 400, precio("4500"));
            catalogo.agregarOpcion("Tofu salteado", proteinas, "Tofu con salsa de soya y jengibre", 190, precio("3500"));

            catalogo.agregarOpcion("Arroz blanco", guarniciones, "Porción de arroz con culantro", 200, precio("1200"));
            catalogo.agregarOpcion("Puré de papa", guarniciones, "Puré cremoso con mantequilla", 230, precio("1500"));
            catalogo.agregarOpcion("Papas fritas", guarniciones, "Papas en bastón", 365, precio("1800"));
            catalogo.agregarOpcion("Frijoles molidos", guarniciones, "Frijol negro molido", 180, precio("1000"));
            catalogo.agregarOpcion("Plátano maduro", guarniciones, "Plátano maduro horneado", 250, precio("1300"));
            catalogo.agregarOpcion("Vegetales al vapor", guarniciones, "Brócoli, zanahoria y chayote", 80, precio("1200"));

            catalogo.agregarOpcion("Ensalada verde", ensaladas, "Lechuga, pepino y tomate", 60, precio("1500"));
            catalogo.agregarOpcion("Ensalada César", ensaladas, "Lechuga romana, crutones y aderezo César", 330, precio("2500"));
            catalogo.agregarOpcion("Repollo con tomate", ensaladas, "Repollo picado con tomate y limón", 50, precio("1000"));
            int caprese = catalogo.agregarOpcion("Ensalada caprese", ensaladas, "Tomate, mozzarella y albahaca", 250, precio("2800")).getId();
            catalogo.cambiarDisponibilidad(caprese, false);

            catalogo.agregarOpcion("Flan de coco", postres, "Flan casero con coco rallado", 300, precio("1800"));
            catalogo.agregarOpcion("Tres leches", postres, "Queque bañado en tres leches", 420, precio("2200"));
            catalogo.agregarOpcion("Ensalada de frutas", postres, "Frutas de temporada", 120, precio("1600"));
            catalogo.agregarOpcion("Helado de vainilla", postres, "Dos bolas de helado", 210, precio("1500"));

            catalogo.agregarOpcion("Agua", bebidas, "Botella de 500 ml", 0, precio("800"));
            catalogo.agregarOpcion("Fresco natural de cas", bebidas, "Vaso de 400 ml", 90, precio("1200"));
            catalogo.agregarOpcion("Gaseosa", bebidas, "Lata de 355 ml", 140, precio("1000"));
            catalogo.agregarOpcion("Café negro", bebidas, "Taza de café chorreado", 5, precio("900"));
            catalogo.agregarOpcion("Batido de fresa en leche", bebidas, "Vaso de 400 ml", 250, precio("1800"));
        } catch (DatoMenuInvalidoException e) {
            // Los datos de arriba son fijos y válidos; si falla es un error de programación.
            throw new IllegalStateException("Los datos iniciales del menú son inválidos: " + e.getMessage(), e);
        }
    }

    private static BigDecimal precio(String colones) {
        return new BigDecimal(colones);
    }
}
