# AppSolicitudes_Grupo12

Aplicación móvil Android para la **gestión y seguimiento de solicitudes de clientes**, desarrollada para la asignatura **DSY1105 – Desarrollo de Aplicaciones Móviles** (Duoc UC, Escuela de Informática y Telecomunicaciones, CITT) como parte de la segunda evaluación.

Caso: *Gestión móvil y seguimiento de solicitudes de clientes* (Caso_DSY1105_005D).

## Integrantes

| Nombre | Sección | Grupo |
|--------|---------|-------|
| Vicente Lopez | 005D | 12 |
| Isaac Rivas | 005D | 12 |

Profesor: Vicente Julio Zapata Valenzuela

## Descripción del proyecto

Una pequeña empresa de servicios informáticos recibe hoy las solicitudes de sus clientes por WhatsApp, lo que dificulta el seguimiento, la trazabilidad y la visibilidad de cada requerimiento. Esta aplicación es un **Producto Mínimo Viable (MVP)** que representa cómo una solicitud puede quedar registrada, identificada con un código único, asignada a un responsable y disponible para consulta según el perfil de cada usuario.

Para esta etapa se desarrolló el **módulo de Solicitudes** con **tres perfiles** (cliente, administrador y técnico), demostrando:

- Arquitectura MVVM real (View, ViewModel, Repository, DAO, Room).
- Persistencia local con Room (con migración de base de datos).
- Navegación con paso de argumentos y control de acceso por perfil.
- Formularios con validación controlada desde el ViewModel.
- Gestión de estado con `UiState` + `StateFlow`.
- Animaciones con la API de Compose y tema visual propio (claro y oscuro).
- Diseño adaptable según el tamaño de ventana (Window Size Classes).
- Recursos nativos: cámara (foto de evidencia y escáner QR) y galería.

> Todos los datos utilizados son **ficticios**. La app no lee conversaciones reales ni se integra con WhatsApp.

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
| `material-icons-core` | Íconos básicos de Material (Add, ArrowBack, Menu, Person, Lock, Edit, Create, Build, List, ExitToApp, Warning). |
| CameraX (`camera-core`, `camera-camera2`, `camera-lifecycle`, `camera-view`) | Abre la cámara y muestra su vista previa en el escáner QR. |
| ML Kit `barcode-scanning` | **Lee** el código QR a partir de las imágenes de la cámara. |
| ZXing `core` | **Genera** la imagen del código QR de cada solicitud. |
| Coil `coil-compose` | Carga y muestra en segundo plano la foto de evidencia. |

`TakePicture` y `PickVisualMedia` vienen con `activity-compose`, `FileProvider` con `androidx.core` y `org.json` (texto del QR) con Android, por lo que no requieren dependencias nuevas.

## Perfiles y cuentas de prueba

Las tres cuentas ficticias se crean automáticamente en Room la primera vez que alguien inicia sesión (si no existen).

| Perfil | Usuario | Clave | Qué puede hacer |
|--------|---------|-------|-----------------|
| Cliente | `demo` | `1234` | Registrar solicitudes, ver y editar las que creó. |
| Administrador | `admin` | `1234` | Ver todas las solicitudes, editarlas y asignarlas (o reasignarlas) a técnicos. |
| Técnico | `tecnico` | `1234` | Ver (solo lectura) las solicitudes que le asignaron. |

> Las claves se guardan en texto plano solo porque son datos ficticios de una evaluación. En una app real nunca se haría así.

## Funcionalidades implementadas

### Autenticación, perfiles y menú
- Inicio de sesión con cuentas ficticias guardadas en Room.
- Validación de campos vacíos; ante credenciales incorrectas se muestra el mismo mensaje ("Usuario o clave incorrectos") sin revelar cuál dato falló.
- Botón para mostrar u ocultar la clave.
- Control de acceso por perfil: la consulta se filtra en el DAO y el detalle y el escáner QR vuelven a comprobar el acceso.
- Menú lateral con opciones según el perfil y cierre de sesión (el login se elimina del historial de navegación al ingresar).

