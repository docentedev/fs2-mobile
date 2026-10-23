# Requerimientos iniciales — Disco

Documento de referencia funcional y técnica del caso **Disco**, pensado para:

- entender el problema y sus flujos principales;
- servir como base para que cada estudiante construya **su propia solución**;
- priorizar un MVP;
- validar el resultado con criterios de aceptación claros.

> **Este documento no es un contrato de implementación exacta.**
>
> Describe un caso de estudio y una solución de referencia. Cada estudiante
> puede —y se espera que lo haga— adaptar:
>
> - el nombre de la app y el paquete;
> - las versiones de las librerías;
> - la estructura exacta de paquetes;
> - el diseño visual y los componentes;
> - la organización interna de las capas;
> - el modelo de datos, siempre que conserve las relaciones esenciales;
> - la cantidad y el nombre de las pantallas.
>
> Lo que debe mantenerse es el **núcleo del caso**: usuarios con roles,
> clases, inscripción, asistencia, cuestionario y registro mediante QR.
>
> **Documento complementario:** `docs/ARQUITECTURA.md` explica cómo está
> implementada la solución de referencia. Este documento explica **qué
> problema resolver** y qué comportamientos se esperan.

> **Regla de compilación temprana:** después de crear el proyecto y agregar las
> librerías iniciales, ejecutá `./gradlew :app:assembleDebug` antes de
> implementar cualquier funcionalidad. No avances hasta confirmar que todo
> compila y que las dependencias son compatibles.

---

## 1. Cómo usar este documento como prompt

Si querés pedirle a un modelo que genere una aplicación similar, usá este
documento como especificación de entrada. El modelo debe **adaptar** la
solución, no copiarla literalmente.

Un prompt base puede ser:

```text
Genera una aplicación Android para gestionar clases universitarias,
asistencia y cuestionarios, basada en el caso "Disco" descrito en este
documento.

Usá los requerimientos funcionales RF-01 a RF-11, las reglas de negocio
RN-01 a RN-12 y el modelo de datos de la sección 7 como guía. Podés adaptar
libremente la arquitectura, las pantallas, los nombres, el diseño y las
versiones de librerías, siempre que se conserve el núcleo del caso:
roles, clases, inscripción, asistencia, cuestionario y QR.

Base técnica sugerida (adaptable):
- Kotlin + Jetpack Compose + Material 3.
- Room para persistencia local (sin backend).
- Navigation Compose.
- Arquitectura por capas: UI -> Repository -> DAO -> Room.
- Rutas centralizadas y tipadas (no strings de rutas en pantallas).
- Componentes de UI reutilizables.
- Cámara + ML Kit para escanear QR y ZXing para generarlos.

Antes de implementar funcionalidades:
1. Crea `CHANGELOG.md` y escribí el plan completo por hitos con checkboxes,
   siguiendo el formato de la sección 19. El plan debe incluir el checkpoint de
   compilación temprana.
2. Crea el proyecto y agrega únicamente las librerías iniciales.
3. Compila inmediatamente con `./gradlew :app:assembleDebug`.
4. No avances hasta confirmar que el proyecto compila y que las librerías son
   compatibles entre sí.
5. Implementa los hitos en orden, actualizando el checklist después de cada
   cambio.

Reglas de trabajo obligatorias:
- El primer checkpoint siempre es una compilación temprana.
- Si agregás una librería nueva, compilá antes de seguir.
- Todo cambio debe tener primero un plan con checklist en `CHANGELOG.md`.
- Un ítem debe describir un cambio pequeño y verificable.
- Marcar `[x]` solo después de compilar/probar.
- Si algo queda pendiente o falla, dejarlo `[ ]` y explicar el motivo.
- Agregar una sección `### Verificación` con los comandos ejecutados.
- No borrar entradas anteriores del CHANGELOG.

Entregables:
1. Proyecto Android compilable desde las primeras etapas.
2. Pantallas y navegación funcionales.
3. Persistencia local con las entidades y relaciones principales.
4. Datos demo iniciales.
5. `CHANGELOG.md` con el plan y los cambios marcados.
6. Comportamientos equivalentes a los criterios de aceptación de la sección 11.

