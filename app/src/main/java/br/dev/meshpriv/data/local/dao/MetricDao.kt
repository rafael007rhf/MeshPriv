package br.dev.meshpriv.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import br.dev.meshpriv.data.local.entity.MetricEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MetricDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(metric: MetricEntity)

    @Query("SELECT * FROM metrics ORDER BY recordedAt ASC")
    suspend fun getAll(): List<MetricEntity>

    @Query("SELECT * FROM metrics ORDER BY recordedAt ASC")
    fun observeAll(): Flow<List<MetricEntity>>
}
