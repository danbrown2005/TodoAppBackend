package uk.co.autotrader.springpractice.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class JwtTokenVerificationService {

    private final String secretKey;

    public JwtTokenVerificationService(@Value("${secretkey}") String key){
        this.secretKey = key;
    }

    public boolean verifyToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(secretKey)
                    .parseClaimsJws(token)
                    .getBody();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String getUsername(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(secretKey)
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getSubject();
        } catch (Exception e) {
            return null;
        }
    }

    public Claims verifyTokenAndReturnClaims(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(secretKey)
                    .parseClaimsJws(token)
                    .getBody();
            return claims;
        } catch (Exception e) {
            return null;
        }
    }
}
