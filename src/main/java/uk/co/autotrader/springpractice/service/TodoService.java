package uk.co.autotrader.springpractice.service;

import uk.co.autotrader.springpractice.auth.Encryptor;
import org.springframework.stereotype.Service;
import uk.co.autotrader.springpractice.domain.CreateTodoItemRequest;
import uk.co.autotrader.springpractice.domain.TodoItem;
import uk.co.autotrader.springpractice.repository.TodoRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class TodoService {
    private final Encryptor encryptor;
    private final UserService userService;
    private final TodoRepository repository;

    public TodoService(TodoRepository repository, UserService userService, Encryptor encryptor) {
        this.repository = repository;
        this.encryptor = encryptor;
        this.userService = userService;
    }

    public TodoItem getByID(String username, int id) {
        if (repository.userExists(username)) {
            TodoItem encryptedTodoItem =  repository.getById(username, id);
            return new TodoItem(encryptedTodoItem.id(), encryptor.decrypt(encryptedTodoItem.content()), encryptedTodoItem.dueDate());
        }
        return new TodoItem(id, "Invalid username", LocalDate.now());
    }

    public List<TodoItem> getAll(String username) {
        if (repository.userExists(username)) {
            List<TodoItem> todoItems = repository.getAll(username);
            List<TodoItem> unencryptedTodoItems = new ArrayList<>();
            for (TodoItem todoItem : todoItems){
                unencryptedTodoItems.add(new TodoItem(todoItem.id(), encryptor.decrypt(todoItem.content()), todoItem.dueDate()));
            }
            return unencryptedTodoItems;
        }
        return Collections.emptyList();
    }

    public boolean deleteById(String username, int id) {
        if (repository.userExists(username)) {
            return repository.deleteById(username, id);
        }
        return false;
    }

    public boolean deleteAll(String username) {
        if (repository.userExists(username)) {
            return repository.deleteAll(username);
        }
        return false;
    }

    public TodoItem add(String username, CreateTodoItemRequest todoItem) {
        CreateTodoItemRequest encryptedTodoItem = new CreateTodoItemRequest(encryptor.encrypt(todoItem.content()), todoItem.dueDate());
        if (repository.userExists(username) && todoItem.dueDate() != null) {
            if (!todoItem.content().matches("-?\\d+(\\.\\d+)?") && todoItem.content().length() > 0) {
                TodoItem encryptedTodo = repository.add(username, encryptedTodoItem);
                return new TodoItem(encryptedTodo.id(), encryptor.decrypt(encryptedTodo.content()), encryptedTodo.dueDate());
            }
        }
        return new TodoItem(0, "invalid", todoItem.dueDate());
    }

    public TodoItem update(String username, CreateTodoItemRequest todoItem, int id) {
        CreateTodoItemRequest encryptedTodoItem = new CreateTodoItemRequest(encryptor.encrypt(todoItem.content()), todoItem.dueDate());
        if (repository.userExists(username)) {
            TodoItem encryptedTodo = repository.update(username, encryptedTodoItem, id);
            return new TodoItem(encryptedTodo.id(), encryptor.decrypt(encryptedTodo.content()), encryptedTodo.dueDate());
        }
        return new TodoItem(0, "Invalid username", todoItem.dueDate());
    }

    public void completeTodo(String username, int id){
        userService.completeTodo(id, username);
    }

}


