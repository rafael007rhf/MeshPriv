package br.dev.meshpriv.data.mesh

/**
 * Cache de pacotes já vistos — evita loops de retransmissão no flooding.
 *
 * O clock é injetável para permitir testar a expiração por TTL sem esperar 10 minutos reais.
 */
class SeenMessageCache(
    private val maxEntries: Int = MAX_ENTRIES_PADRAO,
    private val ttlMs: Long = TTL_PADRAO_MS,
    private val clock: () -> Long = System::currentTimeMillis
) {

    companion object {
        const val MAX_ENTRIES_PADRAO = 500
        const val TTL_PADRAO_MS = 10 * 60 * 1000L // 10 minutos
    }

    // LinkedHashMap mantém ordem de inserção → a primeira chave é sempre a mais antiga
    private val seen = LinkedHashMap<String, Long>()

    @Synchronized
    fun hasSeen(packetId: String): Boolean {
        removeExpired()
        return seen.containsKey(packetId)
    }

    @Synchronized
    fun markSeen(packetId: String) {
        removeExpired()
        seen[packetId] = clock()
        while (seen.size > maxEntries) {
            seen.remove(seen.keys.first())
        }
    }

    private fun removeExpired() {
        val cutoff = clock() - ttlMs
        val iterator = seen.entries.iterator()
        while (iterator.hasNext()) {
            if (iterator.next().value < cutoff) iterator.remove()
        }
    }
}
