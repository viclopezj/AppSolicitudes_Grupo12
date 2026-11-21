# AppSolicitudes_Grupo12

Aplicación móvil Android para la **gestión y seguimiento de solicitudes de clientes**, desarrollada para la asignatura **DSY1105 – Desarrollo de Aplicaciones Móviles** (Duoc UC, Escuela de Informática y Telecomunicaciones, CITT) como parte de la **tercera evaluación**.

Caso: *Gestión móvil y seguimiento de solicitudes de clientes* (Caso_DSY1105_005D).

## Integrantes

| Nombre | Sección | Grupo |
|--------|---------|-------|
| Vicente Lopez | 005D | 12 |
| Isaac Rivas | 005D | 12 |

Profesor: Vicente Julio Zapata Valenzuela

## Descripción del proyecto

Una pequeña empresa de servicios informáticos recibe hoy las solicitudes de sus clientes por WhatsApp, lo que dificulta el seguimiento, la trazabilidad y la visibilidad de cada requerimiento. Esta aplicación es un **Producto Mínimo Viable (MVP)** que representa cómo una solicitud puede quedar registrada, identificada con un código único, asignada a un responsable y disponible para consulta según el perfil de cada usuario.

Para esta etapa se amplió el **módulo de Solicitudes** con **tres perfiles** (cliente, administrador y técnico), un ciclo de vida de **8 estados** con reglas por perfil, consumo de una **API REST**, **avisos locales** y extras como búsqueda por texto, edición con reglas, eliminación lógica, evidencia con límites y un WhatsApp simulado, demostrando:

- Arquitectura MVVM real (View, ViewModel, Repository, DAO, Room).
- Persistencia local con Room (versión 5, con migraciones que conservan los datos).
- Modelo de negocio: estados, prioridad, historial, comentarios y reglas de transición, edición y eliminación por perfil (`ReglasTransicion`).
- Consumo de una API REST con Retrofit (categorías del formulario) con manejo de carga, error y lista vacía.
- Avisos locales (notificaciones de Android) cuando cambia una solicitud.
- Navegación con paso de argumentos y control de acceso por perfil.
- Formularios con validación controlada desde el ViewModel.
- Gestión de estado con `UiState` + `StateFlow`.
- Animaciones con la API de Compose y tema visual propio (claro y oscuro).
- Diseño adaptable según el tamaño de ventana (Window Size Classes).
- Recursos nativos: cámara (foto de evidencia y escáner QR), galería y notificaciones.
- Pruebas automatizadas: 2 unitarias y 2 de interfaz.

> Todos los datos utilizados son **ficticios**. La app no lee conversaciones reales ni se conecta a WhatsApp: el envío por WhatsApp es solo una simulación.

## Tecnologías

- Kotlin 2.2.10
- Jetpack Compose + Material Design 3 (tema propio: paleta verde azulada, tipografía y formas personalizadas; modo claro y oscuro según la configuración del sistema)
- Material 3 Adaptive 1.3.0 (Window Size Classes)
- Arquitectura MVVM (View, ViewModel, Repository, DAO, Room)
- Room 3.0.3 con `sqlite-bundled` 2.7.0 y `sqlite-async` (generación de código con KSP 2.3.6)
- Navigation Compose 2.9.8
- Corrutinas, `StateFlow` y `UiState`
- CameraX 1.4.2 + ML Kit Barcode Scanning 17.3.0 (lectura de QR)
- ZXing core 3.5.3 (generación de QR)
- Coil 3.3.0 (visualización de la foto de evidencia)
- Android Gradle Plugin 9.4.1, Gradle 9.6.0, Compose BOM 2026.02.01
- compileSdk 37, targetSdk 37, minSdk 24
- Retrofit 3.0.0 + converter-gson 3.0.0 (consumo de la API REST; OkHttp 4.12 viene incluido)
- Pruebas: Kotest 6.2.2, MockK 1.14.12, kotlinx-coroutines-test 1.10.2 y Compose UI Test

### Dependencias agregadas y para qué se usan

| Dependencia | Para qué se usa |
|-------------|-----------------|
| KSP 2.3.6 | Generador de código que Room necesita al compilar. |
| Room 3 (`room3-runtime`, `room3-compiler`) | Base de datos local: Entity, DAO y Database. |
| `sqlite-bundled` | Driver de SQLite obligatorio en Room 3. |
| `sqlite-async` | Aporta `executeSQL()` para escribir la migración de la base de datos. |
| Navigation Compose | Navegación entre pantallas (`NavHost` y `NavController`). |
| `lifecycle-viewmodel-compose` | Obtener el ViewModel con `viewModel()` en Compose. |
| `androidx.compose.material3.adaptive:adaptive` | Obtiene la clase de ancho de la ventana (`currentWindowAdaptiveInfoV2`) para adaptar navegación y pantallas. |
| `material-icons-core` | Íconos básicos de Material (Add, ArrowBack, Menu, Person, Lock, Edit, Create, Build, List, ExitToApp, Warning, Search, Close). |
| CameraX (`camera-core`, `camera-camera2`, `camera-lifecycle`, `camera-view`) | Abre la cámara y muestra su vista previa en el escáner QR. |
| ML Kit `barcode-scanning` | **Lee** el código QR a partir de las imágenes de la cámara. |
| ZXing `core` | **Genera** la imagen del código QR de cada solicitud. |
| Coil `coil-compose` | Carga y muestra en segundo plano la foto de evidencia. |
| Retrofit 3.0.0 | Cliente HTTP: define la API como una interfaz (`ApiService`) y hace las llamadas de red. |
| `converter-gson` 3.0.0 | Convierte el JSON de la API en objetos Kotlin (`ProductoDto`). |
| Kotest 6.2.2 (`runner-junit5`, `assertions-core`) | Pruebas unitarias con estilo `FunSpec` y comprobaciones legibles (`shouldBe`). |
| MockK 1.14.12 | Crea un `ApiService` simulado para probar el repositorio sin internet. |
| kotlinx-coroutines-test 1.10.2 | `runTest` para probar funciones `suspend`. |
| Compose UI Test (`ui-test-junit4`) | Pruebas de interfaz que se ejecutan en emulador o teléfono. |