No agregues backend, autenticación real ni funciones fuera del alcance de la
sección 3.2.
```

> También se puede adjuntar el archivo completo y pedir: “Implementa una
> solución para este caso, adaptándola a tu criterio”.

---

### 1.1 Protocolo de trabajo obligatorio: compilación temprana + plan + CHANGELOG

#### Checkpoint 0 — compilación temprana (obligatorio)

Con el plan ya registrado en `CHANGELOG.md`, el primer checkpoint técnico es
conseguir un esqueleto compilable. Antes de implementar cualquier pantalla o
lógica de negocio:

1. Crear el proyecto Android base.
2. Agregar **solo las librerías iniciales** que se vayan a usar (por ejemplo:
   Compose, Material 3, Navigation, Room/KSP, cámara y escaneo de QR, o sus
   equivalentes).
3. Ejecutar inmediatamente:
   ```bash
   ./gradlew :app:assembleDebug
   ```
4. Corregir incompatibilidades de versiones, plugins o dependencias.
5. No avanzar hasta que la compilación sea exitosa.
6. Registrar el resultado en `CHANGELOG.md`.

Si se agrega una nueva librería después, se repite la compilación antes de
seguir con la funcionalidad. El objetivo es detectar temprano problemas de
compatibilidad, no descubrirlos al final.

> **Regla:** un proyecto que no compila no está listo para implementar
> features. La compilación temprana es la primera puerta de calidad.

#### Plan y CHANGELOG

Para que el trabajo sea trazable, todo cambio debe seguir este flujo:

1. **Antes de tocar código**, abrir o crear `CHANGELOG.md` en la raíz.
2. Agregar una sección con el formato:
   ```markdown
   ## [No lanzado] — AAAA-MM-DD

   ### Plan
   - [ ] Paso pequeño y verificable.
   - [ ] Otro paso.
   ```
3. Escribir el plan **antes** de implementar, no después.
4. Implementar un paso a la vez.
5. Marcar `[x]` **solo** cuando el paso esté implementado y verificado.
6. Si un paso falla o queda bloqueado, dejarlo `[ ]` y explicar el motivo.
7. Agregar al final:
   ```markdown
   ### Verificación
   - `./gradlew :app:assembleDebug` — OK
   - `./gradlew :app:testDebugUnitTest` — OK
   ```
8. No borrar ni reescribir entradas anteriores del CHANGELOG.

**Regla de oro:** si un cambio no está en el plan del CHANGELOG, no se
implementa. Si surge un cambio nuevo, primero se agrega al checklist y después
se ejecuta.

Este protocolo aplica tanto al proyecto completo como a cada iteración
posterior.

---

## 2. Objetivo del producto

Disco es una aplicación Android **local-first** para que profesores y
estudiantes gestionen:

- clases;
- inscripciones;
- asistencia;
- cuestionarios simples;
- identificación mediante códigos QR.

La aplicación debe funcionar sin conexión a internet y guardar todo en el
dispositivo.

### Problema que resuelve

En una clase universitaria se necesita:

1. saber quién asistió;
2. que el estudiante confirme asistencia y responda un cuestionario;
3. que el profesor no tenga que cargar listas manualmente;
4. que el registro sea rápido mediante QR.

### Propuesta de valor

- El profesor crea una clase y muestra un QR.
- El estudiante escanea el QR para acceder a la clase y responder.
- El profesor escanea el QR del estudiante para marcarlo presente.
- Todo queda registrado localmente y se refleja en la UI en tiempo real.

---

## 3. Alcance

### 3.1 Incluido en el MVP

- Login por correo electrónico y contraseña contra una API remota (tokens JWT).
- Auto-login al abrir la app si los tokens son válidos.
- Roles de Profesor y Estudiante (decididos por los *groups* del JWT).
- Home diferenciado por rol.
- Home diferenciado por rol.
- CRUD parcial de clases (crear, listar, ver, editar).
- Inscripción de estudiantes a clases.
- Registro de asistencia.
- Cuestionario básico.
- Generación y escaneo de QR.
- Perfil de usuario con QR personal.
- Logout.
- Persistencia local de los datos.
- Datos de demostración iniciales.

### 3.2 Fuera de alcance del MVP

- Backend, API remota o sincronización. **(Nota: la solución de referencia
  actual sí implementa autenticación contra una API remota; el resto del
  contenido de negocio sigue siendo local-first. Ver sección 17.)**
- Autenticación real con contraseña, token o biometría. **(Implementada en la
  solución de referencia actual: login con contraseña + tokens JWT + refresh
  automático ante 401.)**
- Recuperación de contraseña.
- Notificaciones push.
- Múltiples instituciones o multi-tenant.
- Panel web.
- Reportes exportables (PDF/Excel).
- Firma digital.
- Geolocalización.
- Carga masiva de estudiantes.
- Cuestionarios dinámicos completamente configurables desde la UI.
- Internacionalización completa (la referencia está en español).
- Modo multiusuario simultáneo en el mismo dispositivo.

### 3.3 Núcleo del caso vs. decisiones adaptables

Este documento describe **un caso de estudio**, no una implementación única.
Cada estudiante puede adaptar la solución a su criterio, siempre que el núcleo
del caso se mantenga.

| Aspecto | Núcleo que debe conservarse | Decisiones adaptables |
|---|---|---|
| **Problema** | Gestión de clases, inscripción, asistencia, cuestionario y registro por QR. | Cómo se presentan y organizan los flujos. |
| **Actores** | Un rol docente y un rol estudiante. | Nombres, roles adicionales, forma de autenticación demo. |
| **Flujo principal** | El docente crea una clase; el estudiante se vincula; se registra asistencia; se responde un cuestionario. | Cantidad de pasos, pantallas intermedias y navegación. |
| **Datos** | Usuarios, roles, clases, inscripción/asistencia y respuestas. | Nombres de tablas, normalización, tipos de IDs y tecnología de persistencia. |
| **QR** | Generar y escanear códigos para identificar clases y estudiantes. | Formato, prefijos, librería y diseño del escáner. |
| **Pantallas** | Debe existir una forma de login, vista de clases, asistencia, cuestionario y perfil. | Cantidad, nombres, diseño, componentes y combinación de pantallas. |
| **Stack** | Aplicación Android funcional y local-first. | Kotlin/Java, Compose/XML, Room/SQLite u otra persistencia local, versiones de librerías. |
| **Datos demo** | Facilitar la prueba del flujo completo. | Nombres, correos, clase y respuestas de ejemplo. |
| **Criterios de aceptación** | El comportamiento descrito debe poder demostrarse. | Textos, IDs, nombres de botones y detalles visuales. |
| **Documentación** | Plan con checklist y registro en `CHANGELOG.md`. | Formato del changelog y nivel de detalle. |

**Lo que no se negocia:** que el caso se pueda usar de punta a punta. El resto
es margen de diseño e implementación.

---

## 4. Actores y roles

| Actor | Descripción | Permisos principales |
|---|---|---|
| **Profesor** | Usuario con rol `Profesor`. | Crear/editar sus clases, mostrar QR de clase, escanear QR de estudiantes, ver asistencias y respuestas. |
| **Estudiante** | Usuario con rol `Estudiante`. | Ver sus clases inscriptas, escanear QR de clase, responder cuestionario, mostrar su QR personal. |
| **Sistema** | Lógica local de la app. | Validar reglas, persistir datos, generar/leer QR, navegar. |

Notas:

- Un usuario puede tener más de un rol (relación N:M).
- Si un usuario tiene ambos roles, la referencia prioriza **Profesor**; cada
  solución puede definir otra regla si la documenta.
- No existe un rol “Administrador” implementado, aunque el modelo de datos
  podría soportarlo agregando filas a `roles`.

---

## 5. Requerimientos funcionales

Convención de prioridad:

- **M** = obligatorio para el MVP.
- **S** = deseable.
- **C** = opcional / futura evolución.

### RF-01 — Login por correo y contraseña (contra API)

**Prioridad: M**

- La pantalla inicial debe mostrar un campo de correo electrónico y un campo
  de contraseña.
- Debe existir un botón “Ingresar”.
- Al ingresar, se valida contra una API remota (`POST api/auth/login`).
- Si la autenticación es exitosa, se guardan los tokens (`accessToken`,
  `refreshToken`, `idToken`) en el dispositivo y se decodifica el `idToken`
  (JWT) para obtener el usuario y sus roles.
- El usuario se sincroniza con la base local: si no existe, se crea con los
  roles correspondientes; si la base está vacía, se siembran datos demo.
- Si el usuario tiene rol Profesor (o Admin), se navega al home de profesor.
- Si tiene rol Estudiante, se navega al home de estudiante.
- Si tiene ambos, se prioriza Profesor.
- Si no tiene ninguno, se muestra un error.
- Si el email/contraseña son inválidos, la API responde error y la app muestra
  un mensaje (sin navegar).
- Debe existir feedback de carga mientras se autentica.
- La sesión se restaura automáticamente al abrir la app si los tokens son
  válidos (auto-login).

**Validaciones mínimas:**

- Correo y contraseña no deben estar vacíos.
- Se debe mostrar un mensaje claro si las credenciales son inválidas o no hay
  red.

**Nota de alcance:** esta funcionalidad va más allá del MVP original, que no
pedía backend ni autenticación real. Ver sección 17.

### RF-02 — Datos demo iniciales

**Prioridad: M**

Cuando no existe información inicial, la app debe poder sembrar:

- Rol `Profesor`.
- Rol `Estudiante`.
- Usuario `Docente Demo` (`docente.dev@gmail.com`) con rol Profesor.
- Clase `Primer Curso`, del profesor Docente Demo.
- Usuario `Estudiante Demo` (`demo@estudiante.edu`) con rol Estudiante.
- Inscripción del estudiante demo en esa clase.
- Asistencia inicial del estudiante demo como presente, con respuestas de ejemplo.

**Regla de implementación:** el sembrado no debe ejecutarse en cada login. Solo
cuando falten los datos base (base de datos vacía). Para una app real, mover
esta lógica a un `Seeder` o a un entorno de debug.

### RF-03 — Home del profesor

**Prioridad: M**

- Mostrar el nombre o mensaje de bienvenida del profesor.
- Listar únicamente las clases donde `profesorId` coincide con el usuario.
- Cada clase se muestra como una tarjeta clicable con:
  - nombre;
  - descripción.
- Al tocar una clase, navegar al resumen de la clase.
- Mostrar estado vacío si no tiene clases.
- Mostrar un botón de acción flotante (FAB) para crear clase.
- El estado vacío debe incluir un botón “Crear Primera Clase”.
- La lista debe actualizarse automáticamente al crear una clase.

### RF-04 — Crear clase

**Prioridad: M**

- Se abre desde el FAB o desde el estado vacío.
- Debe ser un diálogo o pantalla con:
  - nombre (obligatorio);
  - descripción;
  - estructura del cuestionario en JSON.
- El cuestionario debe tener un valor por defecto:
  ```json
  {"preguntas":["¿Asististe a la sesión?","¿Tienes alguna duda?"]}
  ```
- El botón “Crear” debe estar deshabilitado si el nombre está vacío.
- Al crear:
  - se guarda la clase con el profesor actual;
  - se cierra el diálogo;
  - la lista se actualiza.
- No se permite crear una clase sin profesor asociado.

### RF-05 — Home del estudiante

**Prioridad: M**

- Mostrar mensaje de bienvenida con el nombre del estudiante.
- Listar únicamente las clases en las que está inscripto.
- Cada tarjeta debe mostrar:
  - nombre;
  - descripción;
  - estado de asistencia: `PRESENTE` o `PENDIENTE`;
  - estado del cuestionario: respondido o pendiente.
- Las tarjetas deben diferenciarse visualmente según la asistencia.
- Al tocar una tarjeta, navegar al cuestionario de esa clase.
- Mostrar botón “Escanear QR de Clase del Profesor”.
- Mostrar estado vacío si no está inscripto en ninguna clase.
- La lista debe actualizarse automáticamente cuando cambie la asistencia.

### RF-06 — Resumen de clase (profesor)

**Prioridad: M**

- Mostrar:
  - nombre de la clase;
  - descripción;
  - profesor a cargo;
  - estructura del cuestionario (JSON);
  - cantidad de estudiantes inscriptos.
- Permitir mostrar el QR de la clase.
- Permitir escanear el QR de un estudiante.
- Al escanear un estudiante válido:
  - registrarlo como presente en la clase;
  - guardar la asistencia;
  - mostrar confirmación;
  - actualizar la UI.
- Si el QR es inválido, mostrar error sin cerrar la app.
- Permitir navegar al detalle de asistencias.
- Permitir navegar a editar la clase.

### RF-07 — Detalle de clase

**Prioridad: M**

- Mostrar nombre de la clase y subtítulo de respuestas/asistencias.
- Listar los registros de asistencia de la clase.
- Por cada registro mostrar:
  - nombre del estudiante;
  - email;
  - estado `PRESENTE` o `AUSENTE`;
  - respuestas del cuestionario si existen.
- Si no hay registros, mostrar un mensaje vacío.
- La lista debe actualizarse en tiempo real.

### RF-08 — Editar clase

**Prioridad: M**

- Cargar los datos actuales de la clase.
- Permitir editar:
  - nombre;
  - descripción;
  - estructura del cuestionario (JSON).
- Guardar cambios y volver atrás.
- Mantener el profesor original.
- Validar que el nombre no quede vacío (deseable; el MVP actual no lo valida).

### RF-09 — Cuestionario del estudiante

**Prioridad: M**

- Mostrar información de la clase y del profesor.
- Mostrar un checkbox para marcar asistencia como `PRESENTE`.
- Si está presente:
  - mostrar las preguntas del cuestionario;
  - permitir responder.
- Si no está presente:
  - mostrar un mensaje indicando que debe marcarse presente para responder;
  - no permitir guardar respuestas.
- Al guardar:
  - registrar/actualizar asistencia;
  - guardar respuestas solo si está presente;
  - volver a la pantalla anterior;
  - reflejar los cambios en la lista del home.

**Nota sobre el MVP:** la UI actual muestra dos preguntas fijas y también
visualiza el JSON de estructura. Una evolución deseable es renderizar
dinámicamente las preguntas del JSON.

### RF-10 — Perfil y QR personal

**Prioridad: M**

- Mostrar:
  - nombre del usuario;
  - email;
  - roles asignados.
- Permitir mostrar el QR personal del estudiante.
- El contenido del QR debe identificar al estudiante. La referencia usa
  `ESTUDIANTE_{usuarioId}`.
- Permitir cerrar sesión.
- Al cerrar sesión:
  - limpiar el usuario actual;
  - volver al login;
  - limpiar el back stack.

### RF-11 — Navegación entre secciones

**Prioridad: M**

La navegación puede resolverse con barra inferior, menú, pestañas, botones u
otro mecanismo. La solución de referencia usa una barra inferior.

- Debe existir una forma clara de acceder al home de clases y al perfil.
- La barra inferior, si se usa, puede mostrarse solo en las pantallas
  principales.
- Las pantallas secundarias deben permitir volver.
- El botón atrás del sistema debe funcionar.
- El login no debe quedar en el back stack después de ingresar.
- Logout debe limpiar el back stack.
- La navegación entre secciones debe evitar duplicar destinos innecesariamente.

---

## 6. Reglas de negocio

Las clases y consultas mencionadas en la columna “Dónde se aplica” pertenecen a
la solución de referencia; cada implementación puede usar otros nombres.

| ID | Regla | Dónde se aplica |
|---|---|---|
| **RN-01** | Un usuario puede tener varios roles y un rol varios usuarios. | Modelo N:M `usuario_rol_cross_ref`. |
| **RN-02** | Un profesor no puede inscribirse como estudiante de su propia clase. | `UniversidadRepository.inscribirEstudianteEnClase`. |
| **RN-03** | Un estudiante solo puede tener un registro de asistencia por clase. | PK compuesta `(usuarioId, claseId)`. |
| **RN-04** | La inscripción crea una asistencia inicial con `esPresente = false`. | Repositorio. |
| **RN-05** | Solo se guardan respuestas si `esPresente = true`. | Repositorio y pantalla de cuestionario. |
| **RN-06** | El profesor solo ve sus propias clases. | Consulta `getClasesByProfesor`. |
| **RN-07** | El estudiante solo ve clases donde tiene asistencia/inscripción. | Consulta con join a `asistencia_clase`. |
| **RN-08** | Al borrar un usuario o clase, sus asistencias y roles se eliminan en cascada. | FKs `CASCADE`. |
| **RN-09** | No se puede borrar un profesor que tenga clases. | FK `RESTRICT` en `clases.profesorId`. |
| **RN-10** | Si un usuario tiene ambos roles, la referencia prioriza Profesor. | Lógica de login. |
| **RN-11** | El QR de clase debe identificar una clase válida. La referencia usa `CLASE_{claseId}`. | Lector de QR. |
| **RN-12** | El QR de estudiante debe identificar un estudiante válido. La referencia usa `ESTUDIANTE_{usuarioId}`. | Lector de QR. |

---

## 7. Modelo de datos

El siguiente es un **modelo de referencia**. Se pueden cambiar nombres de
tablas y campos, normalizar relaciones, usar otros tipos de ID o agregar
tablas auxiliares, siempre que se conserven las relaciones esenciales:
usuarios con roles, clases con un docente, inscripción/asistencia de
estudiantes y respuestas del cuestionario.

### 7.1 Entidades persistentes

#### `usuarios`

| Campo | Tipo | Restricción |
|---|---|---|
| `usuarioId` | Long | PK, autogenerado. |
| `nombre` | String | Obligatorio. |
| `email` | String | Obligatorio. Se recomienda único (el MVP no lo impone). |

#### `roles`

| Campo | Tipo | Restricción |
|---|---|---|
| `rolId` | Long | PK, autogenerado. |
| `nombreRol` | String | Obligatorio. Ejemplos: `Profesor`, `Estudiante`. |

#### `usuario_rol_cross_ref`

| Campo | Tipo | Restricción |
|---|---|---|
| `usuarioId` | Long | PK compuesta, FK a `usuarios`, CASCADE. |
| `rolId` | Long | PK compuesta, FK a `roles`, CASCADE. |

#### `clases`

| Campo | Tipo | Restricción |
|---|---|---|
| `claseId` | Long | PK, autogenerado. |
| `nombre` | String | Obligatorio. |
| `descripcion` | String | Obligatorio. |
| `profesorId` | Long | FK a `usuarios`, RESTRICT, indexado. |
| `cuestionarioEstructuraJson` | String | Obligatorio en el MVP. |

#### `asistencia_clase`

| Campo | Tipo | Restricción |
|---|---|---|
| `usuarioId` | Long | PK compuesta, FK a `usuarios`, CASCADE, indexado. |
| `claseId` | Long | PK compuesta, FK a `clases`, CASCADE, indexado. |
| `esPresente` | Boolean | Default `false`. |
| `respuestasCuestionarioJson` | String? | Nullable. Solo si `esPresente = true`. |

### 7.2 Relaciones

```text
usuarios 1 ───── N clases                 (profesorId)
usuarios N ───── N roles                  (usuario_rol_cross_ref)
usuarios N ───── N clases                 (asistencia_clase)
```

### 7.3 Modelos de consulta

La app puede exponer objetos compuestos para la UI:

- `UsuarioConRoles(usuario, roles)`.
- `ClaseConDetalles(clase, profesor, asistencias)`.
- `RolConUsuarios(rol, usuarios)`.

No son tablas: son proyecciones de lectura.

### 7.4 Entidades opcionales / no usadas en el MVP

Existen en el código pero no están registradas en la base ni se usan:

- `CursoEntity`.
- `EstudianteEntity`.
- `EstudianteCursoCrossRef`.

Para una nueva implementación, **no es necesario incluirlas** salvo que se
decida adoptar un modelo académico más amplio (cursos separados de clases).

---

## 8. Navegación

Los nombres de destinos y rutas son de referencia. Se pueden cambiar,
combinar pantallas o usar otro esquema de navegación, siempre que los flujos
principales sean accesibles.

### 8.1 Mapa de pantallas

```text
login
  ├── teacher_home/{profesorId}
  │     └── session/{claseId}
  │           ├── session_detail/{claseId}
  │           └── session_edit/{claseId}
  │
  ├── student_home/{estudianteId}
  │     └── student_questionnaire/{claseId}/{estudianteId}
  │
  └── profile/{usuarioId}   (accesible desde la barra inferior)
