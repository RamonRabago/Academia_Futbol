package com.escuelafutbol.academia.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.escuelafutbol.academia.data.local.dao.AcademiaConfigDao
import com.escuelafutbol.academia.data.local.dao.SessionCategoriaRecienteDao
import com.escuelafutbol.academia.data.local.dao.SessionParentPortadaJugadorDao
import com.escuelafutbol.academia.data.local.dao.SessionOperationalMirrorDao
import com.escuelafutbol.academia.data.local.dao.AsistenciaDao
import com.escuelafutbol.academia.data.local.dao.DiaEntrenamientoDao
import com.escuelafutbol.academia.data.local.dao.DiaEntrenamientoOverrideDao
import com.escuelafutbol.academia.data.local.dao.CategoriaDao
import com.escuelafutbol.academia.data.local.dao.JugadorDao
import com.escuelafutbol.academia.data.local.dao.CobroMensualDao
import com.escuelafutbol.academia.data.local.dao.StaffCategoriaDao
import com.escuelafutbol.academia.data.local.dao.StaffDao
import com.escuelafutbol.academia.data.local.entity.AcademiaConfig
import com.escuelafutbol.academia.data.local.entity.SessionCategoriaReciente
import com.escuelafutbol.academia.data.local.entity.SessionParentPortadaJugador
import com.escuelafutbol.academia.data.local.entity.Asistencia
import com.escuelafutbol.academia.data.local.entity.DiaEntrenamiento
import com.escuelafutbol.academia.data.local.entity.DiaEntrenamientoOverride
import com.escuelafutbol.academia.data.local.entity.Categoria
import com.escuelafutbol.academia.data.local.entity.Jugador
import com.escuelafutbol.academia.data.local.entity.JugadorHistorial
import com.escuelafutbol.academia.data.local.entity.CobroMensualAlumno
import com.escuelafutbol.academia.data.local.entity.Staff
import com.escuelafutbol.academia.data.local.entity.StaffCategoria

@Database(
    entities = [
        Jugador::class,
        JugadorHistorial::class,
        Asistencia::class,
        DiaEntrenamiento::class,
        DiaEntrenamientoOverride::class,
        Categoria::class,
        AcademiaConfig::class,
        SessionCategoriaReciente::class,
        SessionParentPortadaJugador::class,
        Staff::class,
        StaffCategoria::class,
        CobroMensualAlumno::class,
    ],
    version = 33,
    exportSchema = false,
)
abstract class AcademiaDatabase : RoomDatabase() {
    abstract fun jugadorDao(): JugadorDao
    abstract fun asistenciaDao(): AsistenciaDao

    abstract fun diaEntrenamientoDao(): DiaEntrenamientoDao
    abstract fun diaEntrenamientoOverrideDao(): DiaEntrenamientoOverrideDao
    abstract fun categoriaDao(): CategoriaDao
    abstract fun academiaConfigDao(): AcademiaConfigDao
    abstract fun sessionCategoriaRecienteDao(): SessionCategoriaRecienteDao
    abstract fun sessionParentPortadaJugadorDao(): SessionParentPortadaJugadorDao
    abstract fun sessionOperationalMirrorDao(): SessionOperationalMirrorDao
    abstract fun staffDao(): StaffDao

    abstract fun staffCategoriaDao(): StaffCategoriaDao

    abstract fun cobroMensualDao(): CobroMensualDao

