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
@Table(name = "`order`")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class OrderEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long uuid;

    @Column(nullable = false)
    private Long sellerUuid;

    @Column(nullable = false)
    private Long buyerUuid;

    @Column(nullable = false)
    private Long productUuid;

    @Column(columnDefinition = "VARCHAR(40)", nullable = true)
    @Length(max = 40)
    private String name;

    @Column(columnDefinition = "VARCHAR(11)", nullable = true)
    @Length(max = 11)
    private String phoneNum;

    @Column(columnDefinition = "VARCHAR(100)", nullable = false)
    private String productName;

    @Column(name = "productOptions", columnDefinition = "JSON", nullable = false)
    @JdbcTypeCode(SqlTypes.JSON)
    private List<ProductDTO.Option> option;

    @Column(nullable = false)
    private int price;

    @Column(columnDefinition = "VARCHAR(100)")
    @Length(max = 100)
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private OrderStatus status;

    @Column(nullable = false)
    private Long broadcastUuid;

    @Column(nullable = false)
    private LocalDateTime orderDateTime;
}
