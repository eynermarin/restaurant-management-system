
package restaurante.atencion.service;

import java.math.BigDecimal;
import java.util.ArrayList;

import restaurante.atencion.model.*;
import restaurante.atencion.exception.*;
import restaurante.compartido.ConsultaMenu;
import restaurante.compartido.VerificadorUsoOpcion;
import restaurante.menu.model.OpcionMenu;

public class ServicioAtencion implements VerificadorUsoOpcion {

    private final ArrayList<Mesa> mesas;
    private final ArrayList<Visita> historial;
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

        this.siguientePedido = 1;
        this.siguientePago = 1;

        for (int i = 1; i <= 8; i++) {
            mesas.add(new Mesa(i));
        }
    }

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

    public ArrayList<OpcionMenu> obtenerMenu() {
        return menu.opcionesDisponibles();
    }

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
} catch (restaurante.menu.exception.DatoMenuInvalidoException e) {
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

        try {
            Pago pago = new Pago(
                siguientePago,
                metodo,
                monto,
                referencia,
                alias
            );

            mesa.registrarPago(pago);
            siguientePago++;

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
    }

    @Override
    public boolean opcionUsadaEnPedidos(int idOpcion) {

        for (Mesa mesa : mesas) {

            Visita visita = mesa.getVisitaActual();

            if (visita != null && contieneOpcion(visita, idOpcion)) {
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
}