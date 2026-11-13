
package restaurante.atencion.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.math.BigDecimal;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import restaurante.atencion.control.ControladorAtencion;
import restaurante.atencion.model.*;
import restaurante.atencion.observador.BitacoraEventos;

public class PanelCuenta extends JPanel {

    private ControladorAtencion controlador;
    private BitacoraEventos bitacora;

    private JComboBox<Integer> comboMesas;
    private JComboBox<String> comboComensales;
    private JComboBox<String> comboMetodo;

    private JRadioButton radioConjunta;
    private JRadioButton radioIndividual;

    private JTextField campoMonto;
    private JTextField campoReferencia;

    private JTable tablaDetalle;
    private JTable tablaPagos;

    private DefaultTableModel modeloDetalle;
    private DefaultTableModel modeloPagos;

    private JLabel etiquetaTotal;
    private JLabel etiquetaSaldo;
    private JLabel etiquetaSaldoIndividual;

    private JTextArea areaBitacora;

    public PanelCuenta() {
        setLayout(new BorderLayout(10, 10));
        inicializarComponentes();
    }

    private void inicializarComponentes() {

        JLabel titulo = new JLabel("CUENTAS Y PAGOS");
        titulo.setHorizontalAlignment(SwingConstants.CENTER);
        add(titulo, BorderLayout.NORTH);

        // TABLAS

        modeloDetalle = new DefaultTableModel(
            new Object[]{"Comensal", "Subtotal", "Saldo"}, 0
        ) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };

        tablaDetalle = new JTable(modeloDetalle);

