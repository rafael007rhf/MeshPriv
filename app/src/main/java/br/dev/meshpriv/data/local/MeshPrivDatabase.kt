package br.dev.meshpriv.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import br.dev.meshpriv.data.local.dao.MessageDao
import br.dev.meshpriv.data.local.dao.MetricDao
import br.dev.meshpriv.data.local.dao.PeerDao
import br.dev.meshpriv.data.local.entity.MessageEntity
import br.dev.meshpriv.data.local.entity.MetricEntity
import br.dev.meshpriv.data.local.entity.PeerEntity

@Database(
    entities = [MessageEntity::class, PeerEntity::class, MetricEntity::class],
    version = 1,
    exportSchema = false // TODO(semana-N): exportar schema quando houver migração v1→v2
)
@TypeConverters(MeshPrivTypeConverters::class)
abstract class MeshPrivDatabase : RoomDatabase() {

    abstract fun messageDao(): MessageDao
    abstract fun peerDao(): PeerDao
    abstract fun metricDao(): MetricDao

    companion object {
        private const val DATABASE_NAME = "meshpriv.db"

        @Volatile
        private var instance: MeshPrivDatabase? = null

        // Sem fallbackToDestructiveMigration: os dados de experimento não podem ser apagados.
        // Migrações futuras devem ser versionadas via addMigrations().
        fun getInstance(context: Context): MeshPrivDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    MeshPrivDatabase::class.java,
                    DATABASE_NAME
                ).build().also { instance = it }
            }
    }
}