Las notificaciones locales usan `NotificationCompat`, que viene con `androidx.core`, por lo que no requieren dependencias nuevas. El permiso `POST_NOTIFICATIONS` (Android 13+) se declara en el manifest y se pide al entrar al menú.

`TakePicture` y `PickVisualMedia` vienen con `activity-compose`, `FileProvider` con `androidx.core` y `org.json` (texto del QR) con Android, por lo que no requieren dependencias nuevas.

## Perfiles y cuentas de prueba

Las cuentas ficticias se crean automáticamente en Room la primera vez que alguien inicia sesión (si no existen). Todas usan la clave `1234`.

| Perfil | Usuarios | Qué puede hacer |
|--------|----------|-----------------|
| Cliente | `demo`, `cliente2`, `cliente3` | Registrar solicitudes (categoría desde la API), ver y editar las suyas (solo en Recibida, En revisión y Pendiente de información), responder cuando se le pide información, confirmar la resolución o pedir su reapertura, eliminar las suyas (Cerradas o Canceladas) y compartir la referencia por WhatsApp (simulado). |
| Administrador | `admin` | Ver todas las solicitudes, filtrarlas y buscarlas, pasarlas a revisión, asignar o reasignar técnicos desde el panel de asignación, cambiar la prioridad, pedir información, cancelar, cerrar, editar en los estados permitidos, eliminar solicitudes Cerradas o Canceladas y ver el panel resumen. |
| Técnico | `tecnico`, `tecnico2` | Ver las solicitudes que le asignaron, iniciar el trabajo, pedir información, registrar avances con foto opcional (solo En proceso) y resolver. No edita ni elimina. |

> Las claves se guardan en texto plano solo porque son datos ficticios de una evaluación. En una app real nunca se haría así.

## Funcionalidades implementadas

### Autenticación, perfiles y menú
- Inicio de sesión con cuentas ficticias guardadas en Room.
- Validación de campos vacíos; ante credenciales incorrectas se muestra el mismo mensaje ("Usuario o clave incorrectos") sin revelar cuál dato falló.
- Botón para mostrar u ocultar la clave.
- La pantalla de login muestra la ayuda con las seis cuentas de prueba, ordenadas por perfil.
- Control de acceso por perfil: la consulta se filtra en el DAO y el detalle y el escáner QR vuelven a comprobar el acceso.
- Menú lateral con opciones según el perfil y cierre de sesión (el login se elimina del historial de navegación al ingresar).

### Registro de solicitudes (cliente)
- Formulario con título, descripción, categoría, servicio relacionado y foto de evidencia opcional. La **categoría** se elige de una lista obtenida desde una **API REST** (ver más abajo); si la red falla, se escribe a mano. El **servicio relacionado** es un campo de texto libre, multilínea y opcional.
- Validaciones controladas desde el ViewModel, con errores visibles en los campos (`isError` y `supportingText`, más un ícono de advertencia):
  - Título obligatorio.
  - Descripción obligatoria con largo mínimo (10 caracteres).
  - Categoría obligatoria.
  - El servicio relacionado no se valida (es opcional).
  - Cada error desaparece al volver a escribir en ese campo.
- Generación de **código único** (`SOL-XXXXXXXX`, con índice único en Room), registro de fecha y hora, estado inicial **"Recibida"**, prioridad **"Media"** y primer registro en el historial.
- Al guardar, un diálogo muestra el código de seguimiento (se puede copiar) y su **código QR**.
- No se puede registrar mientras se está copiando una foto.

### Lista de solicitudes
- Muestra las solicitudes almacenadas en Room según el perfil, con código, título, categoría y estado (etiqueta de color).
- Se actualiza automáticamente al registrar, editar o asignar (`Flow` de Room).
- Estados de interfaz: Loading, Success, Empty y Error (con botón Reintentar).
- **Filtros** por estado, prioridad y categoría (todos los perfiles), más cliente y responsable (solo administrador, con la opción "Sin asignar"). Incluye **orden por fecha** (más recientes / más antiguas) y el botón **Quitar filtros**. Todo se filtra en el ViewModel. Se distingue entre "no hay solicitudes" y "ninguna coincide con el filtro".
- **Búsqueda por texto** sobre los chips: busca en código, título, descripción y categoría, sin distinguir mayúsculas, y se combina con los filtros. La X del campo y "Quitar filtros" la limpian.
- Las solicitudes eliminadas (eliminación lógica) no aparecen en la lista ni en los filtros.
- El botón para crear solicitudes solo aparece al cliente.
- Acceso al escáner QR desde la barra superior.

