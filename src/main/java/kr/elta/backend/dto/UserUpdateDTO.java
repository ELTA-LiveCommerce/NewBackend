package kr.elta.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import kr.elta.backend.entity.BankType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserUpdateDTO {
    @Length(max = 11, message = "11자 이내로 입력해주세요.")
    private String phoneNum;

    @Length(max = 40, message = "40자 이내로 입력해주세요.")
    private String name;

    @Length(max = 255, message = "255자 이내로 입력해주세요.")
    private String profileImg;

    @Length(max = 255, message = "255자 이내로 입력해주세요.")
    private String bannerImg;

    @Length(max = 20, message = "20자 이내로 입력해주세요.")
    private String accountNum;

    private BankType accountType;

    private String oldPassword;

    private String newPassword;

    @Length(max = 100, message = "100자 이내로 입력해주세요.")
    private String address;

    @Length(max = 1000, message = "1000자 이내로 입력해주세요.")
    private String description;
}
