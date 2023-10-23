package uk.co.autotrader.springpractice.service;

import uk.co.autotrader.springpractice.auth.AuthManager;
import uk.co.autotrader.springpractice.auth.Encryptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import uk.co.autotrader.springpractice.domain.TodoItem;
import uk.co.autotrader.springpractice.domain.User;
import uk.co.autotrader.springpractice.repository.TodoRepository;
import uk.co.autotrader.springpractice.repository.UserRepository;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {
    private final Encryptor encryptor;
    private final AuthManager authManager;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(4);
    private final UserRepository repository;
    private final TodoRepository todoRepository;
    public UserService(UserRepository repository, TodoRepository todoRepository, Encryptor encryptor, AuthManager authManager) {
        this.repository = repository;
        this.todoRepository = todoRepository;
        this.encryptor = encryptor;
        this.authManager = authManager;
    }

    public String addUser(User user) {
        if (!repository.userExists(user.username())) {
            if (user.username().length() > 0 && user.password().length() > 0){
            User hashedUser = new User(user.username(), this.encoder.encode(user.password()));
            repository.addUser(hashedUser);
                try {
                    return authManager.generateToken(user.username());
                } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                    return null;
                }
            }
        }
        return null;
    }

    public String validateUser(User user) {
        if(repository.userExists(user.username()) && encoder.matches(user.password(), repository.getPassword(user))){
            try {
                return authManager.generateToken(user.username());
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    public boolean usernameExists(String username){
        return repository.usernameExists(username);
    }

    public int getCompletedTaskCount(String username){
        return repository.getCompletedTasks(username);
    }

    public boolean completeTodo(int taskId, String username){
        return this.repository.completeTodo(taskId, username);
    }

    public int getTotalTaskCount(String username){
        return this.todoRepository.getAll(username).size();
    }

    public double getTaskCompletedPercentage(String username){
        int completedTasks = getCompletedTaskCount(username);
        int totalTasks = getTotalTaskCount(username) + completedTasks;
        double division = (double) completedTasks / totalTasks;
        return (double) Math.round(division * 100);


    }

    public List<TodoItem> getOverdueTasks(String username){
        List<TodoItem> todoItems = this.todoRepository.getAll(username);
        List<TodoItem> overdue = new ArrayList<>();
        List<TodoItem> decryptedTodoItem = encryptor.decryptTodoList(todoItems);
        for (TodoItem todoItem : decryptedTodoItem){
            if (todoItem.dueDate().isBefore(LocalDate.now())) {
                overdue.add(todoItem);
                if (overdue.size() >= 3){break;}
            }
        }

        return overdue;
    }

    public List<TodoItem> getUpcomingTasks(String username){
        List<TodoItem> todoItems = this.todoRepository.getAll(username);
        List<TodoItem> upcoming = new ArrayList<>();
        List<TodoItem> decryptedTodoItem = encryptor.decryptTodoList(todoItems);
        for (TodoItem todoItem : decryptedTodoItem){
            if (todoItem.dueDate().isEqual(LocalDate.now()) || todoItem.dueDate().isAfter(LocalDate.now())) {
                upcoming.add(todoItem);
                if (upcoming.size() >= 3){break;}
            }
        }
        return upcoming;
    }

}
