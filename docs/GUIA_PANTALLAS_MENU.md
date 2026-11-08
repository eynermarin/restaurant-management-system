# Guía: pantallas del menú con el GUI Builder de NetBeans

> **Las pantallas ya están creadas.** `PanelAdministracionMenu`, `PanelBusquedaMenu`,
> `PanelRecomendaciones` y `VentanaPrincipal` existen como formularios del GUI Builder
> (archivos `.form` + `.java`). Ábrelos con doble clic y usa la pestaña **Design**
> para moverlos o cambiarlos. Esta guía explica cómo están armados, por si quieres
> modificarlos o rehacerlos tú mismo.
>
> Diferencia con lo que harías arrastrando: los paneles usan **BorderLayout,
> GridBagLayout y FlowLayout** en lugar de *Free Design*. Se editan igual en Design
> (clic derecho → *Set Layout* / *Customize Layout…*) y se acomodan solos al
> cambiar el tamaño de la ventana.

Toda la lógica ya está hecha en `restaurante.menu` (modelo, servicios, búsquedas,
combinaciones) y se usa a través de **una sola clase: `ControladorMenu`**.
Las pantallas solo hacen tres cosas en cada botón:

1. leer lo que el usuario escribió o eligió,
2. llamar **un** método de `controlador`,
3. mostrar el resultado o el mensaje del error.

Ninguna validación va en la pantalla: si un dato está mal, el controlador lanza
una excepción con un mensaje ya listo para mostrar.

---

## 0. Abrir el proyecto

1. **File → Open Project** y elegir la carpeta `D:\PROYECTO-restaurant-management-system`
   (NetBeans la reconoce como proyecto Maven por el `pom.xml`).
2. Clic derecho al proyecto → **Clean and Build**. La primera vez descarga JUnit.
3. Para correr las pruebas: clic derecho → **Test** (deben pasar las 26).

Las pantallas se crean en el paquete **`restaurante.menu.view`**: clic derecho al
paquete → **New → JPanel Form…**

### Cosas que se repiten en todas las pantallas

**Una tabla con modelo propio** (`JTable` dentro de su `JScrollPane`):
- Properties → `model` → botón `…` → arriba elegir **Custom code** → escribir
  `new ModeloTablaOpciones()` (o `new ModeloTablaCombinaciones()`).
- Properties → `autoCreateRowSorter` ✔ (ordenar haciendo clic en el encabezado).
- Properties → `selectionMode` → `SINGLE_SELECTION`.

**Un JComboBox que muestra objetos** (categorías, proteínas):
- Pestaña **Code** del componente → `Type Parameters` → `<Object>`.
- Properties → `model` → **Custom code** → `new javax.swing.DefaultComboBoxModel<>()`.

**Un JSpinner numérico**:
- Properties → `model` → `…` → **Number**, tipo **Integer**, con mínimo, máximo y paso.
  Así el usuario ni siquiera puede escribir letras o negativos.

**Cómo agregar un evento**: clic derecho al componente → **Events → Action →
actionPerformed** (o doble clic al botón). NetBeans crea el método y ahí se pega
el código de esta guía. El código gris (bloques protegidos) nunca se edita.

**El código fuera de los eventos** (campos y métodos de ayuda) se pega en la vista
**Source**, debajo del constructor, fuera de las zonas grises.

---

## 1. `PanelAdministracionMenu` — altas, cambios y bajas

### Diseño (pestaña Design)

| Componente (Palette) | Nombre de variable | Ajustes |
|---|---|---|
| JTable | `tblOpciones` | modelo `new ModeloTablaOpciones()` |
| JTextField | `txtNombre` | |
| JComboBox | `cmbCategoria` | `<Object>`, modelo vacío |
| JTextArea (en JScrollPane) | `txtDescripcion` | `lineWrap` ✔, `rows` 3 |
| JSpinner | `spnCalorias` | Integer, mín 0, máx 5000, paso 10 |
| JTextField | `txtPrecio` | `toolTipText`: "Ej.: 2500 o 2500.50" |
| JButton | `btnAgregar` | texto "Agregar" |
| JButton | `btnModificar` | texto "Guardar cambios" |
| JButton | `btnDisponibilidad` | texto "Activar / desactivar" |
| JButton | `btnEliminar` | texto "Eliminar" |
| JButton | `btnLimpiar` | texto "Limpiar" |
| JLabel | `lblEstado` | texto vacío (aquí salen los avisos) |

Sugerencia de acomodo: tabla a la izquierda, formulario a la derecha dentro de un
`JPanel` con borde **TitledBorder** "Datos de la opción", botones abajo.

Para renombrar la variable: clic derecho al componente → **Change Variable Name…**

