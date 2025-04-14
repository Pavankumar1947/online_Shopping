package filters;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import model.User;
import repository.UserRepository;
import util.JwtUtil;


@Slf4j
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepo;

    public JwtFilter(JwtUtil jwtUtil, UserRepository userRepo) {
        this.jwtUtil = jwtUtil;
        this.userRepo = userRepo;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String jwt = authHeader.substring(7);
            String email = jwtUtil.extractUsername(jwt);

            if (email != null) {
                log.info("JWT token found for user: {}", email);

                if (SecurityContextHolder.getContext().getAuthentication() == null) {
                    try {
                        User user = userRepo.findByEmail(email).orElse(null);

                        if (user != null && jwtUtil.validateToken(jwt, new org.springframework.security.core.userdetails.User(
                                user.getEmail(), user.getPassword(), List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole()))))) {

                            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                    user.getEmail(), null,
                                    List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole())));

                            SecurityContextHolder.getContext().setAuthentication(authToken);
                            log.info("User authenticated and security context set for: {}", email);
                        } else {
                            log.warn("Invalid token or user not found for email: {}", email);
                        }
                    } catch (Exception e) {
                        log.error("Error during JWT validation for user: {}", email, e);
                    }
                }
            }
        } else {
            log.warn("Authorization header not found or malformed.");
        }
        filterChain.doFilter(request, response);
    }
}

