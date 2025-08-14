package kr.elta.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Entity
@Table(name = "sellerBroadcastPlan")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class SellerBroadcastPlanEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long uuid;

    @Column(nullable = false)
    private Long sellerUuid;

    @Column(nullable = false)
    private Long planUuid;

    @Column(nullable = false)
    private int remainMinute;

    @Column(nullable = false)
    private int maxViewer;
}