### Detalle y edición
- Modo lectura con todos los datos, foto de evidencia y código QR. El cliente ve además el botón de WhatsApp simulado debajo del QR.
- Modo edición de título, descripción, categoría (desde la API) y servicio (texto libre opcional), con las mismas validaciones del formulario. Disponible para el cliente (sus solicitudes) y el administrador, **solo en Recibida, En revisión y Pendiente de información**; no se edita en Asignada, En proceso, Resuelta, Cerrada ni Cancelada. Una solicitud Resuelta que el cliente reabre pasa a En revisión y vuelve a ser editable. El técnico nunca edita.
- Código, fecha, estado y foto no son editables.
- El detalle muestra además la **prioridad**, la **última actualización**, los **comentarios** y el **historial** de cambios (quién, cuándo, de qué estado a cuál). El cliente no ve los comentarios internos del técnico (el filtro está en el DAO).
- Estados: Loading, Success (lectura o edición), NoEncontrada y Error.
- Al final del detalle: la caja plegable **Evidencias del trabajo** y, cuando la regla lo permite, el botón **Eliminar solicitud** (ver más abajo).

### Estados y reglas de transición
Una solicitud pasa por 8 estados: **Recibida, En revisión, Pendiente de información, Asignada, En proceso, Resuelta, Cerrada y Cancelada**. Las prioridades son **Baja, Media (por defecto), Alta y Urgente**.

Quién puede cambiar de estado está centralizado en `ReglasTransicion` (un objeto sin dependencias de Android, por eso se prueba con tests unitarios). El repositorio vuelve a comprobar la regla antes de guardar.

| Perfil | Transiciones permitidas |
|--------|-------------------------|
| Administrador | Recibida → En revisión; En revisión → Pendiente de información; Resuelta → Cerrada; cancelar desde Recibida, En revisión, Pendiente de información, Asignada o En proceso. Pasar a **Asignada** ocurre solo al asignar un técnico desde el panel de asignación (no desde el detalle). |
| Técnico | Asignada → En proceso; Asignada o En proceso → Pendiente de información; En proceso → Resuelta. |
| Cliente | Pendiente de información → En proceso (si ya hay técnico) o En revisión (si no); Resuelta → Cerrada (confirma); Resuelta → En revisión (pide reapertura). |

- **Cerrada** y **Cancelada** no tienen salida para ningún perfil.
- Piden un **mensaje obligatorio**: cancelar, pedir información, resolver, responder información y pedir reapertura.
- Cada cambio de estado (con su historial) se guarda en una **transacción** de Room.
- **Edición:** solo el cliente (sus solicitudes) y el administrador, y únicamente en Recibida, En revisión y Pendiente de información (`puedeEditar`). El técnico nunca edita.
- **Reasignar técnico:** solo mientras la solicitud está Asignada o En proceso (`puedeReasignar`).
- **Registrar avances:** solo el técnico y solo En proceso (`puedeRegistrarAvance`).
- **Eliminación lógica:** solo Cerrada o Cancelada, para el cliente (las suyas) y el administrador (`puedeEliminar`).

### Administrador
- En el detalle, sección **Gestión**: cambiar la prioridad.
- Asignar o reasignar técnico se hace solo desde el **panel de asignación** (no desde el detalle).
- En el detalle, sección **Acciones**: pasar a revisión, solicitar información, cancelar (con motivo) y cerrar.
- **Panel resumen:** total de solicitudes y cantidad por cada estado.

### Técnico
- Ve las solicitudes que le asignaron, con sus comentarios e historial.
- Acciones: **Iniciar trabajo**, **Solicitar información** y **Registrar resolución** (con mensaje obligatorio).
- Formulario **Registrar avance**: texto, interruptor de comentario interno y foto opcional (cámara o galería, JPG/PNG/WEBP de hasta 5 MB). Solo aparece mientras la solicitud está **En proceso** (después de "Iniciar trabajo"), y el repositorio vuelve a comprobarlo.
- Los avances del técnico se muestran en la caja plegable **"Evidencias del trabajo (N)"**, al final del detalle y cerrada por defecto. Al abrirla se ven los avances ya registrados y, si el técnico puede registrar, el formulario. El cliente y el administrador la ven solo en lectura; la sección "Comentarios" muestra únicamente los del cliente y el administrador.
- El técnico nunca edita datos ni elimina solicitudes.

### Cliente
- **Responder solicitud de información:** botón único con respuesta obligatoria. La respuesta queda como comentario visible y la solicitud vuelve a En proceso (si hay técnico) o a En revisión.
- Con la solicitud **Resuelta**: **Confirmar resolución** (pasa a Cerrada) o **Pedir reapertura** (pasa a En revisión, con motivo obligatorio).

### API REST: categorías del formulario (Retrofit)
- Se consume **DummyJSON** (datos ficticios): `GET https://dummyjson.com/products?limit=10&select=title,category,description`. La respuesta trae los elementos dentro de un objeto contenedor (`products`, `total`, `skip`, `limit`); `products` y `ProductoDto` son solo el formato de DummyJSON.
- Lo que entrega la API no se trata como productos: sus elementos se asignan por orden a las **categorías de problemas técnicos** del caso (Desarrollo web, Mantenimiento web, Incidente técnico, Hosting, Soporte de correo, Marketing digital, Solicitud de cambio, Consulta, Acceso o configuración y Otro requerimiento).
- Capas: `ApiService` (interfaz Retrofit) → `CatalogoRepository` (convierte el DTO en `CategoriaCatalogo` y entrega un `Result`) → `CatalogoViewModel` (`CatalogoUiState`: Loading, Success, Empty y Error) → `SelectorCategoria` en el formulario y en la edición.
- Se descartan los elementos sin id o sin título. Si la API trae menos de 10 válidos, se muestran menos categorías.
- Si la red falla o el catálogo viene vacío, se muestra el mensaje con **Reintentar** y un campo de texto manual para la categoría. La app no se cierra.
- El nombre elegido se guarda en el mismo campo de siempre (`categoria`), sin cambios en Room. Bajo la lista se muestra la etiqueta "Catálogo de prueba (datos ficticios)".

