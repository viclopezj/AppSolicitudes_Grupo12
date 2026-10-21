# AppSolicitudes_Grupo12

Aplicación móvil Android para la **gestión y seguimiento de solicitudes de clientes**, desarrollada para la asignatura **DSY1105 – Desarrollo de Aplicaciones Móviles** (Duoc UC, Escuela de Informática y Telecomunicaciones, CITT) como parte de la segunda evaluación.

Caso: *Gestión móvil y seguimiento de solicitudes de clientes* (Caso_DSY1105_005D).

## Integrantes

| Nombre |
|--------|
| Vicente Lopez |
| Isaac Rivas |

## Descripción del proyecto

Una pequeña empresa de servicios informáticos recibe hoy las solicitudes de sus clientes por WhatsApp, lo que dificulta el seguimiento, la trazabilidad y la visibilidad de cada requerimiento. Esta aplicación es un **Producto Mínimo Viable (MVP)** que representa cómo una solicitud puede quedar registrada, identificada con un código único, asignada a un responsable y disponible para consulta según el perfil de cada usuario.

Para esta etapa se desarrolló el **módulo de Solicitudes** con **tres perfiles** (cliente, administrador y técnico), demostrando arquitectura MVVM real, persistencia local con Room, navegación, formularios con validación, gestión de estado, animaciones con la API de Compose (`AnimatedContent`, `AnimatedVisibility` y `animateColorAsState`) y uso de recursos nativos (cámara y galería).

> Todos los datos utilizados son **ficticios**. La app no lee conversaciones reales ni se integra con WhatsApp.

## Tecnologías

- Kotlin 2.2.10
- Jetpack Compose + Material Design 3 (tema propio: paleta verde azulada, tipografía y formas personalizadas, modo claro y oscuro)
- Arquitectura MVVM (View, ViewModel, Repository, DAO, Room)
- Room 3.0.3 con `sqlite-bundled` 2.7.0 (generación de código con KSP 2.3.6)
- Navigation Compose 2.9.8
- Corrutinas, `StateFlow` y `UiState`
- CameraX 1.4.2 + ML Kit Barcode Scanning 17.3.0 (lectura de QR)
- ZXing core 3.5.3 (generación de QR)
- Coil 3.3.0 (visualización de la foto de evidencia)
- Android Gradle Plugin 9.4.1, Compose BOM 2026.02.01

### Dependencias agregadas y para qué se usan

| Dependencia | Para qué se usa |
|-------------|-----------------|
| KSP 2.3.6 | Generador de código que Room necesita al compilar. |
| Room 3 (`room3-runtime`, `room3-compiler`) | Base de datos local: Entity, DAO y Database. |
| `sqlite-bundled` / `sqlite-async` | Driver de SQLite obligatorio en Room 3 y `executeSQL()` para la migración. |
| Navigation Compose | Navegación entre pantallas. |
| `lifecycle-viewmodel-compose` | Obtener el ViewModel con `viewModel()` en Compose. |
| `material-icons-core` | Íconos básicos (Add, ArrowBack, Menu, Person, Lock, Edit, Warning...). |
| CameraX (`camera-core`, `camera-camera2`, `camera-lifecycle`, `camera-view`) | Abre la cámara y muestra su vista previa en el escáner QR. |
| ML Kit `barcode-scanning` | **Lee** el código QR a partir de las imágenes de la cámara. |
| ZXing `core` | **Genera** la imagen del código QR de cada solicitud. |
| Coil `coil-compose` | Carga y muestra la foto de evidencia en segundo plano. |

`TakePicture` y `PickVisualMedia` vienen con `activity-compose`, `FileProvider` con `androidx.core` y `org.json` (texto del QR) con Android, por lo que no requieren dependencias nuevas.

## Perfiles y cuentas de prueba

Las tres cuentas ficticias se crean automáticamente en Room al iniciar sesión por primera vez.

| Perfil | Usuario | Clave | Qué puede hacer |
|--------|---------|-------|-----------------|
| Cliente | `demo` | `1234` | Registrar solicitudes, ver y editar las que creó. |
| Administrador | `admin` | `1234` | Ver todas las solicitudes, editarlas y asignarlas a técnicos. |
| Técnico | `tecnico` | `1234` | Ver (solo lectura) las solicitudes que le asignaron. |

## Funcionalidades implementadas

