package com.oem.evwarranty.domain.user;

import com.oem.evwarranty.common.config.JwtTokenProvider;
import com.oem.evwarranty.common.enums.UserRole;
import com.oem.evwarranty.domain.user.dto.AuthDTO;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final OtpVerificationRepository otpRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;

    private static final SecureRandom RANDOM = new SecureRandom();

    public AuthService(AuthenticationManager authenticationManager,
                       UserRepository userRepository,
                       RoleRepository roleRepository,
                       OtpVerificationRepository otpRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       JwtTokenProvider tokenProvider,
                       PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.otpRepository = otpRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.tokenProvider = tokenProvider;
        this.passwordEncoder = passwordEncoder;
    }

    public AuthDTO.LoginResponse authenticate(AuthDTO.LoginRequest request) {
        String identifier = request.getIdentifier();
        if (identifier.isBlank()) {
            throw new IllegalArgumentException("Username or Email is required");
        }

        // Locate user by username or email
        User user = userRepository.findByUsername(identifier)
                .or(() -> userRepository.findByEmail(identifier))
                .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getUsername(), request.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String accessToken = tokenProvider.generateToken(authentication);
        String refreshToken = createOrUpdateRefreshToken(user);

        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        List<String> roleNames = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        String primaryRole = roleNames.isEmpty() ? "CLIENT" : roleNames.get(0).replace("ROLE_", "");

        AuthDTO.UserSummary userSummary = AuthDTO.UserSummary.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(primaryRole)
                .avatar(user.getAvatarUrl() != null ? user.getAvatarUrl() : "/team/avatar-1.png")
                .roles(roleNames)
                .build();

        return AuthDTO.LoginResponse.builder()
                .accessToken(accessToken)
                .token(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(900L)
                .user(userSummary)
                .build();
    }

    public AuthDTO.RegisterResponse registerUser(AuthDTO.RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email is already registered: " + request.getEmail());
        }

        String username = request.getEmail().split("@")[0];
        if (userRepository.existsByUsername(username)) {
            username = username + "_" + System.currentTimeMillis() % 10000;
        }

        Role clientRole = roleRepository.findByName("ROLE_CLIENT")
                .or(() -> roleRepository.findByName("CLIENT"))
                .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_CLIENT").description("Client role").build()));

        User newUser = User.builder()
                .username(username)
                .email(request.getEmail())
                .fullName(request.getFullName())
                .password(passwordEncoder.encode(request.getPassword()))
                .active(true)
                .roles(new HashSet<>(Set.of(clientRole)))
                .build();

        User savedUser = userRepository.save(newUser);

        return AuthDTO.RegisterResponse.builder()
                .userId(savedUser.getId())
                .email(savedUser.getEmail())
                .fullName(savedUser.getFullName())
                .role("CLIENT")
                .build();
    }

    public AuthDTO.OtpResponse sendPasswordResetOtp(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }

        // Verify email existence
        if (!userRepository.existsByEmail(email.trim())) {
            throw new IllegalArgumentException("No account found registered with email: " + email);
        }

        // Check 30-second rate limiting
        Optional<OtpVerification> lastOtp = otpRepository.findTopByEmailOrderByCreatedAtDesc(email.trim());
        if (lastOtp.isPresent()) {
            LocalDateTime lastCreatedAt = lastOtp.get().getCreatedAt();
            if (lastCreatedAt != null && lastCreatedAt.plusSeconds(30).isAfter(LocalDateTime.now())) {
                long remainingSeconds = ChronoUnit.SECONDS.between(LocalDateTime.now(), lastCreatedAt.plusSeconds(30));
                throw new IllegalArgumentException("Please wait " + Math.max(1, remainingSeconds) + " seconds before requesting a new OTP");
            }
        }

        // Generate 6-digit OTP
        int code = 100000 + RANDOM.nextInt(900000);
        String otpCode = String.valueOf(code);

        OtpVerification verification = OtpVerification.builder()
                .email(email.trim())
                .otpCode(otpCode)
                .purpose("RESET_PASSWORD")
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .isUsed(false)
                .attemptsCount(0)
                .createdAt(LocalDateTime.now())
                .build();

        otpRepository.save(verification);

        return AuthDTO.OtpResponse.builder()
                .email(email.trim())
                .resendAvailableInSeconds(30)
                .expiresInSeconds(300)
                .build();
    }

    public void resetPasswordWithOtp(AuthDTO.ResetPasswordRequest request) {
        OtpVerification verification = otpRepository
                .findTopByEmailAndPurposeAndIsUsedFalseOrderByCreatedAtDesc(request.getEmail().trim(), "RESET_PASSWORD")
                .orElseThrow(() -> new IllegalArgumentException("No active OTP request found for email: " + request.getEmail()));

        if (verification.isExpired()) {
            throw new IllegalArgumentException("OTP code has expired. Please request a new code.");
        }

        if (verification.getAttemptsCount() >= 5) {
            throw new IllegalArgumentException("Maximum verification attempts exceeded. Please request a new OTP.");
        }

        if (!verification.getOtpCode().equals(request.getOtpCode().trim())) {
            verification.setAttemptsCount(verification.getAttemptsCount() + 1);
            otpRepository.save(verification);
            throw new IllegalArgumentException("Invalid OTP code. Please try again.");
        }

        User user = userRepository.findByEmail(request.getEmail().trim())
                .orElseThrow(() -> new IllegalArgumentException("User account not found"));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        verification.setIsUsed(true);
        otpRepository.save(verification);
    }

    public AuthDTO.UserSummary getCurrentUserProfile(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new org.springframework.security.authentication.InsufficientAuthenticationException("User not authenticated");
        }

        User user = userRepository.findByUsername(authentication.getName())
                .or(() -> userRepository.findByEmail(authentication.getName()))
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + authentication.getName()));

        List<String> roleNames = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        String primaryRole = roleNames.isEmpty() ? "CLIENT" : roleNames.get(0).replace("ROLE_", "");

        return AuthDTO.UserSummary.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(primaryRole)
                .avatar(user.getAvatarUrl() != null ? user.getAvatarUrl() : "/team/avatar-1.png")
                .roles(roleNames)
                .build();
    }

    private String createOrUpdateRefreshToken(User user) {
        String tokenString = UUID.randomUUID().toString();
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(tokenString)
                .expiryDate(Instant.now().plus(7, ChronoUnit.DAYS))
                .build();

        refreshTokenRepository.save(refreshToken);
        return tokenString;
    }
}
