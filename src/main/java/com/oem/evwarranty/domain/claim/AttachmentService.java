package com.oem.evwarranty.domain.claim;

import com.oem.evwarranty.common.service.FileStorageService;
import com.oem.evwarranty.domain.user.User;
import com.oem.evwarranty.domain.user.UserRepository;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Service for managing ClaimAttachment entity and file operations.
 */
@Service
@Transactional
public class AttachmentService {

    private final ClaimAttachmentRepository attachmentRepository;
    private final WarrantyClaimRepository claimRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    public AttachmentService(ClaimAttachmentRepository attachmentRepository,
                             WarrantyClaimRepository claimRepository,
                             UserRepository userRepository,
                             FileStorageService fileStorageService) {
        this.attachmentRepository = attachmentRepository;
        this.claimRepository = claimRepository;
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
    }

    /**
     * Upload an attachment file and bind to a WarrantyClaim.
     */
    public ClaimAttachment uploadAttachment(Long claimId,
                                            MultipartFile file,
                                            ClaimAttachment.AttachmentType type,
                                            String description,
                                            String username) {
        WarrantyClaim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new NoSuchElementException("Warranty claim not found with ID: " + claimId));

        User user = null;
        if (username != null && !username.isBlank()) {
            user = userRepository.findByUsername(username).orElse(null);
        }

        String storedFileName = fileStorageService.storeFile(file);

        ClaimAttachment attachment = ClaimAttachment.builder()
                .warrantyClaim(claim)
                .fileName(file.getOriginalFilename())
                .filePath(storedFileName)
                .fileType(file.getContentType())
                .fileSize(file.getSize())
                .attachmentType(type != null ? type : ClaimAttachment.AttachmentType.OTHER)
                .description(description)
                .uploadedBy(user)
                .uploadedAt(LocalDateTime.now())
                .build();

        return attachmentRepository.save(attachment);
    }

    /**
     * Retrieve attachment metadata by ID.
     */
    @Transactional(readOnly = true)
    public ClaimAttachment getAttachmentById(Long attachmentId) {
        return attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new NoSuchElementException("Attachment not found with ID: " + attachmentId));
    }

    /**
     * Get physical file resource for download.
     */
    @Transactional(readOnly = true)
    public Resource downloadAttachmentFile(Long attachmentId) {
        ClaimAttachment attachment = getAttachmentById(attachmentId);
        return fileStorageService.loadFileAsResource(attachment.getFilePath());
    }

    /**
     * List all attachments belonging to a warranty claim.
     */
    @Transactional(readOnly = true)
    public List<ClaimAttachment> getAttachmentsByClaimId(Long claimId) {
        return attachmentRepository.findByWarrantyClaimId(claimId);
    }

    /**
     * Delete an attachment metadata and its physical file.
     */
    public void deleteAttachment(Long attachmentId) {
        ClaimAttachment attachment = getAttachmentById(attachmentId);
        fileStorageService.deleteFile(attachment.getFilePath());
        attachmentRepository.delete(attachment);
    }
}