```

### 8.2 Pantallas

| Pantalla | Destino | Rol | Descripción |
|---|---|---|---|
| Login | `login` | Público | Ingreso por email. |
| Home profesor | `teacher_home/{profesorId}` | Profesor | Lista de clases propias. |
| Home estudiante | `student_home/{estudianteId}` | Estudiante | Lista de clases inscriptas. |
| Resumen de clase | `session/{claseId}` | Profesor | QR, estructura, accesos. |
| Detalle de clase | `session_detail/{claseId}` | Profesor | Asistencias y respuestas. |
| Editar clase | `session_edit/{claseId}` | Profesor | Edición de datos. |
| Cuestionario | `student_questionnaire/{claseId}/{estudianteId}` | Estudiante | Asistencia y respuestas. |
| Perfil | `profile/{usuarioId}` | Ambos | Datos, roles, QR y logout. |

### 8.3 Buenas prácticas de navegación (adaptables)

La solución de referencia centraliza las rutas y las tipa. No es obligatorio
usar exactamente ese esquema, pero se recomienda:

- centralizar las rutas en un solo lugar;
- evitar strings de rutas dispersos en las pantallas;
- declarar argumentos y tipos de forma clara;
- detectar la pantalla actual sin comparar strings sueltos;
- cuidar el back stack al navegar entre pestañas y al cerrar sesión.

---

## 9. Códigos QR

Los formatos de esta sección son una **convención de referencia**. Se pueden
cambiar los prefijos o la estructura, siempre que se distinga claramente entre
un QR de clase y uno de estudiante y que ambos se puedan generar y leer.

### 9.1 Formatos

| Tipo | Generación | Ejemplo | Validación |
|---|---|---|---|
| QR de clase | `CLASE_{claseId}` | `CLASE_1` | Prefijo `CLASE_` + número. |
| QR de estudiante | `ESTUDIANTE_{usuarioId}` | `ESTUDIANTE_2` | Prefijo `ESTUDIANTE_` + número. |

### 9.2 Generación

- El QR debe renderizarse como imagen.
- El profesor puede mostrar el QR de la clase.
- El estudiante puede mostrar su QR personal.
- El contenido del QR debe mostrarse también como texto para debugging.

### 9.3 Escaneo

- Debe solicitarse permiso de cámara.
- Debe mostrarse vista previa de la cámara.
- Debe detectarse el primer QR válido.
- Debe existir un campo de texto editable como alternativa manual.
- El botón “Procesar QR” debe estar deshabilitado si el contenido está vacío.
- Un QR inválido debe mostrar un mensaje de error, no cerrar la app.
- Al escanear un QR de estudiante válido, el profesor registra asistencia.
- Al escanear un QR de clase válido, el estudiante navega al cuestionario.

---

## 10. Requerimientos no funcionales

### 10.1 Técnicos

La solución de referencia usa el siguiente stack. Se puede reemplazar por
otro equivalente dentro del ecosistema Android, siempre que se mantenga una app
funcional, local-first y compilable.

| ID | Requerimiento | Prioridad |
|---|---|---|
| **RNF-01** | App Android nativa (la referencia usa Kotlin). | S |
| **RNF-02** | UI declarativa (la referencia usa Jetpack Compose + Material 3). | S |
| **RNF-03** | Persistencia local (la referencia usa Room + SQLite). | S |
| **RNF-04** | Navegación entre pantallas (la referencia usa Navigation Compose). | S |
| **RNF-05** | Separación por capas (la referencia usa UI → Repository → DAO → Room). | S |
| **RNF-06** | Operaciones de base de datos fuera del hilo principal. | M |
| **RNF-07** | Datos observables para actualizar la UI (la referencia usa `Flow`). | S |
| **RNF-08** | Rutas y argumentos organizados; se recomienda centralizarlos. | S |
| **RNF-09** | Componentes de UI reutilizables. | S |
| **RNF-10** | Código compilable con `./gradlew :app:assembleDebug`. | M |
| **RNF-11** | La app funciona sin conexión y guarda los datos localmente. | M |

### 10.2 Plataforma

Valores de referencia; se pueden adaptar según el entorno del curso:

- `minSdk = 27`.
- `targetSdk = 35`.
- `compileSdk = 35`.
- Orientación libre (portrait/landscape), sin bloqueo.
- Permiso de cámara declarado.
- La app debe funcionar sin conexión.
- Los datos no deben salir del dispositivo.

### 10.3 UX / UI

La solución de referencia usa Material 3 y español. El estilo visual, los
colores y la tipografía son libres; se recomienda:

- interfaz legible y consistente;
- tema claro/oscuro automático o una alternativa justificada;
- colores y tipografía desde el tema, no hardcodeados en pantallas;
- mensajes de carga en pantallas que esperan datos;
- estados vacíos claros;
- feedback ante errores (Toast, snackbar o texto visible);
- botones con contenido descriptivo y accesibles;
- iconos con `contentDescription` cuando aportan información;
- listas largas con `LazyColumn` o equivalente eficiente;
- que la navegación no tape contenido.

### 10.4 Calidad

- No debe haber crashes por QR inválido, email inexistente o lista vacía.
- La app debe mantener la consistencia ante recreación de actividad
  (deseable; la referencia ya restaura la sesión desde los tokens).
- Las reglas de negocio críticas deben poder testearse.
- El código debe separar claramente pantallas, componentes, navegación y datos.

---

## 11. Criterios de aceptación

Los siguientes escenarios describen **comportamientos esperados**. Los textos,
nombres, IDs y detalles visuales pueden adaptarse a cada solución; lo importante
es que el flujo se pueda demostrar de punta a punta.

### CA-01 — Login de profesor

```gherkin
Dado que existe el usuario docente.dev@gmail.com con rol Profesor
Cuando ingreso el correo y la contraseña y presiono "Ingresar"
Entonces veo el home de profesor
Y veo la clase "Primer Curso"
Y puedo acceder a mis clases y a mi perfil
```

### CA-02 — Login de estudiante

```gherkin
Dado que existe el usuario demo@estudiante.edu con rol Estudiante
Cuando ingreso el correo y la contraseña y presiono "Ingresar"
Entonces veo el home de estudiante
Y veo la clase "Primer Curso"
Y la tarjeta indica PRESENTE
```

### CA-03 — Usuario inexistente o sin rol

```gherkin
Cuando ingreso un correo sin usuario asociado
Entonces veo un mensaje de error
Y no navego a ningún home
```

### CA-04 — Crear clase

```gherkin
Dado que estoy en el home del profesor
Cuando presiono el FAB y completo nombre y descripción
Y presiono "Crear"
Entonces la clase aparece en la lista
Y la lista se actualiza sin recargar manualmente
```

### CA-05 — Validación de nombre

```gherkin
Dado que estoy en el diálogo de crear clase
Cuando el nombre está vacío
Entonces el botón "Crear" está deshabilitado
```

### CA-06 — Mostrar QR de clase

```gherkin
Dado que estoy en el resumen de una clase
Cuando presiono la opción para mostrar su QR
Entonces veo un QR válido que identifica la clase
Y el contenido puede variar según el formato elegido (referencia: "CLASE_1")
```

### CA-07 — Escanear estudiante

```gherkin
Dado que estoy en el resumen de la clase
Cuando escaneo el QR válido que identifica a un estudiante
Entonces el estudiante queda marcado como PRESENTE
Y veo una confirmación
Y la asistencia se actualiza
```

### CA-08 — QR inválido

```gherkin
Cuando escaneo un código que no empieza con el prefijo esperado
Entonces veo un mensaje de error
Y la app sigue funcionando
```

### CA-09 — Cuestionario

```gherkin
Dado que soy estudiante y estoy en el cuestionario de una clase
Cuando marco que estoy presente y respondo las preguntas
Y presiono "Guardar Encuesta y Asistencia"
Entonces se guarda la asistencia
Y se guardan las respuestas
Y vuelvo a la pantalla anterior
```

### CA-10 — Regla de asistencia

```gherkin
Dado que soy estudiante y no estoy marcado como presente
Cuando intento responder el cuestionario
Entonces el sistema no guarda respuestas
Y me muestra un mensaje indicando que debo marcarme presente
```

### CA-11 — Perfil y QR

```gherkin
Dado que estoy en mi perfil
Entonces veo mis datos y mis roles
Cuando presiono la opción para mostrar mi QR
Entonces veo un QR válido que me identifica como estudiante
Y el contenido puede variar según el formato elegido (referencia: "ESTUDIANTE_{miId}")
```

### CA-12 — Logout

```gherkin
Dado que estoy en cualquier pantalla autenticada
Cuando presiono "Cerrar Sesión"
Entonces vuelvo al login
Y no puedo volver atrás a la pantalla anterior
```

### CA-13 — Navegación entre secciones

```gherkin
Dado que estoy en una pantalla principal
Cuando uso el mecanismo de navegación de la app
Entonces puedo acceder a mis clases y a mi perfil
Y puedo volver desde las pantallas secundarias
```

### CA-14 — Persistencia

```gherkin
Dado que creo una clase
Cuando cierro y vuelvo a abrir la app
Entonces la clase sigue apareciendo en la lista
```

---

## 12. Datos demo sugeridos

| Dato | Valor |
|---|---|
| Profesor | Docente Demo — `docente.dev@gmail.com` |
| Estudiante | Estudiante Demo — `demo@estudiante.edu` |
| Roles | `Profesor`, `Estudiante` |
| Clase | `Primer Curso` |
| Descripción | `Curso de prueba de persistencia local` |
| Cuestionario | `{"preguntas":["¿Asististe a la clase?","¿Qué aprendiste hoy?"]}` |
| Asistencia del estudiante demo | Presente, con respuestas de ejemplo |

La solución de referencia precarga estos correos con accesos rápidos en el
login. Los nombres, correos y datos pueden adaptarse.

---

## 13. Estructura de pantallas y componentes

### 13.1 Pantallas de referencia

La solución de referencia usa estas pantallas. Se pueden combinar, dividir o
renombrar según el diseño elegido:

1. Login.
2. Home de profesor.
3. Home de estudiante.
4. Resumen de clase.
5. Detalle de clase.
6. Editar clase.
7. Cuestionario de estudiante.
8. Perfil.

### 13.2 Componentes reutilizables de referencia

La solución de referencia extrae estos componentes. No es obligatorio tenerlos
con esos nombres, pero suele mejorar la consistencia:

- Scaffold con barra superior y botón volver.
- Tarjeta de sección con padding y color.
- Tarjeta de clase clicable.
- Estado vacío con acción opcional.
- Diálogo para mostrar QR.
- Diálogo para escanear QR.
- Vista de cámara con permiso.
- Botón con ícono.
- Campo de texto con etiqueta.
- Confirmación/errores mediante Toast o texto.

### 13.3 Estructura de paquetes sugerida

```text
com.rojas.disco/
├── MainActivity.kt
├── di/                (módulos Hilt: DatabaseModule, NetworkModule)
├── data/
│   ├── CodigoQr.kt
│   ├── local/
│   │   ├── database/
│   │   ├── entities/
│   │   └── model/
│   ├── remote/        (AuthApi, TokenDataStore, TokenInterceptor, TokenAuthenticator, DTOs)
│   │   └── model/
│   └── repository/    (UniversidadRepository, AuthRepository)
└── ui/
    ├── navigation/
    ├── viewmodels/
    ├── screens/
    ├── components/
    └── theme/
