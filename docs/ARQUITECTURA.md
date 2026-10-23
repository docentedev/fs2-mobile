# Arquitectura de Disco

Documento de referencia para entender cómo está organizada la aplicación, qué
responsabilidad tiene cada capa, cómo fluyen los datos y por qué se eligió cada
tipo de Kotlin (`data class`, `sealed class`, `object`, etc.).

> **Alcance:** este documento describe el código tal como está hoy en
> `app/src/main/java/com/rojas/disco`. No es una arquitectura ideal teórica:
> documenta la implementación real, sus decisiones y su deuda técnica.

### Cómo leer este documento

- **Entender la organización general:** secciones 3 y 4.
- **Entender cómo se conectan UI, repositorio y base de datos:** secciones 3, 5 y 6.
- **Entender por qué se eligió cada tipo de Kotlin:** sección 7.
- **Agregar una pantalla o ruta nueva:** sección 8.6.
- **Revisar limitaciones y próximos pasos:** sección 12.

---

## 1. Resumen de la aplicación

**Disco** es una aplicación Android nativa para gestionar clases universitarias,
asistencia y cuestionarios.

Roles funcionales:

- **Profesor:** crea clases, muestra el QR de una clase, escanea el QR de un
  estudiante para marcarlo presente y revisa asistencias/respuestas.
- **Estudiante:** se inscribe escaneando el QR de una clase, responde el
  cuestionario y muestra su QR personal para que el profesor lo marque presente.

Flujos principales:

1. Login por correo electrónico.
2. Listado de clases del profesor o del estudiante.
3. Detalle de clase, QR y asistencias.
4. Creación y edición de clases.
5. Registro de asistencia y respuestas de cuestionario.

La aplicación es **local-first**: toda la información vive en SQLite mediante
Room. No hay backend ni sincronización remota implementada.

---

## 2. Stack tecnológico

| Tecnología | Versión / uso | Para qué se usa |
|---|---|---|
| Kotlin | 2.1.0 | Lenguaje principal. |
| Jetpack Compose | BOM `2026.02.01` | UI declarativa. |
| Material 3 | Incluido en el BOM | Componentes visuales y tema. |
| Navigation Compose | 2.8.8 | Navegación entre pantallas. |
| Room | 2.8.5 + KSP | Persistencia local SQLite y consultas reactivas. |
| Kotlin Coroutines / Flow | Incluido con Room/Compose | Operaciones asíncronas y observación de datos. |
| CameraX | 1.4.1 | Vista previa de cámara. |
| ML Kit Barcode Scanning | 17.3.0 | Detección de códigos QR. |
| ZXing Core | 3.5.4 | Generación de imágenes QR. |
| Hilt | 2.55 | Inyección de dependencias. |
| Hilt Navigation Compose | 1.2.0 | Inyección de ViewModels en la navegación de Compose. |
| Retrofit / OkHttp | 2.11.0 / 4.12.0 | Declarados para una futura API remota; hoy no se usan. |
| KSP | 2.1.0-1.0.29 | Generación de código de Room y Hilt. |
| Gradle + version catalog | `gradle/libs.versions.toml` | Dependencias centralizadas. |

No se usan actualmente:

- Backend o Retrofit (declaradas dependencias para futuro).
- Serialización tipada de JSON (`kotlinx.serialization`/Moshi/Gson en código).
- Tests de UI más allá de los ejemplos generados por la plantilla.

---

## 3. Vista general por capas

La app sigue una separación clara en tres capas con inyección de dependencias mediante Hilt:

```text
┌─────────────────────────────────────────────────────────────────────┐
│                         CAPA DE PRESENTACIÓN                        │
│                                                                     │
│  MainActivity (@AndroidEntryPoint)                                  │
│    ├── NavHost + Destino (rutas)                                    │
│    ├── Screens (Login, Home profesor/estudiante, Session, Profile…) │
│    ├── ViewModels (@HiltViewModel + StateFlow + UiState)            │
│    ├── Components reutilizables (tarjetas, estados, QR, scaffold)   │
│    └── Theme (Material 3)                                           │
│                                                                     │
│  Inyección con hiltViewModel() / SavedStateHandle                   │
└───────────────────────────────┬─────────────────────────────────────┘
                                │ StateFlow / suspend
                                ▼
┌─────────────────────────────────────────────────────────────────────┐
│                      CAPA DE LÓGICA / REPOSITORIO                   │
│                                                                     │
│  UniversidadRepository (@Inject @Singleton)                         │
│    ├── Expone Flow para datos observables                           │
│    ├── Expone suspend para escrituras/consultas puntuales           │
│    └── Contiene reglas de negocio (inscripción, asistencia)         │
└───────────────────────────────┬─────────────────────────────────────┘
                                │ DAO (@Inject via DatabaseModule)
                                ▼
┌─────────────────────────────────────────────────────────────────────┐
│                         CAPA DE DATOS LOCAL                         │
│                                                                     │
│  UniversidadDao (interface generada por Room)                       │
│  AppDatabase (Room Database proveída por DatabaseModule)            │
│  Entities (tablas)                                                  │
│  Relation models (@Embedded / @Relation)                            │
│  CodigoQr (formatos de QR)                                          │
│                                                                     │
│                           SQLite                                    │
└─────────────────────────────────────────────────────────────────────┘
```

### Dirección de las dependencias

- La UI conoce al repositorio.
- El repositorio conoce al DAO.
- El DAO conoce las entidades y modelos de relación.
- Las capas inferiores **no** conocen Compose ni la UI.

