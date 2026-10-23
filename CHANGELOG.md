# Changelog

Todos los cambios relevantes de este proyecto se documentan en este archivo.

**Protocolo de trabajo:** antes de cada cambio se escribe un plan con
checklist en la sección `### Plan`. Cada ítem se marca `[x]` únicamente después
de implementarlo y verificarlo. Las entradas anteriores no se eliminan.

Formato basado en [Keep a Changelog](https://keepachangelog.com/es-ES/1.1.0/).

---

## [No lanzado] — 2026-10-23

### Plan

- [x] Revisar exhaustivamente la documentación y contrastarla con la app.
- [x] Actualizar `docs/ARQUITECTURA.md` con las capas remotas, flujos de
      autenticación (mermaid), auto-login y tutoriales de extensión.
- [x] Actualizar `docs/REQUERIMIENTOS.md` para reflejar el login contra la
      API, los datos demo reales y el alcance ampliado.
- [x] Actualizar `docs/README.md` con el nuevo índice.
- [x] Alinear el código con la documentación (eliminar `AppConfig` sin uso y la
      dependencia `datastore-rxjava3` huérfana).
- [x] Verificar compilación y ensamblado del APK.

### Agregado

- `docs/ARQUITECTURA.md`: capa remota (`AuthApi`, `TokenDataStore`,
  `TokenInterceptor`, `TokenAuthenticator`, `AuthRepository`), diagramas
  Mermaid (login, refresh ante 401, auto-login, logout), recetas/tutoriales
  (nueva pantalla, llamada a la API, guardar en Room, DataStore) y sección de
  configuración de la URL de la API.
- `docs/REQUERIMIENTOS.md`: RF-01 actualizado al login con contraseña contra
  API, RF-02 con los datos demo reales, CA-01/CA-02 actualizados, alcance
  (3.1/3.2) y evoluciones marcadas según lo implementado.

### Cambiado

- Eliminado `config/AppConfig.kt` (sin uso; la URL proviene de
  `BuildConfig.API_BASE_URL`).
- Eliminada la dependencia `androidx.datastore:datastore-rxjava3` de
  `app/build.gradle.kts`.
- `docs/ARQUITECTURA.md`: estructura de directorios y mapa de archivos
  actualizados para reflejar la capa remota y la ausencia de `config/`.

### Verificación

- `./gradlew :app:compileDebugKotlin` — OK.
- `./gradlew :app:assembleDebug` — OK.

---

## [No lanzado] — 2026-10-23

### Plan

- [x] Corregir la compilación del login contra la API.
- [x] Reescribir `TokenDataStore` con el `DataStore` de coroutines
      (la versión Rx tenía imports incorrectos y llamadas que no compilaban).
- [x] Corregir `TokenInterceptor`: usar lectura bloqueante del token dentro de
      `intercept()`.
- [x] Corregir el import de `LoginResponse` en `AuthRepository`.
- [x] Corregir los imports de `KeyboardOptions`, `KeyboardActions` y
      `PasswordVisualTransformation` en `LoginScreen`.
- [x] Corregir la llamada nula insegura en `LoginViewModel.syncUserWithLocalDb`.
- [x] Hacer que el logout borre los tokens guardados.
- [x] Calificar el `Context` de `NetworkModule` con `@ApplicationContext`.
- [x] Implementar auto-login: restaurar la sesión al iniciar la app si hay
      tokens válidos guardados.
- [x] Implementar renovación de tokens (`refresh`) con
      `okhttp3.Authenticator`.
- [x] Verificar compilación y ensamblado del APK.

### Agregado

- `data/remote/model/RefreshTokenRequest.kt`: cuerpo de la petición
  `POST api/auth/refresh`.
- `AuthApi`: agrega `refresh(RefreshTokenRequest)` sobre `api/auth/refresh`.
- `data/remote/TokenAuthenticator.kt`: `Authenticator` de OkHttp que renueva el
  `accessToken` al recibir un 401 (usando el `refreshToken`), reintenta la
  petición con el nuevo token y borra los tokens si la renovación falla. Usa un
  cliente Retrofit aparte para evitar ciclos y maneja reintentos concurrentes.
- `ui/viewmodels/SesionViewModel.kt`: verifica al iniciar la app si hay una
  sesión válida (token no expirado + email presente en la base local) y expone
  `SesionRestaurada` (`Verificando` / `SinSesion` / `ConSesion`).
- Auto-login en `MainActivity`: mientras se verifica la sesión se muestra una
  pantalla de carga; si hay sesión se arranca directo en el home del rol
  correspondiente y, si no, en el login.
- `TokenInterceptor`: ya no agrega el token a las peticiones de `auth/refresh`.

### Corregido

- `data/remote/TokenDataStore.kt`: implementa el almacenamiento de tokens con
  `DataStore<Preferences>` (coroutines) en lugar de la variante Rx; se conservan
  `saveTokens`, `getAccessToken`, `getRefreshToken`, `getIdToken`, `clearTokens`,
  `getUserInfo` e `isAccessTokenExpired`.
- `data/remote/TokenInterceptor.kt`: remplaza `withContext` (no permitido en
  `intercept()`) por una lectura bloqueante del token.
- `data/repository/AuthRepository.kt`: el import de `LoginResponse` apunta a
  `data.remote.model`.
- `ui/screens/LoginScreen.kt`: imports de `KeyboardOptions`/`KeyboardActions`
  (`androidx.compose.foundation.text`) y de `PasswordVisualTransformation`
  (`androidx.compose.ui.text.input`).
- `ui/viewmodels/LoginViewModel.kt`: `split("@")?.first()` ya no llama
  `first()` sobre un receptor nulo.
- `di/NetworkModule.kt`: `provideOkHttpClient` recibe un
  `@ApplicationContext Context`.

### Cambiado

- `ui/viewmodels/ProfileViewModel.kt`: agrega `logout()`, que limpia los tokens
  del `AuthRepository`.
- `ui/screens/ProfileScreen.kt`: el botón «Cerrar Sesión» llama a
  `viewModel.logout()` antes de navegar al login.

### Verificación

- `./gradlew :app:compileDebugKotlin` — OK.
- `./gradlew :app:assembleDebug` — OK.

### Pendiente

- [ ] Mensajes de error amigables para respuestas HTTP (p. ej., 401 para
      credenciales incorrectas).

---

## [No lanzado] — 2026-10-22

### Plan

- [x] Centralizar las rutas de navegación en una `sealed class`.
- [x] Eliminar los strings de rutas de `MainActivity` y de las pantallas.
- [x] Centralizar los formatos de códigos QR.
- [x] Reemplazar los prefijos `CLASE_` y `ESTUDIANTE_` hardcodeados.
- [x] Extraer el scaffold con barra superior reutilizable.
- [x] Extraer tarjetas reutilizables (`TarjetaSeccion`, `TarjetaClase`).
- [x] Extraer el estado vacío reutilizable.
- [x] Adaptar las pantallas a los componentes reutilizables.
- [x] Documentar la arquitectura en `docs/ARQUITECTURA.md`.
- [x] Documentar los requerimientos en `docs/REQUERIMIENTOS.md`.
- [x] Revisar los requerimientos para que sean una guía adaptable y exigir
      compilación temprana después de agregar las librerías iniciales.
- [x] Agregar un índice de documentación en `docs/README.md`.
- [x] Verificar compilación y ensamblado del APK.

### Agregado

- `app/src/main/java/com/rojas/disco/ui/navigation/Destino.kt`:
  rutas tipadas, argumentos, construcción de rutas y helpers de navegación.
- `app/src/main/java/com/rojas/disco/data/CodigoQr.kt`:
  formatos de QR centralizados con `generar()` y `leer()`.
- `app/src/main/java/com/rojas/disco/ui/components/PantallaBase.kt`:
  `PantallaConBarraSuperior`.
- `app/src/main/java/com/rojas/disco/ui/components/Tarjetas.kt`:
  `TarjetaSeccion` y `TarjetaClase`.
- `app/src/main/java/com/rojas/disco/ui/components/Estados.kt`:
  `EstadoVacio`.
- `docs/ARQUITECTURA.md`: arquitectura, capas, estructura y justificación de
  tipos de Kotlin.
- `docs/REQUERIMIENTOS.md`: especificación funcional, reglas de negocio,
  modelo de datos, criterios de aceptación y protocolo de plan/checklist.
- `docs/README.md`: índice de la documentación.
- `CHANGELOG.md`: este archivo.

### Cambiado

- `MainActivity.kt` ahora usa `Destino`, `composableDestino`, `navegarA` y
  `navegarAPestana`; no contiene strings de rutas.
- La barra inferior se construye a partir de una lista de configuración.
- `ProfileScreen`, `SessionScreen` y `StudentHomeScreen` usan `CodigoQr`.
- `SessionScreen`, `SessionDetailScreen`, `SessionEditScreen` y
  `StudentQuestionnaireScreen` usan `PantallaConBarraSuperior`.
- `TeacherHomeScreen` y `StudentHomeScreen` usan `TarjetaClase` y `EstadoVacio`.
- Las tarjetas de sección usan `TarjetaSeccion`.
- `docs/REQUERIMIENTOS.md` ahora se presenta como guía adaptable, distingue el
  núcleo del caso de las decisiones libres y exige un checkpoint de compilación
  temprana antes de implementar funcionalidades.
- Los criterios de aceptación y el plan por hitos se ajustaron para admitir
  nombres, pantallas, formatos de QR y stacks alternativos.

### Verificación

- `./gradlew :app:compileDebugKotlin` — OK.
- `./gradlew :app:assembleDebug` — OK.
- APK generado en `app/build/outputs/apk/debug/app-debug.apk`.
- Revisión de documentación: no aplica compilación adicional; no se modificó
  código de la app.

### Pendiente

- [ ] Agregar tests unitarios para `Destino` y `CodigoQr`.
- [ ] Migrar el estado de sesión a `rememberSaveable` o a un ViewModel.
- [ ] Mover los datos demo de `LoginScreen` a un `Seeder`.
- [ ] Eliminar el `AndroidManifest.xml` duplicado en la ruta anidada.

---

## [No lanzado] — 2026-10-23

### Plan

- [x] Agregar permisos de notificaciones y servicio en primer plano al
      `AndroidManifest.xml`.
- [x] Agregar `NTFY_BASE_URL` a `BuildConfig`.
- [x] Crear `NtfyTopics` con la misma derivación SHA-256 que el backend.
- [x] Crear `NotificationService` (foreground) que se suscribe al SSE de ntfy
      y muestra notificaciones push.
- [x] Iniciar el servicio al restaurar la sesión y al hacer login.
- [x] Detener el servicio al cerrar sesión.
- [x] Solicitar permiso `POST_NOTIFICATIONS` en Android 13+.
- [x] Verificar compilación y tests.

### Agregado

- `app/src/main/java/com/rojas/disco/notification/NtfyTopics.kt`: derivación del
  tópico secreto (`app_vibe_` + 20 hex de SHA-256 del email).
- `app/src/main/java/com/rojas/disco/notification/NotificationService.kt`:
  servicio foreground con canal de notificaciones, reconexión automática y
  parseo de payloads SSE de ntfy.

### Cambiado

- `AndroidManifest.xml`: permisos `POST_NOTIFICATIONS`,
  `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_DATA_SYNC` y declaración del
  servicio.
- `app/build.gradle.kts`: `BuildConfig.NTFY_BASE_URL`.
- `SesionViewModel`, `LoginViewModel` y `ProfileViewModel`: inicio/detención del
  servicio según la sesión.
- `MainActivity.kt`: solicitud de permiso de notificaciones en Android 13+.

### Verificación

- `./gradlew :app:assembleDebug` — OK.
- `./gradlew :app:testDebugUnitTest` — OK.

---

## [No lanzado] — 2026-10-23

### Plan

- [x] Corregir el parseo de la respuesta de login para desempaquetar `ApiResponse<T>`.
- [x] Actualizar `AuthApi`, `AuthRepository` y `TokenAuthenticator`.
- [x] Verificar compilación.

### Cambiado

- `AuthApi` ahora devuelve `ApiResponse<LoginResponse>` para login y refresh.
- `AuthRepository` y `TokenAuthenticator` desempaquetan `data` antes de usar los tokens.

### Verificación

- `./gradlew :app:assembleDebug` — OK.

---

## [No lanzado] — 2026-10-23

### Plan

- [x] Documentar el flujo de notificaciones ntfy y cómo enviarlas.

### Agregado

- `docs/NOTIFICACIONES_NTFY.md`: derivación del tópico, flujo completo,
  comandos para enviar notificaciones (backend y directo a ntfy) y
  requisitos para que se vean en la app.

### Verificación

- Revisión de documentación; no se modificó código.

---

## [No lanzado] — 2026-10-23

### Plan (migración de paquetes: core/ + feature/, mismo módulo)

- [x] Crear estructura de paquetes `core/*` y `feature/*` (sin dividir Gradle).
- [x] Mover `notification/*` → `feature/notificaciones/` y actualizar referencias.
- [x] Mover `ui/theme/*` y `ui/components/*` → `core/designsystem/`.
- [x] Mover `data/remote/*` → `core/network/remote/` y `core/datastore/`.
- [x] Mover `data/local/*` → `core/database/local/`.
- [x] Mover pantallas y ViewModels a su `feature/*` correspondiente.
- [x] Mover repositorios a `feature/*/data/` y `core/database/repository/`.
- [x] Actualizar todos los imports y declaraciones de package.
- [ ] Verificar compilación y recorrido manual.

### Verificación
- `./gradlew :app:compileDebugKotlin` — OK
- `./gradlew :app:assembleDebug` — OK
- `./gradlew :app:testDebugUnitTest` — OK