### Avisos locales (notificaciones)
- Son notificaciones locales reales de Android (`NotificationCompat`), generadas desde `SolicitudRepository` **después** de que el cambio se guardó bien. Sin servidor.
- Como todo corre en un solo teléfono, el aviso dice para quién es (por ejemplo: "Aviso para el cliente: …").
- Eventos: nueva solicitud (administrador), solicitud asignada (técnico), cambio de prioridad (cliente), pendiente de información (cliente), solicitud en proceso cuando el técnico inicia el trabajo (cliente), nuevo comentario visible del técnico (cliente), resuelta (cliente), cerrada (técnico), reabierta (administrador), eliminada por el administrador (cliente) y referencia compartida por WhatsApp simulado (cliente). Los comentarios internos no avisan, ni tampoco cancelar, la respuesta del cliente a una solicitud de información o que el cliente elimine su propia solicitud.- Android 13 o superior pide el permiso al entrar al menú. Si se rechaza, no hay avisos y la app sigue funcionando.
- Al tocar una notificación solo se abre la app.

### Asignación de solicitudes (administrador)
- Pantalla con todas las solicitudes (no eliminadas) y un selector de técnicos.
- Los controles de asignación solo aparecen en Recibida, En revisión, Asignada y En proceso; en los demás estados se muestra el técnico como texto.
- Al asignar, la solicitud pasa al estado **"Asignada"** y queda visible para ese técnico. Reasignar solo se permite en Asignada o En proceso.
- Solo se pueden asignar cuentas con perfil técnico (validado en el DAO).
- Aviso de éxito que se cierra solo a los 3 segundos.

### Eliminación lógica
- Solo se pueden eliminar solicitudes **Cerradas o Canceladas**. El cliente elimina únicamente las suyas, el administrador las de cualquier cliente y el técnico no elimina.
- El botón "Eliminar solicitud" está al final del detalle y pide confirmación.
- No se borra nada: la solicitud queda marcada como eliminada (columna `eliminada`, Room versión 5) y deja una línea en el historial. Su historial, comentarios y fotos siguen guardados.
- La solicitud eliminada deja de verse en la lista, los filtros, la búsqueda, el panel resumen, el panel de asignación y el escáner QR (las consultas del DAO filtran `eliminada = 0`).
- Si elimina el administrador, se avisa al cliente afectado.

### WhatsApp simulado (cliente)
- Debajo del QR del detalle, el cliente ve el botón **"Compartir referencia por WhatsApp (simulado)"**. Los demás perfiles no lo ven.
- Abre un diálogo con el mensaje: código, título, categoría y un enlace de seguimiento ficticio (`https://seguimiento.solicitudes.example/...`). No incluye descripción, nombres ni foto.
- Al confirmar con "Enviar (simulado)" se muestra una notificación local y un mensaje en pantalla. **No se abre WhatsApp real** ni se envía nada.
- El texto se arma en `MensajeWhatsApp` (sin dependencias de Android) y el aviso lo publica el `DetalleSolicitudViewModel`.

### Recursos nativos: cámara y galería
- **Foto de evidencia (opcional):** tomada con la cámara (`TakePicture` + `FileProvider`) o elegida de la galería (`PickVisualMedia`, sin permisos). Se guarda como archivo propio de la app (`filesDir/evidencias`) y se vincula a la solicitud; se muestra en el detalle con Coil.
- La foto se borra si se reemplaza, se quita o se sale del formulario sin registrar. La copia desde la galería se hace en segundo plano (`Dispatchers.IO`).
- **Límite de formato y tamaño:** toda evidencia (la del cliente y la del técnico) debe ser JPG, PNG o WEBP y pesar como máximo 5 MB. Si no cumple, se muestra un mensaje y la foto no se guarda (`EvidenciaArchivos`).
- **Escáner QR:** lee el QR de una solicitud (CameraX + ML Kit), muestra sus datos y, si la solicitud existe en el teléfono y el usuario tiene acceso, permite abrir su detalle. Si el QR no pertenece a la app, se informa y se puede escanear otro.
- Estados del escáner: Escaneando, Buscando, Leida y NoValido.
- Manejo del permiso de cámara denegado y de errores de lectura de imagen, sin cierres inesperados.

### Animaciones y retroalimentación visual
- **`AnimatedContent`:** fundido suave entre los estados de la lista y del detalle (y entre modo lectura y edición).
- **`animateColorAsState`:** la etiqueta de estado cambia de color con una transición de 400 ms (por ejemplo, de "Recibida" a "Asignada").
- **`AnimatedVisibility`:** aviso de éxito de la asignación, vista previa de la foto de evidencia, botón "Quitar foto" y mensaje de error del login.
- Íconos en los campos del formulario y del login.

