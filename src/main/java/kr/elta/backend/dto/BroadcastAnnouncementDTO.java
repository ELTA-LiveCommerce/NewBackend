package kr.elta.backend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BroadcastAnnouncementDTO {
    @NotNull(message = "방송 UUID는 필수 입력 값입니다.")
    private Long broadcastUuid;
    
    @Size(max = 1000, message = "공지사항은 1000자 이하입니다.")
    private String announcement;
}