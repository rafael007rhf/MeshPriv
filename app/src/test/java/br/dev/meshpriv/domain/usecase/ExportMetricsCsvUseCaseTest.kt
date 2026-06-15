package br.dev.meshpriv.domain.usecase

import br.dev.meshpriv.domain.model.DeliveryMetric
import br.dev.meshpriv.domain.repository.MetricsRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ExportMetricsCsvUseCaseTest {

    private val metricsRepository = mockk<MetricsRepository>()
    private val useCase = ExportMetricsCsvUseCase(metricsRepository, mockk(relaxed = true))

    @Test
    fun `csv gerado segue exatamente o formato da especificação`() = runTest {
        coEvery { metricsRepository.getAllMetrics() } returns listOf(
            DeliveryMetric(
                metricId = "m1",
                messageId = "msg1",
                sourceId = "AAAA1111",
                destinationId = "BBBB2222",
                latencyMs = 500L,
                hopCount = 2,
                delivered = true,
                batteryLevelStart = 80,
                batteryLevelEnd = 78,
                networkSize = 3,
                recordedAt = 1750000000000L
            ),
            DeliveryMetric(
                metricId = "m2",
                messageId = "msg2",
                sourceId = "AAAA1111",
                destinationId = "CCCC3333",
                latencyMs = -1L,
                hopCount = 7,
                delivered = false,
                batteryLevelStart = 78,
                batteryLevelEnd = 77,
                networkSize = 2,
                recordedAt = 1750000060000L
            )
        )

        val lines = useCase.generateCsv().trim().lines()

        assertEquals(
            "metricId,messageId,sourceId,destinationId,latencyMs,hopCount,delivered,batteryStart,batteryEnd,networkSize,recordedAt",
            lines[0]
        )
        assertEquals("m1,msg1,AAAA1111,BBBB2222,500,2,true,80,78,3,1750000000000", lines[1])
        assertEquals("m2,msg2,AAAA1111,CCCC3333,-1,7,false,78,77,2,1750000060000", lines[2])
        assertEquals(3, lines.size)
    }

    @Test
    fun `csv sem métricas contém apenas o cabeçalho`() = runTest {
        coEvery { metricsRepository.getAllMetrics() } returns emptyList()

        val csv = useCase.generateCsv().trim()

        assertEquals(ExportMetricsCsvUseCase.CSV_HEADER, csv)
    }
}