### Diseño adaptable (Window Size Classes)
- La clase de ancho se obtiene con `currentWindowAdaptiveInfoV2()` en `ui/adaptive/WindowSizeUtils.kt`, que la traduce a un enum propio `AppWindowSizeClass` (Compact, Medium, Expanded). Se calcula una vez en `AppNavigation` y se entrega a las pantallas.
- Compacta (< 600 dp): menú lateral modal y lista de una columna.
- Mediana (600–839 dp) y expandida (≥ 840 dp): menú lateral permanente y cuadrículas adaptativas para solicitudes (tarjetas de al menos 320 dp) y asignaciones (340 dp).
- Login, formulario, detalle, escáner y menú limitan su ancho máximo en ventanas amplias con `Modifier.adaptiveContentWidth` (`ui/adaptive/AdaptiveContent.kt`).
- Se actualiza al rotar, redimensionar o usar pantalla dividida.

## Navegación

El recorrido principal es **Login → Menú → Lista → Formulario**, y desde la lista se abre el detalle o el escáner QR. Las rutas están definidas como constantes en `AppNavigation.kt`; las pantallas no conocen las rutas, solo reciben funciones (`onGuardado`, `onVolver`, etc.).

| Ruta | Pantalla | Cómo se llega |
|------|----------|---------------|
| `login` | LoginScreen | Pantalla inicial. |
| `menu/{nombre}/{usuarioId}/{rol}` | MenuScreen | Tras ingresar con credenciales correctas. |
| `lista/{usuarioId}/{rol}` | ListaSolicitudesScreen | Desde el menú o al guardar una solicitud. |
| `formulario/{usuarioId}/{rol}` | FormularioScreen | Botón + de la lista o menú (solo cliente). |
| `detalle/{id}/{usuarioId}/{rol}` | DetalleSolicitudScreen | Al tocar una tarjeta o desde el escáner QR. |
| `qr/{usuarioId}/{rol}` | QrScannerScreen | Botón "Escanear QR" de la lista. |
| `asignaciones/{usuarioId}/{rol}` | AsignarSolicitudesScreen | Opción del menú (solo administrador). |
| `resumen/{usuarioId}/{rol}` | ResumenScreen | Opción "Panel resumen" del menú (solo administrador). |

## Arquitectura MVVM

```
Pantalla (View) → ViewModel (valida, UiState + StateFlow) → Repository (código, fecha, estado, Result) → DAO → Room
```

- **View (Compose):** Dibuja el estado y envía eventos. No valida ni accede a la base de datos.
- **ViewModel:** Mantiene el estado (StateFlow), valida los campos y pide al Repository guardar o consultar con corrutinas.
- **Estado de UI (UiState):** Clases de la carpeta `state/` que describen lo que la pantalla debe mostrar en cada momento (campos escritos, errores, cargando/guardando, filtros). Pertenecen a la capa de presentación: las crea y modifica el ViewModel y la View solo las observa. No son parte del Model de datos.
- **Model (datos):** Datos persistentes y su acceso, en la carpeta `model/data/`: Entity, DAO, AppDatabase y Repository.
- **DAO y Room:** Ejecutan las operaciones sobre la base local.

Hay un ViewModel por flujo: login, formulario, lista, detalle, asignación, escáner QR, resumen, avance del técnico y catálogo. El repositorio recibe sus dependencias por constructor (por ejemplo `SolicitudRepository(db, notificador)` y `CatalogoRepository(api)`), lo que permite probarlo con datos simulados. El `DetalleSolicitudViewModel` también recibe el `Notificador` para publicar el aviso del WhatsApp simulado.

## Persistencia con Room

Base `solicitudes_database`, **versión 5**, con las tablas `solicitudes`, `usuarios`, `historial_solicitud` y `comentarios_solicitud`.

- La migración **2 → 3** agrega `creadoPorUsuarioId` y `asignadoAUsuarioId` sin borrar registros, y vincula las solicitudes anteriores a la cuenta `demo` si existe.
- La migración **3 → 4** agrega `prioridad` (las solicitudes antiguas quedan en "Media") y `ultimaActualizacion`, crea las tablas de historial y comentarios (con índice por solicitud) y deja un primer registro de historial para cada solicitud existente. No se borra ningún dato.
- La migración **4 → 5** agrega `eliminada` (`INTEGER NOT NULL DEFAULT 0`) para la eliminación lógica: las solicitudes existentes quedan visibles y no se borra ningún dato. Las consultas del DAO (`obtenerVisibles`, `obtenerTodas`, `obtenerPorId` y `obtenerPorCodigoVisible`) filtran `eliminada = 0`.
- Los estados y las prioridades se guardan como texto legible (por ejemplo "En revisión").
- `AppDatabase` entrega una única instancia compartida por toda la app y configura `BundledSQLiteDriver`.
- Los cambios de estado, prioridad o asignación se guardan junto con su historial dentro de una transacción (`withWriteTransaction`).

## Estructura del proyecto

