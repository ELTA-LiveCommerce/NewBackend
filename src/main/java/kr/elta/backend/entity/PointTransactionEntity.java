package kr.elta.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Entity
@Table(name = "pointTransaction")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class PointTransactionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long uuid;

    @Column(nullable = false)
    @JsonIgnore
    private Long sellerUuid;

    @Column(nullable = false)
    private double price;

    @Column(nullable = false)
    private Boolean isPlus;

    @Column(columnDefinition = "VARCHAR(100)")
    @Length(max = 100)
    private String description;
}
