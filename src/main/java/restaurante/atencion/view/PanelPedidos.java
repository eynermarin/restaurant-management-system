
package restaurante.atencion.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import restaurante.atencion.control.ControladorAtencion;
import restaurante.atencion.model.*;
import restaurante.menu.model.OpcionMenu;

public class PanelPedidos extends JPanel {

    private ControladorAtencion controlador;

    private JComboBox<Integer> comboMesas;
    private JComboBox<String> comboComensales;

    private JTable tablaMenu;
    private JTable tablaPedidos;

    private DefaultTableModel modeloMenu;
    private DefaultTableModel modeloPedidos;

    private JSpinner spinnerCantidad;

    private JLabel etiquetaTotal;
    private JLabel etiquetaSubtotal;

    public PanelPedidos() {

        setLayout(new BorderLayout(10, 10));

        inicializarComponentes();
    }

    private void inicializarComponentes() {

        JLabel titulo = new JLabel("GESTION DE PEDIDOS");
        titulo.setHorizontalAlignment(SwingConstants.CENTER);

        add(titulo, BorderLayout.NORTH);

        JPanel panelCentral = new JPanel(
            new GridLayout(2, 1, 10, 10)
        );

        // TABLA DEL MENU

        modeloMenu = new DefaultTableModel(
            new Object[]{
                "ID", "Producto", "Categoria",
                "Calorias", "Precio"
            }, 0
        ) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };

        tablaMenu = new JTable(modeloMenu);
        tablaMenu.setSelectionMode(
            ListSelectionModel.SINGLE_SELECTION
        );

        JPanel panelMenu = new JPanel(new BorderLayout());

        panelMenu.add(
            new JLabel("PRODUCTOS DISPONIBLES"),
            BorderLayout.NORTH
        );

        panelMenu.add(
            new JScrollPane(tablaMenu),
            BorderLayout.CENTER
        );

        panelCentral.add(panelMenu);

        // TABLA DE PEDIDOS

