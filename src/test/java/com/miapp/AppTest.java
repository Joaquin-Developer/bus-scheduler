package com.miapp;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import redis.clients.jedis.Jedis;

public class AppTest {
    @Test
    public void testRedisConnection() {
        try (Jedis jedis = new Jedis("localhost", 6379)) {
            String response = jedis.ping();
            assertEquals("PONG", response, "Redis no responde correctamente");
        } catch (Exception e) {
            fail("Error conectando a Redis: " + e.getMessage());
        }
    }

}