### Registro de solicitudes (cliente)
- Formulario con título, descripción, categoría (10 categorías del caso), servicio relacionado y foto de evidencia opcional.
- Validaciones controladas desde el ViewModel, con errores visibles en los campos (`isError` y `supportingText`, más un ícono de advertencia):
  - Título obligatorio.
  - Descripción obligatoria con largo mínimo (10 caracteres).
  - Categoría obligatoria.
  - Servicio relacionado obligatorio, salvo en "Consulta" y "Otro requerimiento".
- Cada error desaparece al volver a escribir en ese campo.
- Generación de **código único** (`SOL-XXXXXXXX`, con índice único en Room), registro de fecha y hora y estado inicial **"Recibida"**.
- Al guardar, un diálogo muestra el código de seguimiento (se puede copiar) y su **código QR**.
- No se puede registrar mientras se está copiando una foto.

### Lista de solicitudes
- Muestra las solicitudes almacenadas en Room según el perfil, con código, título, categoría y estado (etiqueta de color).
- Se actualiza automáticamente al registrar, editar o asignar (`Flow` de Room).
- Estados de interfaz: Loading, Success, Empty y Error (con botón Reintentar).
- **Filtro por categoría** y **orden por fecha** (más recientes / más antiguas). Se distingue entre "no hay solicitudes" y "ninguna coincide con el filtro".
- El botón para crear solicitudes solo aparece al cliente.
- Acceso al escáner QR desde la barra superior.

### Detalle y edición
- Modo lectura con todos los datos, foto de evidencia y código QR.
- Modo edición de título, descripción, categoría y servicio, con las mismas validaciones del formulario. Disponible para el cliente (sus solicitudes) y el administrador; el técnico solo consulta.
- Código, fecha, estado y foto no son editables.
- Estados: Loading, Success (lectura o edición), NoEncontrada y Error.

### Asignación de solicitudes (administrador)
- Pantalla con todas las solicitudes y un selector de técnicos.
- Al asignar, la solicitud pasa al estado **"Asignada"** y queda visible para ese técnico; también se puede reasignar.
- Solo se pueden asignar cuentas con perfil técnico (validado en el DAO).
- Aviso de éxito que se cierra solo a los 3 segundos.

### Recursos nativos: cámara y galería
- **Foto de evidencia (opcional):** tomada con la cámara (`TakePicture` + `FileProvider`) o elegida de la galería (`PickVisualMedia`, sin permisos). Se guarda como archivo propio de la app (`filesDir/evidencias`) y se vincula a la solicitud; se muestra en el detalle con Coil.
- La foto se borra si se reemplaza, se quita o se sale del formulario sin registrar. La copia desde la galería se hace en segundo plano (`Dispatchers.IO`).
- **Escáner QR:** lee el QR de una solicitud (CameraX + ML Kit), muestra sus datos y, si la solicitud existe en el teléfono y el usuario tiene acceso, permite abrir su detalle. Si el QR no pertenece a la app, se informa y se puede escanear otro.
- Estados del escáner: Escaneando, Buscando, Leida y NoValido.
- Manejo del permiso de cámara denegado y de errores de lectura de imagen, sin cierres inesperados.

### Animaciones y retroalimentación visual
- **`AnimatedContent`:** fundido suave entre los estados de la lista y del detalle (y entre modo lectura y edición).
- **`animateColorAsState`:** la etiqueta de estado cambia de color con una transición de 400 ms (por ejemplo, de "Recibida" a "Asignada").
- **`AnimatedVisibility`:** aviso de éxito de la asignación, vista previa de la foto de evidencia, botón "Quitar foto" y mensaje de error del login.
- Íconos en los campos del formulario y del login.

### Diseño adaptable (Window Size Classes)
- La clase de ancho se obtiene con `currentWindowAdaptiveInfoV2()` en `utils/WindowSizeUtils.kt`, que la traduce a un enum propio `AppWindowSizeClass` (Compact, Medium, Expanded). Se calcula una vez en `AppNavigation` y se entrega a las pantallas.
- Compacta (< 600 dp): menú lateral modal y lista de una columna.
- Mediana (600–839 dp) y expandida (≥ 840 dp): menú lateral permanente y cuadrículas adaptativas para solicitudes (tarjetas de al menos 320 dp) y asignaciones (340 dp).
- Login, formulario, detalle, escáner y menú limitan su ancho máximo en ventanas amplias con `Modifier.adaptiveContentWidth` (`utils/AdaptiveContent.kt`).
- Se actualiza al rotar, redimensionar o usar pantalla dividida.

