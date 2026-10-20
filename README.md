# AppSolicitudes_Grupo12

Aplicación móvil Android para la **gestión y seguimiento de solicitudes de clientes**, desarrollada para la asignatura **DSY1105 – Desarrollo de Aplicaciones Móviles** (Duoc UC, Escuela de Informática y Telecomunicaciones, CITT) como parte de la segunda evaluación.

Caso: *Gestión móvil y seguimiento de solicitudes de clientes* (Caso_DSY1105_005D).

## Integrantes

| Nombre |
|--------|
| Vicente Lopez |
| Isaac Rivas |

## Descripción del proyecto

Una pequeña empresa de servicios informáticos recibe hoy las solicitudes de sus clientes por WhatsApp, lo que dificulta el seguimiento, la trazabilidad y la visibilidad de cada requerimiento. Esta aplicación es un **Producto Mínimo Viable (MVP)** que representa cómo una solicitud puede quedar registrada, identificada con un código único y disponible para consulta.

Para esta etapa se desarrolló el **módulo de Solicitudes**, demostrando arquitectura MVVM real, persistencia local con Room, navegación, formularios con validación, gestión de estado y uso de un recurso nativo (cámara).

> Todos los datos utilizados son **ficticios**. La app no lee conversaciones reales ni se integra con WhatsApp.

## Tecnologías

- Kotlin 2.2.10
- Jetpack Compose + Material Design 3
- Arquitectura MVVM (View, ViewModel, Repository, DAO, Room)
- Room 3.0.3 con `sqlite-bundled` 2.7.0 (generación de código con KSP 2.3.6)
- Navigation Compose 2.9.8
- Corrutinas, `StateFlow` y `UiState`
- CameraX 1.4.2 + ML Kit Barcode Scanning 17.3.0 (lectura de QR)
- ZXing core 3.5.3 (generación de QR)
- Coil 3.3.0 (visualización de la foto de evidencia)
- Android Gradle Plugin 9.4.1, Compose BOM 2026.02.01

## Funcionalidades implementadas

### Autenticación y menú
- Inicio de sesión con cuenta ficticia guardada en Room.
- Validación de campos vacíos y mensajes de error claros.
- Menú lateral (drawer) con acceso a la lista, al formulario y cierre de sesión.

### Registro de solicitudes
- Formulario con título, descripción, categoría (10 categorías del caso) y servicio relacionado.
- Validaciones controladas desde el ViewModel, con errores visibles en los campos (`isError` y `supportingText`):
  - Título obligatorio.
  - Descripción obligatoria con largo mínimo (10 caracteres).
  - Categoría obligatoria.
  - Servicio relacionado obligatorio salvo en "Consulta" y "Otro requerimiento".
- Generación de **código único** (`SOL-XXXXXXXX`), registro de fecha y hora, y estado inicial **"Recibida"**.
- Al guardar, un diálogo muestra el código de seguimiento y su **código QR**.

### Lista de solicitudes
- Muestra las solicitudes almacenadas en Room con código, título, categoría y estado.
- Se actualiza automáticamente al registrar o editar (`Flow`).
- Estados de interfaz: Loading, Success, Empty y Error (con botón Reintentar).
- **Filtros** por categoría y **orden** por fecha (más recientes / más antiguas).

### Detalle y edición
- Modo lectura con todos los datos, foto de evidencia y código QR.
- Modo edición de título, descripción, categoría y servicio, con las mismas validaciones del formulario.
- Código, fecha, estado y foto no son editables.

### Recurso nativo: cámara
- **Foto de evidencia (opcional):** tomada con la cámara (`TakePicture` + `FileProvider`) o elegida de la galería (`PickVisualMedia`). Se guarda como archivo propio de la app y se vincula a la solicitud.
- **Escáner QR:** lee el QR de una solicitud, muestra sus datos y, si la solicitud existe en el teléfono, permite abrir su detalle. Si el QR no pertenece a la app, se informa y se puede escanear otro.
- Manejo del permiso de cámara denegado y de errores de lectura de imagen, sin cierres inesperados.

## Estructura del proyecto

```
app/src/main/java/com/example/appsolicitudes_grupo12/
├── data/
│   ├── local/          SolicitudEntity, SolicitudDao, UsuarioEntity, UsuarioDao, AppDatabase
│   └── repository/     SolicitudRepository, AuthRepository
├── model/              UiStates, errores, filtros y datos del QR
├── navigation/         AppNavigation
├── ui/
│   ├── screen/         Login, Menú, Lista, Formulario, Detalle, Escáner QR
│   └── theme/          Color, Theme, Type
├── utils/              QrScanner, QrUtils, EvidenciaUtils, CameraPermissionHelper
├── viewModel/          ViewModels de cada pantalla
└── MainActivity.kt
```

## Requisitos para ejecutar

- Android Studio (versión reciente con soporte para AGP 9.x)
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
6. **Iniciar sesión** con la cuenta de prueba:
   - Usuario: `demo`
   - Clave: `1234`

> Si modificas las tablas de Room durante el desarrollo, sube la versión de la base de datos o desinstala la app del dispositivo antes de volver a ejecutarla.

## Cómo probar el flujo

1. Inicia sesión con `demo / 1234`.
2. Desde el menú, entra a **Registrar nueva solicitud**.
3. Intenta registrar con campos vacíos: deben aparecer los errores.
4. Completa los datos, adjunta una foto (opcional) y pulsa **Registrar solicitud**. Se mostrará el código y su QR.
5. En **Mis solicitudes** verás la solicitud en la lista; pruébala con los filtros y el orden.
6. Toca una tarjeta para ver el detalle y editar sus datos.
7. Cierra y vuelve a abrir la app: la solicitud sigue guardada (persistencia en Room).
8. Pulsa **Escanear QR** en la barra superior de la lista y apunta a un QR de otra pantalla o de otro teléfono.

## Limitaciones conocidas

1. El QR es una copia de los datos al momento de crearlo: si la solicitud se edita después, un QR ya impreso o capturado queda desactualizado.
2. La descripción dentro del QR se corta a 300 caracteres (título y servicio a 100).
3. La foto no viaja en el QR: solo existe en el teléfono que la tomó.
4. Cada teléfono tiene su propia base de datos, por lo que "Abrir detalle" solo aparece donde la solicitud existe.
5. Un teléfono no puede escanear su propia pantalla: se necesita otro dispositivo o una captura abierta en un computador.

## Fuera del alcance de esta etapa

Perfiles diferenciados (cliente, administrador, técnico), registro de usuarios, prioridad, responsable, historial, comentarios, notificaciones, integración con WhatsApp y datos compartidos entre dispositivos (API REST / Retrofit).

## Datos y privacidad

Toda la información (usuarios, solicitudes, fotos y QR) es **ficticia**. No se deben usar datos personales, conversaciones reales ni información comercial sensible.