```
app/src/main/java/com/example/appsolicitudes_grupo12/
├── model/
│   ├── aviso/
│   │   ├── AvisoSolicitud.kt          (textos de los avisos)
│   │   ├── Notificador.kt             (interfaz; por defecto no avisa)
│   │   └── NotificadorAndroid.kt      (notificación real de Android)
│   ├── catalogo/
│   │   └── CategoriaCatalogo.kt       (id y nombre de cada categoría)
│   ├── compartir/
│   │   └── MensajeWhatsApp.kt         (texto del WhatsApp simulado)
│   ├── data/
│   │   ├── files/
│   │   │   └── EvidenciaArchivos.kt
│   │   ├── local/                     (Room)
│   │   │   ├── AppDatabase.kt
│   │   │   ├── SolicitudDao.kt / SolicitudEntity.kt
│   │   │   ├── UsuarioDao.kt / UsuarioEntity.kt
│   │   │   ├── HistorialSolicitudDao.kt / HistorialSolicitudEntity.kt
│   │   │   └── ComentarioSolicitudDao.kt / ComentarioSolicitudEntity.kt
│   │   ├── remote/                    (API REST con Retrofit)
│   │   │   ├── ApiService.kt
│   │   │   ├── ProductoDto.kt
│   │   │   └── RetrofitInstance.kt
│   │   └── repository/
│   │       ├── AuthRepository.kt
│   │       ├── CatalogoRepository.kt
│   │       ├── SolicitudRepository.kt
│   │       └── UsuarioRepository.kt
│   ├── qr/
│   │   ├── CodigoQr.kt
│   │   └── DatosQr.kt
│   └── solicitud/
│       ├── CategoriasSolicitud.kt
│       ├── EstadoSolicitud.kt
│       ├── PrioridadSolicitud.kt
│       ├── ReglasTransicion.kt        (quién cambia qué estado)
│       └── ValidadorSolicitud.kt
├── state/                             (estados de interfaz, UiState)
│   ├── AccionesUiState.kt
│   ├── AsignarSolicitudesUiState.kt
│   ├── AvanceUiState.kt
│   ├── CatalogoUiState.kt
│   ├── DetalleSolicitudUiState.kt
│   ├── EdicionSolicitud.kt
│   ├── FiltroSolicitudes.kt
│   ├── FormularioErrores.kt
│   ├── FormularioUiState.kt
│   ├── ListaSolicitudesUiState.kt
│   ├── LoginUiState.kt
│   ├── QrUiState.kt
│   └── ResumenUiState.kt
├── navigation/
│   └── AppNavigation.kt
├── ui/
│   ├── adaptive/
│   │   ├── AdaptiveContent.kt
│   │   └── WindowSizeUtils.kt
│   ├── camera/
│   │   ├── CameraPermissionHelper.kt
│   │   └── QrScanner.kt
│   ├── components/
│   │   ├── AccionesSolicitud.kt
│   │   ├── AvanceSolicitud.kt
│   │   ├── ChipFiltro.kt
│   │   ├── ComentariosSolicitud.kt
│   │   ├── CompartirWhatsApp.kt       (botón y diálogo del WhatsApp simulado)
│   │   ├── EliminarSolicitud.kt       (botón con confirmación)
│   │   ├── EtiquetaEstado.kt
│   │   ├── EvidenciasTrabajo.kt       (caja plegable del técnico)
│   │   ├── Formatos.kt
│   │   ├── GestionSolicitud.kt
│   │   ├── HistorialSolicitud.kt
│   │   ├── IconoError.kt
│   │   ├── ImagenQrSolicitud.kt
│   │   └── SelectorCategoria.kt
│   ├── screen/
│   │   ├── AsignarSolicitudesScreen.kt
│   │   ├── DetalleSolicitudScreen.kt
│   │   ├── FormularioScreen.kt
│   │   ├── ListaSolicitudesScreen.kt
│   │   ├── LoginScreen.kt
│   │   ├── MenuScreen.kt
│   │   ├── QrScannerScreen.kt
│   │   └── ResumenScreen.kt
│   └── theme/
│       ├── Color.kt
│       ├── Theme.kt
│       └── Type.kt
├── viewModel/
│   ├── AsignarSolicitudesViewModel.kt
│   ├── AvanceSolicitudViewModel.kt
│   ├── CatalogoViewModel.kt
│   ├── DetalleSolicitudViewModel.kt
│   ├── FormularioViewModel.kt
│   ├── ListaSolicitudesViewModel.kt
│   ├── LoginViewModel.kt
│   ├── QrViewModel.kt
│   └── ResumenViewModel.kt
└── MainActivity.kt

app/src/test/java/com/example/appsolicitudes_grupo12/          (pruebas unitarias)
├── model/solicitud/ReglasTransicionTest.kt
└── model/data/repository/CatalogoRepositoryTest.kt

app/src/androidTest/java/com/example/appsolicitudes_grupo12/   (pruebas de interfaz)
├── FormularioScreenTest.kt
└── AccionesSolicitudTest.kt

app/src/main/res/xml/   file_paths.xml (carpeta que comparte el FileProvider)
```

> **Nota sobre `state/`:** esta carpeta contiene los **estados de interfaz (UiState)**. Pertenecen a la capa de presentación: los crea y modifica el ViewModel y la View solo los observa. El Model de datos de la arquitectura MVVM corresponde a `model/` (entidades, DAO, base de datos, API y repositorios).

> **Reorganización (tercera evaluación):** la antigua carpeta `utils/` se eliminó y sus archivos se repartieron por responsabilidad.
> - `model/data/files/EvidenciaArchivos.kt`: guarda, copia y borra las fotos de evidencia; crea la URI del `FileProvider`; valida que sean JPG, PNG o WEBP y de hasta 5 MB.
> - `model/qr/CodigoQr.kt`: arma y lee el texto JSON del QR y lo dibuja como imagen (ZXing).
> - `model/solicitud/`: estados, prioridades, categorías, validación y reglas de transición compartidas por todas las pantallas.
> - `ui/components/`: composables reutilizables. `ui/camera/`: escáner y permiso de cámara. `ui/adaptive/`: tamaño de ventana.
> - `model/data/repository/UsuarioRepository.kt`: consulta de técnicos, para que el ViewModel de asignaciones no use el DAO directo.

## Requisitos para ejecutar

