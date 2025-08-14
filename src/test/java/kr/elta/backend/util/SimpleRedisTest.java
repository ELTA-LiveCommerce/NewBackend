package kr.elta.backend.util;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class SimpleRedisTest {

    @Autowired
    private RedisUtil redisUtil;

    @Test
    public void simpleRedisTest() {
        System.out.println("=== Redis 간단 테스트 시작 ===");
        
        // 1. 연결 테스트
        System.out.println("1. Redis 연결 테스트");
        boolean connected = redisUtil.isConnected();
        System.out.println("Redis 연결 상태: " + connected);
        assertTrue(connected, "Redis 서버에 연결되어야 합니다");
        
        // 2. 저장 테스트
        System.out.println("2. 데이터 저장 테스트");
        String key = "test:simple";
        String value = "Hello Redis!";
        boolean saved = redisUtil.set(key, value);
        System.out.println("저장 결과: " + saved);
        assertTrue(saved, "데이터 저장이 성공해야 합니다");
        
        // 3. 조회 테스트
        System.out.println("3. 데이터 조회 테스트");
        String retrieved = redisUtil.getString(key);
        System.out.println("조회된 값: " + retrieved);
        assertEquals(value, retrieved, "저장된 값과 조회된 값이 일치해야 합니다");
        
        // 4. 업데이트 테스트
        System.out.println("4. 데이터 업데이트 테스트");
        String newValue = "Updated Redis!";
        boolean updated = redisUtil.update(key, newValue);
        System.out.println("업데이트 결과: " + updated);
        assertTrue(updated, "데이터 업데이트가 성공해야 합니다");
        
        String updatedValue = redisUtil.getString(key);
        System.out.println("업데이트된 값: " + updatedValue);
        assertEquals(newValue, updatedValue, "업데이트된 값이 일치해야 합니다");
        
        // 5. 삭제 테스트
        System.out.println("5. 데이터 삭제 테스트");
        boolean deleted = redisUtil.delete(key);
        System.out.println("삭제 결과: " + deleted);
        assertTrue(deleted, "데이터 삭제가 성공해야 합니다");
        
        String deletedValue = redisUtil.getString(key);
        System.out.println("삭제 후 조회 결과: " + deletedValue);
        assertNull(deletedValue, "삭제된 키는 null을 반환해야 합니다");
        
        System.out.println("=== Redis 간단 테스트 완료 ===");
    }
}