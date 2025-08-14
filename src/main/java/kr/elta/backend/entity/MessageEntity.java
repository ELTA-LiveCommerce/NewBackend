package kr.elta.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDateTime;

@Entity
@Table(name = "message")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class MessageEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long uuid;

    @Column(nullable = false)
    @JsonIgnore
    private Long chatUuid;

    @Column(nullable = false)
    private Long senderUuid;

    @Column(columnDefinition = "VARCHAR(1000)")
    @Length(max = 1000)
    private String message;

    @Column(name = "createDateTime", nullable = false)
    private LocalDateTime createDateTime;

    @Column(name = "isRead", nullable = false)
    @JsonIgnore
    private Boolean isRead;
}
