package uk.co.autotrader.springpractice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.co.autotrader.springpractice.domain.CreateTodoItemRequest;
import uk.co.autotrader.springpractice.service.TodoService;

@RestController
@RequestMapping("/todo")
public class TodoController {

    private final TodoService todoService;

    public TodoController(TodoService todoService) {
        this.todoService = todoService;
    }

    @GetMapping()
    public ResponseEntity<?> getAll(@RequestAttribute("username") String username) {
        return todoService.getAll(username);
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<?> getByID(@RequestAttribute("username") String username, @PathVariable("id") int id) {
            return todoService.getByID(username, id);
    }

    @GetMapping(value = "/overdue")
    public ResponseEntity<?> getOverdue(@RequestAttribute("username") String username) {
            return todoService.getOverdue(username);
    }

    @GetMapping(value = "/upcoming")
    public ResponseEntity<?> getUpcoming(@RequestAttribute("username") String username) {
            return todoService.getUpcoming(username);
    }

    @GetMapping(value="/progress")
    public ResponseEntity<?> getProgress(@RequestAttribute("username") String username){
            return todoService.getProgress(username);
    }

    @GetMapping(value="/completed")
    public ResponseEntity<?> getCompletedCount(@RequestAttribute("username") String username){
            return todoService.getCompletedCount(username);
    }

    @PostMapping()
    public ResponseEntity<?> add(@RequestBody CreateTodoItemRequest todo, @RequestAttribute("username") String username) {
            return todoService.add(username, todo);
    }

    @PostMapping(value = "/complete/{id}")
    public ResponseEntity<?> complete(@PathVariable("id") int id, @RequestAttribute("username") String username) {
            return todoService.complete(username, id);
    }

    @PutMapping(value = "/{id}")
    public ResponseEntity<?> update(@RequestBody CreateTodoItemRequest todo, @PathVariable("id") int id,@RequestAttribute("username") String username) {
            return todoService.update(username, todo, id);
        }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<?> deleteById(@PathVariable("id") int id, @RequestAttribute("username") String username) {
            return todoService.deleteById(username, id);
    }

    @DeleteMapping()
    public ResponseEntity<?> deleteAll(@RequestAttribute("username") String username) {
            return todoService.deleteAll(username);
    }

}





