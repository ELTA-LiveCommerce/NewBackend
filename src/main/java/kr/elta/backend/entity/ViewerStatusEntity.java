package kr.elta.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "viewerStatus")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class ViewerStatusEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long uuid;

    @Column(nullable = false)
    private Long sellerUuid;

    @Column(nullable = false)
    private Long viewerUuid;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ViewerStatus status;
}