Esto permite cambiar la fuente de datos (por ejemplo, agregar Retrofit) sin
tocar las pantallas, siempre que se mantenga la API del repositorio.

### Sobre la capa de dominio

No existe un paquete `domain` separado. Las reglas de negocio actuales viven en
`UniversidadRepository`:

- Un profesor no puede inscribirse como estudiante de su propia clase.
- Solo se guardan respuestas si el estudiante está presente.
- La inscripción crea una asistencia inicial en `false`.

Si la app crece, esas reglas podrían extraerse a casos de uso (`UseCase`) o a
modelos de dominio, pero hoy el repositorio cumple ese rol sin complejidad
innecesaria.

---

## 4. Estructura de directorios

```text
Disco/
├── app/
│   ├── build.gradle.kts
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── java/com/rojas/disco/
│       │   │   ├── DiscoApplication.kt       (Anotada con @HiltAndroidApp)
│       │   │   ├── MainActivity.kt           (Anotada con @AndroidEntryPoint)
│       │   │   │
│       │   │   ├── di/                       (Módulos de Inyección de Dependencias)
│       │   │   │   └── DatabaseModule.kt     (Provee AppDatabase y UniversidadDao)
│       │   │   │
│       │   │   ├── data/
│       │   │   │   ├── CodigoQr.kt
│       │   │   │   ├── local/
│       │   │   │   │   ├── database/
│       │   │   │   │   │   ├── AppDatabase.kt
│       │   │   │   │   │   └── UniversidadDao.kt
│       │   │   │   │   ├── entities/
│       │   │   │   │   │   ├── UsuarioEntity.kt
│       │   │   │   │   │   ├── RolEntity.kt
│       │   │   │   │   │   ├── UsuarioRolCrossRef.kt
│       │   │   │   │   │   ├── ClaseEntity.kt
│       │   │   │   │   │   ├── AsistenciaClaseEntity.kt
│       │   │   │   │   │   ├── CursoEntity.kt              (sin uso)
│       │   │   │   │   │   ├── EstudianteEntity.kt          (sin uso)
│       │   │   │   │   │   └── EstudianteCursoCrossRef.kt   (sin uso)
│       │   │   │   │   └── model/
│       │   │   │   │       ├── UsuarioConRoles.kt
│       │   │   │   │       ├── ClaseConDetalles.kt
│       │   │   │   │       └── RolConUsuarios.kt
│       │   │   │   └── repository/
│       │   │   │       └── UniversidadRepository.kt (@Inject @Singleton)
│       │   │   │
│       │   │   └── ui/
│       │   │       ├── navigation/
│       │   │       │   └── Destino.kt
│       │   │       ├── viewmodels/           (ViewModels con @HiltViewModel)
│       │   │       │   ├── LoginViewModel.kt
│       │   │       │   ├── TeacherHomeViewModel.kt
│       │   │       │   ├── StudentHomeViewModel.kt
│       │   │       │   ├── SessionViewModel.kt
│       │   │       │   ├── SessionDetailViewModel.kt
│       │   │       │   ├── SessionEditViewModel.kt
│       │   │       │   ├── StudentQuestionnaireViewModel.kt
│       │   │       │   └── ProfileViewModel.kt
│       │   │       ├── screens/
│       │   │       │   ├── LoginScreen.kt
│       │   │       │   ├── TeacherHomeScreen.kt
│       │   │       │   ├── StudentHomeScreen.kt
│       │   │       │   ├── StudentQuestionnaireScreen.kt
│       │   │       │   ├── SessionScreen.kt
│       │   │       │   ├── SessionDetailScreen.kt
│       │   │       │   ├── SessionEditScreen.kt
│       │   │       │   └── ProfileScreen.kt
│       │   │       ├── components/
│       │   │       │   ├── PantallaBase.kt
│       │   │       │   ├── Tarjetas.kt
│       │   │       │   ├── Estados.kt
│       │   │       │   └── QrComponents.kt
│       │   │       └── theme/
│       │   │           ├── Color.kt
│       │   │           ├── Theme.kt
│       │   │           └── Type.kt
│       │   │
│       │   └── res/                  (íconos, temas XML, strings, backups)
│       │
│       ├── test/                     (tests JVM)
│       └── androidTest/              (tests instrumentados)
│
├── docs/
│   └── ARQUITECTURA.md               (este documento)
├── gradle/
│   └── libs.versions.toml
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── gradlew / gradlew.bat
└── local.properties                  (no versionado)
```

### Qué va en cada paquete

| Paquete | Responsabilidad | No debería contener |
|---|---|---|
| `com.rojas.disco` | Punto de entrada, composición de dependencias y `NavHost`. | Lógica de negocio o consultas SQL. |
| `data.local.entities` | Clases `@Entity` que representan tablas. | Lógica de UI o navegación. |
| `data.local.model` | Proyecciones de consulta con `@Embedded`/`@Relation`. | Tablas nuevas ni escrituras. |
| `data.local.database` | `@Database`, DAO e inicialización de Room. | Estado de Compose. |
| `data.repository` | API de datos para la UI y reglas de negocio simples. | Referencias a `NavController` o composables. |
| `data` | Tipos de datos transversales, como `CodigoQr`. | Persistencia. |
| `ui.navigation` | Rutas tipadas y helpers de navegación. | Pantallas concretas. |
| `ui.screens` | Una pantalla por archivo y su estado local. | Componentes genéricos reutilizables. |
| `ui.components` | UI reutilizable sin dependencia del repositorio. | Consultas a Room. |
| `ui.theme` | Colores, tipografía y tema Material 3. | Lógica de negocio. |

