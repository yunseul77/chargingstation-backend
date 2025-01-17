package elice.chargingstationbackend.user;

import elice.chargingstationbackend.user.security.JwtUtil;
import elice.chargingstationbackend.user.service.CustomUserDetailsService;
import elice.chargingstationbackend.user.service.RefreshTokenService;
import elice.chargingstationbackend.user.service.UserService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final BCryptPasswordEncoder passwordEncoder;
    private final CustomUserDetailsService customUserDetailsService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenService refreshTokenService;
    private final JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody UserDto userDto) {
        try {
            // 사용자 타입에 따라 역할 설정
            if ("business".equalsIgnoreCase(userDto.getUserType())) {
                userDto.setRoles(Collections.singleton(Role.ROLE_BUSINESS));
            } else {
                userDto.setRoles(Collections.singleton(Role.ROLE_USER));
            }

            userService.createUser(userDto);
            return ResponseEntity.ok(Collections.singletonMap("message", "회원 가입이 완료되었습니다."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Collections.singletonMap("reason", e.getMessage()));
        }
    }


    @GetMapping("/info")
    public ResponseEntity<UserDto> getUserInfo(@RequestHeader("Authorization") String token) {
        if (token != null && token.startsWith("Bearer ")) {
            String jwt = token.substring(7);
            try {
                String email = jwtUtil.getEmailFromToken(jwt);
                if (email != null) {
                    Optional<User> optionalUser = userRepository.findByEmail(email);
                    if (optionalUser.isPresent()) {
                        User user = optionalUser.get();
                        UserDto userDTO = new UserDto();
                        userDTO.setId(user.getId());
                        userDTO.setEmail(user.getEmail());
                        userDTO.setUsername(user.getUsername());
                        userDTO.setAddress(user.getAddress());
                        userDTO.setPhoneNumber(user.getPhoneNumber());
                        userDTO.setConnectorType(user.getConnectorType());
                        userDTO.setRoles(user.getRoles());
                        return ResponseEntity.ok(userDTO);
                    } else {
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
                    }
                }
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @PostMapping("/login")
    @ResponseBody
    public ResponseEntity<?> login(@RequestBody Map<String, String> userData, HttpServletResponse response) {
        String email = userData.get("email");
        String password = userData.get("password");

        try {
            Authentication auth = userService.authenticate(email, password);
            Object principal = auth.getPrincipal();

            if (principal instanceof CustomUser customUser) {

                User user = userRepository.findByEmail(customUser.getUsername())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + customUser.getUsername()));

                String accessToken = JwtUtil.createAccessToken(auth);

                RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

                Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken.getToken());
                refreshTokenCookie.setMaxAge(7 * 24 * 60 * 60);
                refreshTokenCookie.setHttpOnly(true);
                refreshTokenCookie.setPath("/");
                response.addCookie(refreshTokenCookie);

                Map<String, Object> responseData = new HashMap<>();
                responseData.put("message", "로그인되었습니다.");
                responseData.put("accessToken", accessToken);

                return ResponseEntity.ok(responseData);
            } else {
                throw new IllegalArgumentException("Unexpected principal type");
            }
        } catch (UsernameNotFoundException | IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "잘못된 이메일 또는 비밀번호입니다.");
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/token/refresh")
    public ResponseEntity<?> refreshToken(HttpServletRequest request) {
        String refreshToken = Arrays.stream(request.getCookies())
            .filter(cookie -> "refreshToken".equals(cookie.getName()))
            .findFirst()
            .map(Cookie::getValue)
            .orElse(null);

        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Refresh Token이 없습니다.");
        }

        try {
            Claims claims = JwtUtil.extractToken(refreshToken);
            String username = claims.get("username", String.class);

            UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);
            Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

            String newAccessToken = JwtUtil.createAccessToken(auth);

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("accessToken", newAccessToken);

            return ResponseEntity.ok(responseData);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("유효하지 않은 Refresh Token입니다.");
        }
    }

    @PostMapping("/logout")
    @ResponseBody
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }

        Cookie refreshTokenCookie = new Cookie("refreshToken", null);
        refreshTokenCookie.setMaxAge(0);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setPath("/");
        response.addCookie(refreshTokenCookie);

        String refreshToken = refreshTokenService.getRefreshTokenFromRequest(request);
        if (refreshToken != null) {
            refreshTokenRepository.deleteByToken(refreshToken);
        }

        Map<String, String> responseData = new HashMap<>();
        responseData.put("message", "로그아웃되었습니다.");

        return ResponseEntity.ok(responseData);
    }

    @PutMapping("/mypage/update")
    public ResponseEntity<UserDto> updateUser(@RequestBody UserDto userDTO, @RequestHeader("Authorization") String token) {
        if (token != null && token.startsWith("Bearer ")) {
            String jwt = token.substring(7);
            try {
                String email = jwtUtil.getEmailFromToken(jwt);
                if (email != null) {
                    User updatedUser = userService.updateUser(email, userDTO);
                    return ResponseEntity.ok(userDTO);
                }
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @DeleteMapping("/mypage/delete")
    public ResponseEntity<Void> deleteUser(@RequestHeader("Authorization") String token) {
        if (token != null && token.startsWith("Bearer ")) {
            String jwt = token.substring(7);
            try {
                String email = jwtUtil.getEmailFromToken(jwt);
                if (email != null) {
                    userService.deleteUser(email);
                    return ResponseEntity.noContent().build();
                }
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @GetMapping("/list")
    public ResponseEntity<List<UserDto>> getUserList() {
        List<UserDto> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }
}
