package kr.elta.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.PositiveOrZero;
import kr.elta.backend.dto.ProductDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.hibernate.validator.constraints.Length;

import java.util.List;

@Entity
@Table(name = "product")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class ProductEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long uuid;

    @Column(columnDefinition = "VARCHAR(100)", nullable = false)
    @Length(max = 100)
    private String name;

    @Column(nullable = false)
    private int price;

    @Column(columnDefinition = "VARCHAR(1000)", nullable = true)
    @Length(max = 1000)
    private String description;

    @Column(columnDefinition = "VARCHAR(100)", nullable = true)
    @Length(max = 100)
    private String image;

    @Column(nullable = false)
    private Boolean isPublic;

    @Column(nullable = false)
    private Long userUuid;

    @Column(name = "productOptions", columnDefinition = "JSON", nullable = false)
    @JdbcTypeCode(SqlTypes.JSON)
    private List<ProductDTO.Option> option;
}