```

Se puede usar otro nombre de paquete y otra organización interna. Lo
recomendable es mantener una separación clara entre UI, navegación, datos y
lógica de negocio.

---

## 14. Entregables esperados

Los entregables describen **resultados**, no una estructura exacta.

1. Proyecto Android compilable desde las primeras etapas.
2. **Checkpoint de compilación temprana:** proyecto base con las librerías
   iniciales compilando antes de implementar funcionalidades.
3. Persistencia local con las entidades y relaciones principales.
4. Una capa de acceso a datos y otra de lógica/repositorio, con nombres y
   organización adaptables.
5. Navegación funcional entre los flujos principales.
6. Pantallas equivalentes que cubran login, clases, asistencia, cuestionario,
   QR y perfil.
7. Generación y escaneo de códigos QR.
8. Datos demo para probar el flujo completo.
9. UI funcional y legible (el estilo visual es libre).
10. Comportamientos equivalentes a los criterios de aceptación CA-01 a CA-14.
11. `CHANGELOG.md` con el plan, los cambios y las verificaciones.
12. Documentación breve de la arquitectura y de cómo ejecutar el proyecto.
13. Nota de adaptación: qué se mantuvo del caso y qué se cambió respecto de
    esta referencia.

---

## 15. Criterios de “terminado”

Se considera terminado cuando:

- el proyecto compila después de agregar las librerías iniciales;
- compila nuevamente al final, sin errores;
- se puede instalar y abrir en un dispositivo/emulador;
- el login demo funciona;
- se puede crear una clase;
- se puede mostrar un QR de clase;
- se puede escanear un QR de estudiante y registrar asistencia;
- el estudiante puede responder el cuestionario;
- el profesor ve las respuestas;
- el perfil muestra el QR personal;
- el logout funciona;
- los datos persisten al cerrar y abrir la app;
- no hay crashes con entradas inválidas;
- el `CHANGELOG.md` refleja el plan y las verificaciones;
- la solución documenta brevemente sus adaptaciones respecto de esta
  referencia.

La centralización de rutas, el nombre de las pantallas y la estructura exacta
de paquetes son decisiones de implementación, no condiciones de terminado.

---

## 16. Supuestos

- Un dispositivo es usado por un profesor o un estudiante a la vez.
- No hay concurrencia real entre usuarios.
- La cámara es opcional: si no hay permiso, se puede ingresar el código
  manualmente.
- Los correos son identificadores únicos en la práctica, aunque el MVP no
  impone una restricción `UNIQUE` en la base.
- El cuestionario es simple y de texto.
- No se requiere autenticación segura en el MVP. **(La referencia actual sí
  usa tokens JWT con refresh automático; la firma se valida en el servidor y
  el cliente solo lee los claims.)**
- La app no necesita internet para funcionar.
- Cada solución puede tener otra arquitectura, otros nombres y otra UI; lo que
  se evalúa es el caso de uso y el cumplimiento de los flujos.

---

## 17. Evoluciones recomendadas

Ordenadas por impacto:

1. **ViewModel + UiState** para sobrevivir a recreaciones y separar lógica. **(Completado.)**
2. **Autenticación real** con contraseña o proveedor externo. **(Completado parcialmente: login con contraseña + tokens JWT + refresh automático ante 401.)**
3. **Cuestionarios dinámicos** a partir del JSON, con modelos tipados.
4. **Persistencia del estado de sesión**. **(Parcialmente: auto-login desde tokens; el `currentUsuarioId` de la UI sigue en memoria.)**
5. **Inyección de dependencias** (Hilt/Koin) si crece el número de dependencias. **(Completado: Hilt.)**
6. **Reportes/exportación** de asistencias.
7. **Sincronización remota** con backend. **(Pendiente: solo la autenticación viaja por la API.)**
8. **Roles adicionales** (Coordinador, Administrador) usando el modelo N:M.
9. **Validación de email único** y normalización.
10. **Tests automatizados** de reglas de negocio y navegación.

---

## 18. Glosario mínimo

| Término | Definición |
|---|---|
| **Clase** | Sesión o curso dictado por un profesor. |
| **Inscripción** | Registro que vincula a un estudiante con una clase. Se materializa en `asistencia_clase`. |
| **Asistencia** | Estado de presencia de un estudiante en una clase, más sus respuestas. |
| **Cuestionario** | Preguntas definidas para una clase y las respuestas del estudiante. |
| **QR de clase** | Código con formato `CLASE_{claseId}`. |
| **QR de estudiante** | Código con formato `ESTUDIANTE_{usuarioId}`. |
| **MVP** | Versión mínima funcional descrita en este documento. |
| **Local-first** | La app funciona y persiste datos localmente, sin depender de un servidor. |

---

## 19. Anexo A — Plan de implementación y CHANGELOG

Este anexo define cómo debe trabajar el modelo o desarrollador que reciba este
documento. El objetivo es que cada cambio sea planificado, verificable y
trazable.

### 19.1 Formato obligatorio de `CHANGELOG.md`

```markdown
# Changelog

