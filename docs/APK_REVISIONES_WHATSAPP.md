# APK release para compartir (WhatsApp y similares)

APK generada con **`./gradlew :app:assembleRelease`**. El tipo **release** está configurado con **firma *debug*** del SDK (`signingConfig = debug` en `app/build.gradle.kts`): se puede instalar fuera de Play Store, **no** sirve como artefacto de publicación en tienda.

**Salida típica del build:** `app/build/outputs/apk/release/app-release.apk`

Tras cada generación que vaya a pruebas o reparto, añade una fila a la tabla inferior (fecha local del equipo, versión según `versionName`/`versionCode` en `app/build.gradle.kts`, commit `git rev-parse --short HEAD`, tamaño del `.apk`).

---

## Historial de revisiones

| # | Fecha (local) | Versión (`versionName` / code) | Commit | Tamaño (bytes) | Notas |
|---|----------------|----------------------------------|--------|----------------|--------|
| 1 | 2026-04-23 | 1.0.2 / 3 | `213593b` | 17451676 (~16,6 MiB) | Build tras commit de UI (competencias, estadísticas, asistencia, padres, etc.). Copiar `app-release.apk` desde la ruta de salida indicada arriba. |

Entrega anterior documentada en el changelog del producto: sección **[1.0.1] — 2026-04-21** en `CHANGELOG.md` (misma variante release + firma debug).
