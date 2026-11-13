# Sistema de atención y menú inteligente para restaurantes

Proyecto de Programación Orientada a Objetos. Aplicación de escritorio en Java (Swing) que
administra las mesas de un restaurante desde que llega un grupo hasta que paga y se va:
visitas, pedidos por comensal, cuenta conjunta o individual, pagos ficticios, y un menú por
categorías con búsquedas y un filtro que arma comidas completas sin pasar un límite de calorías.

| | |
|---|---|
| Equipo | Persona 1: atención (mesas, pedidos, pagos) · Persona 2: menú (catálogo, búsquedas, combinaciones) |
| Lenguaje | Java 17 o superior |
| Interfaz | Swing, hecha con NetBeans |
| Proyecto | Maven (NetBeans lo abre directo) |
| Pruebas | JUnit 5, 41 pruebas |
| Entrega | Antes del viernes 20 de noviembre de 2026 |

---

## 1. Cómo ejecutarlo

1. Abrir NetBeans → **File → Open Project** → elegir esta carpeta (la reconoce por el `pom.xml`).
2. Clic derecho al proyecto → **Clean and Build**. La primera vez descarga JUnit.
3. **Run Project (F6)**. Abre `restaurante.VentanaPrincipal`.
4. Para correr las pruebas: clic derecho al proyecto → **Test**.

**Sin NetBeans:** después de *Clean and Build* queda `target/SistemaRestaurante.jar`. Se abre con
doble clic o con `java -jar SistemaRestaurante.jar` (necesita Java 17 o superior).

Al iniciar ya hay datos de ejemplo: 5 categorías, 25 opciones de menú, 8 mesas libres y
10 visitas cerradas en los últimos 12 días (para que las estadísticas tengan datos).

## 2. Manual breve de uso

| Pestaña | Para qué sirve |
|---|---|
| **Menú** | Agregar, modificar, activar/desactivar y eliminar opciones. Clic en una fila para cargarla en el formulario. |
| **Búsqueda** | Ver el menú en árbol por categoría y filtrar por categoría, texto, rango de precio, rango de calorías y disponibilidad. |
| **Recomendaciones** | Elegir una proteína y un máximo de calorías, y ver todas las comidas completas que caben. A la derecha se activan o desactivan las categorías requeridas. |
| **Mesas** | Ver el estado de las 8 mesas y abrir una visita: mesa, mesero, cantidad de personas y alias separados por coma. |
| **Pedidos** | Elegir mesa y comensal, seleccionar un producto (se ven calorías y precio) y agregarlo. También anular un pedido con un motivo. |
| **Cuentas** | Pedir la cuenta (conjunta o individual), registrar pagos en efectivo o tarjeta y cerrar la visita. Abajo está la bitácora de eventos. |
| **Estadísticas** | Elegir una de las 3 consultas y un rango de fechas (por defecto, los últimos 30 días). Clic en un encabezado para ordenar. |

**Atender una visita completa:**
Mesas → abrir visita · Pedidos → agregar productos (se puede volver más veces) ·
Cuentas → Solicitar cuenta → Registrar pago hasta que el saldo sea 0 → Cerrar visita.
La mesa vuelve a quedar disponible.

## 3. Estructura del código

