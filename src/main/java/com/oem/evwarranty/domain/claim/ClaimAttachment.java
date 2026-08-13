package com.oem.evwarranty.domain.claim;

import com.oem.evwarranty.domain.user.User;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * ClaimAttachment entity for storing documents related to warranty claims.
 */
@Entity
@Table(name = "claim_attachments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClaimAttachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "claim_id", nullable = false)
    private WarrantyClaim warrantyClaim;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;

    @Column(name = "file_type", length = 100)
    private String fileType;

    @Column(name = "file_size")
    private Long fileSize;

    @Enumerated(EnumType.STRING)
    @Column(name = "attachment_type", length = 30)
    private AttachmentType attachmentType;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by")
    private User uploadedBy;

    @Column(name = "uploaded_at")
    private LocalDateTime uploadedAt;

    @PrePersist
    protected void onCreate() {
        uploadedAt = LocalDateTime.now();
    }

    public enum AttachmentType {
        PHOTO,
        VIDEO,
        DIAGNOSTIC_REPORT,
        INVOICE,
        OTHER
    }
}



