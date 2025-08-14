package kr.elta.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDateTime;

@Entity
@Table(name = "inquiry")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class InquiryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long uuid;

    @Column(name = "userUuid", nullable = false)
    private Long userUuid;

    @Column(name = "title", columnDefinition = "VARCHAR(100)", nullable = false)
    @Length(max = 100)
    private String title;

    @Column(name = "isEnd", nullable = false)
    private Boolean isEnd;

    @Column(name = "userName", columnDefinition = "VARCHAR(40)")
    @Length(max = 40)
    private String userName;

    @Column(name = "userPhoneNum", columnDefinition = "CHAR(11)")
    @Length(max = 11)
    private String userPhoneNum;

    @Column(name = "unreadCount", nullable = false)
    private int unreadCount;

    @Column(name = "createDateTime", nullable = false)
    private LocalDateTime createDateTime;
}
