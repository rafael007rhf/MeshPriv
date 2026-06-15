package br.dev.meshpriv.data.mesh

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SeenMessageCacheTest {

    @Test
    fun `pacote marcado como visto é deduplicado`() {
        val cache = SeenMessageCache()

        assertFalse(cache.hasSeen("pkt-abc"))
        cache.markSeen("pkt-abc")
        assertTrue(cache.hasSeen("pkt-abc"))
        assertFalse(cache.hasSeen("pkt-outro"))
    }

    @Test
    fun `ao exceder 500 entradas a mais antiga é removida`() {
        val cache = SeenMessageCache()

        repeat(501) { cache.markSeen("pkt-$it") }

        assertFalse("A entrada mais antiga deveria ter sido removida", cache.hasSeen("pkt-0"))
        assertTrue(cache.hasSeen("pkt-1"))
        assertTrue(cache.hasSeen("pkt-500"))
    }

    @Test
    fun `entradas expiram após o TTL de 10 minutos`() {
        var agora = 0L
        val cache = SeenMessageCache(clock = { agora })

        cache.markSeen("pkt-abc")

        // Um instante antes de expirar: ainda visto
        agora = SeenMessageCache.TTL_PADRAO_MS
        assertTrue(cache.hasSeen("pkt-abc"))

        // Passado o TTL: removido
        agora = SeenMessageCache.TTL_PADRAO_MS + 1
        assertFalse(cache.hasSeen("pkt-abc"))
    }

    @Test
    fun `entradas recentes sobrevivem à limpeza de expiradas`() {
        var agora = 0L
        val cache = SeenMessageCache(clock = { agora })

        cache.markSeen("pkt-velho")
        agora = SeenMessageCache.TTL_PADRAO_MS
        cache.markSeen("pkt-novo")
        agora = SeenMessageCache.TTL_PADRAO_MS + 1

        assertFalse(cache.hasSeen("pkt-velho"))
        assertTrue(cache.hasSeen("pkt-novo"))
    }
}
