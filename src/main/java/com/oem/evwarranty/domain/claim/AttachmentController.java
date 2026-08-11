package com.oem.evwarranty.domain.claim;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST Controller for uploading, downloading, listing, and deleting claim attachments.
 */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Claim Attachments", description = "REST APIs for Managing Diagnostic Photos, Videos, and Invoices")
public class AttachmentController {

    private final AttachmentService attachmentService;

    public AttachmentController(AttachmentService attachmentService) {
        this.attachmentService = attachmentService;
    }

    @PostMapping(value = "/claims/{claimId}/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('SC_STAFF', 'SC_TECHNICIAN', 'EVM_STAFF', 'ADMIN')")
    @Operation(summary = "Upload Claim Attachment", description = "Upload a diagnostic photo, video, or report file for a warranty claim")
    public ResponseEntity<?> uploadAttachment(
            @PathVariable Long claimId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "attachmentType", required = false) ClaimAttachment.AttachmentType attachmentType,
            @RequestParam(value = "description", required = false) String description,
            Authentication authentication) {

        String username = authentication != null ? authentication.getName() : null;
        ClaimAttachment attachment = attachmentService.uploadAttachment(claimId, file, attachmentType, description, username);

        return ResponseEntity.ok(toMap(attachment));
    }

    @GetMapping("/attachments/{id}/download")
    @Operation(summary = "Download Attachment File", description = "Download binary file content of a claim attachment")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long id) {
        ClaimAttachment attachment = attachmentService.getAttachmentById(id);
        Resource resource = attachmentService.downloadAttachmentFile(id);

        String contentType = attachment.getFileType() != null ? attachment.getFileType() : "application/octet-stream";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + attachment.getFileName() + "\"")
                .body(resource);
    }

    @GetMapping("/claims/{claimId}/attachments")
    @Operation(summary = "List Claim Attachments", description = "Retrieve metadata list of all files attached to a warranty claim")
    public ResponseEntity<List<Map<String, Object>>> getAttachmentsByClaim(@PathVariable Long claimId) {
        List<ClaimAttachment> attachments = attachmentService.getAttachmentsByClaimId(claimId);
        List<Map<String, Object>> result = attachments.stream().map(this::toMap).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/attachments/{id}")
    @PreAuthorize("hasAnyRole('SC_STAFF', 'EVM_STAFF', 'ADMIN')")
    @Operation(summary = "Delete Attachment", description = "Delete an attachment record and remove its physical file from disk")
    public ResponseEntity<?> deleteAttachment(@PathVariable Long id) {
        attachmentService.deleteAttachment(id);
        return ResponseEntity.ok(Map.of("message", "Attachment deleted successfully", "id", id));
    }

    private Map<String, Object> toMap(ClaimAttachment a) {
        return Map.of(
                "id", a.getId(),
                "claimId", a.getWarrantyClaim().getId(),
                "fileName", a.getFileName() != null ? a.getFileName() : "",
                "fileType", a.getFileType() != null ? a.getFileType() : "",
                "fileSize", a.getFileSize() != null ? a.getFileSize() : 0L,
                "attachmentType", a.getAttachmentType() != null ? a.getAttachmentType().name() : "OTHER",
                "description", a.getDescription() != null ? a.getDescription() : "",
                "uploadedBy", a.getUploadedBy() != null ? a.getUploadedBy().getUsername() : "",
                "uploadedAt", a.getUploadedAt() != null ? a.getUploadedAt().toString() : ""
        );
    }
}
