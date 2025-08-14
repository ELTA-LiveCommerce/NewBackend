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
@Table(name = "`return`")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class ReturnEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long uuid;

    @Column(nullable = false)
    private Long orderUuid;

    @Column(nullable = false)
    private Long sellerUuid;

    @Column(columnDefinition = "VARCHAR(40)", nullable = true)
    @Length(max = 40)
    private String name;

    @Column(columnDefinition = "VARCHAR(100)", nullable = false)
    @Length(max = 100)
    private String productName;

    @Column(name = "productOptions", columnDefinition = "JSON", nullable = false)
    @JdbcTypeCode(SqlTypes.JSON)
    private List<ProductDTO.Option> option;

    @Column(nullable = false)
    private int price;

    @Column(name = "accountNum", columnDefinition = "VARCHAR(20)", nullable = true)
    @Length(max = 20)
    private String accountNum;

    @Enumerated(EnumType.STRING)
    @Column(name = "accountType", nullable = true)
    private BankType accountType;

    @Column(columnDefinition = "VARCHAR(100)", nullable = false)
    @Length(max = 100)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private ReturnStatus status;

    @Column(nullable = false)
    private LocalDateTime returnDateTime;
}