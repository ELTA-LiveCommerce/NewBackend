package kr.elta.backend.dto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDTO {
    @NotNull
    private Long uuid;

    @NotNull
    @Length(max = 100, message = "100자 이내로 입력해주세요.")
    private String address;

    @NotNull
    private Long broadcastUuid;

    @NotNull
    private List<ProductDTO.Option> option;
}
