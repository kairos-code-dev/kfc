package dev.kairoscode.kfc.internal.ratelimit

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.assertj.core.api.Assertions.assertThat

class RateLimitConfigTest {
    @Test
    fun `RateLimitConfig should use default values when not specified`() {
        // === arrange & act ===
        val config = RateLimitConfig()

        // === assert ===
        assertThat(config.capacity).isEqualTo(50)
        assertThat(config.refillRate).isEqualTo(50)
        assertThat(config.enabled).isTrue()
        assertThat(config.waitTimeoutMillis).isEqualTo(60000L)
    }

    @Test
    fun `RateLimitConfig should accept custom values`() {
        // === arrange & act ===
        val config = RateLimitConfig(
            capacity = 100,
            refillRate = 200,
            enabled = false,
            waitTimeoutMillis = 30000L
        )

        // === assert ===
        assertThat(config.capacity).isEqualTo(100)
        assertThat(config.refillRate).isEqualTo(200)
        assertThat(config.enabled).isFalse()
        assertThat(config.waitTimeoutMillis).isEqualTo(30000L)
    }

    @Test
    fun `RateLimitConfig should throw exception when capacity is zero`() {
        // === act & assert ===
        assertThrows<IllegalArgumentException> {
            RateLimitConfig(capacity = 0)
        }
    }

    @Test
    fun `RateLimitConfig should throw exception when capacity is negative`() {
        // === act & assert ===
        assertThrows<IllegalArgumentException> {
            RateLimitConfig(capacity = -1)
        }
    }

    @Test
    fun `RateLimitConfig should throw exception when refillRate is zero`() {
        // === act & assert ===
        assertThrows<IllegalArgumentException> {
            RateLimitConfig(refillRate = 0)
        }
    }

    @Test
    fun `RateLimitConfig should throw exception when waitTimeoutMillis is zero`() {
        // === act & assert ===
        assertThrows<IllegalArgumentException> {
            RateLimitConfig(waitTimeoutMillis = 0L)
        }
    }
}

class RateLimitingSettingsTest {
    @Test
    fun `RateLimitingSettings should use default RateLimitConfig for all sources`() {
        // === arrange & act ===
        val settings = RateLimitingSettings()

        // === assert ===
        assertThat(settings.krx.capacity).isEqualTo(50)
        assertThat(settings.krx.refillRate).isEqualTo(50)
        assertThat(settings.naver.capacity).isEqualTo(50)
        assertThat(settings.naver.refillRate).isEqualTo(50)
        assertThat(settings.opendart.capacity).isEqualTo(50)
        assertThat(settings.opendart.refillRate).isEqualTo(50)
    }

    @Test
    fun `RateLimitingSettings should accept custom configs for each source`() {
        // === arrange ===
        val krxConfig = RateLimitConfig(capacity = 100)
        val naverConfig = RateLimitConfig(capacity = 200)
        val opendartConfig = RateLimitConfig(capacity = 150)

        // === act ===
        val settings = RateLimitingSettings(
            krx = krxConfig,
            naver = naverConfig,
            opendart = opendartConfig
        )

        // === assert ===
        assertThat(settings.krx.capacity).isEqualTo(100)
        assertThat(settings.naver.capacity).isEqualTo(200)
        assertThat(settings.opendart.capacity).isEqualTo(150)
    }

    @Test
    fun `krxDefault should return config with capacity and refillRate of 50`() {
        // === arrange & act ===
        val config = RateLimitingSettings.krxDefault()

        // === assert ===
        assertThat(config.capacity).isEqualTo(50)
        assertThat(config.refillRate).isEqualTo(50)
        assertThat(config.enabled).isTrue()
    }

    @Test
    fun `naverDefault should return config with capacity and refillRate of 50`() {
        // === arrange & act ===
        val config = RateLimitingSettings.naverDefault()

        // === assert ===
        assertThat(config.capacity).isEqualTo(50)
        assertThat(config.refillRate).isEqualTo(50)
        assertThat(config.enabled).isTrue()
    }

    @Test
    fun `openDartDefault should return config with capacity and refillRate of 50`() {
        // === arrange & act ===
        val config = RateLimitingSettings.openDartDefault()

        // === assert ===
        assertThat(config.capacity).isEqualTo(50)
        assertThat(config.refillRate).isEqualTo(50)
        assertThat(config.enabled).isTrue()
    }

    @Test
    fun `unlimited should return settings with all sources disabled`() {
        // === arrange & act ===
        val settings = RateLimitingSettings.unlimited()

        // === assert ===
        assertThat(settings.krx.enabled).isFalse()
        assertThat(settings.naver.enabled).isFalse()
        assertThat(settings.opendart.enabled).isFalse()
    }
}