        modeloPagos = new DefaultTableModel(
            new Object[]{
                "ID", "Comensal", "Metodo", "Monto", "Referencia"
            }, 0
        ) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };

        tablaPagos = new JTable(modeloPagos);

        JPanel panelTablas = new JPanel(new GridLayout(2, 1, 8, 8));
        panelTablas.add(new JScrollPane(tablaDetalle));
        panelTablas.add(new JScrollPane(tablaPagos));

        add(panelTablas, BorderLayout.CENTER);

        // FORMULARIO

        JPanel panelInferior = new JPanel(new BorderLayout(5, 5));

        JPanel formulario = new JPanel(new GridLayout(5, 2, 8, 8));

        comboMesas = new JComboBox<>();
        comboComensales = new JComboBox<>();
        comboMetodo = new JComboBox<>(
            new String[]{"EFECTIVO", "TARJETA"}
        );

        campoMonto = new JTextField();
        campoReferencia = new JTextField();

        radioConjunta = new JRadioButton("Conjunta", true);
        radioIndividual = new JRadioButton("Individual");

        ButtonGroup grupoModalidad = new ButtonGroup();
        grupoModalidad.add(radioConjunta);
        grupoModalidad.add(radioIndividual);

        JPanel panelModalidad = new JPanel(new FlowLayout());
        panelModalidad.add(radioConjunta);
        panelModalidad.add(radioIndividual);

        formulario.add(new JLabel("Mesa:"));
        formulario.add(comboMesas);

        formulario.add(new JLabel("Modalidad:"));
        formulario.add(panelModalidad);

        formulario.add(new JLabel("Comensal:"));
        formulario.add(comboComensales);

        formulario.add(new JLabel("Metodo de pago:"));
        formulario.add(comboMetodo);

        formulario.add(new JLabel("Monto:"));
        formulario.add(campoMonto);

        panelInferior.add(formulario, BorderLayout.NORTH);

        JPanel panelReferencia = new JPanel(new FlowLayout());
        panelReferencia.add(new JLabel("Referencia:"));
        campoReferencia.setColumns(15);
        panelReferencia.add(campoReferencia);

        JPanel panelAcciones = new JPanel(new BorderLayout());
        panelAcciones.add(panelReferencia, BorderLayout.NORTH);

        JPanel botones = new JPanel(new FlowLayout());

        JButton botonSolicitar = new JButton("Solicitar cuenta");
        JButton botonPagar = new JButton("Registrar pago");
        JButton botonCerrar = new JButton("Cerrar visita");
        JButton botonActualizar = new JButton("Actualizar");

        botones.add(botonSolicitar);
        botones.add(botonPagar);
        botones.add(botonCerrar);
        botones.add(botonActualizar);

        panelAcciones.add(botones, BorderLayout.CENTER);

        JPanel panelTotales = new JPanel(new FlowLayout());

        etiquetaTotal = new JLabel("Total: 0");
        etiquetaSaldo = new JLabel("Saldo: 0");
        etiquetaSaldoIndividual = new JLabel("Saldo individual: 0");

        panelTotales.add(etiquetaTotal);
        panelTotales.add(etiquetaSaldo);
        panelTotales.add(etiquetaSaldoIndividual);

        panelAcciones.add(panelTotales, BorderLayout.SOUTH);
        panelInferior.add(panelAcciones, BorderLayout.CENTER);

        // BITACORA

        areaBitacora = new JTextArea(4, 30);
        areaBitacora.setEditable(false);

        JPanel panelBitacora = new JPanel(new BorderLayout());
        panelBitacora.add(new JLabel("BITACORA"), BorderLayout.NORTH);
        panelBitacora.add(
            new JScrollPane(areaBitacora), BorderLayout.CENTER
        );

        panelInferior.add(panelBitacora, BorderLayout.SOUTH);

        add(panelInferior, BorderLayout.SOUTH);

        // EVENTOS

        botonSolicitar.addActionListener(e -> solicitarCuenta());
        botonPagar.addActionListener(e -> registrarPago());
        botonCerrar.addActionListener(e -> cerrarVisita());
        botonActualizar.addActionListener(e -> refrescar());

        comboMesas.addActionListener(e -> actualizarDetalle());

        comboComensales.addActionListener(e -> actualizarSaldoIndividual());

        radioConjunta.addActionListener(e -> actualizarSaldoIndividual());
        radioIndividual.addActionListener(e -> actualizarSaldoIndividual());
    }

    public void setControlador(ControladorAtencion controlador) {

        if (this.controlador != null && bitacora != null) {
            this.controlador.eliminarObservador(bitacora);
        }

        this.controlador = controlador;

        if (controlador != null) {
            bitacora = new BitacoraEventos();
            controlador.agregarObservador(bitacora);
        }

        refrescar();
    }

    public void refrescar() {

        if (controlador == null) {
            return;
        }

        Integer anterior = (Integer) comboMesas.getSelectedItem();

        comboMesas.removeAllItems();

        for (Mesa mesa : controlador.mesas()) {
            if (mesa.getVisitaActual() != null) {
                comboMesas.addItem(mesa.getNumero());
            }
        }

        if (anterior != null) {
            comboMesas.setSelectedItem(anterior);
        }

        actualizarDetalle();
    }

    private void actualizarDetalle() {

        modeloDetalle.setRowCount(0);
        modeloPagos.setRowCount(0);
        comboComensales.removeAllItems();

        if (controlador == null) {
            return;
        }

        Integer numero = (Integer) comboMesas.getSelectedItem();

        if (numero == null) {
            etiquetaTotal.setText("Total: 0");
            etiquetaSaldo.setText("Saldo: 0");
            etiquetaSaldoIndividual.setText("Saldo individual: 0");
            return;
        }

        Visita visita = controlador.visitaActual(numero);

        if (visita == null) {
            return;
        }

        // Si ya se pidió la cuenta, mostrar la modalidad que se eligió
        if (visita.getModalidadCuenta() == ModalidadCuenta.INDIVIDUAL) {
            radioIndividual.setSelected(true);
        } else if (visita.getModalidadCuenta() == ModalidadCuenta.CONJUNTA) {
            radioConjunta.setSelected(true);
        }

        for (Comensal comensal : visita.getComensales()) {

            String alias = comensal.getAlias();

            comboComensales.addItem(alias);

            modeloDetalle.addRow(new Object[]{
                alias,
                visita.subtotalComensal(alias),
                visita.saldoComensal(alias)
            });
        }

        for (Pago pago : visita.getPagos()) {

            modeloPagos.addRow(new Object[]{
                pago.getId(),
                pago.getAliasComensal() == null
                    ? "CONJUNTA" : pago.getAliasComensal(),
                pago.getMetodo(),
                pago.getMonto(),
                pago.getReferencia()
            });
        }

        etiquetaTotal.setText(
            "Total: " + controlador.totalMesa(numero)
        );

        etiquetaSaldo.setText(
            "Saldo: " + controlador.saldoMesa(numero)
        );

        actualizarSaldoIndividual();
        actualizarBitacora();
    }

    private void actualizarSaldoIndividual() {

        if (controlador == null) {
            return;
        }

        Integer numero = (Integer) comboMesas.getSelectedItem();
        String alias = (String) comboComensales.getSelectedItem();

        if (numero == null || alias == null) {
            etiquetaSaldoIndividual.setText("Saldo individual: 0");
            return;
        }

        etiquetaSaldoIndividual.setText(
            "Saldo " + alias + ": " +
            controlador.saldoComensal(numero, alias)
        );
    }

    private void solicitarCuenta() {

        try {

            Integer numero = (Integer) comboMesas.getSelectedItem();

            if (numero == null) {
                throw new IllegalArgumentException(
                    "Seleccione una mesa."
                );
            }

            controlador.solicitarCuenta(
                numero, radioIndividual.isSelected()
            );

            JOptionPane.showMessageDialog(
                this, "Cuenta solicitada correctamente."
            );

            refrescar();

        } catch (RuntimeException e) {
            mostrarError(e);
        }
    }

    private void registrarPago() {

        try {

            Integer numero = (Integer) comboMesas.getSelectedItem();

            if (numero == null) {
                throw new IllegalArgumentException(
                    "Seleccione una mesa."
                );
            }

            Visita visita = controlador.visitaActual(numero);

            String alias = null;

            if (visita.getModalidadCuenta()
                    == ModalidadCuenta.INDIVIDUAL) {

                alias = (String) comboComensales.getSelectedItem();
            }

            controlador.registrarPago(
                numero,
                alias,
                (String) comboMetodo.getSelectedItem(),
                campoMonto.getText(),
                campoReferencia.getText()
            );

            JOptionPane.showMessageDialog(
                this, "Pago registrado correctamente."
            );

            campoMonto.setText("");
            campoReferencia.setText("");

            refrescar();

        } catch (RuntimeException e) {
            mostrarError(e);
        }
    }

    private void cerrarVisita() {

        try {

            Integer numero = (Integer) comboMesas.getSelectedItem();

            if (numero == null) {
                throw new IllegalArgumentException(
                    "Seleccione una mesa."
                );
            }

            controlador.cerrarVisita(numero);

            JOptionPane.showMessageDialog(
                this, "Visita cerrada correctamente."
            );

            refrescar();

        } catch (RuntimeException e) {
            mostrarError(e);
        }
    }

    private void actualizarBitacora() {

        if (bitacora != null) {
            areaBitacora.setText(bitacora.obtenerTexto());
        }
    }

    private void mostrarError(RuntimeException e) {

        JOptionPane.showMessageDialog(
            this,
            e.getMessage(),
            "Error",
            JOptionPane.ERROR_MESSAGE
        );
    }
}