Todos los cambios relevantes de este proyecto se documentan en este archivo.

## [No lanzado] — AAAA-MM-DD

### Plan
- [ ] Configurar el proyecto Android.
- [ ] Crear las entidades de persistencia local.
- [ ] Implementar el login.

### Agregado
- ...

### Cambiado
- ...

### Verificación
- `./gradlew :app:assembleDebug` — OK
- `./gradlew :app:testDebugUnitTest` — OK

## [No lanzado] — AAAA-MM-DD (iteración anterior)

### Plan
- [x] ...
```

### 19.2 Reglas de marcado

| Estado | Significado |
|---|---|
| `[ ]` | Pendiente. |
| `[x]` | Implementado y verificado. |
| `[!]` | Bloqueado; requiere una decisión o falta información. |

Reglas:

0. **Antes de implementar features**, el proyecto debe compilar con las
   librerías iniciales. Este es el primer checkpoint y no se puede omitir.
1. El plan se escribe **antes** del código.
2. Cada ítem debe ser pequeño: un archivo, una pantalla, una consulta o una
   regla.
3. Un ítem solo pasa a `[x]` cuando compila y se verificó.
4. Si un ítem cambia de alcance, se actualiza el texto del checklist; no se
   borra.
5. Las entradas anteriores no se eliminan.
6. Cada iteración debe terminar con una sección `### Verificación`.
7. Si no se puede ejecutar una verificación, se indica explícitamente por qué.
8. Cada vez que se agrega una librería nueva, se compila antes de continuar.

