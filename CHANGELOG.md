# Changelog

Todos los cambios relevantes de este proyecto se documentan en este archivo.

**Protocolo de trabajo:** antes de cada cambio se escribe un plan con
checklist en la sección `### Plan`. Cada ítem se marca `[x]` únicamente después
de implementarlo y verificarlo. Las entradas anteriores no se eliminan.

Formato basado en [Keep a Changelog](https://keepachangelog.com/es-ES/1.1.0/).

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
