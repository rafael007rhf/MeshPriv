package br.dev.meshpriv.domain.model

data class DeliveryMetric(
    val metricId: String,        // UUID v4
    val messageId: String,       // referência à mensagem
    val sourceId: String,
    val destinationId: String,
    val latencyMs: Long,         // receivedAt - sentAt (-1 quando não entregue)
    val hopCount: Int,
    val delivered: Boolean,
    val batteryLevelStart: Int,  // 0–100 (-1 quando desconhecido)
    val batteryLevelEnd: Int,    // 0–100
    val networkSize: Int,        // número de peers conectados no momento do envio
    val recordedAt: Long
)
