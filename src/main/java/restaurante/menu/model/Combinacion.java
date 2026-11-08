package restaurante.menu.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Una comida completa sugerida: la proteína elegida más una opción de cada
 * categoría adicional. Implementa Comparable para poder ordenar los resultados
 * por calorías (y si empatan, por nombre).
 */
public class Combinacion implements Comparable<Combinacion> {

    private ArrayList<OpcionMenu> opciones;
    private int totalCalorias;
    private BigDecimal totalPrecio;

    public Combinacion(List<OpcionMenu> opciones) {
        this.opciones = new ArrayList<>(opciones);
        this.totalCalorias = 0;
        this.totalPrecio = BigDecimal.ZERO;
        for (OpcionMenu opcion : opciones) {
            totalCalorias = totalCalorias + opcion.getCalorias();
            totalPrecio = totalPrecio.add(opcion.getPrecio());
        }
    }

    /** Devuelve una copia, para que nadie cambie la combinación desde afuera. */
    public ArrayList<OpcionMenu> getOpciones() {
        return new ArrayList<>(opciones);
    }

    public int getTotalCalorias() {
        return totalCalorias;
    }

    public BigDecimal getTotalPrecio() {
        return totalPrecio;
    }

    /** Por ejemplo: "Pollo a la plancha + Arroz blanco + Ensalada verde". */
    public String descripcion() {
        String texto = "";
        for (int i = 0; i < opciones.size(); i++) {
            if (i > 0) {
                texto = texto + " + ";
            }
            texto = texto + opciones.get(i).getNombre();
        }
        return texto;
    }

    @Override
    public int compareTo(Combinacion otra) {
        if (this.totalCalorias != otra.totalCalorias) {
            return this.totalCalorias - otra.totalCalorias;
        }
        return this.descripcion().compareToIgnoreCase(otra.descripcion());
    }

    @Override
    public String toString() {
        return descripcion() + " - " + totalCalorias + " kcal";
    }
}
