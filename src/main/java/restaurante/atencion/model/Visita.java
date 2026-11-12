
package restaurante.atencion.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import restaurante.atencion.exception.PedidoEnVisitaCerradaException;

public class Visita {

    private final String mesero;
    private final LocalDateTime fechaApertura;
    private LocalDateTime fechaCierre;

    private final ArrayList<Comensal> comensales;
    private final ArrayList<ItemPedido> pedidos;
    private final ArrayList<Pago> pagos;

    private ModalidadCuenta modalidadCuenta;
    private boolean cuentaSolicitada;
    private boolean cerrada;

    public Visita(String mesero, ArrayList<String> alias) {

        if (mesero == null || mesero.trim().isEmpty()) {
            throw new IllegalArgumentException(
                "Debe indicar el mesero responsable."
            );
        }

        if (alias == null || alias.isEmpty()) {
            throw new IllegalArgumentException(
                "Debe registrar al menos un comensal."
            );
        }

        this.mesero = mesero.trim();
        this.fechaApertura = LocalDateTime.now();

        this.comensales = new ArrayList<>();
        this.pedidos = new ArrayList<>();
        this.pagos = new ArrayList<>();

        for (String nombre : alias) {

            Comensal nuevo = new Comensal(nombre);

            if (buscarComensal(nuevo.getAlias()) != null) {
                throw new IllegalArgumentException(
                    "No se permiten alias repetidos."
                );
            }

            comensales.add(nuevo);
        }

        this.cuentaSolicitada = false;
        this.cerrada = false;
    }

    public Comensal buscarComensal(String alias) {

        if (alias == null) {
            return null;
        }

        for (Comensal c : comensales) {
            if (c.getAlias().equalsIgnoreCase(alias.trim())) {
                return c;
            }
        }

        return null;
    }

    public void agregarPedido(ItemPedido pedido) {

        if (cerrada || cuentaSolicitada) {
            throw new IllegalStateException(
                "La visita no permite nuevos pedidos."
            );
        }

        if (pedido == null) {
            throw new IllegalArgumentException(
                "El pedido no puede ser nulo."
            );
        }

        if (buscarComensal(pedido.getAliasComensal()) == null) {
            throw new IllegalArgumentException(
                "El comensal no pertenece a esta visita."
            );
        }

        for (ItemPedido actual : pedidos) {
            if (actual.getId() == pedido.getId()) {
                throw new IllegalArgumentException(
                    "Ya existe un pedido con ese identificador."
                );
            }
        }

        pedidos.add(pedido);
    }

    public void anularPedido(int id, String motivo) {

        if (cerrada || cuentaSolicitada) {
            throw new IllegalStateException(
                "No se pueden anular pedidos en esta etapa."
            );
        }

        for (ItemPedido pedido : pedidos) {
            if (pedido.getId() == id) {
                pedido.anular(motivo);
                return;
            }
        }

        throw new IllegalArgumentException(
            "No se encontro el pedido."
        );
    }

    public BigDecimal subtotalComensal(String alias) {

        if (buscarComensal(alias) == null) {
            throw new IllegalArgumentException(
                "El comensal no existe."
            );
        }

        BigDecimal total = BigDecimal.ZERO;

        for (ItemPedido pedido : pedidos) {
            if (pedido.getAliasComensal().equalsIgnoreCase(
                    alias.trim())) {

                total = total.add(pedido.calcularSubtotal());
            }
        }

        return total;
    }

    public BigDecimal subtotalCategoria(String categoria) {

        if (categoria == null) {
            throw new IllegalArgumentException(
                "La categoria no puede ser nula."
            );
        }

        BigDecimal total = BigDecimal.ZERO;

        for (ItemPedido pedido : pedidos) {
            if (pedido.getCategoria().equalsIgnoreCase(
                    categoria.trim())) {

                total = total.add(pedido.calcularSubtotal());
            }
        }

        return total;
    }

    public BigDecimal calcularTotal() {

        BigDecimal total = BigDecimal.ZERO;

        for (ItemPedido pedido : pedidos) {
            total = total.add(pedido.calcularSubtotal());
        }

        return total;
    }

    public BigDecimal totalPagado() {

        BigDecimal total = BigDecimal.ZERO;

        for (Pago pago : pagos) {
            total = total.add(pago.getMonto());
        }

        return total;
    }

    public BigDecimal saldoPendiente() {
        return calcularTotal().subtract(totalPagado());
    }

    public BigDecimal saldoComensal(String alias) {

        BigDecimal saldo = subtotalComensal(alias);

        for (Pago pago : pagos) {
            if (pago.getAliasComensal() != null
                    && pago.getAliasComensal()
                        .equalsIgnoreCase(alias.trim())) {

                saldo = saldo.subtract(pago.getMonto());
            }
        }

        return saldo;
    }

    public void solicitarCuenta(ModalidadCuenta modalidad) {

        if (cerrada || cuentaSolicitada || modalidad == null) {
            throw new IllegalStateException(
                "No se puede solicitar la cuenta."
            );
        }

        this.modalidadCuenta = modalidad;
        this.cuentaSolicitada = true;
    }

    public void registrarPago(Pago pago) {

        if (!cuentaSolicitada || cerrada) {
            throw new IllegalStateException(
                "La cuenta no esta disponible para pagos."
            );
        }

        if (pago == null) {
            throw new IllegalArgumentException(
                "El pago no puede ser nulo."
            );
        }

        BigDecimal saldo;

        if (modalidadCuenta == ModalidadCuenta.INDIVIDUAL) {

            if (pago.getAliasComensal() == null) {
                throw new IllegalArgumentException(
                    "Debe indicar el comensal."
                );
            }

            saldo = saldoComensal(pago.getAliasComensal());

        } else {

            if (pago.getAliasComensal() != null
                    && !pago.getAliasComensal().isEmpty()) {
                throw new IllegalArgumentException(
                    "La cuenta conjunta no requiere comensal."
                );
            }

            saldo = saldoPendiente();
        }

        if (pago.getMonto().compareTo(saldo) > 0) {
            throw new IllegalArgumentException(
                "El pago supera el saldo pendiente."
            );
        }

        for (Pago actual : pagos) {
            if (actual.getId() == pago.getId()) {
                throw new IllegalArgumentException(
                    "Ya existe un pago con ese identificador."
                );
            }
        }

        pagos.add(pago);
    }

    public void cerrarVisita() {

        if (!cuentaSolicitada || cerrada) {
            throw new IllegalStateException(
                "No se puede cerrar esta visita."
            );
        }

        if (saldoPendiente().compareTo(BigDecimal.ZERO) != 0) {
            throw new IllegalStateException(
                "La cuenta tiene un saldo pendiente."
            );
        }

        this.cerrada = true;
        this.fechaCierre = LocalDateTime.now();
    }

    public ArrayList<Comensal> getComensales() {
        return new ArrayList<>(comensales);
    }

    public ArrayList<ItemPedido> getPedidos() {
        return new ArrayList<>(pedidos);
    }

    public ArrayList<Pago> getPagos() {
        return new ArrayList<>(pagos);
    }

    public String getMesero() {
        return mesero;
    }

    public LocalDateTime getFechaApertura() {
        return fechaApertura;
    }

    public LocalDateTime getFechaCierre() {
        return fechaCierre;
    }

    public ModalidadCuenta getModalidadCuenta() {
        return modalidadCuenta;
    }

    public boolean isCuentaSolicitada() {
        return cuentaSolicitada;
    }

    public boolean isCerrada() {
        return cerrada;
    }
}
