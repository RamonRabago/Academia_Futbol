package com.escuelafutbol.academia.notification

/**
 * Contrato compartido entre notificaciones locales, FCM y [com.escuelafutbol.academia.MainActivity].
 * Las rutas deben coincidir con las de navegación principal (`Tab.*.route`).
 */
object LocalNotificationContract {
    const val EXTRA_NAV_ROUTE = "extra_nav_route"

    const val CHANNEL_MATCH = "academia_local_proximo_partido"
    const val CHANNEL_NOTICE = "academia_local_aviso"
    const val CHANNEL_REMINDER = "academia_local_recordatorio"

    /** Rutas frecuentes para deep link (mismas que NavHost). */
    const val ROUTE_INICIO = "inicio"
    const val ROUTE_PADRES = "padres"
    const val ROUTE_COMPETENCIAS = "competencias"
    const val ROUTE_CONTENIDO = "contenido"
    const val ROUTE_ASISTENCIA = "asistencia"
    const val ROUTE_JUGADORES = "jugadores"
}

enum class LocalNotificationKind {
    /** Próximo partido / competiciones. */
    NEXT_MATCH,

    /** Aviso o mensaje del club. */
    CLUB_NOTICE,

    /** Recordatorio genérico. */
    SIMPLE_REMINDER,
}
