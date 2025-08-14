package kr.elta.backend.util;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RedisUtilTest {

    @Autowired
    private RedisUtil redisUtil;

    private static final String TEST_KEY = "test:key";
    private static final String TEST_VALUE = "test_value";
    private static final String UPDATED_VALUE = "updated_value";

    @BeforeEach
    void setUp() {
        // 테스트 전 키 정리
        redisUtil.delete(TEST_KEY);
    }

    @AfterEach
    void tearDown() {
        // 테스트 후 키 정리
        redisUtil.delete(TEST_KEY);
    }

    @Test
    @Order(1)
    @DisplayName("Redis 연결 상태 확인")
    void testConnection() {
        // Redis 연결 상태 확인
        assertTrue(redisUtil.isConnected(), "Redis 서버에 연결되어야 합니다");
    }

    @Test
    @Order(2)
    @DisplayName("키 존재 여부 확인 - 존재하지 않는 키")
    void testHasKey_NotExists() {
        // 존재하지 않는 키 확인
        assertFalse(redisUtil.hasKey(TEST_KEY), "존재하지 않는 키는 false를 반환해야 합니다");
    }

    @Test
    @Order(3)
    @DisplayName("Redis에 데이터 저장 (Create)")
    void testSet() {
        // 데이터 저장
        assertTrue(redisUtil.set(TEST_KEY, TEST_VALUE), "데이터 저장이 성공해야 합니다");
        
        // 키 존재 확인
        assertTrue(redisUtil.hasKey(TEST_KEY), "저장된 키가 존재해야 합니다");
    }

    @Test
    @Order(4)
    @DisplayName("Redis에서 데이터 조회 (Read)")
    void testGet() {
        // 먼저 데이터 저장
        redisUtil.set(TEST_KEY, TEST_VALUE);
        
        // 데이터 조회
        Object value = redisUtil.get(TEST_KEY);
        assertEquals(TEST_VALUE, value, "저장된 값과 조회된 값이 일치해야 합니다");
        
        // 문자열로 조회
        String stringValue = redisUtil.getString(TEST_KEY);
        assertEquals(TEST_VALUE, stringValue, "문자열로 조회된 값이 일치해야 합니다");
    }

    @Test
    @Order(5)
    @DisplayName("존재하지 않는 키 조회")
    void testGet_NotExists() {
        // 존재하지 않는 키 조회
        Object value = redisUtil.get("nonexistent:key");
        assertNull(value, "존재하지 않는 키는 null을 반환해야 합니다");
        
        String stringValue = redisUtil.getString("nonexistent:key");
        assertNull(stringValue, "존재하지 않는 키는 null을 반환해야 합니다");
    }

    @Test
    @Order(6)
    @DisplayName("Redis 데이터 업데이트 (Update)")
    void testUpdate() {
        // 먼저 데이터 저장
        redisUtil.set(TEST_KEY, TEST_VALUE);
        
        // 데이터 업데이트
        assertTrue(redisUtil.update(TEST_KEY, UPDATED_VALUE), "데이터 업데이트가 성공해야 합니다");
        
        // 업데이트된 값 확인
        String updatedValue = redisUtil.getString(TEST_KEY);
        assertEquals(UPDATED_VALUE, updatedValue, "업데이트된 값이 올바르게 반영되어야 합니다");
    }

    @Test
    @Order(7)
    @DisplayName("존재하지 않는 키 업데이트 시도")
    void testUpdate_NotExists() {
        // 존재하지 않는 키 업데이트 시도
        assertFalse(redisUtil.update("nonexistent:key", "value"), 
                   "존재하지 않는 키 업데이트는 실패해야 합니다");
    }

    @Test
    @Order(8)
    @DisplayName("TTL과 함께 데이터 저장")
    void testSetWithTTL() throws InterruptedException {
        // TTL 2초로 설정
        assertTrue(redisUtil.set(TEST_KEY, TEST_VALUE, 2, TimeUnit.SECONDS), 
                  "TTL과 함께 데이터 저장이 성공해야 합니다");
        
        // 즉시 조회 - 값이 존재해야 함
        assertEquals(TEST_VALUE, redisUtil.getString(TEST_KEY), "TTL 내에는 값이 존재해야 합니다");
        
        // TTL 확인 (약 2초)
        long ttl = redisUtil.getExpire(TEST_KEY);
        assertTrue(ttl > 0 && ttl <= 2, "TTL이 올바르게 설정되어야 합니다");
        
        // 3초 대기 후 확인
        Thread.sleep(3000);
        assertNull(redisUtil.get(TEST_KEY), "TTL 만료 후에는 값이 존재하지 않아야 합니다");
    }

    @Test
    @Order(9)
    @DisplayName("Duration과 함께 데이터 저장")
    void testSetWithDuration() {
        // Duration 5초로 설정
        Duration duration = Duration.ofSeconds(5);
        assertTrue(redisUtil.set(TEST_KEY, TEST_VALUE, duration), 
                  "Duration과 함께 데이터 저장이 성공해야 합니다");
        
        // 값 확인
        assertEquals(TEST_VALUE, redisUtil.getString(TEST_KEY), "Duration 내에는 값이 존재해야 합니다");
        
        // TTL 확인
        long ttl = redisUtil.getExpire(TEST_KEY);
        assertTrue(ttl > 0 && ttl <= 5, "Duration TTL이 올바르게 설정되어야 합니다");
    }

    @Test
    @Order(10)
    @DisplayName("TTL 설정")
    void testExpire() {
        // 먼저 데이터 저장 (TTL 없이)
        redisUtil.set(TEST_KEY, TEST_VALUE);
        
        // TTL 설정
        assertTrue(redisUtil.expire(TEST_KEY, 10, TimeUnit.SECONDS), 
                  "TTL 설정이 성공해야 합니다");
        
        // TTL 확인
        long ttl = redisUtil.getExpire(TEST_KEY);
        assertTrue(ttl > 0 && ttl <= 10, "TTL이 올바르게 설정되어야 합니다");
    }

    @Test
    @Order(11)
    @DisplayName("Redis에서 데이터 삭제 (Delete)")
    void testDelete() {
        // 먼저 데이터 저장
        redisUtil.set(TEST_KEY, TEST_VALUE);
        assertTrue(redisUtil.hasKey(TEST_KEY), "삭제 전에 키가 존재해야 합니다");
        
        // 데이터 삭제
        assertTrue(redisUtil.delete(TEST_KEY), "데이터 삭제가 성공해야 합니다");
        
        // 삭제 확인
        assertFalse(redisUtil.hasKey(TEST_KEY), "삭제 후에는 키가 존재하지 않아야 합니다");
        assertNull(redisUtil.get(TEST_KEY), "삭제된 키는 null을 반환해야 합니다");
    }

    @Test
    @Order(12)
    @DisplayName("여러 키 동시 삭제")
    void testDeleteMultiple() {
        String key1 = "test:key1";
        String key2 = "test:key2";
        String key3 = "test:key3";
        
        // 여러 데이터 저장
        redisUtil.set(key1, "value1");
        redisUtil.set(key2, "value2");
        redisUtil.set(key3, "value3");
        
        // 모든 키가 존재하는지 확인
        assertTrue(redisUtil.hasKey(key1));
        assertTrue(redisUtil.hasKey(key2));
        assertTrue(redisUtil.hasKey(key3));
        
        // 여러 키 동시 삭제
        long deletedCount = redisUtil.delete(key1, key2, key3);
        assertEquals(3, deletedCount, "3개의 키가 삭제되어야 합니다");
        
        // 삭제 확인
        assertFalse(redisUtil.hasKey(key1));
        assertFalse(redisUtil.hasKey(key2));
        assertFalse(redisUtil.hasKey(key3));
    }

    @Test
    @Order(13)
    @DisplayName("패턴으로 키 검색")
    void testKeys() {
        String prefix = "search:test:";
        
        // 테스트 데이터 저장
        redisUtil.set(prefix + "1", "value1");
        redisUtil.set(prefix + "2", "value2");
        redisUtil.set(prefix + "3", "value3");
        redisUtil.set("other:key", "other_value");
        
        // 패턴으로 키 검색
        Set<String> keys = redisUtil.keys(prefix + "*");
        
        assertEquals(3, keys.size(), "패턴에 맞는 3개의 키가 검색되어야 합니다");
        assertTrue(keys.contains(prefix + "1"));
        assertTrue(keys.contains(prefix + "2"));
        assertTrue(keys.contains(prefix + "3"));
        assertFalse(keys.contains("other:key"));
        
        // 정리
        redisUtil.delete(prefix + "1", prefix + "2", prefix + "3", "other:key");
    }

    @Test
    @Order(14)
    @DisplayName("다양한 데이터 타입 저장 및 조회")
    void testDifferentDataTypes() {
        // 문자열
        String stringKey = "test:string";
        String stringValue = "Hello Redis";
        redisUtil.set(stringKey, stringValue);
        assertEquals(stringValue, redisUtil.getString(stringKey));
        
        // 숫자
        String numberKey = "test:number";
        Integer numberValue = 12345;
        redisUtil.set(numberKey, numberValue);
        assertEquals(numberValue.toString(), redisUtil.getString(numberKey));
        
        // Boolean
        String booleanKey = "test:boolean";
        Boolean booleanValue = true;
        redisUtil.set(booleanKey, booleanValue);
        assertEquals(booleanValue.toString(), redisUtil.getString(booleanKey));
        
        // 정리
        redisUtil.delete(stringKey, numberKey, booleanKey);
    }

    @Test
    @Order(15)
    @DisplayName("전체 CRUD 시나리오 테스트")
    void testFullCRUDScenario() {
        String key = "test:scenario";
        String originalValue = "original";
        String updatedValue = "updated";
        
        // 1. Create - 데이터 저장
        assertTrue(redisUtil.set(key, originalValue), "데이터 생성이 성공해야 합니다");
        assertTrue(redisUtil.hasKey(key), "생성된 키가 존재해야 합니다");
        
        // 2. Read - 데이터 조회
        assertEquals(originalValue, redisUtil.getString(key), "저장된 값을 올바르게 조회해야 합니다");
        
        // 3. Update - 데이터 업데이트
        assertTrue(redisUtil.update(key, updatedValue), "데이터 업데이트가 성공해야 합니다");
        assertEquals(updatedValue, redisUtil.getString(key), "업데이트된 값을 올바르게 조회해야 합니다");
        
        // 4. Delete - 데이터 삭제
        assertTrue(redisUtil.delete(key), "데이터 삭제가 성공해야 합니다");
        assertFalse(redisUtil.hasKey(key), "삭제된 키는 존재하지 않아야 합니다");
        assertNull(redisUtil.get(key), "삭제된 키는 null을 반환해야 합니다");
    }
}