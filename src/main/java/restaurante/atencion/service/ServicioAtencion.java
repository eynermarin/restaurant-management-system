
package restaurante.atencion.service;

import java.math.BigDecimal;
import java.util.ArrayList;

import restaurante.atencion.model.*;
import restaurante.atencion.exception.*;
import restaurante.atencion.observador.*;
import restaurante.atencion.pago.*;
import restaurante.compartido.ConsultaMenu;
import restaurante.compartido.VerificadorUsoOpcion;
import restaurante.menu.model.OpcionMenu;
import restaurante.menu.exception.DatoMenuInvalidoException;

public class ServicioAtencion implements VerificadorUsoOpcion {

    private final ArrayList<Mesa> mesas;
    private final ArrayList<Visita> historial;
    private final ArrayList<ObservadorVisita> observadores;
    private final ConsultaMenu menu;

    private int siguientePedido;
    private int siguientePago;

    public ServicioAtencion(ConsultaMenu menu) {

        if (menu == null) {
            throw new IllegalArgumentException(
                "El menu no puede ser nulo."
            );
        }

        this.menu = menu;
        this.mesas = new ArrayList<>();
        this.historial = new ArrayList<>();
        this.observadores = new ArrayList<>();

        this.siguientePedido = 1;
        this.siguientePago = 1;

        for (int i = 1; i <= 8; i++) {
            mesas.add(new Mesa(i));
        }
    }

    // =========================
    // GESTION DE MESAS
    // =========================

    public ArrayList<Mesa> getMesas() {
        return new ArrayList<>(mesas);
    }

    public ArrayList<Visita> getHistorial() {
        return new ArrayList<>(historial);
    }

    public Mesa buscarMesa(int numero) {

        for (Mesa mesa : mesas) {
            if (mesa.getNumero() == numero) {
                return mesa;
            }
        }

        throw new MesaNoDisponibleException(
            "No existe la mesa " + numero
        );
    }

    public void abrirVisita(int numero, String mesero,
                            ArrayList<String> clientes) {

        Mesa mesa = buscarMesa(numero);
        mesa.abrirVisita(mesero, clientes);
    }

    // =========================
    // GESTION DEL MENU
    // =========================

    public ArrayList<OpcionMenu> obtenerMenu() {
        return menu.opcionesDisponibles();
    }

    // =========================
    // GESTION DE PEDIDOS
    // =========================

    public void agregarPedido(int numeroMesa,
                              String alias,
                              int idOpcion,
                              int cantidad) {

        Mesa mesa = buscarMesa(numeroMesa);

        if (!mesa.getEstado().equals("OCUPADA")) {
            throw new PedidoEnVisitaCerradaException(
                "La mesa no permite nuevos pedidos."
            );
        }

        OpcionMenu opcion;

        try {
            opcion = menu.buscarOpcion(idOpcion);
        } catch (DatoMenuInvalidoException e) {
            throw new IllegalArgumentException(
                "La opcion del menu no existe: " + idOpcion,
                e
            );
        }

        if (!opcion.isDisponible()) {
            throw new IllegalArgumentException(
                "La opcion del menu no esta disponible."
            );
        }

        if (cantidad <= 0) {
            throw new IllegalArgumentException(
                "La cantidad debe ser positiva."
            );
        }

        ItemPedido pedido = new ItemPedido(
            siguientePedido,
            alias,
            opcion.getId(),
            opcion.getNombre(),
            String.valueOf(opcion.getCategoria()),
            cantidad,
            opcion.getPrecio()
        );

        mesa.agregarPedido(pedido);
        siguientePedido++;

        notificarPedido(numeroMesa, pedido);
    }

    public void anularPedido(int numeroMesa,
                             int idPedido,
                             String motivo) {

        Mesa mesa = buscarMesa(numeroMesa);

        if (!mesa.getEstado().equals("OCUPADA")) {
            throw new PedidoEnVisitaCerradaException(
                "No se pueden anular pedidos en esta etapa."
            );
        }

        mesa.getVisitaActual().anularPedido(idPedido, motivo);
    }

    public BigDecimal obtenerTotal(int numeroMesa) {

        Mesa mesa = buscarMesa(numeroMesa);

        if (mesa.getVisitaActual() == null) {
            return BigDecimal.ZERO;
        }

        return mesa.getVisitaActual().calcularTotal();
    }

    // =========================
    // CUENTAS Y PAGOS
    // =========================

    public void solicitarCuenta(int numeroMesa,
                                ModalidadCuenta modalidad) {

        Mesa mesa = buscarMesa(numeroMesa);
        mesa.solicitarCuenta(modalidad);
    }