    companion object {

        private val MIGRATION_1_2 =
            object : Migration(1, 2) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL(
                        "CREATE TABLE IF NOT EXISTS `categorias` (`nombre` TEXT NOT NULL, PRIMARY KEY(`nombre`))",
                    )
                }
            }

        private val MIGRATION_2_3 =
            object : Migration(2, 3) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS `academia_config` (
                          `id` INTEGER NOT NULL,
                          `nombreAcademia` TEXT NOT NULL,
                          `logoRutaAbsoluta` TEXT,
                          PRIMARY KEY(`id`)
                        )
                        """.trimIndent(),
                    )
                    db.execSQL(
                        "INSERT OR IGNORE INTO `academia_config` (`id`, `nombreAcademia`, `logoRutaAbsoluta`) VALUES (1, 'Mi Academia', NULL)",
                    )
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS `staff` (
                          `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                          `nombre` TEXT NOT NULL,
                          `rol` TEXT NOT NULL,
                          `telefono` TEXT,
                          `email` TEXT
                        )
                        """.trimIndent(),
                    )
                }
            }

        private val MIGRATION_3_4 =
            object : Migration(3, 4) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL(
                        "ALTER TABLE academia_config ADD COLUMN portadaRutaAbsoluta TEXT",
                    )
                }
            }

        private val MIGRATION_4_5 =
            object : Migration(4, 5) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL("ALTER TABLE staff ADD COLUMN fotoRutaAbsoluta TEXT")
                }
            }

        private val MIGRATION_5_6 =
            object : Migration(5, 6) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL("ALTER TABLE jugadores ADD COLUMN fotoRutaAbsoluta TEXT")
                    db.execSQL(
                        "ALTER TABLE jugadores ADD COLUMN fechaAltaMillis INTEGER NOT NULL DEFAULT 0",
                    )
                    db.execSQL(
                        "ALTER TABLE jugadores ADD COLUMN activo INTEGER NOT NULL DEFAULT 1",
                    )
                    db.execSQL("ALTER TABLE jugadores ADD COLUMN fechaBajaMillis INTEGER")
                    db.execSQL(
                        """
                        UPDATE jugadores SET fechaAltaMillis =
                          (CAST(strftime('%s','now') AS INTEGER) * 1000)
                        WHERE fechaAltaMillis = 0
                        """.trimIndent().replace("\n", " "),
                    )
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS `jugador_historial` (
                          `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                          `jugadorId` INTEGER NOT NULL,
                          `tipo` TEXT NOT NULL,
                          `fechaMillis` INTEGER NOT NULL,
                          `detalle` TEXT
                        )
                        """.trimIndent(),
                    )
                    db.execSQL(
                        """
                        INSERT INTO jugador_historial (jugadorId, tipo, fechaMillis, detalle)
                        SELECT id, 'ALTA', fechaAltaMillis, NULL FROM jugadores
                        """.trimIndent().replace("\n", " "),
                    )
                }
            }

        private val MIGRATION_6_7 =
            object : Migration(6, 7) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL("ALTER TABLE jugadores ADD COLUMN mensualidad REAL")
                    db.execSQL(
                        "ALTER TABLE academia_config ADD COLUMN mensualidadVisibleProfesor INTEGER NOT NULL DEFAULT 0",
                    )
                    db.execSQL(
                        "ALTER TABLE academia_config ADD COLUMN mensualidadVisibleCoordinador INTEGER NOT NULL DEFAULT 0",
                    )
                    db.execSQL(
                        "ALTER TABLE academia_config ADD COLUMN mensualidadVisibleDueno INTEGER NOT NULL DEFAULT 0",
                    )
                    db.execSQL(
                        "ALTER TABLE academia_config ADD COLUMN rolDispositivo TEXT NOT NULL DEFAULT 'PADRE_TUTOR'",
                    )
                }
            }

        private val MIGRATION_7_8 =
            object : Migration(7, 8) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL(
                        """
                        UPDATE academia_config SET
                          mensualidadVisibleProfesor = 1,
                          mensualidadVisibleCoordinador = 1,
                          mensualidadVisibleDueno = 1
                        WHERE id = 1
                        """.trimIndent().replace("\n", " "),
                    )
                }
            }

        private val MIGRATION_8_9 =
            object : Migration(8, 9) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL("ALTER TABLE academia_config ADD COLUMN pinStaffHash TEXT")
                }
            }

        private val MIGRATION_9_10 =
            object : Migration(9, 10) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL("ALTER TABLE jugadores ADD COLUMN remoteId TEXT")
                    db.execSQL("ALTER TABLE asistencias ADD COLUMN remoteId TEXT")
                    db.execSQL("ALTER TABLE jugador_historial ADD COLUMN remoteId TEXT")
                    db.execSQL("ALTER TABLE staff ADD COLUMN remoteId TEXT")
                    db.execSQL("ALTER TABLE categorias ADD COLUMN remoteId TEXT")
                    db.execSQL("ALTER TABLE academia_config ADD COLUMN remoteAcademiaId TEXT")
                }
            }

        private val MIGRATION_10_11 =
            object : Migration(10, 11) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL("ALTER TABLE jugadores ADD COLUMN fotoUrlSupabase TEXT")
                    db.execSQL("ALTER TABLE staff ADD COLUMN fotoUrlSupabase TEXT")
                    db.execSQL("ALTER TABLE academia_config ADD COLUMN logoUrlSupabase TEXT")
                    db.execSQL("ALTER TABLE academia_config ADD COLUMN portadaUrlSupabase TEXT")
                }
            }

        private val MIGRATION_11_12 =
            object : Migration(11, 12) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL("ALTER TABLE academia_config ADD COLUMN temaColorPrimarioHex TEXT")
                    db.execSQL("ALTER TABLE academia_config ADD COLUMN temaColorSecundarioHex TEXT")
                }
            }

        private val MIGRATION_12_13 =
            object : Migration(12, 13) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL("ALTER TABLE categorias ADD COLUMN portadaRutaAbsoluta TEXT")
                    db.execSQL("ALTER TABLE categorias ADD COLUMN portadaUrlSupabase TEXT")
                }
            }

        private val MIGRATION_13_14 =
            object : Migration(13, 14) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL("ALTER TABLE jugadores ADD COLUMN fechaNacimientoMillis INTEGER")
                }
            }

        private val MIGRATION_14_15 =
            object : Migration(14, 15) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL("ALTER TABLE jugadores ADD COLUMN curp TEXT")
                    db.execSQL("ALTER TABLE jugadores ADD COLUMN actaNacimientoRutaAbsoluta TEXT")
                    db.execSQL("ALTER TABLE jugadores ADD COLUMN actaNacimientoUrlSupabase TEXT")
                }
            }

        private val MIGRATION_15_16 =
            object : Migration(15, 16) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL(
                        "ALTER TABLE jugadores ADD COLUMN becado INTEGER NOT NULL DEFAULT 0",
                    )
                }
            }

        private val MIGRATION_16_17 =
            object : Migration(16, 17) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL("ALTER TABLE jugadores ADD COLUMN curpDocumentoRutaAbsoluta TEXT")
                    db.execSQL("ALTER TABLE jugadores ADD COLUMN curpDocumentoUrlSupabase TEXT")
                }
            }

        private val MIGRATION_17_18 =
            object : Migration(17, 18) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL("ALTER TABLE academia_config ADD COLUMN codigoClubRemoto TEXT")
                }
            }

        private val MIGRATION_20_21 =
            object : Migration(20, 21) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL("ALTER TABLE academia_config ADD COLUMN cloudMembresiaRol TEXT")
                    db.execSQL("ALTER TABLE academia_config ADD COLUMN cloudCoachCategoriasJson TEXT")
                }
            }

        private val MIGRATION_21_22 =
            object : Migration(21, 22) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL("ALTER TABLE academia_config ADD COLUMN codigoInviteCoachRemoto TEXT")
                    db.execSQL("ALTER TABLE academia_config ADD COLUMN codigoInviteCoordinatorRemoto TEXT")
                    db.execSQL("ALTER TABLE academia_config ADD COLUMN codigoInviteParentRemoto TEXT")
                }
            }

        private val MIGRATION_22_23 =
            object : Migration(22, 23) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS `session_categoria_reciente` (
                          `userId` TEXT NOT NULL,
                          `categoriaNombre` TEXT,
                          PRIMARY KEY(`userId`)
                        )
                        """.trimIndent().replace("\n", " "),
                    )
                }
            }

        private val MIGRATION_23_24 =
            object : Migration(23, 24) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL("ALTER TABLE jugadores ADD COLUMN altaPorUserId TEXT")
                }
            }

        private val MIGRATION_24_25 =
            object : Migration(24, 25) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL("ALTER TABLE jugadores ADD COLUMN altaPorNombre TEXT")
                }
            }

        private val MIGRATION_25_26 =
            object : Migration(25, 26) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL(
                        "ALTER TABLE asistencias ADD COLUMN needsCloudPush INTEGER NOT NULL DEFAULT 0",
                    )
                }
            }

        private val MIGRATION_26_27 =
            object : Migration(26, 27) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL("ALTER TABLE staff ADD COLUMN sueldoMensual REAL")
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS `cobros_mensuales_alumno` (
                          `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                          `jugadorId` INTEGER NOT NULL,
                          `periodoYyyyMm` TEXT NOT NULL,
                          `importeEsperado` REAL NOT NULL,
                          `importePagado` REAL NOT NULL,
                          `notas` TEXT,
                          `remoteId` TEXT,
                          `needsCloudPush` INTEGER NOT NULL DEFAULT 0
                        )
                        """.trimIndent().replace("\n", " "),
                    )
                    db.execSQL(
                        "CREATE UNIQUE INDEX IF NOT EXISTS `index_cobros_mensuales_alumno_jugadorId_periodoYyyyMm` " +
                            "ON `cobros_mensuales_alumno` (`jugadorId`, `periodoYyyyMm`)",
                    )
                }
            }

        private val MIGRATION_27_28 =
            object : Migration(27, 28) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL("ALTER TABLE academia_config ADD COLUMN diaLimitePagoMes INTEGER")
                }
            }

        private val MIGRATION_28_29 =
            object : Migration(28, 29) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL("ALTER TABLE academia_config ADD COLUMN remoteAcademiaCuentaUserId TEXT")
                }
            }

        private val MIGRATION_29_30 =
            object : Migration(29, 30) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS `dias_entrenamiento` (
                          `fechaDia` INTEGER NOT NULL,
                          `scopeKey` TEXT NOT NULL,
                          PRIMARY KEY(`fechaDia`, `scopeKey`)
                        )
                        """.trimIndent().replace("\n", " "),
                    )
                    db.execSQL(
                        "INSERT OR IGNORE INTO `dias_entrenamiento` (`fechaDia`, `scopeKey`) " +
                            "SELECT DISTINCT `fechaDia`, '' FROM `asistencias`",
                    )
                }
            }

        private val MIGRATION_30_31 =
            object : Migration(30, 31) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL(
                        "ALTER TABLE academia_config ADD COLUMN recursosUltimaVistaAtMillis INTEGER NOT NULL DEFAULT 0",
                    )
                }
            }

        private val MIGRATION_31_32 =
            object : Migration(31, 32) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS `session_parent_portada_jugador` (
                          `userId` TEXT NOT NULL,
                          `jugadorRemoteId` TEXT,
                          PRIMARY KEY(`userId`)
                        )
                        """.trimIndent().replace("\n", " "),
                    )
                }
            }

        private val MIGRATION_32_33 =
            object : Migration(32, 33) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL(
                        "ALTER TABLE academia_config ADD COLUMN diasEntrenoSemanaIsoJson TEXT NOT NULL DEFAULT '[2,4]'",
                    )
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS `dias_entreno_override` (
                          `fechaDia` INTEGER NOT NULL,
                          `scopeKey` TEXT NOT NULL,
                          `valorForzado` INTEGER NOT NULL,
                          PRIMARY KEY(`fechaDia`, `scopeKey`)
                        )
                        """.trimIndent().replace("\n", " "),
                    )
                    db.execSQL(
                        """
                        INSERT OR IGNORE INTO `dias_entreno_override` (`fechaDia`, `scopeKey`, `valorForzado`)
                        SELECT `fechaDia`, `scopeKey`, 1 FROM `dias_entrenamiento`
                        """.trimIndent().replace("\n", " "),
                    )
                }
            }

        private val MIGRATION_19_20 =
            object : Migration(19, 20) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL(
                        "ALTER TABLE academia_config ADD COLUMN academiaGestionNubePermitida INTEGER NOT NULL DEFAULT 1",
                    )
                }
            }

        private val MIGRATION_18_19 =
            object : Migration(18, 19) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS `staff_categorias` (
                          `staffId` INTEGER NOT NULL,
                          `categoriaNombre` TEXT NOT NULL,
                          PRIMARY KEY(`staffId`, `categoriaNombre`),
                          FOREIGN KEY(`staffId`) REFERENCES `staff`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                          FOREIGN KEY(`categoriaNombre`) REFERENCES `categorias`(`nombre`) ON UPDATE NO ACTION ON DELETE CASCADE
                        )
                        """.trimIndent().replace("\n", " "),
                    )
                    db.execSQL(
                        "CREATE INDEX IF NOT EXISTS `index_staff_categorias_staffId` ON `staff_categorias` (`staffId`)",
                    )
                    db.execSQL(
                        "CREATE INDEX IF NOT EXISTS `index_staff_categorias_categoriaNombre` ON `staff_categorias` (`categoriaNombre`)",
                    )
                }
            }

        fun create(context: Context): AcademiaDatabase =
            Room.databaseBuilder(
                context.applicationContext,
                AcademiaDatabase::class.java,
                "academia.db",
            )
                .addMigrations(
                    MIGRATION_1_2,
                    MIGRATION_2_3,
                    MIGRATION_3_4,
                    MIGRATION_4_5,
                    MIGRATION_5_6,
                    MIGRATION_6_7,
                    MIGRATION_7_8,
                    MIGRATION_8_9,
                    MIGRATION_9_10,
                    MIGRATION_10_11,
                    MIGRATION_11_12,
                    MIGRATION_12_13,
                    MIGRATION_13_14,
                    MIGRATION_14_15,
                    MIGRATION_15_16,
                    MIGRATION_16_17,
                    MIGRATION_17_18,
                    MIGRATION_18_19,
                    MIGRATION_19_20,
                    MIGRATION_20_21,
                    MIGRATION_21_22,
                    MIGRATION_22_23,
                    MIGRATION_23_24,
                    MIGRATION_24_25,
                    MIGRATION_25_26,
                    MIGRATION_26_27,
                    MIGRATION_27_28,
                    MIGRATION_28_29,
                    MIGRATION_29_30,
                    MIGRATION_30_31,
                    MIGRATION_31_32,
                    MIGRATION_32_33,
                )
                .build()
    }
}