### Animaciones y retroalimentación visual
- **`AnimatedContent`:** fundido suave entre los estados Loading, Success, Empty y Error de la lista y del detalle (y entre modo lectura y modo edición del detalle).
- **`animateColorAsState`:** la etiqueta de estado cambia de color con una transición de 400 ms (por ejemplo, al pasar de "Recibida" a "Asignada").
- **`AnimatedVisibility`:** aparecen y se esconden con animación el aviso de éxito de la asignación (que se cierra solo a los 3 segundos), la vista previa de la foto de evidencia, el botón "Quitar foto" y el mensaje de error del login.
- **Íconos en formularios:** ícono inicial en cada campo, ícono de advertencia cuando el campo tiene error y botón para mostrar u ocultar la clave en el login.

### Autenticación, perfiles y menú
- Inicio de sesión con cuentas ficticias guardadas en Room.
- Validación de campos vacíos y mensajes de error claros.
- Control de acceso por perfil: cada usuario solo ve las solicitudes que le corresponden (la consulta se filtra en el DAO, y el detalle y el escáner QR también comprueban el acceso).
- Menú lateral (drawer) con opciones según el perfil y cierre de sesión.

### Registro de solicitudes (cliente)
- Formulario con título, descripción, categoría (10 categorías del caso), servicio relacionado y foto de evidencia opcional.
- Validaciones controladas desde el ViewModel, con errores visibles en los campos (`isError` y `supportingText`):
  - Título obligatorio.
  - Descripción obligatoria con largo mínimo (10 caracteres).
  - Categoría obligatoria.
  - Servicio relacionado obligatorio salvo en "Consulta" y "Otro requerimiento".
- Generación de **código único** (`SOL-XXXXXXXX`), registro de fecha y hora, y estado inicial **"Recibida"**.
- Al guardar, un diálogo muestra el código de seguimiento y su **código QR**.

### Lista de solicitudes
- Muestra las solicitudes almacenadas en Room según el perfil, con código, título, categoría y estado (con etiqueta de color).
- Se actualiza automáticamente al registrar, editar o asignar (`Flow`).
- Estados de interfaz: Loading, Success, Empty y Error (con botón Reintentar).
- **Filtros** por categoría y **orden** por fecha (más recientes / más antiguas).
- El botón para crear solicitudes solo aparece al cliente.

### Detalle y edición
- Modo lectura con todos los datos, foto de evidencia y código QR.
- Modo edición de título, descripción, categoría y servicio, con las mismas validaciones del formulario. Disponible para el cliente (sus solicitudes) y el administrador; el técnico solo consulta.
- Código, fecha, estado y foto no son editables.

### Asignación de solicitudes (administrador)
- Pantalla con todas las solicitudes y un selector de técnicos.
- Al asignar, la solicitud pasa al estado **"Asignada"** y queda visible para ese técnico.
- Solo se pueden asignar cuentas con perfil técnico (validado en el DAO).

### Recursos nativos: cámara y galería
- **Foto de evidencia (opcional):** tomada con la cámara (`TakePicture` + `FileProvider`) o elegida de la galería (`PickVisualMedia`). Se guarda como archivo propio de la app y se vincula a la solicitud.
- **Escáner QR:** lee el QR de una solicitud, muestra sus datos y, si la solicitud existe en el teléfono y el usuario tiene acceso, permite abrir su detalle. Si el QR no pertenece a la app, se informa y se puede escanear otro.
- Manejo del permiso de cámara denegado y de errores de lectura de imagen, sin cierres inesperados.

## Estructura del proyecto

```
app/src/main/java/com/example/appsolicitudes_grupo12/
├── data/
│   ├── local/          SolicitudEntity, SolicitudDao, UsuarioEntity, UsuarioDao, AppDatabase
│   └── repository/     SolicitudRepository, AuthRepository
├── model/              UiStates (lista, detalle, QR, login, formulario), errores, filtros y datos del QR
├── navigation/         AppNavigation
├── ui/
│   ├── screen/         Login, Menú, Lista, Formulario, Detalle, Asignar solicitudes, Escáner QR, ImagenQrSolicitud
│   └── theme/          Color, Theme, Type
├── utils/              QrScanner, QrUtils, EvidenciaUtils, CameraPermissionHelper
├── viewModel/          ViewModels de cada pantalla
└── MainActivity.kt
app/src/main/res/xml/   file_paths.xml (carpeta que comparte el FileProvider)
```

