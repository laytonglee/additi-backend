package groupproject.additibackend.service;

import groupproject.additibackend.model.User;
import io.jsonwebtoken.Claims;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import java.util.function.Function;

@Service
public interface JwtService {
    String generateAccessToken(User user);
    String extractUserName(String token);
    <T> T extractClaim(String token, Function<Claims, T> claimsResolver);
    boolean validateToken(String token, UserDetails userDetails) ;
    String getRefreshToken(User user);
}
