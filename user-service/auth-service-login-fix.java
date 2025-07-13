// Add this to your auth-service AuthController

@PostMapping("/login")
public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
    try {
        // Authenticate user
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginRequest.getEmail(),
                loginRequest.getPassword()
            )
        );

        // Get user details
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        
        // Find user in database
        Utilisateur user = utilisateurRepository.findByEmail(loginRequest.getEmail())
            .orElseThrow(() -> new RuntimeException("User not found"));

        // Generate JWT token
        String token = jwtUtil.generateToken(userDetails.getUsername(), user.getRole().name());

        // Return response
        LoginResponse response = LoginResponse.builder()
            .token(token)
            .user(UserDto.builder()
                .id(user.getId())
                .nom(user.getNom())
                .prenom(user.getPrenom())
                .email(user.getEmail())
                .role(user.getRole())
                .build())
            .build();

        return ResponseEntity.ok(response);

    } catch (BadCredentialsException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(new ErrorResponse("Invalid email or password"));
    }
}

// Add these DTOs
@Data
@Builder
public class LoginRequest {
    private String email;
    private String password;
}

@Data
@Builder
public class LoginResponse {
    private String token;
    private UserDto user;
}

@Data
@Builder
public class UserDto {
    private UUID id;
    private String nom;
    private String prenom;
    private String email;
    private Role role;
}

// Update registration to support all roles (ADMIN only)
@PostMapping("/register")
public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
    // Check if user is ADMIN (for creating MANAGER/TECHNICIEN)
    if (request.getRole() != Role.UTILISATEUR) {
        // Validate that current user is ADMIN
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !hasRole(auth, "ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse("Only ADMIN can create MANAGER/TECHNICIEN users"));
        }
    }

    // Rest of registration logic...
}