```
src/main/java/restaurante/
    VentanaPrincipal          ventana con las 7 pestañas (conecta los dos módulos)
    compartido/               interfaces entre módulos: ConsultaMenu, VerificadorUsoOpcion
    menu/                     Persona 2
        model/                Categoria, OpcionMenu, Combinacion, ResultadoCombinaciones
        busqueda/             CriterioBusqueda (Strategy) y sus criterios
        service/              CatalogoMenu, BuscadorMenu, GeneradorCombinaciones, DatosMenuIniciales
        control/              ControladorMenu
        exception/            DatoMenuInvalidoException, OpcionEnUsoException, SinCombinacionesException
        view/                 3 pantallas (GUI Builder: .form + .java) y modelos de tabla/árbol
    atencion/                 Persona 1
        model/                Mesa, Visita, Comensal, ItemPedido, Pago, ModalidadCuenta
        estado/               EstadoMesa (State) y sus 3 estados
        observador/           ObservadorVisita (Observer) y BitacoraEventos
        estadistica/          ConsultaEstadistica (Strategy) y las 3 consultas
        pago/                 MetodoPago (abstracta), PagoEfectivo, PagoTarjeta
        service/              ServicioAtencion, ServicioEstadisticas, DatosAtencionIniciales
        control/              ControladorAtencion, ControladorEstadisticas
        exception/            MesaNoDisponibleException, PedidoEnVisitaCerradaException, PagoInvalidoException
        view/                 PanelMesas, PanelPedidos, PanelCuenta, PanelEstadisticas (GUI Builder)
src/test/java/restaurante/    pruebas de los dos módulos
```

**Capas.** Cada módulo separa *modelo* (datos y sus reglas), *servicio* (operaciones),
*controlador* (traduce lo que viene de la pantalla) y *vista* (pantallas). Las pantallas no
deciden reglas: llaman al controlador y muestran el resultado o el mensaje del error.

**Cómo se conectan los módulos.** Solo por dos interfaces de `compartido`:

| Interfaz | La implementa | La usa | Para qué |
|---|---|---|---|
| `ConsultaMenu` | `CatalogoMenu` | `ServicioAtencion` | Listar y buscar opciones del menú al tomar pedidos |
| `VerificadorUsoOpcion` | `ServicioAtencion` | `CatalogoMenu` | Saber si una opción ya se pidió, para no eliminarla |

## 4. Diseño orientado a objetos

**Encapsulamiento.** Todos los atributos son privados. No hay setters sueltos: los cambios
pasan por métodos con nombre de acción (`actualizar`, `abrirVisita`, `registrarPago`) que
validan primero y modifican después, así un objeto nunca queda a medio cambiar. Las listas se
devuelven como copia. El estado de una mesa solo cambia por sus operaciones, nunca desde la
pantalla.

**Interfaces usadas por clientes reales.**
`CriterioBusqueda` (la usa `BuscadorMenu`), `ConsultaEstadistica` (la usa `ServicioEstadisticas`), `EstadoMesa` (la usa `Mesa`),
`ObservadorVisita` (la usa `ServicioAtencion`), `ConsultaMenu` y `VerificadorUsoOpcion`
(conectan los módulos) y `Comparable` (en `Combinacion`, para ordenar resultados).

**Clases abstractas.**
- `CriterioRango`: guarda mínimo y máximo, valida el rango y compara. `CriterioPorRangoPrecio`
  y `CriterioPorRangoCalorias` solo dicen qué valor de la opción miden.
- `MetodoPago`: valida que el monto sea positivo y no pase del saldo. `PagoEfectivo` genera la
  referencia sola; `PagoTarjeta` exige una referencia.

**Polimorfismo.** El buscador trata igual a los 5 criterios; el servicio de estadísticas trata
igual a sus 3 consultas; la mesa trata igual a sus 3 estados; el servicio trata igual a todos sus observadores y métodos de pago.

## 5. Patrones de diseño

### Strategy: criterios de búsqueda (menú)
- **Problema:** buscar por varios criterios combinables sin llenar el buscador de `if`.
- **Clases:** `CriterioBusqueda` (contrato), `CriterioPorCategoria`, `CriterioPorTexto`,
  `CriterioPorRangoPrecio`, `CriterioPorRangoCalorias`, `CriterioPorDisponibilidad`;
  `BuscadorMenu` las aplica y `ControladorMenu` decide cuáles usar según los campos llenos.
