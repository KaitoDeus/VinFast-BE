package com.oem.evwarranty.domain.preorder;

import com.oem.evwarranty.common.enums.UserRole;
import com.oem.evwarranty.domain.client.ClientProfile;
import com.oem.evwarranty.domain.client.ClientProfileRepository;
import com.oem.evwarranty.domain.user.Role;
import com.oem.evwarranty.domain.user.RoleRepository;
import com.oem.evwarranty.domain.user.User;
import com.oem.evwarranty.domain.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
public class PreOrderService {

    private static final Logger log = LoggerFactory.getLogger(PreOrderService.class);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final PreOrderRepository preOrderRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ClientProfileRepository clientProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final Optional<JavaMailSender> mailSender;

    public PreOrderService(PreOrderRepository preOrderRepository,
                           UserRepository userRepository,
                           RoleRepository roleRepository,
                           ClientProfileRepository clientProfileRepository,
                           PasswordEncoder passwordEncoder,
                           Optional<JavaMailSender> mailSender) {
        this.preOrderRepository = preOrderRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.clientProfileRepository = clientProfileRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailSender = mailSender;
    }

    public Page<PreOrderDTO.Response> getPreOrders(String query, String status, Pageable pageable) {
        return preOrderRepository.searchPreOrders(query, status, pageable)
                .map(this::toResponse);
    }

    public Optional<PreOrderDTO.Response> getPreOrderById(Long id) {
        return preOrderRepository.findById(id)
                .map(this::toResponse);
    }

    public PreOrderDTO.Response createPreOrder(PreOrderDTO.CreateRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        String phone = request.getPhone().trim();

        // 1. Check if user already exists
        Optional<User> existingUserOpt = userRepository.findByEmail(email);
        if (existingUserOpt.isEmpty() && phone != null && !phone.isBlank()) {
            existingUserOpt = userRepository.findByPhone(phone);
        }

        User user;
        boolean isNewAccount = false;
        String generatedRawPassword = null;

        if (existingUserOpt.isPresent()) {
            user = existingUserOpt.get();
        } else {
            // 2. Auto-provision new User with CLIENT role
            isNewAccount = true;
            generatedRawPassword = generateRandomPassword(8);

            Role clientRole = roleRepository.findByName(UserRole.CLIENT.name())
                    .orElseGet(() -> roleRepository.save(Role.builder()
                            .name(UserRole.CLIENT.name())
                            .description("Khách hàng cá nhân")
                            .build()));

            String username = email.split("@")[0] + "_" + (System.currentTimeMillis() % 10000);

            user = User.builder()
                    .username(username)
                    .email(email)
                    .phone(phone)
                    .fullName(request.getFullName())
                    .password(passwordEncoder.encode(generatedRawPassword))
                    .roles(Set.of(clientRole))
                    .avatarUrl("/team/avatar-1.png")
                    .active(true)
                    .build();

            user = userRepository.save(user);

            // 3. Create initial ClientProfile with welcome bonus points
            ClientProfile profile = ClientProfile.builder()
                    .user(user)
                    .address("Việt Nam")
                    .points(200)
                    .totalSpent(BigDecimal.ZERO)
                    .totalBookings(0)
                    .status("ACTIVE")
                    .build();

            clientProfileRepository.save(profile);

            // 4. Send asynchronous email/SMS notifications
            sendWelcomeNotification(email, request.getFullName(), generatedRawPassword, request.getScooterModel());
        }

        // 5. Create PreOrder record
        String preorderCode = "PO-2028-" + String.format("%04d", (preOrderRepository.count() + 1));
        BigDecimal deposit = request.getDepositAmount() != null 
                ? request.getDepositAmount() 
                : BigDecimal.valueOf(2000000.0);

        PreOrder preOrder = PreOrder.builder()
                .preorderCode(preorderCode)
                .fullName(request.getFullName())
                .phone(phone)
                .email(email)
                .color(request.getColor() != null ? request.getColor() : "Crimson Red")
                .scooterModel(request.getScooterModel())
                .content(request.getContent())
                .depositAmount(deposit)
                .accountCreated(isNewAccount)
                .user(user)
                .status("PENDING")
                .build();

        PreOrder saved = preOrderRepository.save(preOrder);
        PreOrderDTO.Response response = toResponse(saved);
        if (isNewAccount) {
            response.setTemporaryPassword(generatedRawPassword);
        }
        return response;
    }

    public PreOrderDTO.Response updateStatus(Long id, String newStatus) {
        PreOrder preOrder = preOrderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn đặt cọc với ID: " + id));

        preOrder.setStatus(newStatus.toUpperCase());
        return toResponse(preOrderRepository.save(preOrder));
    }

    public PreOrderDTO.Response toResponse(PreOrder p) {
        if (p == null) return null;

        String code = p.getPreorderCode() != null 
                ? p.getPreorderCode() 
                : "PO-2028-" + String.format("%04d", p.getId());

        String dateStr = p.getCreatedAt() != null ? p.getCreatedAt().format(FORMATTER) : "N/A";
        String encodedEmail = URLEncoder.encode(p.getEmail() != null ? p.getEmail() : "", StandardCharsets.UTF_8);
        String redirectUrl = "/login?email=" + encodedEmail;

        BigDecimal deposit = p.getDepositAmount() != null ? p.getDepositAmount() : BigDecimal.valueOf(2000000.0);
        String depositFormatted = String.format("%,d VNĐ", deposit.longValue());

        return PreOrderDTO.Response.builder()
                .id(code)
                .preorderId(code)
                .numericId(p.getId())
                .accountCreated(Boolean.TRUE.equals(p.getAccountCreated()))
                .fullName(p.getFullName())
                .phone(p.getPhone())
                .email(p.getEmail())
                .color(p.getColor())
                .scooterModel(p.getScooterModel())
                .content(p.getContent())
                .depositAmount(deposit)
                .depositAmountFormatted(depositFormatted)
                .redirectLoginUrl(redirectUrl)
                .status(capitalize(p.getStatus()))
                .createdAt(dateStr)
                .build();
    }

