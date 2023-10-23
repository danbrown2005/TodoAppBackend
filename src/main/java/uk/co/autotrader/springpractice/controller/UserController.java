package uk.co.autotrader.springpractice.controller;
import io.jsonwebtoken.Claims;
import uk.co.autotrader.springpractice.auth.AuthManager;
import org.springframework.web.bind.annotation.*;
import uk.co.autotrader.springpractice.domain.Token;
import uk.co.autotrader.springpractice.domain.User;
import uk.co.autotrader.springpractice.domain.Username;
import uk.co.autotrader.springpractice.service.JwtTokenVerificationService;
import uk.co.autotrader.springpractice.service.UserService;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

@RestController
public class UserController {

    private final UserService userService;
    private final AuthManager authManager;
    private final JwtTokenVerificationService jwtService;
    public UserController(UserService userService, AuthManager authManager, JwtTokenVerificationService jwtService){
        this.userService = userService;
        this.authManager = authManager;
        this.jwtService = jwtService;
    }


    @PostMapping(value="/user/registration")
    public void addUser(@RequestBody User user){
         userService.addUser(user);
    }

    @PostMapping(value="/user/validate")
    public String userLogin(@RequestBody User user){
        // returns token if logged in or null if unable to login
        return userService.validateUser(user);
    }

    @PostMapping(value="/user/validate/token")
    public boolean validateToken(@RequestBody Token token){
        // returns token if logged in or null if unable to login
        return jwtService.verifyToken(token.token());
    }

    @PostMapping(value="/user/validate/username")
    public boolean usernameExists(@RequestBody Username username){
        return userService.usernameExists(username.username());
    }

    @GetMapping(value="/progress")
    public double getUserProgress(@RequestHeader("authorization") String token){
       if (jwtService.verifyToken(token)){
           if (!userService.usernameExists(jwtService.getUsername(token))){return 0;}
           return userService.getTaskCompletedPercentage(jwtService.getUsername(token));
       }
       return 0.0;
    }

    @GetMapping(value="/completed")
    public int getCompletedTaskCount(@RequestHeader("authorization") String token){
        if (jwtService.verifyToken(token)){
            if (!userService.usernameExists(jwtService.getUsername(token))){return 0;}
            return userService.getCompletedTaskCount(jwtService.getUsername(token));
        }
        return 0;
    }

    @GetMapping(value="/username")
    public String getUsername(@RequestHeader("authorization") String token){
        if (jwtService.verifyToken(token)){
            return jwtService.getUsername(token);
        }
        return "";
    }



}
