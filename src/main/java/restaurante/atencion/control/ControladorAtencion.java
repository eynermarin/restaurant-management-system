
package restaurante.atencion.control;

import java.math.BigDecimal;
import java.util.ArrayList;

import restaurante.atencion.model.*;
import restaurante.atencion.service.ServicioAtencion;
import restaurante.atencion.observador.ObservadorVisita;
import restaurante.compartido.ConsultaMenu;
import restaurante.menu.model.OpcionMenu;

public class ControladorAtencion {

    private final ServicioAtencion servicio;

    public ControladorAtencion(ConsultaMenu menu) {
        this.servicio = new ServicioAtencion(menu);
    }

    // =========================
    // ACCESO AL SERVICIO
    // =========================

    public ServicioAtencion getServicio() {
        return servicio;
    }

    // =========================
    // GESTION DE MESAS
    // =========================

    public ArrayList<Mesa> mesas() {
        return servicio.getMesas();
    }

    public Mesa buscarMesa(int numeroMesa) {
        return servicio.buscarMesa(numeroMesa);
    }

    public String estadoMesa(int numeroMesa) {
        return servicio.buscarMesa(numeroMesa).getEstado();
    }

    public void abrirVisita(int numeroMesa,
                            String mesero,
                            ArrayList<String> clientes) {

        servicio.abrirVisita(
            numeroMesa, mesero, clientes
        );
    }

    public Visita visitaActual(int numeroMesa) {
        return servicio.buscarMesa(numeroMesa).getVisitaActual();
    }

    // =========================
    // CONSULTAS DEL MENU
    // =========================

    public ArrayList<OpcionMenu> opcionesDisponibles() {
        return servicio.obtenerMenu();
    }

    // =========================
    // GESTION DE PEDIDOS
    // =========================

    public void agregarPedido(int numeroMesa,
                              String aliasComensal,
                              int idOpcion,
                              int cantidad) {

        servicio.agregarPedido(
            numeroMesa,
            aliasComensal,
            idOpcion,
            cantidad
        );
    }

    public void anularPedido(int numeroMesa,
                             int idPedido,
                             String motivo) {

        servicio.anularPedido(
            numeroMesa, idPedido, motivo
        );
    }

    public ArrayList<ItemPedido> pedidos(int numeroMesa) {

        Visita visita = visitaActual(numeroMesa);

        if (visita == null) {
            return new ArrayList<>();
        }

        return visita.getPedidos();
    }

    public BigDecimal totalMesa(int numeroMesa) {
        return servicio.obtenerTotal(numeroMesa);
    }

    public BigDecimal subtotalComensal(int numeroMesa,
                                       String alias) {

        Visita visita = visitaActual(numeroMesa);

        if (visita == null) {
            throw new IllegalStateException(
                "La mesa no tiene una visita activa."
            );
        }

        return visita.subtotalComensal(alias);
    }

    /**
     * Subtotal de la mesa por categoría (sin contar anulados), en texto para la
     * pantalla. Por ejemplo: "Proteínas: 9000 · Bebidas: 800".
     */
    public String subtotalesPorCategoria(int numeroMesa) {

        Visita visita = visitaActual(numeroMesa);

        if (visita == null) {
            return "";
        }

        ArrayList<String> categorias = new ArrayList<>();

        for (ItemPedido pedido : visita.getPedidos()) {
            if (!pedido.isAnulado()
                    && !categorias.contains(pedido.getCategoria())) {
                categorias.add(pedido.getCategoria());
            }
        }

        String texto = "";

        for (String categoria : categorias) {
            if (!texto.isEmpty()) {
                texto = texto + " · ";
            }
            texto = texto + categoria + ": "
                    + visita.subtotalCategoria(categoria);
        }

        return texto;
    }

    // =========================
    // CUENTAS Y PAGOS
    // =========================

    public void solicitarCuenta(int numeroMesa,
                                boolean individual) {

        ModalidadCuenta modalidad;

        if (individual) {
            modalidad = ModalidadCuenta.INDIVIDUAL;
        } else {
            modalidad = ModalidadCuenta.CONJUNTA;
        }

        servicio.solicitarCuenta(numeroMesa, modalidad);
    }

    public void registrarPago(int numeroMesa,
                              String alias,
                              String metodo,
                              String montoTexto,
                              String referencia) {

        if (montoTexto == null ||
                montoTexto.trim().isEmpty()) {

            throw new IllegalArgumentException(
                "Debe ingresar un monto."
            );
        }

        BigDecimal monto;

        try {
            monto = new BigDecimal(montoTexto.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                "El monto ingresado no es valido."
            );
        }

        servicio.registrarPago(
            numeroMesa,
            alias,
            metodo,
            monto,
            referencia
        );
    }

    public BigDecimal saldoMesa(int numeroMesa) {
        return servicio.obtenerSaldo(numeroMesa);
    }

    public BigDecimal saldoComensal(int numeroMesa,
                                    String alias) {

        Visita visita = visitaActual(numeroMesa);

        if (visita == null) {
            throw new IllegalStateException(
                "La mesa no tiene una visita activa."
            );
        }

        return visita.saldoComensal(alias);
    }

    public ArrayList<Pago> pagos(int numeroMesa) {

        Visita visita = visitaActual(numeroMesa);

        if (visita == null) {
            return new ArrayList<>();
        }

        return visita.getPagos();
    }

    public void cerrarVisita(int numeroMesa) {
        servicio.cerrarVisita(numeroMesa);
    }

    // =========================
    // HISTORIAL
    // =========================

    public ArrayList<Visita> historial() {
        return servicio.getHistorial();
    }

    // =========================
    // PATRON OBSERVER
    // =========================

    public void agregarObservador(ObservadorVisita observador) {
        servicio.agregarObservador(observador);
    }

    public void eliminarObservador(ObservadorVisita observador) {
        servicio.eliminarObservador(observador);
    }
}