    @Async
    public void sendWelcomeNotification(String email, String fullName, String temporaryPassword, String scooterModel) {
        try {
            if (mailSender.isPresent()) {
                JavaMailSender sender = mailSender.get();
                jakarta.mail.internet.MimeMessage mimeMessage = sender.createMimeMessage();
                org.springframework.mail.javamail.MimeMessageHelper helper = new org.springframework.mail.javamail.MimeMessageHelper(mimeMessage, true, "UTF-8");

                helper.setTo(email);
                helper.setSubject("⚡ [VinFast EV] Xác nhận đặt cọc xe " + scooterModel + " & Cấp tài khoản quản trị");

                String htmlContent = String.format("""
                        <!DOCTYPE html>
                        <html>
                        <head>
                            <meta charset="UTF-8">
                            <style>
                                body { font-family: 'Segoe UI', Arial, sans-serif; background-color: #f4f6f9; margin: 0; padding: 20px; color: #1e293b; }
                                .container { max-width: 600px; margin: 0 auto; background: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 12px rgba(0,0,0,0.08); }
                                .header { background: linear-gradient(135deg, #0284c7 0%%, #0369a1 100%%); color: #ffffff; padding: 28px 24px; text-align: center; }
                                .header h1 { margin: 0; font-size: 22px; font-weight: 700; letter-spacing: 0.5px; }
                                .content { padding: 32px 28px; line-height: 1.6; }
                                .badge { display: inline-block; background: #e0f2fe; color: #0369a1; padding: 6px 12px; border-radius: 6px; font-weight: 600; font-size: 14px; margin-bottom: 16px; }
                                .credentials-box { background: #f8fafc; border: 1px solid #e2e8f0; border-left: 4px solid #0284c7; padding: 18px 20px; border-radius: 8px; margin: 20px 0; }
                                .credential-item { margin: 8px 0; font-size: 15px; }
                                .credential-label { color: #64748b; font-size: 13px; text-transform: uppercase; font-weight: 600; }
                                .credential-value { font-weight: 700; color: #0f172a; font-family: monospace; font-size: 16px; }
                                .btn { display: inline-block; background: #0284c7; color: #ffffff !important; text-decoration: none; padding: 12px 24px; border-radius: 8px; font-weight: 600; text-align: center; margin-top: 10px; }
                                .footer { background: #f1f5f9; padding: 20px; text-align: center; font-size: 13px; color: #64748b; border-top: 1px solid #e2e8f0; }
                            </style>
                        </head>
                        <body>
                            <div class="container">
                                <div class="header">
                                    <h1>VINFAST EV PLATFORM</h1>
                                    <p style="margin: 6px 0 0 0; opacity: 0.9; font-size: 14px;">Xác nhận đặt cọc xe máy điện & Cấp tài khoản</p>
                                </div>
                                <div class="content">
                                    <span class="badge">ĐẶT CỌC THÀNH CÔNG</span>
                                    <p>Kính gửi Quý khách <strong>%s</strong>,</p>
                                    <p>Cảm ơn Quý khách đã tin tưởng lựa chọn dòng xe máy điện <strong>%s</strong> của VinFast. Hệ thống đã tự động cấp tài khoản để Quý khách theo dõi tình trạng đơn đặt cọc và lịch bàn giao xe:</p>
                                    
                                    <div class="credentials-box">
                                        <div class="credential-item">
                                            <div class="credential-label">Tên đăng nhập (Email):</div>
                                            <div class="credential-value">%s</div>
                                        </div>
                                        <div class="credential-item" style="margin-top: 12px;">
                                            <div class="credential-label">Mật khẩu tạm thời:</div>
                                            <div class="credential-value" style="color: #0284c7;">%s</div>
                                        </div>
                                    </div>

                                    <p style="font-size: 14px; color: #64748b;">* Vì lý do bảo mật, Quý khách vui lòng đổi mật khẩu ngay sau lần đăng nhập đầu tiên.</p>
                                    
                                    <div style="text-align: center; margin-top: 24px;">
                                        <a href="http://localhost:3000/login?email=%s" class="btn">ĐĂNG NHẬP NGAY</a>
                                    </div>
                                </div>
                                <div class="footer">
                                    <p style="margin: 0;">Hotline CSKH: <strong>1900 23 23 89</strong> | Email: cskh@vinfast.vn</p>
                                    <p style="margin: 4px 0 0 0;">© 2028 VinFast Auto. All rights reserved.</p>
                                </div>
                            </div>
                        </body>
                        </html>
                        """, fullName, scooterModel, email, temporaryPassword, email);

                helper.setText(htmlContent, true);
                sender.send(mimeMessage);
                log.info(">> [HTML EMAIL SENT] Successfully sent VinFast welcome email to {}", email);
            }
        } catch (Exception e) {
            log.warn("Could not send HTML email to {}: {}. (Will fall back to local log)", email, e.getMessage());
        }
    }

    public static String generateRandomPassword(int length) {
        SecureRandom random = new SecureRandom();
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789@#$!";
        StringBuilder sb = new StringBuilder("VF@");
        for (int i = 3; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return "";
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
}