- Android Studio reciente con soporte para AGP 9.x (desarrollado con Android Studio Rabbit 1 | 2026.2.1)
- JDK: lo gestiona Android Studio mediante `gradle-daemon-jvm.properties` (toolchain configurado en `gradle/gradle-daemon-jvm.properties`)
- Android SDK 37 instalado
- Dispositivo o emulador con Android 7.0 (API 24) o superior
- **Teléfono real recomendado** para probar la cámara y el escáner QR

## Pasos para ejecutar

1. **Clonar el repositorio**
```bash
git clone <URL-del-repositorio>
```
2. **Abrir el proyecto** en Android Studio (`File > Open` y seleccionar la carpeta `AppSolicitudes_Grupo12`).
3. **Sincronizar Gradle** (`File > Sync Project with Gradle Files`) y esperar a que se descarguen las dependencias.
4. **Seleccionar un dispositivo**: un emulador creado en Device Manager o un teléfono con depuración USB activada.
5. **Ejecutar** la app con el botón *Run* (▶) o `Shift + F10`.
6. **Iniciar sesión** con una de las cuentas de prueba (clave `1234`).

> **Base de datos:** la versión actual de Room es la 5. Las migraciones 2 → 3, 3 → 4 y 4 → 5 conservan las solicitudes existentes, por lo que no es necesario desinstalar la app. Si se modifican las tablas, hay que subir la versión y agregar su migración.

> **Android Studio y teléfonos con Android 16:** si al usar un teléfono real aparece un error en *App Inspection* (inspector de base de datos), no es un fallo de la app: ocurre cuando Android Studio no está actualizado.

## Cómo probar el flujo

Todas las cuentas usan la clave `1234`. Lo más claro es probarlo en este orden, cerrando sesión entre perfiles.

### 1. Cliente (`demo`)
1. Entra a **Nueva solicitud** y pulsa **Registrar solicitud** con campos vacíos: deben aparecer los errores de título, descripción y categoría.
2. Elige la **categoría** de la lista (viene de la API; sin internet aparece el mensaje con **Reintentar** y se escribe a mano). El **servicio relacionado** es opcional. Registra: se mostrará el código y su QR.
3. En **Mis solicitudes** prueba la **búsqueda por texto**, los filtros y el orden. La solicitud queda en **Recibida** y todavía se puede **editar**.

### 2. Administrador (`admin`)
1. Se muestra el aviso "Aviso para el administrador" de la nueva solicitud (acepta el permiso de notificaciones al entrar al menú).
2. En **Solicitudes** abre la solicitud y pasa a **En revisión**. Cambia la **prioridad**.
3. Entra a **Asignar solicitudes**, elige a `tecnico` y asígnala: queda **Asignada**.
4. Revisa el **Panel resumen**: los conteos por estado cambian.

### 3. Técnico (`tecnico`)
1. Abre la solicitud asignada y pulsa **Iniciar trabajo** (pasa a En proceso y el cliente recibe el aviso "En proceso"). Antes de eso no aparece el formulario de avances.
2. Abre la caja **Evidencias del trabajo** y registra un **avance** (con foto JPG, PNG o WEBP de hasta 5 MB, opcional). Prueba el interruptor de comentario interno.
3. Pulsa **Solicitar información** e indica el motivo (queda Pendiente de información).

### 4. Cliente otra vez (`demo`)
1. Verás el aviso y el comentario del técnico (los internos no se ven). Pulsa **Responder solicitud de información**: vuelve a En proceso.
2. Con el técnico, pasa la solicitud a **Resuelta** (mensaje obligatorio).
3. Como cliente, prueba **Pedir reapertura** (con motivo) o **Confirmar resolución** (pasa a Cerrada).
4. Revisa el **historial** en el detalle: cada cambio quedó registrado.

### 5. Extras
1. **WhatsApp simulado:** como cliente, en el detalle pulsa **Compartir referencia por WhatsApp (simulado)**, revisa el mensaje y pulsa **Enviar (simulado)**: llega una notificación y no se abre WhatsApp. Con `admin` y `tecnico` el botón no aparece.
2. **Eliminación lógica:** con una solicitud en **Cerrada** o **Cancelada** (el administrador puede cancelar), pulsa **Eliminar solicitud** y confirma: desaparece de la lista, los filtros, el panel resumen, la asignación y el escáner QR. Si elimina el administrador, el cliente recibe un aviso.
3. **Permisos:** el botón **Editar** solo aparece en Recibida, En revisión y Pendiente de información, y el técnico nunca edita ni elimina.
-
### Escáner QR (cualquier perfil)
- Pulsa **Escanear QR** en la barra superior de la lista y apunta a un QR mostrado en otro teléfono o en una captura abierta en un computador.
## Testing

Hay **2 pruebas unitarias** (`src/test`) y **2 pruebas de interfaz** (`src/androidTest`).

| Prueba | Tipo | Qué valida |
|--------|------|------------|
| `ReglasTransicionTest` | Unitaria (Kotest) | 9 casos de las reglas de negocio: quién pasa de Recibida a En revisión, quién inicia y resuelve, que el cliente solo confirma una solicitud Resuelta, la reapertura solo desde Resuelta, que Cerrada y Cancelada no tienen salida, el destino al responder información, cuándo se exige mensaje, prioridad y asignación solo del administrador, y que el técnico registra avances solo En proceso y la edición y la eliminación se limitan por estado y perfil. |
| `CatalogoRepositoryTest` | Unitaria (Kotest + MockK + `runTest`) | 4 casos con un `ApiService` simulado y sin internet: asigna por orden los nombres de categoría del caso a los productos de la API, descarta los que no tienen id o título, trata una respuesta sin productos como lista vacía y entrega un error de red como `Result.failure` en vez de lanzar una excepción. |
| `FormularioScreenTest` | Interfaz (Compose UI Test) | Con el formulario vacío, al pulsar "Registrar solicitud" aparecen los tres errores de validación y no se guarda nada. |
| `AccionesSolicitudTest` | Interfaz (Compose UI Test) | Los botones del cliente cambian según el estado: responder información en "Pendiente de información", confirmar o pedir reapertura en "Resuelta", y ninguno en "Recibida". |

