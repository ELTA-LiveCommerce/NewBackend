package kr.elta.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductDTO {
    private Long id;

    @NotBlank(message = "name는 필수 입력 값입니다.")
    @Size(min = 1, max = 100)
    private String name;

    @NotNull(message = "price는 필수 입력 값입니다.")
    @PositiveOrZero
    private int price;

    @NotNull(message = "isPublic는 필수 입력 값입니다.")
    private Boolean isPublic;

    @NotNull(message = "option는 필수 입력 값입니다.")
    private List<Option> option;

    @Size(max = 1000)
    private String description;

    @Size(max = 100)
    private String image;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Option{
        @NotBlank(message = "name는 필수 입력 값입니다.")
        @Size(min = 1, max = 100)
        private String name;

        @NotNull(message = "quantity는 필수 입력 값입니다.")
        @PositiveOrZero
        private int quantity;
    }
}