---

## 5. Capa de presentación

### 5.1 `MainActivity` e Inyección con Hilt

`MainActivity` está anotada con `@AndroidEntryPoint`. Gracias a Hilt, la infraestructura de datos (`AppDatabase`, `UniversidadDao`, `UniversidadRepository`) es inyectada automáticamente en la gráfica de dependencias sin necesidad de instanciarlas manualmente.

`DiscoApplication` (anotada con `@HiltAndroidApp`) sirve como el contenedor raíz de dependencias para toda la aplicación.

`MainActivity` cumple las siguientes funciones:

1. Configurar Compose con el tema de la app (`DiscoTheme`).
2. Alojar el grafo de navegación (`NavHost` con Navigation Compose).
3. Mantener el estado global de navegación y sesión (`currentUsuarioId`, `isEstudianteUser`).

Además, `AppNavegacion` mantiene el estado de sesión en memoria:

```kotlin
var currentUsuarioId by remember { mutableStateOf<Long?>(null) }
var isEstudianteUser by remember { mutableStateOf(false) }
```

Ese estado decide:

- qué destino abrir después del login;
- qué ítem de la barra inferior está seleccionado;
- si se muestra o no la barra inferior;
- qué hacer al cerrar sesión.

**Limitación actual:** al ser `remember` y no `rememberSaveable`, el estado de
sesión se pierde si el proceso se recrea (por ejemplo, muerte del proceso en
segundo plano). La navegación puede restaurarse, pero `currentUsuarioId` vuelve
a `null`. Ver “Deuda técnica”.

### 5.2 Navegación tipada: `ui/navigation/Destino.kt`

Todas las rutas están centralizadas en una `sealed class`:

```kotlin
sealed class Destino(
    val base: String,
    val argumentos: List<ArgumentoRuta> = emptyList()
) {
    data object Login : Destino("login")

    data object Sesion : Destino(
        base = "session",
        argumentos = listOf(ArgumentoRuta("claseId", NavType.LongType))
    ) {
        fun crearRuta(claseId: Long) = construirRuta(claseId)
        fun claseId(entry: NavBackStackEntry) = entry.long()
    }
}
```

Cada destino conoce:

- su segmento base (`session`);
- sus argumentos y sus tipos (`NavType.LongType`);
- cómo construir una ruta concreta (`crearRuta`);
- cómo leer sus argumentos desde el back stack (`claseId(entry)`).

El patrón se genera automáticamente:

```text
base = "session"
argumentos = ["claseId"]
patron = "session/{claseId}"
```

Y una ruta concreta se construye sin concatenar strings a mano:

```kotlin
Destino.Sesion.crearRuta(42L) // "session/42"
```

Para registrar destinos no se repite `route` ni `arguments`:

```kotlin
composableDestino(Destino.Sesion) { backStackEntry ->
    val claseId = Destino.Sesion.claseId(backStackEntry)
    SessionScreen(claseId = claseId, ...)
}
```

Para navegar:

```kotlin
navController.navegarA(Destino.Sesion, claseId)
navController.navegarAPestana(Destino.Perfil, usuarioId)
```

`navegarAPestana` concentra la configuración de la barra inferior:

```kotlin
popUpTo(graph.findStartDestination().id) { saveState = true }
launchSingleTop = true
restoreState = true
```

#### ¿Por qué se usa una `sealed class` para las rutas?

- **Conjunto cerrado:** solo existen los destinos declarados. El compilador
  conoce todos los casos.
- **Cada destino puede tener una forma distinta:** `Login` no tiene argumentos;
  `CuestionarioEstudiante` tiene dos; otros tienen uno. Una `enum` no modela
  bien esa diferencia.
- **Comportamiento por caso:** cada objeto puede exponer `crearRuta` y lectores
  tipados con nombres claros (`claseId`, `estudianteId`, `usuarioId`).
- **Sin strings sueltos:** el nombre de la ruta y de los argumentos vive en un
  único lugar.
- **Seguridad al refactorizar:** renombrar un destino o un argumento genera
  errores de compilación en los usos, no fallos silenciosos en tiempo de
  ejecución.

#### ¿Por qué `data object` y no `object`?

`data object` aporta una implementación de `toString()` más legible y mantiene
la semántica de objeto único. Se usa para destinos sin parámetros ni estado
(`Login`, `Sesion`, etc.). No se usa `data class` porque no hay instancias con
datos: el destino es un valor único del conjunto.

### 5.3 Pantallas: `ui/screens`

Cada archivo contiene una pantalla y, cuando hace falta, componentes privados o
específicos de esa pantalla.

Patrón general:

```kotlin
@Composable
fun XScreen(
    id: Long,
    repository: UniversidadRepository,
    onNavigateBack: () -> Unit
) {
    val datos by repository.observarAlgo(id).collectAsState(initial = null)
    var estadoLocal by remember { mutableStateOf(false) }

    // UI declarativa
}
```

Responsabilidades de una pantalla:

- Observar datos del repositorio con `collectAsState`.
- Mantener estado efímero de UI (`remember`, `mutableStateOf`).
- Lanzar operaciones `suspend` con `rememberCoroutineScope`.
- Recibir callbacks de navegación (`onNavigateBack`, `onLogin...`).
- Renderizar componentes reutilizables.

Lo que **no** debería hacer:

- Consultar Room directamente.
- Guardar estado global de sesión.
- Construir rutas con strings.

#### Estado y efectos