## Navegación

El recorrido principal es **Login → Menú → Lista → Formulario**, y desde la lista se abre el detalle o el escáner QR. Las rutas están definidas como constantes en `AppNavigation.kt`; las pantallas no conocen las rutas, solo reciben funciones (`onGuardado`, `onVolver`, etc.).

| Ruta | Pantalla | Cómo se llega |
|------|----------|---------------|
| `login` | LoginScreen | Pantalla inicial. |
| `menu/{nombre}/{usuarioId}/{rol}` | MenuScreen | Tras ingresar con credenciales correctas. |
| `lista/{usuarioId}/{rol}` | ListaSolicitudesScreen | Desde el menú o al guardar una solicitud. |
| `formulario/{usuarioId}/{rol}` | SolicitudFormScreen | Botón + de la lista o menú (solo cliente). |
| `detalle/{id}/{usuarioId}/{rol}` | DetalleSolicitudScreen | Al tocar una tarjeta o desde el escáner QR. |
| `qr/{usuarioId}/{rol}` | QrScannerScreen | Botón "Escanear QR" de la lista. |
| `asignaciones/{usuarioId}/{rol}` | AsignarSolicitudesScreen | Opción del menú (solo administrador). |

## Arquitectura MVVM

```
Pantalla (View) → ViewModel (valida, UiState + StateFlow) → Repository (código, fecha, estado, Result) → DAO → Room
```

- **View (Compose):** Dibuja el estado y envía eventos. No valida ni accede a la base de datos.
- **ViewModel:** Mantiene el estado (StateFlow), valida los campos y pide al Repository guardar o consultar con corrutinas.
- **Model: datos:** Datos persistentes y su acceso: Entity, DAO, AppDatabase y Repository.
- **Model: estado:** Estados de interfaz (UiState): lo que la pantalla muestra en tiempo real (campos escritos, errores, cargando/guardando, filtros).
- **DAO y Room:** Ejecutan las operaciones sobre la base local.

Hay un ViewModel por flujo: login, formulario, lista, detalle, asignación y escáner QR.

## Persistencia con Room

Base `solicitudes_database`, **versión 3**, con las tablas `solicitudes` y `usuarios`.

- La migración **2 → 3** agrega `creadoPorUsuarioId` y `asignadoAUsuarioId` sin borrar registros, y vincula las solicitudes anteriores a la cuenta `demo` si existe.
- `AppDatabase` entrega una única instancia compartida por toda la app y configura `BundledSQLiteDriver`.

## Estructura del proyecto

```
app/src/main/java/com/example/appsolicitudes_grupo12/
├── data/
│   ├── local/
│   │   ├── AppDatabase.kt
│   │   ├── SolicitudDao.kt
│   │   ├── SolicitudEntity.kt
│   │   ├── UsuarioDao.kt
│   │   └── UsuarioEntity.kt
│   └── repository/
│       ├── AuthRepository.kt
│       └── SolicitudRepository.kt
├── model/
│   ├── DetalleSolicitudUiState.kt
│   ├── FiltroSolicitudes.kt
│   ├── ListaSolicitudesUiState.kt
│   ├── LoginUiState.kt
│   ├── QrUiState.kt
│   ├── SolicitudErrores.kt
│   ├── SolicitudQr.kt
│   └── SolicitudUiState.kt
├── navigation/
│   └── AppNavigation.kt
├── ui/
│   ├── screen/
│   │   ├── AsignarSolicitudesScreen.kt
│   │   ├── DetalleSolicitudScreen.kt
│   │   ├── IconoError.kt
│   │   ├── ImagenQrSolicitud.kt
│   │   ├── ListaSolicitudesScreen.kt
│   │   ├── LoginScreen.kt
│   │   ├── MenuScreen.kt
│   │   ├── QrScannerScreen.kt
│   │   └── SolicitudFormScreen.kt
│   └── theme/
│       ├── Color.kt
│       ├── Theme.kt
│       └── Type.kt
├── utils/
│   ├── AdaptiveContent.kt
│   ├── CameraPermissionHelper.kt
│   ├── EvidenciaUtils.kt
│   ├── QrScanner.kt
│   ├── QrUtils.kt
│   └── WindowSizeUtils.kt
├── viewModel/
│   ├── AsignarSolicitudesViewModel.kt
│   ├── DetalleSolicitudViewModel.kt
│   ├── ListaSolicitudesViewModel.kt
│   ├── LoginViewModel.kt
│   ├── QrViewModel.kt
│   └── SolicitudViewModel.kt
└── MainActivity.kt
app/src/main/res/xml/   file_paths.xml (carpeta que comparte el FileProvider)
```