### Código fuera de los eventos (Source)

```java
private ControladorMenu controlador;
private int idSeleccionado = 0;   // 0 = ninguna fila elegida

/** Lo llama la ventana principal al arrancar. */
public void setControlador(ControladorMenu controlador) {
    this.controlador = controlador;
    refrescar();
}

/** Recarga combos y tabla con lo que hay en el menú. */
public void refrescar() {
    if (controlador == null) return;   // la ventana aún se está armando
    cmbCategoria.setModel(new javax.swing.DefaultComboBoxModel<>(controlador.categorias().toArray()));
    modelo().setOpciones(controlador.listarOpciones());
    limpiarFormulario();
}

private ModeloTablaOpciones modelo() {
    return (ModeloTablaOpciones) tblOpciones.getModel();
}

private void limpiarFormulario() {
    idSeleccionado = 0;
    txtNombre.setText("");
    txtDescripcion.setText("");
    spnCalorias.setValue(0);
    txtPrecio.setText("");
    tblOpciones.clearSelection();
}

private int calorias() {
    try {
        spnCalorias.commitEdit();   // toma lo que el usuario escribió aunque no haya salido del campo
    } catch (java.text.ParseException e) {
        // texto inválido: el spinner conserva el último valor válido
    }
    return (Integer) spnCalorias.getValue();
}

private void mostrarError(Exception e) {
    javax.swing.JOptionPane.showMessageDialog(this, e.getMessage(), "No se pudo completar",
            javax.swing.JOptionPane.WARNING_MESSAGE);
}
```

Imports que NetBeans pide (o **Ctrl+Shift+I**):
`restaurante.menu.control.ControladorMenu`, `restaurante.menu.model.OpcionMenu`,
`restaurante.menu.exception.*`.

### Eventos

**`tblOpciones` → Events → Mouse → mouseClicked** (carga la fila en el formulario):
```java
int fila = tblOpciones.getSelectedRow();
if (fila < 0) return;
OpcionMenu o = modelo().getOpcion(tblOpciones.convertRowIndexToModel(fila));
idSeleccionado = o.getId();
txtNombre.setText(o.getNombre());
cmbCategoria.setSelectedItem(o.getCategoria());
txtDescripcion.setText(o.getDescripcion());
spnCalorias.setValue(o.getCalorias());
txtPrecio.setText(o.getPrecio().toPlainString());
```

**`btnAgregar` → actionPerformed**:
```java
try {
    OpcionMenu nueva = controlador.agregarOpcion(txtNombre.getText(), cmbCategoria.getSelectedItem(),
            txtDescripcion.getText(), calorias(), txtPrecio.getText());
    refrescar();
    lblEstado.setText("Se agregó «" + nueva.getNombre() + "».");
} catch (DatoMenuInvalidoException e) {
    mostrarError(e);
}
```

**`btnModificar` → actionPerformed**:
```java
try {
    controlador.modificarOpcion(idSeleccionado, txtNombre.getText(), cmbCategoria.getSelectedItem(),
            txtDescripcion.getText(), calorias(), txtPrecio.getText());
    refrescar();
    lblEstado.setText("Cambios guardados.");
} catch (DatoMenuInvalidoException e) {
    mostrarError(e);
}
```
(Si no hay fila elegida, `idSeleccionado` es 0 y el controlador responde
"No se encontró la opción. Seleccione una opción de la lista." La pantalla no
tiene que revisarlo.)

**`btnDisponibilidad` → actionPerformed**:
```java
try {
    OpcionMenu actual = controlador.buscarOpcion(idSeleccionado);
    OpcionMenu cambiada = controlador.cambiarDisponibilidad(idSeleccionado, !actual.isDisponible());
    refrescar();
    lblEstado.setText("«" + cambiada.getNombre() + "» ahora está "
            + (cambiada.isDisponible() ? "disponible." : "desactivada."));
} catch (DatoMenuInvalidoException e) {
    mostrarError(e);
}
```

**`btnEliminar` → actionPerformed** (con confirmación):
```java
int r = javax.swing.JOptionPane.showConfirmDialog(this, "¿Eliminar la opción seleccionada?",
        "Confirmar", javax.swing.JOptionPane.YES_NO_OPTION);
if (r != javax.swing.JOptionPane.YES_OPTION) return;
try {
    controlador.eliminarOpcion(idSeleccionado);
    refrescar();
    lblEstado.setText("Opción eliminada.");
} catch (DatoMenuInvalidoException | OpcionEnUsoException e) {
    mostrarError(e);
}
```

**`btnLimpiar` → actionPerformed**:
```java
limpiarFormulario();
```

---

