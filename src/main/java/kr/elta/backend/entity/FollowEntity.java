package kr.elta.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "follow")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class FollowEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long uuid;

    @Column(nullable = false)
    private Long followerUuid;

    @Column(nullable = false)
    private Long followingUuid;
}
