/**
 * Envía FCM HTTP v1 a padres con hijo en la categoría del mensaje insertado.
 * Despliegue: `supabase functions deploy send-academia-mensaje-push --no-verify-jwt`
 * Secretos: WEBHOOK_SECRET, FCM_SERVICE_ACCOUNT_JSON (JSON completo de cuenta de servicio Firebase).
 * Webhook (Dashboard): tabla academia_mensajes_categoria, INSERT, URL de la función, header Authorization: Bearer <WEBHOOK_SECRET>
 */
import { createClient } from "https://esm.sh/@supabase/supabase-js@2.49.1";
import * as jose from "npm:jose@5.9.6";

const WEBHOOK_SECRET = Deno.env.get("WEBHOOK_SECRET") ?? "";
const FCM_SA_JSON = Deno.env.get("FCM_SERVICE_ACCOUNT_JSON") ?? "";

async function getAccessToken(): Promise<{ accessToken: string; projectId: string }> {
  const sa = JSON.parse(FCM_SA_JSON);
  const pk = await jose.importPKCS8(
    String(sa.private_key).replace(/\\n/g, "\n"),
    "RS256",
  );
  const jwt = await new jose.SignJWT({
    scope: "https://www.googleapis.com/auth/firebase.messaging",
  })
    .setProtectedHeader({ alg: "RS256", typ: "JWT" })
    .setIssuer(sa.client_email)
    .setSubject(sa.client_email)
    .setAudience("https://oauth2.googleapis.com/token")
    .setIssuedAt()
    .setExpirationTime("45m")
    .sign(pk);

  const res = await fetch("https://oauth2.googleapis.com/token", {
    method: "POST",
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    body: new URLSearchParams({
      grant_type: "urn:ietf:params:oauth:grant-type:jwt-bearer",
      assertion: jwt,
    }),
  });
  const body = await res.json();
  if (!res.ok) {
    throw new Error(`oauth: ${JSON.stringify(body)}`);
  }
  return { accessToken: body.access_token as string, projectId: sa.project_id as string };
}

async function sendFcm(
  accessToken: string,
  projectId: string,
  deviceToken: string,
  title: string,
  bodyShort: string,
  data: Record<string, string>,
): Promise<boolean> {
  const url = `https://fcm.googleapis.com/v1/projects/${projectId}/messages:send`;
  const res = await fetch(url, {
    method: "POST",
    headers: {
      Authorization: `Bearer ${accessToken}`,
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      message: {
        token: deviceToken,
        notification: { title, body: bodyShort },
        data,
        android: { priority: "HIGH" },
      },
    }),
  });
  return res.ok;
}

Deno.serve(async (req) => {
  if (req.method !== "POST") {
    return new Response("method not allowed", { status: 405 });
  }
  if (!WEBHOOK_SECRET || req.headers.get("authorization") !== `Bearer ${WEBHOOK_SECRET}`) {
    return new Response("unauthorized", { status: 401 });
  }
  if (!FCM_SA_JSON) {
    return new Response("missing FCM_SERVICE_ACCOUNT_JSON", { status: 500 });
  }

  let payload: { type?: string; record?: Record<string, unknown> };
  try {
    payload = await req.json();
  } catch {
    return new Response("bad json", { status: 400 });
  }

  const rec = payload.record;
  if (!rec || payload.type !== "INSERT") {
    return new Response(JSON.stringify({ skipped: true }), {
      headers: { "Content-Type": "application/json" },
    });
  }

  const academiaId = rec.academia_id as string;
  const categoriaNombre = rec.categoria_nombre as string;
  const titulo = (rec.titulo as string) ?? "Aviso";
  const cuerpo = (rec.cuerpo as string) ?? "";
  if (!academiaId || !categoriaNombre) {
    return new Response("missing academia_id or categoria_nombre", { status: 400 });
  }

  const supabaseUrl = Deno.env.get("SUPABASE_URL")!;
  const serviceKey = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!;
  const sb = createClient(supabaseUrl, serviceKey);

  const { data: parents, error: e1 } = await sb.rpc("parent_user_ids_for_categoria_mensaje", {
    p_academia_id: academiaId,
    p_categoria_nombre: categoriaNombre,
  });
  if (e1) {
    return new Response(e1.message, { status: 500 });
  }
  const rows = (parents ?? []) as { parent_user_id: string }[];
  const ids = [...new Set(rows.map((r) => r.parent_user_id))];
  if (ids.length === 0) {
    return new Response(JSON.stringify({ sent: 0, reason: "no_parents" }), {
      headers: { "Content-Type": "application/json" },
    });
  }

  const { data: tokenRows, error: e2 } = await sb.from("user_fcm_tokens").select("token").in("user_id", ids);
  if (e2) {
    return new Response(e2.message, { status: 500 });
  }
  const tokens = [...new Set((tokenRows ?? []).map((t: { token: string }) => t.token))];
  if (tokens.length === 0) {
    return new Response(JSON.stringify({ sent: 0, reason: "no_tokens" }), {
      headers: { "Content-Type": "application/json" },
    });
  }

  const { accessToken, projectId } = await getAccessToken();
  const bodyShort = cuerpo.length > 140 ? cuerpo.slice(0, 137) + "…" : cuerpo;
  const data: Record<string, string> = {
    open_tab: "padres",
    academia_id: academiaId,
    categoria: categoriaNombre,
  };

  let sent = 0;
  for (const t of tokens.slice(0, 400)) {
    const ok = await sendFcm(accessToken, projectId, t, titulo, bodyShort, data);
    if (ok) sent++;
  }

  return new Response(JSON.stringify({ sent, devices: tokens.length }), {
    headers: { "Content-Type": "application/json" },
  });
});