| Herramienta | Uso en el proyecto | Por qué |
|---|---|---|
| `remember` | Guardar estado local entre recomposiciones. | Evita recrear valores en cada recomposición. |
| `mutableStateOf` | Estado observable de la UI. | Compose recompone al cambiar. |
| `collectAsState` | Convertir un `Flow` de Room en estado Compose. | Room emite cambios y la UI se actualiza sola. |
| `rememberCoroutineScope` | Lanzar escrituras desde callbacks. | El scope se cancela con la pantalla. |
| `LaunchedEffect` | Cargar datos puntuales al entrar o al cambiar un ID. | Efecto controlado por claves. |
| `withContext(Dispatchers.Main)` | Volver al hilo principal tras IO. | Navegar/Toast deben ejecutarse en main. |

El estado se mantiene **lo más abajo posible** (en la pantalla que lo usa). Solo
el estado de sesión sube a `MainActivity` porque lo necesitan el `NavHost` y la
barra inferior.

### 5.4 Componentes reutilizables: `ui/components`

| Archivo | Contiene | Reutilizado en |
|---|---|---|
| `PantallaBase.kt` | `PantallaConBarraSuperior`: `Scaffold` + `TopAppBar` + botón volver. | Session, SessionDetail, SessionEdit, StudentQuestionnaire. |
| `Tarjetas.kt` | `TarjetaSeccion` (tarjeta con padding/color) y `TarjetaClase` (tarjeta clicable con encabezado y pie opcionales). | Varias pantallas y listados. |
| `Estados.kt` | `EstadoVacio` para listas sin datos, con acción opcional. | Home de profesor y estudiante. |
| `QrComponents.kt` | Generación de QR, diálogo para mostrarlo, cámara con CameraX/ML Kit y diálogo de escaneo. | Session, StudentHome, Profile. |

Estos componentes son **stateless** siempre que es posible: reciben datos y
callbacks, no acceden al repositorio. Eso los hace fáciles de reutilizar y de
probar.

### 5.5 Tema: `ui/theme`

- `Color.kt`: paleta base.
- `Type.kt`: tipografía Material 3.
- `Theme.kt`: `DiscoTheme`, con soporte de modo oscuro y color dinámico en
  Android 12+.

`DiscoTheme` envuelve todo el contenido en `MaterialTheme`, de modo que las
pantallas usan `MaterialTheme.colorScheme` y `MaterialTheme.typography` en lugar
de colores/tamaños hardcodeados.

---

## 6. Capa de datos

### 6.1 Repositorio

`UniversidadRepository` es la única puerta de entrada de la UI a los datos.

Expone dos estilos de API:

```kotlin
// Observación reactiva: la UI se actualiza sola.
fun obtenerClasesPorProfesor(profesorId: Long): Flow<List<ClaseEntity>>

// Operación puntual: se espera el resultado.
suspend fun crearClase(clase: ClaseEntity): Long
```

Ventajas:

- La UI no conoce `UniversidadDao` ni Room.
- Las reglas de negocio tienen un lugar claro.
- Es fácil sustituir la fuente de datos o crear un fake para tests.

Reglas implementadas hoy:

```kotlin
// Un profesor no puede inscribirse como estudiante de su propia clase.
if (clase.profesorId == usuarioId) {
    throw IllegalArgumentException(...)
}

// Solo se guardan respuestas si el estudiante está presente.
val respuestasFinales = if (esPresente) respuestasJson else null
```

### 6.2 DAO

`UniversidadDao` es una **interface** anotada con `@Dao`. Room genera la
implementación en build time con KSP.

Métodos:

- `@Insert`, `@Update`, `@Delete`: escrituras.
- `@Query`: SQL explícito.
- `@Transaction` + `@Relation`: consultas con relaciones.
- `Flow<T>`: consultas observables.
- `suspend`: operaciones que no bloquean el hilo principal.

La interface permite:

- que Room genere la implementación;
- que el repositorio dependa de una abstracción;
- crear implementaciones falsas en tests sin base de datos real.

### 6.3 Base de datos

`AppDatabase` es una `abstract class` porque Room necesita heredar de
`RoomDatabase` y generar la implementación.

Registro de entidades:

```kotlin
@Database(
    entities = [
        UsuarioEntity::class,
        RolEntity::class,
        UsuarioRolCrossRef::class,
        ClaseEntity::class,
        AsistenciaClaseEntity::class
    ],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun universidadDao(): UniversidadDao
}
```

El singleton se implementa con `companion object`, `@Volatile` y
`synchronized(this)` (double-checked locking). Así se garantiza una sola
instancia de la base de datos en toda la app, incluso con acceso desde varios
hilos.

### 6.4 Entidades y modelo relacional

```text
usuarios
  usuarioId (PK)
  nombre
  email
      │ 1
      │
      │ N
clases
  claseId (PK)
  nombre
  descripcion
  profesorId (FK -> usuarios.usuarioId, RESTRICT)
  cuestionarioEstructuraJson
      │ 1
      │
      │ N
asistencia_clase
  usuarioId (PK, FK -> usuarios.usuarioId, CASCADE)
  claseId   (PK, FK -> clases.claseId, CASCADE)
  esPresente
  respuestasCuestionarioJson

usuarios N ─── N roles
        mediante usuario_rol_cross_ref
        (usuarioId, rolId) PK compuesta
```

Decisiones importantes:

- **`asistencia_clase` es la tabla intermedia N:M** entre usuarios y clases.
  Además de relacionar, guarda estado propio: `esPresente` y las respuestas.
  Por eso no es una simple cross-ref.