### 19.3 Plan por hitos

El modelo puede copiar estos hitos a `CHANGELOG.md` y adaptarlos a su
solución. Los nombres de archivos, clases y pantallas son de referencia. Si tu
solución no usa Room, Compose o Navigation, reemplazá las tareas por sus
equivalentes y mantené el mismo objetivo. Se recomienda no empezar un hito sin
cerrar el anterior.

#### Hito 0 — Esqueleto compilable (puerta obligatoria)

**Este es el primer entregable y no se puede omitir.** El objetivo es verificar
temprano que las librerías iniciales son compatibles entre sí antes de escribir
pantallas o lógica de negocio.

- [ ] Crear `CHANGELOG.md` con el plan completo.
- [ ] Crear el proyecto Android base.
- [ ] Configurar `applicationId`, `minSdk`, `targetSdk` y `compileSdk`
      (valores de referencia o los que defina el curso).
- [ ] Agregar solo las librerías iniciales, por grupos.
- [ ] Compilar después del primer grupo de dependencias.
- [ ] Compilar después de agregar persistencia local (Room/KSP o equivalente).
- [ ] Compilar después de agregar cámara/QR (CameraX, ML Kit, ZXing o
      equivalentes).
- [ ] Corregir incompatibilidades de versiones o plugins.
- [ ] Confirmar que la app base abre sin crashes.
- [ ] Registrar el resultado en `CHANGELOG.md`.