- **Flujo:** el controlador arma una lista de criterios → el buscador deja pasar las opciones que cumplen todos.
- **Ventaja:** un criterio nuevo es una clase nueva; el buscador no cambia.
- **Costo:** más clases pequeñas.
- **Alternativa considerada:** un método `buscar` con un parámetro por filtro y muchos `if`.
- **También se usa en las estadísticas:** cada consulta implementa `ConsultaEstadistica`; agregar una cuarta es crear una clase y sumarla en `ServicioEstadisticas`.

### State: estados de la mesa (atención)
- **Problema:** lo que se puede hacer con una mesa depende de en qué etapa está.
- **Clases:** `EstadoMesa` (contrato), `MesaDisponible`, `MesaOcupada`, `MesaPendientePago`; `Mesa` delega en su estado.
- **Flujo:** Disponible → `abrirVisita` → Ocupada → `solicitarCuenta` → Pendiente de pago →
  `cerrarVisita` (solo con saldo 0) → Disponible. Una operación no permitida lanza una excepción y la mesa no cambia.
- **Ventaja:** las reglas de cada etapa están en un solo lugar.
- **Costo:** una clase por estado.
- **Alternativa considerada:** un atributo `String estado` con `if` en cada método.

### Observer: avisos de la visita (atención)
- **Problema:** varias partes quieren enterarse de pedidos, pagos y cierres sin que el servicio las conozca.
- **Clases:** `ObservadorVisita` (contrato), `BitacoraEventos` (observador), `ServicioAtencion` (sujeto).
- **Flujo:** después de cada pedido, pago o cierre, el servicio avisa a todos sus observadores; la pantalla Cuentas muestra la bitácora.
- **Ventaja:** se agregan observadores sin tocar el servicio.
- **Costo:** cada observador recibe todos los avisos aunque solo le interese uno, y el flujo es más difícil de seguir al depurar.
- **Alternativa considerada:** que el servicio llame directamente a la pantalla.

## 6. Excepciones propias

Todas se lanzan **antes** de modificar datos, así el sistema queda igual que antes del
intento. Las pantallas las atienden mostrando el mensaje en un `JOptionPane`.

| Excepción | Se produce cuando… | La lanza | Qué queda igual |
|---|---|---|---|
| `DatoMenuInvalidoException` | Nombre vacío o repetido, calorías o precio negativos, rango mal formado, opción inexistente | `OpcionMenu`, `CatalogoMenu`, criterios, `ControladorMenu` | El catálogo |
| `OpcionEnUsoException` | Se intenta eliminar una opción que ya aparece en pedidos | `CatalogoMenu` | La opción y el historial |
| `SinCombinacionesException` | Una categoría requerida no tiene opciones, o ninguna comida cabe en X (el mensaje dice el mínimo necesario) | `GeneradorCombinaciones` | Nada se modifica |
| `MesaNoDisponibleException` | Abrir una mesa ocupada, con cuenta pendiente, o inexistente | Estados de la mesa, `ServicioAtencion` | La visita actual |
| `PedidoEnVisitaCerradaException` | Agregar o anular pedidos cuando la cuenta ya se pidió o la mesa no está ocupada | `ServicioAtencion` | Los pedidos y el total |
| `PagoInvalidoException` | Monto cero, negativo o mayor al saldo; método inexistente; tarjeta sin referencia | `MetodoPago`, `ServicioAtencion` | El saldo y la mesa |

## 7. Algoritmo de combinaciones por calorías

**Regla:** una comida válida es la proteína elegida más **una** opción disponible de cada
categoría activa que no es de proteína, y su suma de calorías es menor o igual a X.

Backtracking con poda (`GeneradorCombinaciones`):
1. Se juntan las categorías requeridas con sus opciones disponibles. Si una está vacía, no hay resultados.
2. Las opciones de cada categoría se ordenan de menos a más calorías.
3. `minimoQueFalta[i]` = suma de la opción más liviana de cada categoría desde la `i` hasta la última.
4. Si la proteína más `minimoQueFalta[0]` ya pasa de X, se avisa cuál es el mínimo necesario.
5. Se elige una opción por categoría. Antes de elegir se revisa
   `acumulado + opción + minimoQueFalta[i + 1] <= X`; si no se cumple, se corta esa
   categoría, porque las siguientes opciones pesan igual o más.
