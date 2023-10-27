package uk.co.autotrader.springpractice.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.stereotype.Service;
import uk.co.autotrader.springpractice.domain.TodoItem;

import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class AuthService {

    private final String secretKey;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(4);

    public AuthService(@Value("${secretkey}") String key) {
        this.secretKey = key;
    }

    public String hash(String text) {
        return encoder.encode(text);
    }

    public boolean matches(String text, String hashedText) {
        return encoder.matches(text, hashedText);
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

    public String generateToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        return Jwts.builder().setClaims(claims).setSubject(username).setIssuedAt(new Date(System.currentTimeMillis())).
                setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 30)).
                signWith(SignatureAlgorithm.HS256, this.secretKey).compact();
    }

    public String getUsernameFromToken(String token) {
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

    public String encrypt(String text) {
        return String.valueOf(Hex.encode(text.getBytes(StandardCharsets.UTF_8)));
    }

    public String decrypt(String encryptedText) {
        return new String(Hex.decode(encryptedText), StandardCharsets.UTF_8);
    }

    public List<TodoItem> decryptTodoList(List<TodoItem> todoItems) {
        List<TodoItem> unencryptedTodoItems = new ArrayList<>();
        for (TodoItem todoItem : todoItems) {
            unencryptedTodoItems.add(new TodoItem(todoItem.id(), this.decrypt(todoItem.content()), todoItem.dueDate()));
        }
        return unencryptedTodoItems;
    }
}
