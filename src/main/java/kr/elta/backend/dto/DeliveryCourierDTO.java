package kr.elta.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeliveryCourierDTO {
    
    @NotEmpty(message = "배송 정보는 최소 하나 이상 필요합니다.")
    @Valid
    private List<DeliveryUpdate> deliveries;
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DeliveryUpdate {
        @NotNull(message = "배송 UUID는 필수입니다.")
        private Long uuid;
        
        @NotBlank(message = "택배사는 필수입니다.")
        @Size(max = 40, message = "택배사는 40자 이하입니다.")
        private String courierCompany;
        
        @NotBlank(message = "송장번호는 필수입니다.")
        @Size(max = 100, message = "송장번호는 100자 이하입니다.")
        private String courierCode;
    }
}