6. Los resultados se ordenan por calorías y, si empatan, por nombre.

- **Correcto:** solo se descarta lo que con seguridad pasa de X (se compara contra el mínimo real de lo que falta).
- **Sin repetidas:** cada combinación toma una sola opción por categoría, en orden fijo.
- **Evidencia:** una prueba lo compara contra fuerza bruta con el menú de ejemplo y da lo mismo.
  La pantalla muestra los pasos explorados y las opciones descartadas por la poda.
- **Complejidad:** en el peor caso revisa todas las combinaciones; con límites normales la poda corta la mayoría.

## 8. Estadísticas

Las 3 consultas leen solo el historial de visitas cerradas, reciben un rango de fechas
(ambas incluidas) y nunca modifican datos. Si no hay datos en el rango, la pantalla lo dice.

| Consulta | Fecha que usa | Cómo se calcula |
|---|---|---|
| Artículos más solicitados | Fecha del pedido | Suma las cantidades por artículo, sin contar los anulados; de más a menos pedido. La columna Categoría permite verlo por categoría. |
| Ventas cobradas por día | Fecha del pago | Suma los pagos de cada día, separados en efectivo y tarjeta. |
| Ventas por mesero | Fecha de cierre | Por mesero: visitas cerradas, personas atendidas, total cobrado y promedio por visita. |

Si la fecha inicial es posterior a la final, se rechaza la consulta con un mensaje.

## 9. Supuestos

- Una opción por categoría en cada combinación calórica.
- Toda categoría activa que no es de proteína es requerida. La categoría de proteínas no se puede desactivar.
- Sin impuestos ni propina: el total es la suma de los precios del menú.
- El precio se copia al momento del pedido; si luego cambia en el menú, el pedido conserva el precio viejo.
- Una opción que ya se pidió no se elimina, solo se desactiva.
- Anulación: solo antes de pedir la cuenta. El pedido no se borra; queda marcado como anulado, con motivo y fecha, y deja de sumar.
- Después de pedir la cuenta no se aceptan pedidos nuevos.
- Un pago no puede superar el saldo (no se maneja vuelto). En cuenta individual, cada comensal paga hasta su propio saldo.
- La referencia del efectivo se genera sola (`EF-...`); la de tarjeta la escribe el usuario.
- Hay 8 mesas fijas. El mesero se escribe a mano.

## 10. Limitaciones conocidas

- Los datos no se guardan entre ejecuciones: al cerrar la aplicación se pierden visitas e historial, y el menú vuelve al de ejemplo.
- Las pantallas Mesas, Pedidos y Cuentas están escritas en código (no tienen `.form`), así que no se editan en el Design de NetBeans.
- Las visitas de ejemplo se crean al iniciar con fechas relativas a hoy; sus opciones del menú quedan marcadas como usadas y no se pueden eliminar, solo desactivar.

## 11. Pruebas

| Clase | Qué revisa |
|---|---|
| `CatalogoMenuTest` (11) | Altas, cambios, bajas, nombres repetidos, datos inválidos sin modificar nada, opción en uso, categoría de proteínas |
| `BusquedaTest` (6) | Cada criterio, combinación de criterios y rangos inválidos |
| `GeneradorCombinacionesTest` (9) | Límite, orden, categoría vacía o desactivada, entradas inválidas, poda, comparación con fuerza bruta |
| `EstadisticasTest` (7) | Las 3 consultas, anulados, filtro por fechas, rango vacío o inválido, datos de ejemplo |
| `PruebasAtencionTest` (8) | Corre las pruebas de atención: mesa, estados, servicio, pagos, anulaciones, precio congelado, Observer y validaciones |

`PruebaInterfaz` abre solo las pantallas de atención y no corre en las pruebas automáticas.