**Verificación obligatoria:** `./gradlew :app:assembleDebug`.

**Criterio de avance:** si la compilación falla, no se empieza el Hito 1. Se
resuelven primero las dependencias y versiones.

#### Hito 1 — Capa de datos

- [ ] Crear `UsuarioEntity`.
- [ ] Crear `RolEntity`.
- [ ] Crear `UsuarioRolCrossRef`.
- [ ] Crear `ClaseEntity`.
- [ ] Crear `AsistenciaClaseEntity`.
- [ ] Crear modelos `UsuarioConRoles`, `ClaseConDetalles` y `RolConUsuarios`.
- [ ] Crear `UniversidadDao` con consultas y relaciones.
- [ ] Crear `AppDatabase` con singleton.
- [ ] Crear `UniversidadRepository` con reglas RN-02, RN-04 y RN-05.
- [ ] Verificar que la capa de persistencia compila y genera el código
      necesario.

**Verificación:** `./gradlew :app:compileDebugKotlin`.

#### Hito 2 — Navegación base

- [ ] Crear `Destino` como `sealed class`.
- [ ] Declarar todos los destinos y argumentos.
- [ ] Implementar `crearRuta`, lectura tipada y `coincideCon`.
- [ ] Crear helpers `navegarA`, `navegarAPestana` y `composableDestino`.
- [ ] Crear `NavHost` con todas las pantallas.
- [ ] Implementar el mecanismo de navegación elegido y el acceso a clases y
      perfil.