- **PK compuesta `(usuarioId, claseId)`**: un usuario tiene como máximo un
  registro de asistencia por clase.
- **`UsuarioRolCrossRef`** es una tabla intermedia pura para la relación N:M
  entre usuarios y roles.
- **`onDelete = CASCADE`** en asistencias y roles: si se borra un usuario o una
  clase, sus relaciones se limpian.
- **`onDelete = RESTRICT`** en `clases.profesorId`: no se puede borrar un
  profesor que todavía tiene clases.
- **Índices** en claves foráneas y columnas de relación para acelerar consultas.

### 6.5 Modelos de relación

Son `data class` que **no son tablas**. Room las rellena al ejecutar consultas
con `@Transaction`.

```kotlin
data class UsuarioConRoles(
    @Embedded val usuario: UsuarioEntity,
    @Relation(...) val roles: List<RolEntity>
)
```

- `@Embedded`: aplana las columnas de `UsuarioEntity` dentro del resultado.
- `@Relation`: indica cómo cargar la lista relacionada.
- `@Junction`: indica la tabla intermedia en relaciones N:M.

Se usan para evitar consultas manuales y para que la UI reciba un objeto
completo y listo para renderizar.

### 6.6 `CodigoQr`

Los formatos de QR están centralizados en una `sealed class`:

```kotlin
sealed class CodigoQr(val prefijo: String) {
    data object Clase : CodigoQr("CLASE_")
    data object Estudiante : CodigoQr("ESTUDIANTE_")

    fun generar(id: Long): String = "$prefijo$id"
    fun leer(codigo: String): Long?
}
```

Así se evita repetir `"CLASE_"`/`"ESTUDIANTE_"` y se valida el formato en un solo
lugar. Si mañana cambia el prefijo o se agrega un tercer tipo de QR, se toca
este archivo y no cada pantalla.

---

## 7. Modelo de datos: por qué cada tipo

### 7.1 ¿Por qué `data class` para las entidades?

Ejemplo:

```kotlin
@Entity(tableName = "usuarios")
data class UsuarioEntity(
    @PrimaryKey(autoGenerate = true)
    val usuarioId: Long = 0,
    val nombre: String,
    val email: String
)
```

Razones:

- **Room necesita propiedades y un constructor estable.** `data class` encaja
  perfecto.
- **Igualdad estructural:** dos usuarios con los mismos valores son iguales.
  Útil para comparar estados y para tests.
- **`copy()`:** permite crear una versión modificada sin mutar la original.
  Se usa, por ejemplo, al editar una clase:
  ```kotlin
  val claseActualizada = clase.copy(nombre = nombre, ...)
  ```
- **`toString()` legible:** ayuda en logs y debugging.
- **Destructuring:** innecesario hoy, pero disponible sin costo.
- **Inmutabilidad:** `val` evita cambios accidentales; los cambios se hacen
  creando una copia y persistiéndola.

`usuarioId = 0` es un valor por defecto porque Room usa `autoGenerate`: al
insertar, la base asigna el ID real y lo devuelve como `Long`.

### 7.2 ¿Por qué `data class` para los modelos de relación?

`UsuarioConRoles`, `ClaseConDetalles` y `RolConUsuarios` son proyecciones de
lectura. No se insertan ni se actualizan directamente.

Se usan `data class` porque:

- Room las instancia por constructor;
- representan un valor inmutable;
- `copy()`/igualdad ayudan si se transforman;
- son objetos de transporte entre datos y UI.

### 7.3 ¿Por qué `data class` para `ArgumentoRuta` e `ItemBarraNavegacion`?

`ArgumentoRuta` describe un valor inmutable:

```kotlin
data class ArgumentoRuta(
    val nombre: String,
    val tipo: NavType<*>
)
```

Tiene igualdad por contenido, `copy()` y `toString()`, y se puede listar y
transformar con `map`. Es un objeto de configuración, no una entidad con
identidad propia.

`ItemBarraNavegacion` (privado de `MainActivity`) agrupa etiqueta, ícono, estado
de selección y acción. Es un `data class` porque es una estructura de datos
pequeña que se crea y recorre en una lista.

### 7.4 ¿Por qué `sealed class` para `Destino`?

Una `sealed class` representa una **jerarquía cerrada**: todos los subtipos se
conocen en tiempo de compilación y viven en el mismo archivo/módulo.

Ventajas frente a strings o constantes:

```kotlin
// Antes: fácil de romper y difícil de rastrear.
navController.navigate("session_detail/$claseId")

// Ahora: tipado y centralizado.
navController.navegarA(Destino.DetalleSesion, claseId)
```

- El compilador puede verificar exhaustividad en un `when`.
- Cada destino puede tener argumentos distintos y métodos propios.
- No se pueden crear destinos arbitrarios por error.
- Los nombres de rutas y argumentos quedan en un único lugar.

Frente a una `enum class`:

- Una `enum` obliga a que todos los casos compartan la misma forma.
  `Destino.Login` no tiene argumentos, `Destino.CuestionarioEstudiante` tiene
  dos y cada uno necesita funciones distintas.
- Una `sealed class` permite que cada subtipo declare su propia API
  (`crearRuta`, `claseId`, `estudianteId`, etc.).

Frente a una `data class` única con campos opcionales:

```kotlin
// Menos seguro: ¿qué argumentos son válidos para cada ruta?
data class Destino(val ruta: String, val args: List<Any>)
```

La jerarquía sellada evita estados inválidos.

