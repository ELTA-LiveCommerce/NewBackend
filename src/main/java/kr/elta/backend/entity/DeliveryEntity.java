package kr.elta.backend.entity;

import jakarta.persistence.*;
import kr.elta.backend.dto.ProductDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "delivery")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class DeliveryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long uuid;

    @Column(nullable = false)
    private Long orderUuid;

    @Column(nullable = false)
    private Long sellerUuid;

    @Column(columnDefinition = "VARCHAR(100)", nullable = false)
    @Length(max = 100)
    private String productName;

    @Column(name = "productOptions", columnDefinition = "JSON", nullable = false)
    @JdbcTypeCode(SqlTypes.JSON)
    private List<ProductDTO.Option> option;

    @Column(nullable = false)
    private LocalDateTime orderDateTime;

    @Column(columnDefinition = "VARCHAR(40)", nullable = true)
    @Length(max = 40)
    private String name;

    @Column(columnDefinition = "VARCHAR(11)", nullable = true)
    @Length(max = 11)
    private String phoneNum;

    @Column(columnDefinition = "VARCHAR(100)", nullable = true)
    @Length(max = 100)
    private String address;

    @Column(columnDefinition = "VARCHAR(40)", nullable = true)
    @Length(max = 40)
    private String courierCompany;

    @Column(columnDefinition = "VARCHAR(100)", nullable = true)
    @Length(max = 100)
    private String courierCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private DeliveryStatus status;
}