## 2. `PanelBusquedaMenu` — búsquedas y menú por categoría

### Diseño

Arrastra un **JSplitPane**. A la izquierda un `JTree`, a la derecha un panel con
los filtros arriba y la tabla abajo.

| Componente | Variable | Ajustes |
|---|---|---|
| JTree (en JScrollPane, lado izquierdo del split) | `arbolMenu` | |
| JComboBox | `cmbFiltroCategoria` | `<Object>`, modelo vacío |
| JTextField | `txtTexto` | tooltip "Busca en nombre y descripción" |
| JTextField | `txtPrecioMin`, `txtPrecioMax` | vacío = sin límite |
| JTextField | `txtCaloriasMin`, `txtCaloriasMax` | vacío = sin límite |
| JCheckBox | `chkSoloDisponibles` | texto "Solo disponibles", `selected` ✔ |
| JButton | `btnBuscar` | texto "Buscar" |
| JButton | `btnLimpiarFiltros` | texto "Limpiar filtros" |
| JLabel | `lblFiltros` | muestra qué filtros se aplicaron |
| JTable | `tblResultados` | modelo `new ModeloTablaOpciones()` |

Agrupa los filtros en un `JPanel` con TitledBorder "Filtros". Si a `txtTexto`
también le agregas el evento `actionPerformed` con `buscar();`, la tecla
**Enter** busca sin tener que hacer clic.

### Código fuera de los eventos

```java
private ControladorMenu controlador;

public void setControlador(ControladorMenu controlador) {
    this.controlador = controlador;
    refrescar();
}

public void refrescar() {
    if (controlador == null) return;   // la ventana aún se está armando
    javax.swing.DefaultComboBoxModel<Object> categorias = new javax.swing.DefaultComboBoxModel<>();
    categorias.addElement("Todas");
    for (Categoria c : controlador.categorias()) {
        categorias.addElement(c);
    }
    cmbFiltroCategoria.setModel(categorias);
    arbolMenu.setModel(ArbolMenu.crearModelo(controlador.menuPorCategoria()));
    buscar();
}

private void buscar() {
    try {
        ((ModeloTablaOpciones) tblResultados.getModel()).setOpciones(controlador.buscar(
                cmbFiltroCategoria.getSelectedItem(), txtTexto.getText(),
                txtPrecioMin.getText(), txtPrecioMax.getText(),
                txtCaloriasMin.getText(), txtCaloriasMax.getText(),
                chkSoloDisponibles.isSelected()));
        lblFiltros.setText("Filtros: " + controlador.descripcionUltimaBusqueda()
                + " · " + tblResultados.getRowCount() + " resultados");
    } catch (DatoMenuInvalidoException e) {
        javax.swing.JOptionPane.showMessageDialog(this, e.getMessage(), "Filtro inválido",
                javax.swing.JOptionPane.WARNING_MESSAGE);
    }
}
```
Imports: `restaurante.menu.model.Categoria`, `restaurante.menu.exception.DatoMenuInvalidoException`.

("Todas" es un `String`, no una `Categoria`, por eso el controlador lo toma como
"sin filtro de categoría".)

### Eventos

**`btnBuscar` → actionPerformed** (y también `txtTexto` → actionPerformed):
```java
buscar();
```

**`btnLimpiarFiltros` → actionPerformed**:
```java
cmbFiltroCategoria.setSelectedIndex(0);
txtTexto.setText("");
txtPrecioMin.setText("");
txtPrecioMax.setText("");
txtCaloriasMin.setText("");
txtCaloriasMax.setText("");
buscar();
```

---

## 3. `PanelRecomendaciones` — filtro calórico y categorías requeridas

### Diseño

| Componente | Variable | Ajustes |
|---|---|---|
| JComboBox | `cmbProteina` | `<Object>`, modelo vacío |
| JSpinner | `spnMaxCalorias` | Integer, mín 0, máx 10000, paso 50, valor 900 |
| JButton | `btnGenerar` | texto "Buscar combinaciones" |
| JTable | `tblCombinaciones` | modelo `new ModeloTablaCombinaciones()` |
| JLabel | `lblResumen` | texto vacío |
| JList (en JScrollPane) | `lstCategorias` | Code → Type Parameters `<Object>`; `selectionMode` SINGLE |
| JButton | `btnCambiarCategoria` | texto "Activar / desactivar categoría" |
| JButton | `btnNuevaCategoria` | texto "Nueva categoría…" |

Pon la lista de categorías en un panel con TitledBorder "Categorías requeridas"
(toda categoría activa debe aparecer en cada combinación).

### Código fuera de los eventos

