
package restaurante.atencion.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import restaurante.atencion.control.ControladorAtencion;
import restaurante.atencion.model.Mesa;
import restaurante.atencion.model.Visita;

public class PanelMesas extends JPanel {

    private ControladorAtencion controlador;

    private JTable tablaMesas;
    private DefaultTableModel modeloTabla;

    private JComboBox<Integer> comboMesas;
    private JTextField campoMesero;
    private JSpinner spinnerPersonas;
    private JTextField campoAlias;

    private JTextArea areaResumen;

    public PanelMesas() {

        setLayout(new BorderLayout(10, 10));

        inicializarComponentes();
    }

    private void inicializarComponentes() {

        JLabel titulo = new JLabel("GESTION DE MESAS");
        titulo.setHorizontalAlignment(SwingConstants.CENTER);

        add(titulo, BorderLayout.NORTH);

        modeloTabla = new DefaultTableModel(
            new Object[]{
                "Mesa", "Estado", "Mesero", "Personas"
            }, 0
        ) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };

        tablaMesas = new JTable(modeloTabla);
        tablaMesas.setSelectionMode(
            ListSelectionModel.SINGLE_SELECTION
        );

        add(new JScrollPane(tablaMesas), BorderLayout.CENTER);

        JPanel panelInferior = new JPanel(
            new BorderLayout(5, 5)
        );

        JPanel formulario = new JPanel(
            new GridLayout(4, 2, 8, 8)
        );

        comboMesas = new JComboBox<>();
        campoMesero = new JTextField();

        spinnerPersonas = new JSpinner(
            new SpinnerNumberModel(1, 1, 12, 1)
        );

        campoAlias = new JTextField();

        formulario.add(new JLabel("Mesa:"));
        formulario.add(comboMesas);

        formulario.add(new JLabel("Mesero:"));
        formulario.add(campoMesero);

        formulario.add(new JLabel("Cantidad de personas:"));
        formulario.add(spinnerPersonas);

        formulario.add(new JLabel("Alias separados por coma:"));
        formulario.add(campoAlias);

        panelInferior.add(formulario, BorderLayout.NORTH);

        JPanel panelBotones = new JPanel(new FlowLayout());

        JButton botonAbrir = new JButton("Abrir visita");
        JButton botonActualizar = new JButton("Actualizar");

        panelBotones.add(botonAbrir);
        panelBotones.add(botonActualizar);

        panelInferior.add(panelBotones, BorderLayout.CENTER);

        areaResumen = new JTextArea(5, 30);
        areaResumen.setEditable(false);

        panelInferior.add(
            new JScrollPane(areaResumen),
            BorderLayout.SOUTH
        );

        add(panelInferior, BorderLayout.SOUTH);

        botonAbrir.addActionListener(e -> abrirVisita());
        botonActualizar.addActionListener(e -> refrescar());

        tablaMesas.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                mostrarResumen();
            }
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

        modeloTabla.setRowCount(0);
        comboMesas.removeAllItems();

        for (Mesa mesa : controlador.mesas()) {

            Visita visita = mesa.getVisitaActual();

            String mesero = "";
            int personas = 0;

            if (visita != null) {
                mesero = visita.getMesero();
                personas = visita.getComensales().size();
            }

            modeloTabla.addRow(new Object[]{
                mesa.getNumero(),
                mesa.getEstado(),
                mesero,
                personas
            });

            if (mesa.estaDisponible()) {
                comboMesas.addItem(mesa.getNumero());
            }
        }

        mostrarResumen();
    }

    private void abrirVisita() {

        if (controlador == null) {
            return;
        }

        try {

            Integer numero = (Integer) comboMesas.getSelectedItem();

            if (numero == null) {
                throw new IllegalArgumentException(
                    "No hay mesas disponibles."
                );
            }

            String mesero = campoMesero.getText().trim();

            int cantidad = (Integer) spinnerPersonas.getValue();

            String[] nombres = campoAlias.getText().split(",");

            ArrayList<String> alias = new ArrayList<>();

            for (String nombre : nombres) {
                if (!nombre.trim().isEmpty()) {
                    alias.add(nombre.trim());
                }
            }

            if (alias.size() != cantidad) {
                throw new IllegalArgumentException(
                    "Debe ingresar exactamente " +
                    cantidad + " alias."
                );
            }

            controlador.abrirVisita(numero, mesero, alias);

            JOptionPane.showMessageDialog(
                this, "Visita abierta correctamente."
            );

            campoMesero.setText("");
            campoAlias.setText("");

            refrescar();

        } catch (RuntimeException e) {

            JOptionPane.showMessageDialog(
                this,
                e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void mostrarResumen() {

        if (controlador == null) {
            return;
        }

        int fila = tablaMesas.getSelectedRow();

        if (fila < 0) {
            areaResumen.setText("Seleccione una mesa.");
            return;
        }

        int numero = (Integer) modeloTabla.getValueAt(fila, 0);

        Visita visita = controlador.visitaActual(numero);

        if (visita == null) {
            areaResumen.setText(
                "Mesa " + numero + " disponible."
            );
            return;
        }

        StringBuilder texto = new StringBuilder();

        texto.append("Mesa: ").append(numero).append("\n");
        texto.append("Mesero: ")
             .append(visita.getMesero()).append("\n");

        texto.append("Comensales:\n");

        visita.getComensales().forEach(c ->
            texto.append("- ")
                 .append(c.getAlias())
                 .append("\n")
        );

        texto.append("Total: ")
             .append(visita.calcularTotal())
             .append("\n");

        texto.append("Saldo: ")
             .append(visita.saldoPendiente());

        areaResumen.setText(texto.toString());
    }
}
