package com.escuelafutbol.academia.ui.design

import androidx.compose.ui.unit.dp

/**
 * Tokens de espaciado y forma compartidos por la app.
 * Preferir estos valores en pantallas nuevas o refactorizadas para mantener consistencia.
 * (Gestos de acceso rápido tipo long-press / swipe pueden añadirse por pantalla sin tocar datos.)
 */
object AcademiaDimens {
    val radiusSm = 8.dp
    val radiusMd = 12.dp
    val radiusLg = 16.dp
    val radiusXl = 20.dp
    /** Entre sm y md; chips compactos y pills de estado. */
    val radiusDense = 10.dp

    val paddingScreenHorizontal = 16.dp
    val paddingCard = 16.dp
    val paddingCardCompact = 12.dp

    val buttonMinHeight = 52.dp
    /** Columna fija de marcador / cantidad en filas de partido. */
    val columnMinScoreboard = 56.dp
    val chipSpacing = 10.dp

    val contextBannerHorizontalPadding = 10.dp
    val contextBannerVerticalPadding = 4.dp

    /** Separación vertical muy compacta (equiv. ~ radiusSm − 2.dp). */
    val gapVerticalTight = 6.dp
    val gapMicro = 2.dp
    val gapSm = 4.dp
    val gapMd = 8.dp
    /** Listas y diálogos a pantalla completa. */
    val spacingListSection = 10.dp
    val spacingDialogBlock = 12.dp
    val paddingChipVerticalDense = contextBannerVerticalPadding + gapMicro

    val iconSizeSm = 22.dp
    val iconSizeMd = 28.dp
    val iconSizeResumen = 26.dp
    val avatarRow = 40.dp
    val avatarSheetRow = 48.dp
    /** Ficha de jugador: avatar principal en formulario (alta / edición). */
    val avatarFormHero = 96.dp
    val avatarResultRow = 44.dp
    val resumenStaffCeldaAltura = 112.dp
    /** Tarjetas métricas en grid (Estadísticas). */
    val statsMetricTileHeight = 92.dp

    val matchResultBarWidth = 5.dp
    /** Línea divisoria fina en listas y tarjetas. */
    val dividerThickness = 1.dp

    val spacingRowComfort = 14.dp
    val iconDialogList = 30.dp
    val iconInset = 24.dp

    /** Inicio: portada superior y logo superpuesto. */
    val homeHeroCoverHeight = 152.dp
    val homeHeroLogoSize = 96.dp
    val homeHeroLogoOverlap = 48.dp
    /** Contenedor circular del icono en atajos de Inicio (área táctil cómoda). */
    val homeShortcutIconContainer = 44.dp

    /** Novedades / feed: bloque de carga inicial. */
    val contentLoadingMinHeight = 220.dp
    /** Novedades: diálogo de detalle con scroll. */
    val contentDetailDialogScrollMax = 460.dp
    /** Novedades: lista de categorías en diálogo al publicar. */
    val contentCategoryDialogScrollMax = 360.dp
    /** Novedades: carrusel de imágenes en tarjeta y detalle. */
    val contentFeedPagerHeight = 268.dp
    /** Novedades: portada al crear publicación. */
    val contentEditorCoverMaxHeight = 220.dp
    /** Novedades: miniatura de fotos del cuerpo en el editor. */
    val contentEditorBodyThumb = 88.dp
    /** Novedades: campo de mensaje en el editor. */
    val contentEditorMessageMinHeight = 200.dp
    /** Novedades: indicadores del pager (puntos). */
    val contentPagerDotActive = 7.dp
    val contentPagerDotInactive = 5.dp
    val contentPagerDotSpacingH = 3.dp

    /** Selector de categoría: hero superior y altura de imagen en tarjeta. */
    val categoryPickerHeroHeight = 208.dp
    val categoryPickerCardImageHeight = 160.dp
}
