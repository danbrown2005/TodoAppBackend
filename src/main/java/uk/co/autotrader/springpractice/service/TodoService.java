package uk.co.autotrader.springpractice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minidev.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.co.autotrader.springpractice.domain.CreateTodoItemRequest;
import uk.co.autotrader.springpractice.domain.TodoItem;
import uk.co.autotrader.springpractice.repository.TodoRepository;

import java.time.LocalDate;
import java.util.List;

@Service
public class TodoService {

    private final TodoRepository repository;
    private final AuthService authService;

    public TodoService(TodoRepository repository, AuthService authService) {
        this.repository = repository;
        this.authService = authService;
    }


    public ResponseEntity<?> getByID(String username, int id) {
        TodoItem todoItem = repository.getById(username, id);
        if (todoItem != null) {
            return new ResponseEntity<>(new TodoItem(id, authService.decrypt(todoItem.content()), todoItem.dueDate()), HttpStatus.OK);
        }
        return new ResponseEntity<>(Messages.TODO_ITEM_NOT_FOUND.getJsonValue(), HttpStatus.NOT_FOUND);

    }

    public ResponseEntity<?> getAll(String username) {
        return ResponseEntity.status(HttpStatus.OK).body(repository.getAll(username)
                .stream()
                .map(todoItem ->
                        new TodoItem(todoItem.id(), authService.decrypt(todoItem.content()), todoItem.dueDate()))
                .toList());
    }

    public ResponseEntity<?> deleteById(String username, int id) {
        repository.deleteById(username, id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(Messages.SUCCESSFULLY_DELETED_TODO_ITEM.getJsonValue());
    }

    public ResponseEntity<?> deleteAll(String username) {
        repository.deleteAll(username);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(Messages.SUCCESSFULLY_DELETED_TODO_ITEM.getJsonValue());
    }

    public ResponseEntity<?> add(String username, CreateTodoItemRequest todoItem) {

        if (todoItem.dueDate() != null && !todoItem.content().matches("-?\\d+(\\.\\d+)?") && todoItem.content().length() > 0) {
            return ResponseEntity.status(HttpStatus.CREATED).body(new TodoItem(repository
                    .add(username, new CreateTodoItemRequest
                            (authService.encrypt(todoItem.content()), todoItem.dueDate())), todoItem.content(), todoItem.dueDate()));

        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Messages.INVALID_FIELDS_TODO_ITEM_REQUEST.getJsonValue());
    }

    public ResponseEntity<?> update(String username, CreateTodoItemRequest todoItem, int id) {
        repository.update(username, new CreateTodoItemRequest(authService.encrypt(todoItem.content()), todoItem.dueDate()), id);
        return ResponseEntity.status(HttpStatus.OK).body(new TodoItem(id, todoItem.content(), todoItem.dueDate()));
    }

    public ResponseEntity<?> complete(String username, int id) {
        repository.completeTodo(id, username);
        return ResponseEntity.status(HttpStatus.OK).body(Messages.SUCCESSFULLY_COMPLETED_TODO_ITEM.getJsonValue());
    }

    public ResponseEntity<?> getOverdue(String username) {
        List<TodoItem> overdue = authService.decryptTodoList(repository.getAll(username))
                .stream().limit(3).filter(todoItem -> todoItem.dueDate().isBefore(LocalDate.now())).toList();
        return ResponseEntity.status(HttpStatus.OK).body(overdue);
    }

    public ResponseEntity<?> getUpcoming(String username) {
        List<TodoItem> upcoming = authService.decryptTodoList(repository.getAll(username))
                .stream().limit(3)
                .filter(todoItem -> todoItem.dueDate().isEqual(LocalDate.now()) || todoItem.dueDate()
                        .isAfter(LocalDate.now())).toList();
        return ResponseEntity.status(HttpStatus.OK).body(upcoming);
    }

    public ResponseEntity<?> getCompletedCount(String username) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("Completed Task count", repository.getCompletedCount(username));
        return ResponseEntity.status(HttpStatus.OK).body(jsonObject);
    }

    public int getTotalTaskCount(String username) {
        return repository.getAll(username).size();
    }

    public ResponseEntity<?> getProgress(String username) {
        int completedTasks = repository.getCompletedCount(username);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("Percentage", (double) Math.round(((double) completedTasks / (getTotalTaskCount(username) + completedTasks)) * 100));

        return ResponseEntity.status(HttpStatus.OK).body(jsonObject);

    }

}


