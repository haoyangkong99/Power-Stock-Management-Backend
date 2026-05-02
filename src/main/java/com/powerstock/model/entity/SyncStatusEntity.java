package com.powerstock.model.entity;

import com.powerstock.common.BaseEntity;
import com.powerstock.model.enums.SyncStatusEnum;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "sync_status")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SyncStatusEntity extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id", nullable = false) private User user;
    @Column(name = "last_sync_time") private Instant lastSyncTime;
    @Enumerated(EnumType.STRING) @Column(name = "status", nullable = false) @Builder.Default private SyncStatusEnum status = SyncStatusEnum.SUCCESS;
    @Column(name = "pending_operations", nullable = false) @Builder.Default private Integer pendingOperations = 0;
    @Column(name = "last_error_message", columnDefinition = "TEXT") private String lastErrorMessage;
}