> **Nota sobre `utils/`:** esta carpeta se agregó **fuera de la estructura base aprobada por el docente** (`data`, `model`, `navigation`, `ui`, `viewModel`) para agrupar funciones de apoyo que no son pantallas ni ViewModels:
> - `QrScanner.kt`: abre la cámara con CameraX y lee el QR con ML Kit.
> - `QrUtils.kt`: arma y lee el texto JSON del QR y lo dibuja como imagen (ZXing).
> - `EvidenciaUtils.kt`: guarda, copia y borra las fotos de evidencia; crea la URI del `FileProvider`.
> - `CameraPermissionHelper.kt`: revisa si hay permiso de cámara.
> - `WindowSizeUtils.kt`: detecta la clase de tamaño de ventana (`AppWindowSizeClass`).
> - `AdaptiveContent.kt`: modificador que limita el ancho del contenido según la clase de ventana.

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

> **Base de datos:** la versión actual de Room es la 3. La migración 2 → 3 conserva las solicitudes existentes, por lo que no es necesario desinstalar la app. Si se modifican las tablas, hay que subir la versión y agregar su migración.

> **Android Studio y teléfonos con Android 16:** si al usar un teléfono real aparece un error en *App Inspection* (inspector de base de datos), no es un fallo de la app: ocurre cuando Android Studio no está actualizado.

## Cómo probar el flujo

### Como cliente (`demo` / `1234`)
1. Desde el menú, entra a **Nueva solicitud**.
2. Intenta registrar con campos vacíos: deben aparecer los errores.
3. Completa los datos, adjunta una foto (cámara o galería, opcional) y pulsa **Registrar solicitud**. Se mostrará el código y su QR.
4. En **Mis solicitudes** verás la solicitud; prueba los filtros y el orden.
5. Toca una tarjeta para ver el detalle y editar sus datos.
6. Cierra la app por completo y vuelve a abrirla: la solicitud sigue guardada.

### Como administrador (`admin` / `1234`)
1. En **Solicitudes** verás las solicitudes de todos los clientes.
2. Entra a **Asignar solicitudes**, elige un técnico y pulsa **Asignar**: el estado cambia a "Asignada".

### Como técnico (`tecnico` / `1234`)
1. En **Solicitudes** verás únicamente las que el administrador le asignó.
2. Abre una para consultar su detalle (solo lectura).

### Escáner QR (cualquier perfil)
- Pulsa **Escanear QR** en la barra superior de la lista y apunta a un QR mostrado en otro teléfono o en una captura abierta en un computador.

## Pruebas realizadas

Se probaron en Android Studio y en teléfono real: login vacío, incorrecto y correcto; menú y cierre de sesión; lista vacía; formulario vacío y válido; persistencia al reiniciar; detalle; edición con errores, cancelada y válida; foto por cámara y galería (incluida la cancelación); visibilidad por perfil; asignación; filtro y orden; QR generado y escaneado; migración Room v2 → v3; tema claro/oscuro con animaciones; y diseño adaptable en ventanas compacta, mediana y expandida (con rotación y redimensionamiento). El detalle y las capturas están en el informe.

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

## Fuera del alcance de esta etapa

Registro de usuarios (las cuentas de prueba se crean automáticamente), cambio de estados por parte del técnico, prioridad, historial, comentarios, notificaciones, integración con WhatsApp y datos compartidos entre dispositivos (API REST / Retrofit).

## Datos y privacidad

Toda la información (usuarios, solicitudes, fotos y QR) es **ficticia**. No se deben usar datos personales, conversaciones reales ni información comercial sensible.
