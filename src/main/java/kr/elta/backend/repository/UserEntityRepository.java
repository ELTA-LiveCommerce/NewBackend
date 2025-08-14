package kr.elta.backend.repository;

import jakarta.validation.constraints.NotBlank;
import kr.elta.backend.entity.Role;
import kr.elta.backend.entity.UserEntity;
import org.hibernate.validator.constraints.Length;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserEntityRepository extends JpaRepository<UserEntity, Long> {
    List<UserEntity> findByIdOrPhoneNum(@NotBlank(message = "id는 필수 입력 값입니다.") String id, String phoneNum);

    Optional<UserEntity> findById(@Length(max = 40) String id);

    List<UserEntity> findByPhoneNumAndDeleteDateTimeIsNull(String phoneNum);

    List<UserEntity> findByPhoneNumAndIdAndDeleteDateTimeIsNull(String phoneNum, String id);

    boolean existsByRoleAndUuid(Role role, Long uuid);

    Optional<UserEntity> findAllByRoleAndUuid(Role role, Long uuid);
    
    // 탈퇴하지 않은 사용자만 조회 (deleteDateTime이 null인 경우)
    Optional<UserEntity> findByRoleAndUrlNameAndDeleteDateTimeIsNull(Role role, @Length(max = 40) String urlName);
    
    Optional<UserEntity> findByRoleAndUuidAndDeleteDateTimeIsNull(Role role, Long uuid);

    List<UserEntity> findAllByIsReviewing(Boolean isReviewing);

    UserEntity findByUuid(Long uuid);
}
