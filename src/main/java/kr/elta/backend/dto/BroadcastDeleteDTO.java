package kr.elta.backend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BroadcastDeleteDTO {
    @NotNull(message = "상품은 필수 입력 값입니다.")
    private List<Long> broadcastUuidList;
}