### 7.5 ¿Por qué `data object` para destinos sin parámetros?

`data object Login` expresa:

- hay una sola instancia;
- no tiene estado;
- su `toString()` es legible (`Login`);
- no necesita constructor ni argumentos.

Un `object` normal también sería único, pero `data object` es más idiomático y
expresivo para valores de una jerarquía sellada.

### 7.6 ¿Por qué `sealed class` para `CodigoQr`?

Los formatos de QR son un conjunto cerrado y pequeño. Una `sealed class`:

- centraliza prefijos y lógica;
- permite agregar un nuevo tipo sin tocar las pantallas;
- evita strings mágicos;
- deja la puerta abierta a un `when` exhaustivo si se necesita.

### 7.7 ¿Por qué `interface` para el DAO?

- Room genera la implementación.
- El repositorio depende del contrato, no de la implementación generada.
- Facilita tests con fakes.
- No necesita estado propio.

### 7.8 ¿Por qué `abstract class` para `AppDatabase`?

Room exige heredar de `RoomDatabase`, que es abstracta. La clase:

- declara las entidades y la versión;
- expone el DAO como método abstracto;
- contiene el singleton en un `companion object`.

No puede ser una `interface` porque necesita heredar de `RoomDatabase`.

### 7.9 ¿Por qué `Flow` en lugar de `suspend` para leer?

- **`Flow`**: para datos que cambian con el tiempo (listas, detalle de clase,
  asistencia). Room emite una nueva lista cuando cambia la tabla y Compose se
  recompone automáticamente.
- **`suspend`**: para operaciones puntuales (insertar, actualizar, buscar por ID
  una vez). No tiene sentido observar un `INSERT`.

### 7.10 ¿Por qué `String?` para las respuestas JSON?

`respuestasCuestionarioJson` es nullable porque:

- un estudiante puede estar inscrito pero no haber respondido;
- un estudiante ausente no debe tener respuestas;
- “sin respuesta” es distinto de “respuesta vacía”.

La nulabilidad expresa esa regla en el tipo y en la base de datos.

### 7.11 ¿Por qué `Boolean` si SQLite no tiene boolean?

Room convierte `Boolean` a `INTEGER` (0/1) automáticamente. Usar `Boolean` en
Kotlin mantiene el modelo legible y evita comparar `1`/`0` en la UI.

### 7.12 ¿Por qué IDs `Long`?

- Room usa `Long` para claves autogeneradas.
- Evita el límite de `Int` en tablas que podrían crecer.
- Es el tipo que devuelven `@Insert` para filas autogeneradas.

### 7.13 ¿Por qué una tabla cross-ref para roles?

La relación usuario ↔ rol es N:M: un usuario puede tener varios roles y un rol
puede pertenecer a varios usuarios. En SQL esto se modela con una tabla
intermedia:

```text
usuario_rol_cross_ref(usuarioId, rolId)
```

`@Junction` le dice a Room que use esa tabla al cargar `UsuarioConRoles`.

### 7.14 ¿Por qué JSON en un `String` y no columnas tipadas?

Actualmente el cuestionario y las respuestas se guardan como texto JSON:

```kotlin
val cuestionarioEstructuraJson: String
val respuestasCuestionarioJson: String?
```

Ventajas:

- flexible: cada clase puede definir preguntas distintas sin migraciones;
- simple: no requiere serialización tipada ni tablas adicionales;
- suficiente para el alcance actual.

Desventajas:

- no se puede consultar por pregunta desde SQL;
- no hay validación de esquema;
- errores de formato solo se detectan al parsear;
- la UI muestra el JSON crudo en algunas pantallas.

Una evolución natural sería `kotlinx.serialization` con `@Serializable` y
modelos `Pregunta`/`Respuesta`, o tablas normalizadas si se necesita consultar
por pregunta.

### 7.15 ¿Por qué `vararg Any` en `construirRuta`?

Es una solución genérica para construir la ruta desde una lista de argumentos:

```kotlin
fun construirRuta(vararg valores: Any): String
```

Permite que los helpers tipados (`crearRuta(profesorId: Long)`) deleguen en una
sola implementación. El `require` valida la cantidad de argumentos.

**Tradeoff:** `Any` pierde el tipo en ese método interno. Por eso no se usa
directamente en las pantallas: se usan las funciones tipadas de cada destino,
que sí reciben `Long`.

### 7.16 ¿Por qué `data class` para `ItemBarraNavegacion`?

La barra inferior se construye recorriendo una lista de configuración. Un
`data class` privado:

- evita repetir dos bloques casi idénticos de `NavigationBarItem`;
- agrupa los datos de cada ítem;
- permite agregar futuros ítems sin duplicar UI.

---

## 8. Navegación: recorrido completo

### 8.1 Registro

```kotlin
NavHost(
    navController = navController,
    startDestination = Destino.Login.patron
) {
    composableDestino(Destino.Login) {
        LoginScreen(...)
    }

    composableDestino(Destino.InicioProfesor) { entry ->
        val profesorId = Destino.InicioProfesor.profesorId(entry)
        TeacherHomeScreen(profesorId = profesorId, ...)
    }
}
```

`composableDestino` evita repetir:

```kotlin
composable(
    route = destino.patron,
    arguments = destino.definicionesArgumentos
)
```

### 8.2 Navegación normal

```kotlin
onNavigateToSession = { claseId ->
    navController.navegarA(Destino.Sesion, claseId)
}
```

### 8.3 Navegación entre pestañas

```kotlin
navController.navegarAPestana(Destino.Perfil, usuarioId)
```

