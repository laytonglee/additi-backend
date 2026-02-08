package groupproject.additibackend.service;

import groupproject.additibackend.model.RefreshToken;
import org.springframework.stereotype.Service;

@Service
public interface RefreshTokenService {
    RefreshToken verify (String token);
    void revoke(String token);
}
