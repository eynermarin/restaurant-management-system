
package restaurante.atencion.model;

import java.util.ArrayList;
import restaurante.atencion.estado.*;

public class Mesa {

    private final int numero;
    private Visita visitaActual;
    private EstadoMesa estado;

    public Mesa(int numero) {

        if (numero <= 0) {
            throw new IllegalArgumentException(
                "El numero de mesa debe ser positivo."
            );
        }

        this.numero = numero;
        this.visitaActual = null;
        this.estado = new MesaDisponible();
    }

    public void abrirVisita(String mesero, ArrayList<String> alias) {
        estado.abrirVisita(this, mesero, alias);
    }

    public void agregarPedido(ItemPedido pedido) {
        estado.agregarPedido(this, pedido);
    }

    public void solicitarCuenta(ModalidadCuenta modalidad) {
        estado.solicitarCuenta(this, modalidad);
    }

    public void registrarPago(Pago pago) {
        estado.registrarPago(this, pago);
    }

    public Visita cerrarVisita() {
        return estado.cerrarVisita(this);
    }

    public void iniciarVisita(Visita visita) {

        if (visitaActual != null || visita == null
                || !estaDisponible()
                || visita.isCerrada()
                || visita.isCuentaSolicitada()) {

            throw new IllegalStateException(
                "No se puede iniciar la visita."
            );
        }

        this.visitaActual = visita;
    }

    public void finalizarVisita() {

        if (!(estado instanceof MesaPendientePago)
                || visitaActual == null
                || !visitaActual.isCerrada()) {

            throw new IllegalStateException(
                "No se puede liberar la mesa."
            );
        }

        this.visitaActual = null;
    }

    public void cambiarEstado(EstadoMesa nuevoEstado) {

        if (nuevoEstado == null) {
            throw new IllegalArgumentException(
                "El estado no puede ser nulo."
            );
        }

        boolean valida = false;

        // Disponible -> Ocupada
        if (estado instanceof MesaDisponible
                && nuevoEstado instanceof MesaOcupada) {

            valida = visitaActual != null
                    && !visitaActual.isCerrada()
                    && !visitaActual.isCuentaSolicitada();
        }

        // Ocupada -> Pendiente de pago
        else if (estado instanceof MesaOcupada
                && nuevoEstado instanceof MesaPendientePago) {

            valida = visitaActual != null
                    && visitaActual.isCuentaSolicitada()
                    && !visitaActual.isCerrada();
        }

        // Pendiente de pago -> Disponible
        else if (estado instanceof MesaPendientePago
                && nuevoEstado instanceof MesaDisponible) {

            valida = visitaActual == null;
        }

        if (!valida) {
            throw new IllegalStateException(
                "Transicion de estado no permitida."
            );
        }

        this.estado = nuevoEstado;
    }

    public int getNumero() {
        return numero;
    }

    public Visita getVisitaActual() {
        return visitaActual;
    }

    public boolean estaDisponible() {
        return estado instanceof MesaDisponible;
    }

    public String getEstado() {
        return estado.getNombre();
    }

    @Override
    public String toString() {
        return "Mesa " + numero;
    }
}
