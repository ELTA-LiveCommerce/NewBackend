package kr.elta.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Entity
@Table(name = "broadcastPlan")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class BroadcastPlanEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long uuid;

    @Column(columnDefinition = "VARCHAR(100)")
    @Length(max = 100)
    private String name;

    @Column(nullable = false)
    private int minute;

    @Column(nullable = false)
    private int maxViewer;

    @Column(nullable = false)
    private double price;

    @Column(nullable = false)
    private Boolean isActive;

    @Column(nullable = false)
    private int displayOrder;
}
