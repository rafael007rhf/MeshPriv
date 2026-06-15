package br.dev.meshpriv.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "metrics")
data class MetricEntity(
    @PrimaryKey val metricId: String,
    val messageId: String,
    val sourceId: String,
    val destinationId: String,
    val latencyMs: Long,
    val hopCount: Int,
    val delivered: Boolean,
    val batteryLevelStart: Int,
    val batteryLevelEnd: Int,
    val networkSize: Int,
    val recordedAt: Long
)
