package kr.elta.backend.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class RedisUtil {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * Redis에서 키에 해당하는 값을 조회
     * 
     * @param key 조회할 키
     * @return 조회된 값 (없으면 null)
     */
    public Object get(String key) {
        try {
            ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
            Object value = valueOperations.get(key);
            log.debug("Redis GET - Key: {}, Value: {}", key, value);
            return value;
        } catch (Exception e) {
            log.error("Redis GET 오류 - Key: {}, Error: {}", key, e.getMessage());
            return null;
        }
    }

    /**
     * Redis에서 키에 해당하는 값을 문자열로 조회
     * 
     * @param key 조회할 키
     * @return 조회된 문자열 값 (없으면 null)
     */
    public String getString(String key) {
        Object value = get(key);
        return value != null ? value.toString() : null;
    }

    /**
     * Redis에 키-값 저장
     * 
     * @param key 저장할 키
     * @param value 저장할 값
     * @return 저장 성공 여부
     */
    public boolean set(String key, Object value) {
        try {
            ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
            valueOperations.set(key, value);
            log.debug("Redis SET - Key: {}, Value: {}", key, value);
            return true;
        } catch (Exception e) {
            log.error("Redis SET 오류 - Key: {}, Value: {}, Error: {}", key, value, e.getMessage());
            return false;
        }
    }

    /**
     * Redis에 키-값을 TTL과 함께 저장
     * 
     * @param key 저장할 키
     * @param value 저장할 값
     * @param timeout 만료 시간
     * @param unit 시간 단위
     * @return 저장 성공 여부
     */
    public boolean set(String key, Object value, long timeout, TimeUnit unit) {
        try {
            ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
            valueOperations.set(key, value, timeout, unit);
            log.debug("Redis SET with TTL - Key: {}, Value: {}, Timeout: {} {}", key, value, timeout, unit);
            return true;
        } catch (Exception e) {
            log.error("Redis SET with TTL 오류 - Key: {}, Value: {}, Error: {}", key, value, e.getMessage());
            return false;
        }
    }

    /**
     * Redis에 키-값을 Duration과 함께 저장
     * 
     * @param key 저장할 키
     * @param value 저장할 값
     * @param duration 만료 시간 (Duration)
     * @return 저장 성공 여부
     */
    public boolean set(String key, Object value, Duration duration) {
        try {
            ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
            valueOperations.set(key, value, duration);
            log.debug("Redis SET with Duration - Key: {}, Value: {}, Duration: {}", key, value, duration);
            return true;
        } catch (Exception e) {
            log.error("Redis SET with Duration 오류 - Key: {}, Value: {}, Error: {}", key, value, e.getMessage());
            return false;
        }
    }

    /**
     * Redis에서 키에 해당하는 값 업데이트 (기존 키가 있을 때만)
     * 
     * @param key 업데이트할 키
     * @param value 새로운 값
     * @return 업데이트 성공 여부
     */
    public boolean update(String key, Object value) {
        try {
            if (hasKey(key)) {
                return set(key, value);
            } else {
                log.warn("Redis UPDATE 실패 - 키가 존재하지 않습니다: {}", key);
                return false;
            }
        } catch (Exception e) {
            log.error("Redis UPDATE 오류 - Key: {}, Value: {}, Error: {}", key, value, e.getMessage());
            return false;
        }
    }

    /**
     * Redis에서 키 삭제
     * 
     * @param key 삭제할 키
     * @return 삭제 성공 여부 (키가 존재하지 않아도 true 반환)
     */
    public boolean delete(String key) {
        try {
            Boolean result = redisTemplate.delete(key);
            log.debug("Redis DELETE - Key: {}, Result: {}", key, result);
            return result != null ? result : false;
        } catch (Exception e) {
            log.error("Redis DELETE 오류 - Key: {}, Error: {}", key, e.getMessage());
            return false;
        }
    }

    /**
     * Redis에서 여러 키 삭제
     * 
     * @param keys 삭제할 키들
     * @return 삭제된 키의 수
     */
    public long delete(String... keys) {
        try {
            Long count = redisTemplate.delete(Set.of(keys));
            log.debug("Redis DELETE Multiple - Keys: {}, Deleted Count: {}", keys, count);
            return count != null ? count : 0;
        } catch (Exception e) {
            log.error("Redis DELETE Multiple 오류 - Keys: {}, Error: {}", keys, e.getMessage());
            return 0;
        }
    }

    /**
     * Redis에 키가 존재하는지 확인
     * 
     * @param key 확인할 키
     * @return 키 존재 여부
     */
    public boolean hasKey(String key) {
        try {
            Boolean exists = redisTemplate.hasKey(key);
            log.debug("Redis HAS_KEY - Key: {}, Exists: {}", key, exists);
            return exists != null ? exists : false;
        } catch (Exception e) {
            log.error("Redis HAS_KEY 오류 - Key: {}, Error: {}", key, e.getMessage());
            return false;
        }
    }

    /**
     * Redis 키의 TTL 조회
     * 
     * @param key 조회할 키
     * @return TTL (초 단위, -1: 만료시간 없음, -2: 키 없음)
     */
    public long getExpire(String key) {
        try {
            Long expire = redisTemplate.getExpire(key);
            log.debug("Redis GET_EXPIRE - Key: {}, TTL: {}", key, expire);
            return expire != null ? expire : -2;
        } catch (Exception e) {
            log.error("Redis GET_EXPIRE 오류 - Key: {}, Error: {}", key, e.getMessage());
            return -2;
        }
    }

    /**
     * Redis 키의 TTL 설정
     * 
     * @param key 키
     * @param timeout 만료 시간
     * @param unit 시간 단위
     * @return 설정 성공 여부
     */
    public boolean expire(String key, long timeout, TimeUnit unit) {
        try {
            Boolean result = redisTemplate.expire(key, timeout, unit);
            log.debug("Redis EXPIRE - Key: {}, Timeout: {} {}, Result: {}", key, timeout, unit, result);
            return result != null ? result : false;
        } catch (Exception e) {
            log.error("Redis EXPIRE 오류 - Key: {}, Error: {}", key, e.getMessage());
            return false;
        }
    }

    /**
     * Redis에서 패턴에 맞는 모든 키 조회
     * 
     * @param pattern 패턴 (예: "user:*", "*session*")
     * @return 매칭되는 키 집합
     */
    public Set<String> keys(String pattern) {
        try {
            Set<String> keys = redisTemplate.keys(pattern);
            log.debug("Redis KEYS - Pattern: {}, Found: {}", pattern, keys != null ? keys.size() : 0);
            return keys;
        } catch (Exception e) {
            log.error("Redis KEYS 오류 - Pattern: {}, Error: {}", pattern, e.getMessage());
            return Set.of();
        }
    }

    /**
     * Redis 연결 상태 확인
     * 
     * @return 연결 상태
     */
    public boolean isConnected() {
        try {
            redisTemplate.getConnectionFactory().getConnection().ping();
            log.debug("Redis 연결 상태: 정상");
            return true;
        } catch (Exception e) {
            log.error("Redis 연결 상태 확인 오류: {}", e.getMessage());
            return false;
        }
    }
}


//@Autowired
//private RedisUtil redisUtil;
//
//// 기본 CRUD
//  redisUtil.set("user:123", "John Doe");
//String name = redisUtil.getString("user:123");
//  redisUtil.update("user:123", "Jane Doe");
//  redisUtil.delete("user:123");
//
//// TTL과 함께 저장
//  redisUtil.set("session:abc", "token", 30, TimeUnit.MINUTES);
//
//// 패턴 검색
//Set<String> userKeys = redisUtil.keys("user:*");