package kr.elta.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class ChatEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long uuid;

    @Column(nullable = false)
    private Long sellerUuid;

    @Column(nullable = false)
    private Long viewerUuid;

    @Column(nullable = false)
    private int sellerUnreadCount;

    @Column(nullable = false)
    private int viewerUnreadCount;

    @Column(columnDefinition = "VARCHAR(1000)")
    @Length(max = 1000)
    private String lastMessage;

    @Column(name = "lastMessageDateTime")
    private LocalDateTime lastMessageDateTime;

    @Column(name = "createDateTime", nullable = false)
    private LocalDateTime createDateTime;
}