**Cómo ejecutarlas**
- **Unitarias:** clic derecho sobre la carpeta `app/src/test` → *Run Tests*, o `gradlew.bat test` en la terminal.
- **De interfaz:** abrir un emulador, clic derecho sobre `app/src/androidTest` → *Run Tests*, o `gradlew.bat connectedDebugAndroidTest`.

Al ejecutar las unitarias, Android Studio puede mostrar advertencias de "Java agent" de MockK. No son errores y las pruebas pasan igual.

## Pruebas realizadas

Además de las pruebas automatizadas, se probó manualmente en Android Studio y en teléfono real: login vacío, incorrecto y correcto; menú y cierre de sesión; formulario vacío y válido; catálogo con y sin internet; persistencia al reiniciar; detalle y edición; foto por cámara y galería; visibilidad por perfil; asignación; filtros y orden; el ciclo completo de estados por cada perfil (con mensajes obligatorios, historial y comentarios internos); avisos locales con el permiso aceptado y rechazado; QR generado y escaneado; migraciones Room v2 → v3 → v4; tema claro/oscuro con animaciones; y diseño adaptable en ventanas compacta, mediana y expandida.

> **Pendiente de completar por el equipo:** al terminar las pruebas manuales de la tercera evaluación (reglas y permisos, evidencia del técnico con límite de formato y tamaño, categoría desde la API, edición, eliminación lógica con migración 4 → 5, búsqueda, avisos nuevos, login y WhatsApp simulado), reemplazar esta nota por una frase que diga qué se probó y dónde (emulador o teléfono real). Solo se debe escribir lo que realmente se probó.

## Limitaciones conocidas

1. El QR es una copia de los datos al momento de crearlo: si la solicitud se edita después, un QR ya impreso o capturado queda desactualizado (el QR que se muestra en el detalle sí se regenera).
2. La descripción dentro del QR se corta a 300 caracteres (título y servicio a 100).
3. La foto no viaja en el QR: solo existe en el teléfono que la tomó.
4. Cada teléfono tiene su propia base de datos, por lo que "Abrir detalle" solo aparece donde la solicitud existe y el perfil tiene acceso a ella.
5. Un teléfono no puede escanear su propia pantalla: se necesita otro dispositivo o una captura abierta en un computador.
6. El escáner solo reconoce QR generados por la propia aplicación (los que traen la marca `AppSolicitudes_Grupo12`). Si el QR de una solicitud se lee con otra app, el contenido aparece como texto JSON.
7. El QR lleva los datos dentro, por lo que cualquiera que lo escanee con el escáner de la app ve esa información aunque su perfil no tenga acceso en Room; el botón "Abrir detalle" sí respeta los permisos.
8. El usuario y el perfil viajan como argumentos de ruta (no hay una sesión central). Es aceptable en una app local y los DAO repiten la validación de acceso, pero con un servidor se reemplazaría por un token de sesión.
9. Las claves de las cuentas de prueba se guardan en texto plano (solo válido por ser datos ficticios).
10. La API de prueba (DummyJSON) entrega productos ficticios: la app solo usa sus elementos (id y título) como base de las categorías del caso y les asigna los nombres por orden. No son servicios reales de la empresa.
11. Los avisos son locales: aparecen en el mismo teléfono, indicando para qué perfil son. No hay envío entre dispositivos, para eso haría falta un servidor.
12. Al tocar un aviso solo se abre la app; no lleva directo al detalle de la solicitud ni hay una bandeja de avisos guardada.
13. Una solicitud solo se puede reabrir desde "Resuelta". Una ya "Cerrada" no se puede reabrir.
14. Si la API falla y la categoría se escribe a mano, esa categoría no aparece en el filtro de categoría de la lista, que usa las categorías fijas del caso.
15. El técnico no adjunta foto al pedir información o resolver; la evidencia se agrega con "Registrar avance".
16. La eliminación lógica oculta la solicitud en la app, pero la fila, su historial, sus comentarios y sus fotos siguen guardados en Room; no hay una pantalla para recuperarla.
17. El WhatsApp es solo una simulación: no abre WhatsApp ni envía mensajes, y el enlace de seguimiento es ficticio (dominio `.example`). Solo lo ve el cliente.
18. Cada avance del técnico admite una sola imagen (JPG, PNG o WEBP de hasta 5 MB).
19. El texto escrito en el campo de búsqueda de la lista puede vaciarse al girar la pantalla.

## Fuera del alcance de esta etapa

Registro de usuarios (las cuentas de prueba se crean automáticamente), integración real con WhatsApp (solo existe una simulación), notificaciones remotas (push) y datos compartidos entre dispositivos (la API REST solo se usa para leer las categorías del formulario).

## Datos y privacidad

Toda la información (usuarios, solicitudes, fotos y QR) es **ficticia**. No se deben usar datos personales, conversaciones reales ni información comercial sensible.
