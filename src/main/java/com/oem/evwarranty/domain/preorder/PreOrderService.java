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
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(email);
                message.setSubject("VinFast EV - Xác nhận đặt mua xe " + scooterModel + " & Cấp tài khoản quản trị");
                message.setText(String.format(
                        "Kính gửi Quý khách %s,\n\n" +
                        "Cảm ơn Quý khách đã đặt cọc xe máy điện %s tại VinFast!\n" +
                        "Hệ thống đã tự động khởi tạo tài khoản quản trị đơn hàng cho Quý khách:\n\n" +
                        "- Tên đăng nhập: %s\n" +
                        "- Mật khẩu tạm thời: %s\n\n" +
                        "Vui lòng đăng nhập tại ứng dụng VinFast để theo dõi tiến độ giao xe.\n\n" +
                        "Trân trọng,\nVinFast Customer Service",
                        fullName, scooterModel, email, temporaryPassword
                ));
                mailSender.get().send(message);
            }
            log.info(">> [NOTIFICATION] Welcome email sent to {} for preorder {} (Temp password: {})", email, scooterModel, temporaryPassword);
        } catch (Exception e) {
            log.warn("Could not send email notification to {}: {}", email, e.getMessage());
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