```java
private ControladorMenu controlador;

public void setControlador(ControladorMenu controlador) {
    this.controlador = controlador;
    refrescar();
}

public void refrescar() {
    if (controlador == null) return;   // la ventana aún se está armando
    cmbProteina.setModel(new javax.swing.DefaultComboBoxModel<>(controlador.proteinasDisponibles().toArray()));
    javax.swing.DefaultListModel<Object> lista = new javax.swing.DefaultListModel<>();
    for (Categoria c : controlador.categorias()) {
        lista.addElement(c + (c.isActiva() ? "  ✔ activa" : "  ✖ inactiva"));
    }
    lstCategorias.setModel(lista);
    ((ModeloTablaCombinaciones) tblCombinaciones.getModel()).limpiar();
    lblResumen.setText(" ");
}

private void mostrarError(Exception e) {
    javax.swing.JOptionPane.showMessageDialog(this, e.getMessage(), "Aviso",
            javax.swing.JOptionPane.INFORMATION_MESSAGE);
}
```
Imports: `restaurante.menu.model.Categoria`, `restaurante.menu.model.ResultadoCombinaciones`.

### Eventos

**`btnGenerar` → actionPerformed**:
```java
ModeloTablaCombinaciones modelo = (ModeloTablaCombinaciones) tblCombinaciones.getModel();
try {
    spnMaxCalorias.commitEdit();
} catch (java.text.ParseException e) {
    // se usa el último valor válido
}
try {
    ResultadoCombinaciones r = controlador.generarCombinaciones(cmbProteina.getSelectedItem(),
            (Integer) spnMaxCalorias.getValue());
    modelo.setCombinaciones(r.getCombinaciones());
    lblResumen.setText(r.resumen());
} catch (DatoMenuInvalidoException | SinCombinacionesException e) {
    modelo.limpiar();
    lblResumen.setText("Sin resultados. Cambie el máximo o las categorías.");
    mostrarError(e);
}
```

**`btnCambiarCategoria` → actionPerformed**:
```java
int i = lstCategorias.getSelectedIndex();
if (i < 0) return;
Categoria c = controlador.categorias().get(i);
try {
    controlador.cambiarEstadoCategoria(c, !c.isActiva());
    refrescar();
} catch (DatoMenuInvalidoException e) {
    mostrarError(e);
}
```

**`btnNuevaCategoria` → actionPerformed**:
```java
String nombre = javax.swing.JOptionPane.showInputDialog(this, "Nombre de la categoría:");
if (nombre == null) return;   // canceló
boolean esProteina = javax.swing.JOptionPane.showConfirmDialog(this, "¿Es una categoría de proteína?",
        "Tipo", javax.swing.JOptionPane.YES_NO_OPTION) == javax.swing.JOptionPane.YES_OPTION;
try {
    controlador.agregarCategoria(nombre, esProteina);
    refrescar();
} catch (DatoMenuInvalidoException e) {
    mostrarError(e);
}
```

---

## 4. Ventana principal (provisional, hasta integrar con atención)

1. Clic derecho al paquete **`restaurante`** → **New → JFrame Form…** →
   nombre **`VentanaPrincipal`** (el `pom.xml` ya la tiene como clase de arranque).
2. Arrastra un **JTabbedPane** que ocupe toda la ventana. Variable: `pestanas`.
3. **Clean and Build** para que NetBeans conozca tus paneles. Luego arrastra
   `PanelAdministracionMenu.java` desde el árbol **Projects** hasta el
   `JTabbedPane`; repite con los otros dos. Cambia el título de cada pestaña en
   Properties → `TabTitle` ("Menú", "Búsqueda", "Recomendaciones").
   Variables: `panelAdministracion`, `panelBusqueda`, `panelRecomendaciones`.
4. En Source, en el constructor, **después** de `initComponents();`:

```java
ControladorMenu controladorMenu = new ControladorMenu();
panelAdministracion.setControlador(controladorMenu);
panelBusqueda.setControlador(controladorMenu);
panelRecomendaciones.setControlador(controladorMenu);
setLocationRelativeTo(null);
```

5. **`pestanas` → Events → Change → stateChanged** (para que al cambiar de pestaña
   se vean los cambios hechos en otra):

```java
panelBusqueda.refrescar();
panelRecomendaciones.refrescar();
```
(NetBeans dispara este evento mientras arma la ventana, antes de que los paneles
tengan controlador; por eso cada `refrescar()` empieza con `if (controlador == null) return;`.)

6. **Run Project (F6)**.

Cuando tu compañero tenga su parte, en este mismo constructor se le pasa
`controladorMenu.getConsultaMenu()` a su módulo, y él registra su verificador con
`controladorMenu.setVerificadorUso(...)`.
