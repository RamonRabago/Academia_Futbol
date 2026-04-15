package com.escuelafutbol.academia.data.remote

import com.escuelafutbol.academia.AcademiaApplication
import com.escuelafutbol.academia.data.local.dao.AcademiaConfigDao
import com.escuelafutbol.academia.data.local.dao.CobroMensualDao
import com.escuelafutbol.academia.data.local.dao.JugadorDao
import com.escuelafutbol.academia.data.local.entity.CobroMensualAlumno

/** Sube o actualiza un cobro mensual en Supabase y reconcilia `remoteId` / `needsCloudPush` en Room. */
suspend fun pushCobroMensualSiNube(
    app: AcademiaApplication,
    academiaConfigDao: AcademiaConfigDao,
    jugadorDao: JugadorDao,
    cobroMensualDao: CobroMensualDao,
    c: CobroMensualAlumno,
) {
    val client = app.supabaseClient ?: return
    val cfg = academiaConfigDao.getActual() ?: return
    val academiaId = cfg.remoteAcademiaId ?: return
    val j = jugadorDao.getById(c.jugadorId) ?: return
    val jr = j.remoteId ?: return
    val repo = CobroMensualRemoteRepository(client)
    runCatching {
        if (c.remoteId == null) {
            val rid = repo.insertar(academiaId, jr, c)
            val local = cobroMensualDao.getByJugadorYPeriodo(c.jugadorId, c.periodoYyyyMm) ?: return
            cobroMensualDao.update(local.copy(remoteId = rid, needsCloudPush = false))
        } else {
            repo.actualizar(c.remoteId, c.copy(needsCloudPush = false))
            cobroMensualDao.update(c.copy(needsCloudPush = false))
        }
    }
}
