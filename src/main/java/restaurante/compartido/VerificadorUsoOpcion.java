package restaurante.compartido;

/**
 * Contrato que el módulo de atención (Persona 1) implementa para que el menú
 * sepa si una opción ya aparece en algún pedido. Una opción usada no se elimina
 * (restricción de integridad); solo se puede desactivar.
 */
public interface VerificadorUsoOpcion {

    boolean opcionUsadaEnPedidos(int idOpcion);
}
