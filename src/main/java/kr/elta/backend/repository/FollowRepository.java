package kr.elta.backend.repository;

import kr.elta.backend.entity.FollowEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FollowRepository extends JpaRepository<FollowEntity, Long> {
    Object countAllByFollowingUuid(Long followingUuid);

    boolean existsByFollowingUuidAndFollowerUuid(Long followingUuid, Long followerUuid);

    void deleteByFollowingUuidAndFollowerUuid(Long uuid, long l);

    Page<FollowEntity> findAllByFollowingUuid(Long followingUuid, Pageable pageable);
    
    /**
     * 특정 사용자를 팔로우하는 모든 팔로워 조회
     */
    List<FollowEntity> findAllByFollowingUuid(Long followingUuid);
}
