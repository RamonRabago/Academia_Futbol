# Notificaciones push (FCM) — configuración

## 1. Firebase (Android)

1. Crea un proyecto en [Firebase Console](https://console.firebase.google.com/) y añade la app Android con el package `com.escuelafutbol.academia`.
2. Descarga **`google-services.json`** y colócalo en **`app/google-services.json`** (el archivo real no se versiona; el build copia `google-services.json.example` si falta).
3. En Firebase → **Project settings** → **Cloud Messaging**, anota el **Project ID**.
4. Crea una **cuenta de servicio** (IAM → Cuentas de servicio → clave JSON). Ese JSON se usará en la Edge Function como `FCM_SERVICE_ACCOUNT_JSON`.

## 2. Supabase (SQL)

Ejecuta las migraciones del repo, en particular:

- `20260429100000_user_fcm_tokens.sql` — tabla `user_fcm_tokens` y RPC `register_fcm_token`.
- `20260428120000_academia_mensajes_categoria.sql` — mensajes por categoría (si aún no está aplicada).

## 3. Edge Function (envío al publicar un aviso)

1. Instala [Supabase CLI](https://supabase.com/docs/guides/cli) y enlaza el proyecto.
2. Secretos (Dashboard → Edge Functions → Secrets o `supabase secrets set`):

   - `WEBHOOK_SECRET` — cadena larga aleatoria (la misma que pondrás en el webhook).
   - `FCM_SERVICE_ACCOUNT_JSON` — **contenido completo** del JSON de la cuenta de servicio Firebase (una sola línea o pegado tal cual según el mecanismo de secretos).

3. Despliega la función:

   ```bash
   supabase functions deploy send-academia-mensaje-push --no-verify-jwt
   ```

4. **Database Webhook** (Supabase Dashboard → Database → Webhooks):

   - Tabla: `academia_mensajes_categoria`
   - Eventos: `INSERT`
   - URL: `https://<REF>.supabase.co/functions/v1/send-academia-mensaje-push`
   - HTTP Headers: `Authorization: Bearer <WEBHOOK_SECRET>` (mismo valor que el secreto).

Con esto, cada aviso nuevo dispara push a los padres con hijo vinculado en esa categoría y token FCM registrado.

## 4. App Android

- Tras iniciar sesión, la app registra el token vía RPC `register_fcm_token`.
- Android 13+: se solicita `POST_NOTIFICATIONS`.
- Al tocar la notificación (cuando el payload incluye `open_tab=padres`), se abre la pestaña **Padres**.

## 5. Pruebas

1. Dos dispositivos/cuentas: coach envía aviso en **Padres**; padre vinculado en esa categoría debe recibir la notificación.
2. Revisa logs de la Edge Function en el Dashboard si no llega nada (tokens vacíos, OAuth FCM, etc.).
