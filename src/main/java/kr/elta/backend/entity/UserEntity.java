package kr.elta.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.springframework.cglib.core.Local;

import java.time.LocalDateTime;

@Entity
@Table(name = "user")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long uuid;

    @Column(name = "id", columnDefinition = "VARCHAR(40)", nullable = false)
    @Length(max = 40)
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    @Column(name = "password", columnDefinition = "VARCHAR(60)", nullable = false)
    @Length(max = 60)
    @JsonIgnore
    private String password;

    @Column(name = "phoneNum", columnDefinition = "VARCHAR(11)", nullable = true)
    @Length(max = 11)
    private String phoneNum;

    @Column(name = "name", columnDefinition = "VARCHAR(40)", nullable = true)
    @Length(max = 40)
    private String name;

    @Column(name = "profileImg", nullable = true)
    private String profileImg;

    @Column(name = "accountNum", columnDefinition = "VARCHAR(20)", nullable = true)
    @Length(max = 20)
    private String accountNum;

    @Enumerated(EnumType.STRING)
    @Column(name = "accountType", nullable = true)
    private BankType accountType;

    @Column(name = "address", columnDefinition = "VARCHAR(100)", nullable = true)
    @Length(max = 100)
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(name = "loginType", nullable = false)
    private LoginType loginType;

    @Column(name = "urlName", columnDefinition = "VARCHAR(40)", nullable = true)
    @Length(max = 40)
    private String urlName;

    @Column(name = "bannerImg", nullable = true)
    private String bannerImg;

    @Column(name = "deleteDateTime", nullable = true)
    private LocalDateTime deleteDateTime;

    @Column(name = "createDateTime", nullable = false)
    private LocalDateTime createDateTime;

    @Column(name = "businessName", columnDefinition = "VARCHAR(40)", nullable = true)
    @Length(max = 40)
    private String businessName;

    @Column(name = "businessAddress", columnDefinition = "VARCHAR(100)", nullable = true)
    @Length(max = 100)
    private String businessAddress;

    @Column(name = "businessNumber", columnDefinition = "CHAR(10)", nullable = true)
    @Length(max = 10)
    private String businessNumber;

    @Column(name = "businessTime", columnDefinition = "CHAR(13)",nullable = true)
    @Length(max = 13)
    private String businessTime;

    @Column(name = "description", columnDefinition = "VARCHAR(1000)", nullable = true)
    @Length(max = 1000)
    private String description;

    @Column(nullable = false)
    @JsonIgnore
    private Boolean isReviewing;

    @Column(nullable = false)
    @JsonIgnore
    private double balance;
}

