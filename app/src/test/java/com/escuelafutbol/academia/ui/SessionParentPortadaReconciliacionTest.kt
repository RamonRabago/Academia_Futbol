package com.escuelafutbol.academia.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Simulación de uso de [reconciliarPortadaPadreConHijosLogica] / [normalizarJugadorRemoteIdParaPortada]
 * sin Room ni UI (lista mock de hijos).
 */
class SessionParentPortadaReconciliacionTest {

    private val ana = HijoPortadaPadreRef("remote-ana", "Ana")
    private val ben = HijoPortadaPadreRef("remote-ben", "Ben")
    private val zoe = HijoPortadaPadreRef("REMOTE-ZOE", "Zoe")

    @Test
    fun normalizar_trims_y_rechaza_vacio() {
        assertEquals("abc", normalizarJugadorRemoteIdParaPortada("  abc  "))
        assertNull(normalizarJugadorRemoteIdParaPortada("   "))
        assertNull(normalizarJugadorRemoteIdParaPortada(null))
    }

    @Test
    fun lista_vacia_devuelve_null() {
        assertNull(reconciliarPortadaPadreConHijosLogica("cualquiera", emptyList()))
    }

    @Test
    fun fallback_seleccion_inexistente_al_primer_hijo_por_nombre() {
        val hijos = listOf(zoe, ana, ben) // desordenados; tras orden por nombre: Ana, Ben, Zoe
        val out = reconciliarPortadaPadreConHijosLogica("no-existe", hijos)
        assertEquals("remote-ana", out)
    }

    @Test
    fun mantiene_seleccion_valida_canonico_desde_lista() {
        val hijos = listOf(ana, ben)
        val out = reconciliarPortadaPadreConHijosLogica("REMOTE-ANA", hijos)
        assertEquals("remote-ana", out)
    }

    @Test
    fun seleccion_null_usa_primer_hijo_ordenado() {
        val hijos = listOf(zoe, ana)
        assertEquals("remote-ana", reconciliarPortadaPadreConHijosLogica(null, hijos))
    }

    @Test
    fun cambio_de_seleccion_simulado_dos_pasos() {
        val hijos = listOf(ana, ben)
        var sel = reconciliarPortadaPadreConHijosLogica(null, hijos)
        assertEquals("remote-ana", sel)
        sel = reconciliarPortadaPadreConHijosLogica(sel, hijos)
        assertEquals("remote-ana", sel)
        sel = normalizarJugadorRemoteIdParaPortada("remote-ben")!!
        sel = reconciliarPortadaPadreConHijosLogica(sel, hijos)
        assertEquals("remote-ben", sel)
    }
}
