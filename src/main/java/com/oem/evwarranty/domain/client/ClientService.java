package com.oem.evwarranty.domain.client;

import com.oem.evwarranty.common.enums.UserRole;
import com.oem.evwarranty.domain.user.Role;
import com.oem.evwarranty.domain.user.RoleRepository;
import com.oem.evwarranty.domain.user.User;
import com.oem.evwarranty.domain.user.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
public class ClientService {

    private final ClientProfileRepository clientProfileRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public ClientService(ClientProfileRepository clientProfileRepository,
                         UserRepository userRepository,
                         RoleRepository roleRepository,
                         PasswordEncoder passwordEncoder) {
        this.clientProfileRepository = clientProfileRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Page<ClientDTO.ClientResponse> getClients(String query, Pageable pageable) {
        return clientProfileRepository.searchClients(query, pageable)
                .map(this::toResponse);
    }

    public Optional<ClientDTO.ClientResponse> getClientById(Long id) {
        return clientProfileRepository.findById(id)
                .map(this::toResponse);
    }

    public ClientDTO.ClientResponse createClient(ClientDTO.CreateClientRequest request) {
        String username = request.getEmail().split("@")[0] + "_" + (System.currentTimeMillis() % 10000);
        
        Role clientRole = roleRepository.findByName(UserRole.CLIENT.name())
                .orElseGet(() -> roleRepository.save(Role.builder().name(UserRole.CLIENT.name()).description("Client Role").build()));

        User user = User.builder()
                .username(username)
                .email(request.getEmail())
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .avatarUrl(request.getAvatarUrl() != null ? request.getAvatarUrl() : "/team/avatar-1.png")
                .password(passwordEncoder.encode("Client@" + (System.currentTimeMillis() % 10000)))
                .roles(Set.of(clientRole))
                .active(true)
                .build();

        User savedUser = userRepository.save(user);

        ClientProfile profile = ClientProfile.builder()
                .user(savedUser)
                .residenceCardNumber(request.getResidenceCardNumber())
                .driverLicenseNumber(request.getDriverLicenseNumber())
                .address(request.getAddress() != null ? request.getAddress() : "Ho Chi Minh City")
                .points(100)
                .totalSpent(BigDecimal.ZERO)
                .totalBookings(0)
                .status("ACTIVE")
                .build();

        ClientProfile savedProfile = clientProfileRepository.save(profile);
        return toResponse(savedProfile);
    }

    public ClientDTO.ClientResponse toResponse(ClientProfile cp) {
        if (cp == null) return null;

        String formattedId = "CL-" + String.format("%03d", cp.getId());
        String fullName = cp.getUser() != null ? cp.getUser().getFullName() : "Alice Johnson";
        String email = cp.getUser() != null ? cp.getUser().getEmail() : "client@vinfast.vn";
        String phone = cp.getUser() != null ? cp.getUser().getPhone() : "+84 901 234 567";
        String avatarUrl = (cp.getUser() != null && cp.getUser().getAvatarUrl() != null) ? cp.getUser().getAvatarUrl() : "/team/avatar-1.png";
        String totalSpentStr = cp.getTotalSpent() != null ? "$" + String.format("%,.2f", cp.getTotalSpent()) : "$0.00";

        return ClientDTO.ClientResponse.builder()
                .id(formattedId)
                .numericId(cp.getId())
                .fullName(fullName)
                .email(email)
                .phone(phone)
                .address(cp.getAddress() != null ? cp.getAddress() : "Ho Chi Minh City")
                .totalBookings(cp.getTotalBookings() != null ? cp.getTotalBookings() : 0)
                .totalSpent(totalSpentStr)
                .totalSpentValue(cp.getTotalSpent() != null ? cp.getTotalSpent() : BigDecimal.ZERO)
                .points(cp.getPoints() != null ? cp.getPoints() : 0)
                .status(cp.getStatus() != null ? capitalize(cp.getStatus()) : "Active")
                .avatarUrl(avatarUrl)
                .residenceCardNumber(cp.getResidenceCardNumber())
                .driverLicenseNumber(cp.getDriverLicenseNumber())
                .build();
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return "";
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
}
