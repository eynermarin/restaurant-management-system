
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

    public void abrirVisita(String mesero,
                            ArrayList<String> alias) {
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
                || !estaDisponible()) {
            throw new IllegalStateException(
                "No se puede iniciar la visita."
            );
        }

        this.visitaActual = visita;
    }

    public void finalizarVisita() {

        if (visitaActual == null || !visitaActual.isCerrada()) {
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