    public void registrarPago(int numeroMesa,
                              String alias,
                              String metodo,
                              BigDecimal monto,
                              String referencia) {

        Mesa mesa = buscarMesa(numeroMesa);

        if (!mesa.getEstado().equals("PENDIENTE DE PAGO")) {
            throw new PagoInvalidoException(
                "La mesa no esta pendiente de pago."
            );
        }

        if (metodo == null) {
            throw new PagoInvalidoException(
                "Debe seleccionar un metodo de pago."
            );
        }

        MetodoPago tipoPago;

        switch (metodo.trim().toUpperCase()) {
            case "EFECTIVO":
                tipoPago = new PagoEfectivo();
                break;

            case "TARJETA":
                tipoPago = new PagoTarjeta();
                break;

            default:
                throw new PagoInvalidoException(
                    "Metodo de pago no disponible."
                );
        }

        Visita visita = mesa.getVisitaActual();

        try {
            BigDecimal saldo;

            if (visita.getModalidadCuenta()
                    == ModalidadCuenta.INDIVIDUAL) {

                if (alias == null || alias.trim().isEmpty()) {
                    throw new PagoInvalidoException(
                        "Debe seleccionar un comensal."
                    );
                }

                saldo = visita.saldoComensal(alias);

            } else {
                saldo = visita.saldoPendiente();
            }

            tipoPago.validar(monto, saldo);

            String referenciaFinal =
                tipoPago.generarReferencia(referencia);

            Pago pago = new Pago(
                siguientePago,
                tipoPago.getNombre(),
                monto,
                referenciaFinal,
                alias
            );

            mesa.registrarPago(pago);
            siguientePago++;

            notificarPago(numeroMesa, pago);

        } catch (IllegalArgumentException e) {
            throw new PagoInvalidoException(e.getMessage());
        }
    }

    public BigDecimal obtenerSaldo(int numeroMesa) {

        Mesa mesa = buscarMesa(numeroMesa);

        if (mesa.getVisitaActual() == null) {
            return BigDecimal.ZERO;
        }

        return mesa.getVisitaActual().saldoPendiente();
    }

    public void cerrarVisita(int numeroMesa) {

        Mesa mesa = buscarMesa(numeroMesa);

        Visita visita = mesa.cerrarVisita();

        historial.add(visita);

        notificarCierre(numeroMesa, visita);
    }

    /**
     * Agrega al historial una visita que ya fue cerrada fuera de una mesa.
     * Se usa para cargar los datos de ejemplo de las estadísticas.
     */
    public void registrarVisitaHistorica(Visita visita) {

        if (visita == null || !visita.isCerrada()) {
            throw new IllegalArgumentException(
                "Solo se pueden agregar al historial visitas cerradas."
            );
        }

        historial.add(visita);
    }

    // =========================
    // INTEGRACION CON EL MENU
    // =========================

    @Override
    public boolean opcionUsadaEnPedidos(int idOpcion) {

        for (Mesa mesa : mesas) {

            Visita visita = mesa.getVisitaActual();

            if (visita != null &&
                    contieneOpcion(visita, idOpcion)) {
                return true;
            }
        }

        for (Visita visita : historial) {
            if (contieneOpcion(visita, idOpcion)) {
                return true;
            }
        }

        return false;
    }

    private boolean contieneOpcion(Visita visita, int idOpcion) {

        for (ItemPedido pedido : visita.getPedidos()) {
            if (pedido.getIdOpcion() == idOpcion) {
                return true;
            }
        }

        return false;
    }

    // =========================
    // PATRON OBSERVER
    // =========================

    public void agregarObservador(ObservadorVisita observador) {

        if (observador == null) {
            throw new IllegalArgumentException(
                "El observador no puede ser nulo."
            );
        }

        if (!observadores.contains(observador)) {
            observadores.add(observador);
        }
    }

    public void eliminarObservador(ObservadorVisita observador) {
        observadores.remove(observador);
    }

    private void notificarPedido(int numeroMesa, ItemPedido pedido) {

        for (ObservadorVisita observador : observadores) {
            observador.pedidoRegistrado(numeroMesa, pedido);
        }
    }

    private void notificarPago(int numeroMesa, Pago pago) {

        for (ObservadorVisita observador : observadores) {
            observador.pagoRegistrado(numeroMesa, pago);
        }
    }

    private void notificarCierre(int numeroMesa, Visita visita) {

        for (ObservadorVisita observador : observadores) {
            observador.visitaCerrada(numeroMesa, visita);
        }
    }
}
