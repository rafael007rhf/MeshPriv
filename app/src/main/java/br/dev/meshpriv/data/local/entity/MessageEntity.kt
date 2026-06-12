package br.dev.meshpriv.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val messageId: String,
    val senderId: String,
    val recipientId: String,
    val content: String,         // texto em claro (apenas se for destinatário)
    val sentAt: Long,
    val receivedAt: Long?,
    val hopCount: Int,
    val status: String           // "SENDING" | "DELIVERED" | "FAILED"
)