Configura `saveState`, `launchSingleTop` y `restoreState` en un solo lugar.

### 8.4 Login y logout

Login reemplaza el destino para que “atrás” no vuelva al formulario:

```kotlin
navController.navigate(Destino.InicioProfesor.crearRuta(profesorId)) {
    popUpTo(Destino.Login.patron) { inclusive = true }
}
```

Logout limpia todo el back stack:

```kotlin
navController.navigate(Destino.Login.patron) {
    popUpTo(0) { inclusive = true }
}
```

### 8.5 Barra inferior

La barra se muestra solo en las pantallas principales:

```kotlin
val enInicio = Destino.InicioProfesor.coincideCon(currentRoute) ||
        Destino.InicioEstudiante.coincideCon(currentRoute)
val enPerfil = Destino.Perfil.coincideCon(currentRoute)
val showBottomBar = enInicio || enPerfil
```

`coincideCon` compara con el patrón actual, que incluye el argumento
(`profile/{usuarioId}`), por eso valida `base` y `base/`.

### 8.6 Cómo agregar una pantalla nueva

1. Crear el composable en `ui/screens/NuevaScreen.kt`.
2. Agregar el destino en `Destino.kt`:
   ```kotlin
   data object Nueva : Destino(
       base = "nueva",
       argumentos = listOf(ArgumentoRuta("id", NavType.LongType))
   ) {
       fun crearRuta(id: Long) = construirRuta(id)
       fun id(entry: NavBackStackEntry) = entry.long()
   }
   ```
3. Registrarlo en `MainActivity`:
   ```kotlin
   composableDestino(Destino.Nueva) { entry ->
       val id = Destino.Nueva.id(entry)
       NuevaScreen(id = id, repository = repository, onNavigateBack = { navController.popBackStack() })
   }
   ```
4. Navegar con `navController.navegarA(Destino.Nueva, id)`.

No se debe escribir `"nueva/$id"` en ningún otro archivo.

---

## 9. Flujos funcionales

### 9.1 Login

1. `LoginScreen` recibe `repository`.
2. El usuario ingresa un correo; si no existe, se crean datos semilla
   (profesor, estudiante, rol, clase y asistencia).
3. Se busca el usuario con roles.
4. Según el rol, se llama a `onLoginProfesor` o `onLoginEstudiante`.
5. `MainActivity` guarda `currentUsuarioId` y navega al home correspondiente.

**Nota:** la creación de datos semilla está dentro de `LoginScreen`. Es código
de demo; en una app real debería moverse a un `Seeder` o al repositorio.

### 9.2 Clase del profesor

1. `TeacherHomeScreen` observa `obtenerClasesPorProfesor`.
2. El FAB abre `DialogCrearClase`.
3. Al crear, se llama `repository.crearClase` en `Dispatchers.IO`.
4. Room inserta y el `Flow` emite la nueva lista; la UI se actualiza sola.
5. Al tocar una tarjeta se navega a `Destino.Sesion`.

### 9.3 QR y asistencia

- Profesor muestra QR de clase:
  - `SessionScreen` usa `CodigoQr.Clase.generar(claseId)`.
- Estudiante escanea QR de clase:
  - `StudentHomeScreen` usa `CodigoQr.Clase.leer(codigo)`.
  - Si es válido, navega a `Destino.CuestionarioEstudiante`.
- Profesor escanea QR de estudiante:
  - `SessionScreen` usa `CodigoQr.Estudiante.leer(codigo)`.
  - Si es válido, `repository.registrarAsistenciaYCuestionario(...)` marca
    presente.
- Estudiante muestra su QR:
  - `ProfileScreen` usa `CodigoQr.Estudiante.generar(usuarioId)`.

### 9.4 Cuestionario

1. `StudentQuestionnaireScreen` observa la clase y la asistencia actual.
2. Si está presente, muestra las preguntas y permite responder.
3. Al guardar, el repositorio inserta/actualiza `AsistenciaClaseEntity`.
4. Solo se guardan respuestas si `esPresente == true`.
5. Vuelve atrás y la lista se actualiza por el `Flow`.

### 9.5 Logout

`ProfileScreen` llama `onLogout`; `MainActivity` limpia el usuario, navega a
`Login` y elimina todo el back stack.

---

## 10. Convenciones del proyecto

- **Paquetes en inglés, UI y textos en español.** Se mantiene así por herencia
  del proyecto; no hay una regla estricta.
- **Sufijos claros:**
  - `Entity` para tablas Room.
  - `Dao` para acceso a datos.
  - `Repository` para la API de datos.
  - `Screen` para pantallas Compose.
  - `Destino` para rutas.
- **Una pantalla por archivo.**
- **Componentes reutilizables sin repositorio.**
- **Estado local con `remember`/`mutableStateOf`; estado compartido en
  `MainActivity`.**
- **Lecturas observables con `Flow`; escrituras con `suspend`.**
- **Rutas y códigos QR nunca como strings sueltos en pantallas.**

---

## 11. Build y generación de código

### Archivos de Gradle

- `settings.gradle.kts`: incluye `:app` y configura repositorios.
- `build.gradle.kts` (raíz): declara plugins sin aplicarlos.
- `app/build.gradle.kts`: configura Android, Compose y dependencias.
- `gradle/libs.versions.toml`: version catalog centralizado.
- `gradle.properties`: configuración de Gradle/Kotlin.

### Room + KSP

Room genera código en build time:

