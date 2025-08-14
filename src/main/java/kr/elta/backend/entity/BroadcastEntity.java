package kr.elta.backend.entity;

import jakarta.persistence.*;
import kr.elta.backend.converter.LongListConverter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "broadcast")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class BroadcastEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long uuid;

    @Column(columnDefinition = "VARCHAR(255)", nullable = false)
    @Length(max = 255)
    private String title;

    @Column(nullable = false)
    private Long sellerUuid;

    @Column(columnDefinition = "VARCHAR(255)", nullable = false)
    @Length(max = 255)
    private String thumbnailUrl;

    @Column(nullable = false)
    private LocalDateTime scheduledDatatime;

    @Column(columnDefinition = "VARCHAR(1000)")
    @Length(max = 1000)
    private String description;

    @Column(nullable = false)
    private int maxViewer;

    @Column(nullable = false, columnDefinition = "TEXT")
    @Convert(converter = LongListConverter.class)
    private List<Long> productUuidList;

    @Column
    private LocalDateTime startDatatime;

    @Column
    private LocalDateTime endDatetime;

    @Column(nullable = false)
    private int shippingFee;

    @Column(columnDefinition = "CHAR(15)")
    private String actualMeetingId;

    @Column(columnDefinition = "CHAR(90)")
    @Length(max = 500)
    private String hlsUrl;
}
