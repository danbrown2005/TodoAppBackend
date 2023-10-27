package uk.co.autotrader.springpractice.controller;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.co.autotrader.springpractice.domain.User;
import uk.co.autotrader.springpractice.service.AuthService;
import uk.co.autotrader.springpractice.service.UserService;


@RestController
public class UserController {

    private final UserService userService;
    private final AuthService jwtService;
    public UserController(UserService userService, AuthService jwtService){
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @PostMapping(value="/user/register")
    public ResponseEntity<?> addUser(@RequestBody User user){
        userService.addUser(user);
        return ResponseEntity.status(HttpStatus.OK).body(userLogin(user));
    }

    @PostMapping(value="/user/login")
    public String userLogin(@RequestBody User user){
        return userService.validateUser(user);
    }

    @PostMapping(value="/user/validate/token")
    public HttpStatus validateToken(){
        return HttpStatus.OK;
    }

    @GetMapping(value="/username")
    public String getUsername(@RequestHeader("authorization") String token){
            return jwtService.getUsernameFromToken(token);

    }
}