```text
app/build/generated/ksp/debug/kotlin/.../AppDatabase_Impl.kt
app/build/generated/ksp/debug/kotlin/.../UniversidadDao_Impl.kt
```

Ese código **no se edita ni se versiona**; se regenera con Gradle.

### Comandos útiles

```bash
# Compilar Kotlin
./gradlew :app:compileDebugKotlin

# Generar el APK de debug
./gradlew :app:assembleDebug

# Tests JVM
./gradlew :app:testDebugUnitTest

# Tests instrumentados (requiere emulador/dispositivo)
./gradlew :app:connectedDebugAndroidTest
```

El APK de debug se genera en:

```text
app/build/outputs/apk/debug/app-debug.apk
```

---

## 12. Deuda técnica y próximos pasos

Estas son observaciones para futuras iteraciones, no errores funcionales:

1. **ViewModel + UiState** - **COMPLETADO**
   - Implementado en todas las pantallas con `ViewModel`, `StateFlow`, `UiState` y `@HiltViewModel`.

2. **Capa de dominio**
   - Extraer reglas como “inscribir estudiante” o “registrar asistencia” a
     casos de uso si el proyecto crece.

3. **Inyección de dependencias** - **COMPLETADO**
   - Implementada con Hilt (`@HiltAndroidApp`, `@AndroidEntryPoint`, `@HiltViewModel`, `DatabaseModule`).

4. **Datos semilla fuera de LoginScreen**
   - Mover la creación de datos demo de `LoginViewModel` a un `Seeder` o a un entorno de debug.

5. **JSON tipado**
   - Sustituir `String` por modelos `@Serializable` y validar el esquema.
   - Evaluar tablas normalizadas si se necesita consultar respuestas.

6. **Navegación type-safe**
   - La `sealed class Destino` ya centraliza rutas.
   - Se podría migrar a `@Serializable` + `toRoute()` de Navigation 2.8+ para
     eliminar por completo los strings de ruta.

7. **Entidades sin uso**
   - `CursoEntity`, `EstudianteEntity` y `EstudianteCursoCrossRef` existen pero
     no están registradas en `@Database` ni se usan.
   - Eliminarlas o integrarlas según el modelo definitivo.

8. **Strings de UI**
   - Las rutas y códigos QR están centralizados; los textos de UI siguen
     hardcodeados en español. Se podrían mover a `strings.xml` para
     internacionalización.

9. **Persistencia del estado de sesión**
   - `currentUsuarioId` usa `remember`; considerar `rememberSaveable` o un
     ViewModel de sesión.

10. **Consultas por elemento**
    - `SessionDetailScreen` consulta los datos de cada estudiante dentro del
      `LazyColumn`. Para muchos registros convendría una consulta con join o un
      modelo de relación que traiga los nombres.

11. **Manifest duplicado** - **RESOLVIDO**
    - Eliminado el manifiesto redundante y la carpeta anidada duplicada.

---

## 13. Glosario

| Término | Significado en este proyecto |
|---|---|
| **Entity** | Clase `@Entity` que representa una tabla de Room. |
| **DAO** | Interface con métodos de acceso a datos; Room genera la implementación. |
| **Repository** | Fachada que expone datos y reglas de negocio a la UI. |
| **Flow** | Stream observable de datos; Room emite cambios. |
| **suspend** | Función que puede pausarse y ejecutarse en una corrutina. |
| **Composable** | Función de UI declarativa de Jetpack Compose. |
| **State hoisting** | Mover el estado hacia arriba para que varios componentes lo compartan. |
| **Sealed class** | Jerarquía cerrada de subtipos conocidos en compilación. |
| **Data class** | Clase con igualdad estructural, `copy()` y `toString()` generados. |
| **Data object** | Objeto único con `toString()` legible; usado para destinos sin estado. |
| **Cross-ref** | Tabla intermedia para relaciones N:M. |
| **KSP** | Procesador de símbolos que genera código de Room en build time. |
| **Composition root** | Punto donde se construyen las dependencias de la app (`MainActivity`). |

---

## 14. Mapa rápido de archivos clave

| Archivo | Si buscás… |
|---|---|
| `DiscoApplication.kt` | Punto de entrada de la aplicación anotado con `@HiltAndroidApp`. |
| `MainActivity.kt` | Navegación global, sesión y barra inferior (`@AndroidEntryPoint`). |
| `di/DatabaseModule.kt` | Módulo de Hilt que provee `AppDatabase` y `UniversidadDao`. |
| `ui/navigation/Destino.kt` | Rutas, argumentos y helpers de navegación. |
| `ui/viewmodels/` | ViewModels de cada pantalla con `StateFlow` y `UiState`. |
| `ui/screens/` | Comportamiento y UI de cada pantalla conectada a su ViewModel via `hiltViewModel()`. |
| `data/repository/UniversidadRepository.kt` | API de datos y reglas de negocio (`@Inject @Singleton`). |
| `data/local/database/UniversidadDao.kt` | Consultas SQL y operaciones de datos. |
| `data/local/database/AppDatabase.kt` | Configuración de Room. |
| `data/local/entities/` | Tablas y relaciones del modelo. |
| `data/local/model/` | Objetos completos para la UI (`UsuarioConRoles`, etc.). |
| `data/CodigoQr.kt` | Formato de los QR. |
| `ui/components/` | Componentes visuales reutilizables. |
| `ui/theme/` | Tema, colores y tipografía. |
| `app/build.gradle.kts` | Dependencias y configuración de Android/Compose/Hilt. |
| `gradle/libs.versions.toml` | Versiones centralizadas. |