- [ ] Verificar navegación básica entre pantallas placeholder.

**Verificación:** `./gradlew :app:assembleDebug` y recorrido manual.

#### Hito 3 — Login y datos demo

- [ ] Crear `LoginScreen`.
- [ ] Implementar búsqueda de usuario por email.
- [ ] Implementar creación de datos demo.
- [ ] Navegar según rol.
- [ ] Mostrar error para usuario inexistente o sin rol.
- [ ] Implementar logout desde perfil.
- [ ] Verificar CA-01, CA-02, CA-03 y CA-12.

**Verificación:** recorrido manual de los cuatro escenarios.

#### Hito 4 — Flujo del profesor

- [ ] Crear `TeacherHomeScreen`.
- [ ] Listar clases del profesor.
- [ ] Implementar estado vacío.
- [ ] Implementar diálogo de creación de clase.
- [ ] Implementar `SessionScreen`.
- [ ] Implementar `SessionDetailScreen`.
- [ ] Implementar `SessionEditScreen`.
- [ ] Verificar CA-04, CA-05, CA-07 y CA-14.

**Verificación:** crear una clase, editarla y verla en el detalle.

#### Hito 5 — Flujo del estudiante

- [ ] Crear `StudentHomeScreen`.
- [ ] Listar clases inscriptas.
- [ ] Mostrar estado de asistencia y cuestionario.
- [ ] Crear `StudentQuestionnaireScreen`.
- [ ] Guardar asistencia y respuestas.
- [ ] Verificar CA-09 y CA-10.

**Verificación:** login como estudiante, responder y ver el cambio en el home.

#### Hito 6 — QR

- [ ] Crear `CodigoQr` como `sealed class`.
- [ ] Implementar generación de imagen QR.
- [ ] Implementar diálogo para mostrar QR.
- [ ] Implementar permiso de cámara.
- [ ] Implementar escaneo con CameraX + ML Kit.
- [ ] Implementar entrada manual como alternativa.
- [ ] Integrar QR de clase y QR de estudiante.
- [ ] Verificar CA-06, CA-07 y CA-08.

**Verificación:** probar QR válidos e inválidos en dispositivo/emulador con
cámara.

#### Hito 7 — Perfil y pulido

- [ ] Crear `ProfileScreen`.
- [ ] Mostrar nombre, email y roles.
- [ ] Mostrar QR personal.
- [ ] Implementar logout.
- [ ] Extraer componentes reutilizables.
- [ ] Implementar estados de carga y vacío.
- [ ] Verificar tema claro/oscuro.
- [ ] Verificar CA-11 y CA-13.

**Verificación:** recorrido completo de todos los criterios de aceptación.

#### Hito 8 — Cierre

- [ ] Ejecutar CA-01 a CA-14 o sus equivalentes adaptados.
- [ ] Corregir defectos encontrados.
- [ ] Ejecutar `./gradlew :app:assembleDebug`.
- [ ] Actualizar `CHANGELOG.md` con el estado final.
- [ ] Documentar brevemente la arquitectura y las adaptaciones realizadas.
- [ ] Confirmar que la navegación funciona y que no hay crashes conocidos.

**Verificación final:** APK generado e instalado; checklist completo.

### 19.4 Ejemplo de una iteración terminada (solución de referencia)

```markdown
## [No lanzado] — 2026-10-22

### Plan
- [x] Crear `CodigoQr` con formatos `CLASE_` y `ESTUDIANTE_`.
- [x] Reemplazar strings de QR en `ProfileScreen`.
- [x] Reemplazar strings de QR en `SessionScreen`.
- [x] Reemplazar strings de QR en `StudentHomeScreen`.
- [ ] Agregar tests unitarios de `leer()`.

### Cambiado
- Los prefijos de QR ahora viven en un solo archivo.
- Las pantallas usan `generar()` y `leer()` en lugar de concatenar strings.

### Verificación
- `./gradlew :app:assembleDebug` — OK
- `./gradlew :app:testDebugUnitTest` — pendiente de tests nuevos.
```

### 19.5 Cuándo pedir confirmación

- Si el modelo trabaja de forma interactiva, puede mostrar el plan y esperar
  aprobación antes del primer hito.
- Si trabaja de forma autónoma, debe escribir el plan completo en
  `CHANGELOG.md`, ejecutar en orden y dejar constancia de cada verificación.
- Si aparece una decisión de producto no cubierta por este documento, debe
  registrar la duda en el CHANGELOG y elegir la opción más conservadora.