        modeloPedidos = new DefaultTableModel(
            new Object[]{
                "ID", "Comensal", "Producto",
                "Cantidad", "Subtotal", "Estado"
            }, 0
        ) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };

        tablaPedidos = new JTable(modeloPedidos);
        tablaPedidos.setSelectionMode(
            ListSelectionModel.SINGLE_SELECTION
        );

        JPanel panelPedidos = new JPanel(new BorderLayout());

        panelPedidos.add(
            new JLabel("PEDIDOS DE LA MESA"),
            BorderLayout.NORTH
        );

        panelPedidos.add(
            new JScrollPane(tablaPedidos),
            BorderLayout.CENTER
        );

        panelCentral.add(panelPedidos);

        add(panelCentral, BorderLayout.CENTER);

        // CONTROLES

        JPanel panelInferior = new JPanel(
            new BorderLayout(5, 5)
        );

        JPanel formulario = new JPanel(
            new GridLayout(3, 2, 8, 8)
        );

        comboMesas = new JComboBox<>();
        comboComensales = new JComboBox<>();

        spinnerCantidad = new JSpinner(
            new SpinnerNumberModel(1, 1, 100, 1)
        );

        formulario.add(new JLabel("Mesa:"));
        formulario.add(comboMesas);

        formulario.add(new JLabel("Comensal:"));
        formulario.add(comboComensales);

        formulario.add(new JLabel("Cantidad:"));
        formulario.add(spinnerCantidad);

        panelInferior.add(formulario, BorderLayout.NORTH);

        // BOTONES

        JPanel panelBotones = new JPanel(new FlowLayout());

        JButton botonAgregar = new JButton("Agregar pedido");
        JButton botonAnular = new JButton("Anular pedido");
        JButton botonActualizar = new JButton("Actualizar");

        panelBotones.add(botonAgregar);
        panelBotones.add(botonAnular);
        panelBotones.add(botonActualizar);

        panelInferior.add(panelBotones, BorderLayout.CENTER);

        // TOTALES

        JPanel panelTotales = new JPanel(new FlowLayout());

        etiquetaSubtotal = new JLabel("Subtotal: 0");
        etiquetaTotal = new JLabel("Total mesa: 0");

        panelTotales.add(etiquetaSubtotal);
        panelTotales.add(etiquetaTotal);

        panelInferior.add(panelTotales, BorderLayout.SOUTH);

        add(panelInferior, BorderLayout.SOUTH);

        // EVENTOS

        botonAgregar.addActionListener(e -> agregarPedido());

        botonAnular.addActionListener(e -> anularPedido());

        botonActualizar.addActionListener(e -> refrescar());

        comboMesas.addActionListener(e -> {
            actualizarComensales();
            actualizarPedidos();
        });

        comboComensales.addActionListener(e -> {
            actualizarSubtotal();
        });
    }

    public void setControlador(ControladorAtencion controlador) {

        this.controlador = controlador;
        refrescar();
    }

    public void refrescar() {

        if (controlador == null) {
            return;
        }

        Integer seleccionAnterior =
            (Integer) comboMesas.getSelectedItem();

        comboMesas.removeAllItems();

        for (Mesa mesa : controlador.mesas()) {

            if (mesa.getVisitaActual() != null) {
                comboMesas.addItem(mesa.getNumero());
            }
        }

        if (seleccionAnterior != null) {
            comboMesas.setSelectedItem(seleccionAnterior);
        }

        actualizarMenu();
        actualizarComensales();
        actualizarPedidos();
    }

    private void actualizarMenu() {

        modeloMenu.setRowCount(0);

        for (OpcionMenu opcion : controlador.opcionesDisponibles()) {

            modeloMenu.addRow(new Object[]{
                opcion.getId(),
                opcion.getNombre(),
                opcion.getCategoria(),
                opcion.getCalorias(),
                opcion.getPrecio()
            });
        }
    }

    private void actualizarComensales() {

        comboComensales.removeAllItems();

        if (controlador == null) {
            return;
        }

        Integer numero = (Integer) comboMesas.getSelectedItem();

        if (numero == null) {
            return;
        }

        Visita visita = controlador.visitaActual(numero);

        if (visita == null) {
            return;
        }

        for (Comensal comensal : visita.getComensales()) {
            comboComensales.addItem(comensal.getAlias());
        }
    }

    private void actualizarPedidos() {

        modeloPedidos.setRowCount(0);

        if (controlador == null) {
            return;
        }

        Integer numero = (Integer) comboMesas.getSelectedItem();

        if (numero == null) {
            etiquetaTotal.setText("Total mesa: 0");
            etiquetaSubtotal.setText("Subtotal: 0");
            return;
        }

        ArrayList<ItemPedido> pedidos =
            controlador.pedidos(numero);

        for (ItemPedido pedido : pedidos) {

            modeloPedidos.addRow(new Object[]{
                pedido.getId(),
                pedido.getAliasComensal(),
                pedido.getNombreOpcion(),
                pedido.getCantidad(),
                pedido.calcularSubtotal(),
                pedido.isAnulado() ? "ANULADO" : "ACTIVO"
            });
        }

        etiquetaTotal.setText(
            "Total mesa: " + controlador.totalMesa(numero)
        );

        actualizarSubtotal();
    }

    private void actualizarSubtotal() {

        if (controlador == null) {
            return;
        }

        Integer numero = (Integer) comboMesas.getSelectedItem();
        String alias = (String) comboComensales.getSelectedItem();

        if (numero == null || alias == null) {
            etiquetaSubtotal.setText("Subtotal: 0");
            return;
        }

        etiquetaSubtotal.setText(
            "Subtotal " + alias + ": " +
            controlador.subtotalComensal(numero, alias)
        );
    }

    private void agregarPedido() {

        try {

            Integer numero = (Integer) comboMesas.getSelectedItem();
            String alias = (String) comboComensales.getSelectedItem();

            int fila = tablaMenu.getSelectedRow();

            if (numero == null || alias == null) {
                throw new IllegalArgumentException(
                    "Debe seleccionar una mesa y un comensal."
                );
            }

            if (fila < 0) {
                throw new IllegalArgumentException(
                    "Debe seleccionar un producto del menu."
                );
            }

            int filaModelo = tablaMenu.convertRowIndexToModel(fila);

            int idOpcion =
                (Integer) modeloMenu.getValueAt(filaModelo, 0);

            int cantidad = (Integer) spinnerCantidad.getValue();

            controlador.agregarPedido(
                numero, alias, idOpcion, cantidad
            );

            JOptionPane.showMessageDialog(
                this, "Pedido agregado correctamente."
            );

            actualizarPedidos();

        } catch (RuntimeException e) {

            JOptionPane.showMessageDialog(
                this,
                e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void anularPedido() {

        try {

            Integer numero = (Integer) comboMesas.getSelectedItem();

            int fila = tablaPedidos.getSelectedRow();

            if (numero == null || fila < 0) {
                throw new IllegalArgumentException(
                    "Debe seleccionar un pedido."
                );
            }

            int filaModelo =
                tablaPedidos.convertRowIndexToModel(fila);

            int idPedido =
                (Integer) modeloPedidos.getValueAt(filaModelo, 0);

            String motivo = JOptionPane.showInputDialog(
                this, "Motivo de anulacion:"
            );

            if (motivo == null) {
                return;
            }

            controlador.anularPedido(
                numero, idPedido, motivo
            );

            JOptionPane.showMessageDialog(
                this, "Pedido anulado correctamente."
            );

            actualizarPedidos();

        } catch (RuntimeException e) {

            JOptionPane.showMessageDialog(
                this,
                e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
}
