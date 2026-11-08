package restaurante.compartido;

import java.util.ArrayList;
import restaurante.menu.exception.DatoMenuInvalidoException;
import restaurante.menu.model.OpcionMenu;

/**
 * Contrato que el módulo de atención (Persona 1) usa para consultar el menú.
 * Lo implementa CatalogoMenu; atención no debe depender de la clase concreta.
 */
public interface ConsultaMenu {

    /** Opciones que se pueden pedir ahora: disponibles y de una categoría activa. */
    ArrayList<OpcionMenu> opcionesDisponibles();

    /** Busca una opción por su id, esté o no disponible. */
    OpcionMenu buscarOpcion(int id) throws DatoMenuInvalidoException;
}
