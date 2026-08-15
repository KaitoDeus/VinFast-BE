package com.oem.evwarranty.domain.user;

import com.oem.evwarranty.common.dto.ApiResponse;
import com.oem.evwarranty.domain.user.dto.AuthDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Authentication (Login, Register, Forgot Password, Reset Password, Active Profile).
 * Complies 100% with BACKEND_JAVA_SPECIFICATION.md for VinFast EV Platform.
 */
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Các API đăng nhập, đăng ký và đặt lại mật khẩu OTP")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(summary = "Đăng nhập hệ thống", description = "Xác thực email/username và mật khẩu, trả về Access Token và Refresh Token")
    public ResponseEntity<ApiResponse<AuthDTO.LoginResponse>> login(@Valid @RequestBody AuthDTO.LoginRequest request) {
        AuthDTO.LoginResponse response = authService.authenticate(request);
        return ResponseEntity.ok(ApiResponse.success("Đăng nhập thành công", response));
    }

    @PostMapping("/register")
    @Operation(summary = "Đăng ký tài khoản người dùng mới", description = "Tạo tài khoản khách hàng mới với quyền CLIENT")
    public ResponseEntity<ApiResponse<AuthDTO.RegisterResponse>> register(@Valid @RequestBody AuthDTO.RegisterRequest request) {
        AuthDTO.RegisterResponse response = authService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Đăng ký tài khoản thành công", response));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Yêu cầu mã OTP 6 số để đặt lại mật khẩu qua email", description = "Gửi mã OTP 6 số qua email với thời hạn 5 phút và giới hạn 30s gửi lại")
    public ResponseEntity<ApiResponse<AuthDTO.OtpResponse>> forgotPassword(@Valid @RequestBody AuthDTO.ForgotPasswordRequest request) {
        AuthDTO.OtpResponse response = authService.sendPasswordResetOtp(request.getEmail());
        return ResponseEntity.ok(ApiResponse.success("Mã xác thực OTP đã được gửi đến email của bạn", response));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Xác thực mã OTP và thiết lập mật khẩu mới", description = "Kiểm tra mã OTP và cập nhật mật khẩu mới cho người dùng")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody AuthDTO.ResetPasswordRequest request) {
        authService.resetPasswordWithOtp(request);
        return ResponseEntity.ok(ApiResponse.success("Mật khẩu của bạn đã được cập nhật thành công", null));
    }

    @GetMapping("/me")
    @Operation(summary = "Lấy thông tin tài khoản đang đăng nhập", description = "Trả về hồ sơ và quyền hạn của người dùng từ Access Token")
    public ResponseEntity<ApiResponse<AuthDTO.UserSummary>> getCurrentUser(Authentication authentication) {
        AuthDTO.UserSummary profile = authService.getCurrentUserProfile(authentication);
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    @PostMapping("/logout")
    @Operation(summary = "Đăng xuất hệ thống", description = "Hủy phiên đăng nhập của người dùng")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        if (authentication != null) {
            new SecurityContextLogoutHandler().logout(request, response, authentication);
        }
        return ResponseEntity.ok(ApiResponse.success("Đăng xuất thành công", null));
    }
}
