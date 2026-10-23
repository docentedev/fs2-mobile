# Notificaciones Push con ntfy (Disco)

La app Disco recibe notificaciones push sin Firebase, usando **ntfy.sh**.
El backend Spring Boot (`cursos`) publica los mensajes y la app se suscribe
por **Server-Sent Events (SSE)** a un tópico secreto derivado del email.

## Cómo se deriva el tópico

Tanto el backend como la app calculan el mismo tópico a partir del email:

- SHA-256 del email normalizado (`trim().toLowerCase()`).
- Prefijo `app_vibe_`.
- Primeros 20 caracteres hexadecimales del hash.

Ejemplo: `docente.dev@gmail.com` → `app_vibe_8e63635b0c5401804b4b`.

## Flujo completo

1. El usuario inicia sesión en Disco. Se guardan tokens y se sincroniza el
   usuario local.
2. `LoginViewModel` / `SesionViewModel` inician `NotificationService` pasando
   el email del usuario.
3. El servicio se declara en primer plano (`startForeground`) con un canal
   de baja prioridad y se conecta a:
   `https://ntfy.sh/<topico>/sse`
4. Al publicar un mensaje en ese tópico, ntfy lo envía por SSE y la app
   muestra una notificación de alta prioridad.
5. Al cerrar sesión, `ProfileViewModel` detiene el servicio.

## Cómo enviar una notificación

### Vía backend (recomendado)

```bash
curl -X POST http://localhost:8080/api/notificar \
  -H "Content-Type: application/json" \
  -d '{
    "email": "docente.dev@gmail.com",
    "message": "Hola docente, notificacion de prueba",
    "title": "Prueba Disco",
    "priority": 4
  }'
```

También podés consultar el tópico generado:

```bash
curl http://localhost:8080/api/notificar/topic/docente.dev@gmail.com
```

### Directo a ntfy (sin backend)

```bash
curl -H "Title: Prueba Disco" \
     -H "Priority: high" \
     -d "Hola docente, notificacion de prueba" \
     https://ntfy.sh/app_vibe_8e63635b0c5401804b4b
```

## Requisitos para que se vea en la app

- La app debe estar con sesión iniciada con el **mismo email** al que se envía.
- El permiso `POST_NOTIFICATIONS` debe estar concedido (Android 13+).
  Si está denegado, el servicio corre pero no se muestran notificaciones.
- El emulador/dispositivo debe tener conectividad a internet (ntfy.sh).

## Archivos clave

- `app/src/main/java/com/rojas/disco/feature/notificaciones/NtfyTopics.kt`
- `app/src/main/java/com/rojas/disco/feature/notificaciones/NotificationService.kt`
- `app/src/main/AndroidManifest.xml` (permisos y servicio)
- `app/build.gradle.kts` (`NTFY_BASE_URL`)
EOF
echo done