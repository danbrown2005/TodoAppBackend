package uk.co.autotrader.springpractice.service;

import org.springframework.stereotype.Service;
import uk.co.autotrader.springpractice.domain.User;
import uk.co.autotrader.springpractice.repository.TodoRepository;
import uk.co.autotrader.springpractice.repository.UserRepository;

@Service
public class UserService {
    private final AuthService authService;
    private final UserRepository repository;
    public UserService(UserRepository repository, AuthService authService) {
        this.repository = repository;
        this.authService = authService;
    }

    public String addUser(User user) {
        if (!repository.userExists(user.username())) {
            if (user.username().length() > 0 && user.password().length() > 0){
            User hashedUser = new User(user.username(), authService.hash(user.password()));
            repository.addUser(hashedUser);
                return authService.generateToken(user.username());
            }
        }
        return null;
    }

    public String validateUser(User user) {
        if(repository.userExists(user.username()) && authService.matches(user.password(), repository.getPassword(user))){
            try {
                return authService.generateToken(user.username());
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    public boolean usernameExists(String username){
        return repository.usernameExists(username);
    }



}