> **Nota sobre `utils/`:** esta carpeta se agregó **fuera de la estructura base aprobada por el docente** (`data`, `model`, `navigation`, `ui`, `viewModel`) para agrupar las funciones de apoyo de la cámara, que no son pantallas ni ViewModels:
> - `QrScanner.kt`: abre la cámara y lee el QR con ML Kit.
> - `QrUtils.kt`: arma y lee el texto del QR y lo dibuja como imagen.
> - `EvidenciaUtils.kt`: guarda, copia y borra las fotos de evidencia.
> - `CameraPermissionHelper.kt`: revisa si hay permiso de cámara.

## Requisitos para ejecutar

- Android Studio actualizado (versión reciente con soporte para AGP 9.x)
- JDK 25 (Android Studio lo gestiona mediante `gradle-daemon-jvm.properties`)
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
6. **Iniciar sesión** con una de las cuentas de prueba (ver tabla de perfiles; clave `1234`).

> **Base de datos:** la versión actual de Room es la 3. La migración 2 → 3 conserva las solicitudes existentes, por lo que no es necesario desinstalar la app. Si modificas las tablas en el futuro, sube la versión y agrega su migración.

> **Android Studio y teléfonos con Android 16:** si al usar un teléfono real aparece un error en *App Inspection* (inspector de base de datos), no es un fallo de la app: ocurre cuando Android Studio no está actualizado. Se soluciona actualizando el IDE.

## Cómo probar el flujo

### Como cliente (`demo` / `1234`)
1. Desde el menú, entra a **Nueva solicitud**.
2. Intenta registrar con campos vacíos: deben aparecer los errores.
3. Completa los datos, adjunta una foto (cámara o galería, opcional) y pulsa **Registrar solicitud**. Se mostrará el código y su QR.
4. En **Mis solicitudes** verás la solicitud; pruébala con los filtros y el orden.
5. Toca una tarjeta para ver el detalle y editar sus datos.
6. Cierra y vuelve a abrir la app: la solicitud sigue guardada (persistencia en Room).

### Como administrador (`admin` / `1234`)
1. En **Solicitudes** verás las solicitudes de todos los clientes.
2. Entra a **Asignar solicitudes**, elige un técnico y pulsa **Asignar**: el estado cambia a "Asignada".

### Como técnico (`tecnico` / `1234`)
1. En **Solicitudes** verás únicamente las que el administrador le asignó.
2. Abre una para consultar su detalle (solo lectura).

### Escáner QR (cualquier perfil)
- Pulsa **Escanear QR** en la barra superior de la lista y apunta a un QR de otra pantalla o de otro teléfono.

## Limitaciones conocidas

1. El QR es una copia de los datos al momento de crearlo: si la solicitud se edita después, un QR ya impreso o capturado queda desactualizado.
2. La descripción dentro del QR se corta a 300 caracteres (título y servicio a 100).
3. La foto no viaja en el QR: solo existe en el teléfono que la tomó.
4. Cada teléfono tiene su propia base de datos, por lo que "Abrir detalle" solo aparece donde la solicitud existe y el perfil tiene acceso a ella.
5. Un teléfono no puede escanear su propia pantalla: se necesita otro dispositivo o una captura abierta en un computador.
6. El escáner de la app solo reconoce QR generados por la propia aplicación (los que traen la marca `AppSolicitudes_Grupo12`). Un QR de otra fuente muestra el aviso "Este código QR no es de una solicitud", y si el QR de una solicitud se lee con otra app de escaneo, el contenido aparece como texto JSON sin formato.
7. El QR lleva los datos de la solicitud dentro, por lo que cualquier persona que lo escanee con el escáner de la app ve esa información aunque su perfil no tenga acceso a la solicitud en Room; el botón "Abrir detalle" sí respeta los permisos. Es consecuencia de que el QR no consulta un servidor.
8. El usuario y el perfil viajan como argumentos de ruta de navegación (no hay una sesión central). Es aceptable en una app local y los DAO repiten la validación de acceso, pero en una app con servidor se reemplazaría por un token de sesión.

## Fuera del alcance de esta etapa

Registro de usuarios (las cuentas de prueba se crean automáticamente), cambio de estados por parte del técnico, prioridad, historial, comentarios, notificaciones, integración con WhatsApp y datos compartidos entre dispositivos (API REST / Retrofit).

## Datos y privacidad

Toda la información (usuarios, solicitudes, fotos y QR) es **ficticia**. No se deben usar datos personales, conversaciones reales ni información comercial sensible.
