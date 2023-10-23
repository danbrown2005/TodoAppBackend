package uk.co.autotrader.springpractice.auth;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.co.autotrader.springpractice.domain.User;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


@Component
public class AuthManager {

    private final String secretkey;

    public AuthManager(@Value("${secretkey}") String key){

        this.secretkey = key;
    }
//        String secretkey = System.getenv("SECRETKEY");


    public String generateToken(String username) throws NoSuchAlgorithmException, InvalidKeySpecException {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, username);
    }

    public String createToken(Map<String, Object> claims, String username) throws NoSuchAlgorithmException, InvalidKeySpecException {
//        String secretkey = System.getenv("SECRETKEY");
        return Jwts.builder().setClaims(claims).setSubject(username).setIssuedAt(new Date(System.currentTimeMillis())).
                setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 30)).
                signWith(SignatureAlgorithm.HS256, this.secretkey).compact();
    }




}
