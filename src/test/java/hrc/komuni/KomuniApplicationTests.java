package hrc.komuni;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class KomuniApplicationTests {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Test
    void contextLoads() {
    }

    @Test
    void testRedisWrite() {
        System.out.println("=== STARTING REDIS TEST ===");
        try {
            String key = "test:connection:key";
            String value = "Hello Redis " + System.currentTimeMillis();

            System.out.println("Attempting to write to Redis: " + key + " = " + value);
            redisTemplate.opsForValue().set(key, value);
            System.out.println("Write successful.");

            System.out.println("Attempting to read from Redis...");
            String retrievedValue = redisTemplate.opsForValue().get(key);
            System.out.println("Read value: " + retrievedValue);

            assertEquals(value, retrievedValue, "Value retrieved from Redis does not match set value");

            // Clean up
            redisTemplate.delete(key);
            System.out.println("Cleanup successful.");
            System.out.println("=== REDIS TEST PASSED ===");
        } catch (Exception e) {
            System.err.println("=== REDIS TEST FAILED ===");
            e.printStackTrace();
            fail("Redis operation failed: " + e.getMessage());
        }
    }

}
