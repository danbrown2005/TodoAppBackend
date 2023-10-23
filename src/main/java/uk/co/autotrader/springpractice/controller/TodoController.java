package uk.co.autotrader.springpractice.controller;

import org.springframework.web.bind.annotation.*;
import uk.co.autotrader.springpractice.domain.CreateTodoItemRequest;
import uk.co.autotrader.springpractice.domain.TodoItem;
import uk.co.autotrader.springpractice.service.JwtTokenVerificationService;
import uk.co.autotrader.springpractice.service.TodoService;
import uk.co.autotrader.springpractice.service.UserService;

import java.util.List;

// TODO ADD PROPER HTTP RETURN TYPES

@RestController
public class TodoController {

    private final TodoService todoService;
    private final UserService userService;
    private final JwtTokenVerificationService jwtService;

    public TodoController(TodoService todoService, UserService userService, JwtTokenVerificationService jwtService) {
        this.todoService = todoService;
        this.userService = userService;
        this.jwtService = jwtService;
    }

    // updated
    @GetMapping(value = "/todo/{id}")
    public TodoItem getById(@PathVariable("id") int id, @RequestHeader("authorization") String token) {
        if (jwtService.verifyToken(token)) {
            return todoService.getByID(jwtService.getUsername(token), id);
        }
        return null;
    }

    // updated
    @GetMapping(value = "/todo")
    public List<TodoItem> getAll(@RequestHeader("authorization") String token) {
        if (jwtService.verifyToken(token)) {
            return todoService.getAll(jwtService.getUsername(token));
        }
        return null;
    }

    @GetMapping(value = "/todo/overdue")
    public List<TodoItem> getOverdue(@RequestHeader("authorization") String token) {
        if (jwtService.verifyToken(token)) {
            return userService.getOverdueTasks(jwtService.getUsername(token));
        }
        return null;
    }

    @GetMapping(value = "/todo/upcoming")
    public List<TodoItem> getUpcoming(@RequestHeader("authorization") String token) {
        if (jwtService.verifyToken(token)) {
            return userService.getUpcomingTasks(jwtService.getUsername(token));
        }
        return null;
    }

    // updated
    @PostMapping(value = "/todo")
    public TodoItem add(@RequestBody CreateTodoItemRequest todo, @RequestHeader("authorization") String token) {
        if (jwtService.verifyToken(token)) {
            TodoItem s =  todoService.add(jwtService.getUsername(token), todo);
            System.out.println(s);
            return s;
        }
        return null;
    }

    @PostMapping(value = "/todo/complete/{id}")
    public void completeTodo(@PathVariable("id") int id, @RequestHeader("authorization") String token) {
        if (jwtService.verifyToken(token)) {
            todoService.completeTodo(jwtService.getUsername(token), id);
        }
    }

    //updated
    @PutMapping(value = "/todo/{id}")
    public TodoItem put(@RequestBody CreateTodoItemRequest todo, @PathVariable("id") int id, @RequestHeader("authorization") String token) {
        if (jwtService.verifyToken(token)) {
            return todoService.update(jwtService.getUsername(token), todo, id);
        }
        return null;
    }

    //updated
    @DeleteMapping(value = "/todo/{id}")
    public boolean deleteById(@PathVariable("id") int id, @RequestHeader("authorization") String token) {
        if (jwtService.verifyToken(token)) {
            return todoService.deleteById(jwtService.getUsername(token), id);
        }
        return false;
    }

    //updated
    @DeleteMapping(value = "/todo")
    public boolean deleteAll(@RequestHeader("authorization") String token) {
        if (jwtService.verifyToken(token)) {
            return todoService.deleteAll(jwtService.getUsername(token));
        }
        return false;
    }

}





