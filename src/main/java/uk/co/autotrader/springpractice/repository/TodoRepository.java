package uk.co.autotrader.springpractice.repository;

import org.jooq.*;
import org.springframework.stereotype.Repository;
import uk.co.autotrader.springpractice.domain.CreateTodoItemRequest;
import uk.co.autotrader.springpractice.domain.TodoItem;


import java.util.ArrayList;
import java.util.List;

import static org.jooq.impl.DSL.*;
import static uk.co.autotrader.generated.Tables.*;
import static uk.co.autotrader.generated.Tables.USERS;


@Repository
public class TodoRepository {

    private final DSLContext context;

    public TodoRepository(DSLContext context) {
        this.context = context;
    }

    public boolean userExists(String username){
        return context.fetchExists(context.select().from(USERS).where(USERS.USERNAME.eq(username)));
    }

    public TodoItem getById(String username, int id) {
        return context.select().from(TODO_ITEM)
                .where(TODO_ITEM.ID.eq(context.select(USER_TODO.TODO_ID).from(USER_TODO).where(USER_TODO.USER_ID
                        .eq(username)).and(USER_TODO.TODO_ID.eq(id)).fetchOneInto(Integer.class))).fetchOneInto(TodoItem
                        .class);
    }

    public boolean deleteById(String username, int id) {
        context.deleteFrom(USER_TODO).where(USER_TODO.TODO_ID.eq(id)).and(USER_TODO.USER_ID.eq(username)).execute();
        context.deleteFrom(TODO_ITEM).where(TODO_ITEM.ID.eq(id)).execute();
        return true;
    }

    public boolean deleteAll(String username){
        List<TodoItem> todoitems = getAll(username);
        context.deleteFrom(USER_TODO).where(USER_TODO.USER_ID.eq(username)).execute();
        for (TodoItem todoItem : todoitems){
            context.deleteFrom(TODO_ITEM).where(TODO_ITEM.ID.eq(todoItem.id())).execute();
        }
        return true;
    }

    public List<TodoItem> getAll(String username){
        List<TodoItem> userItems = new ArrayList<>();
        List<Integer> todoIds =  context.select(USER_TODO.TODO_ID).from(USER_TODO).where(USER_TODO.USER_ID.eq(username)).fetchInto(Integer.class);
        for (Integer id : todoIds){
            userItems.add(context.select().from(TODO_ITEM).where(TODO_ITEM.ID.eq(id)).fetchOneInto(TodoItem.class));
        }
        return userItems;
    }

    public TodoItem add(String username, CreateTodoItemRequest todoitem) {
        TodoItem newTodoItem =  context.insertInto(TODO_ITEM)
                .set(TODO_ITEM.CONTENT, todoitem.content())
                .set(TODO_ITEM.DUE_DATE, todoitem.dueDate())
                .returningResult(TODO_ITEM.ID, TODO_ITEM.CONTENT, TODO_ITEM.DUE_DATE).fetchOneInto(TodoItem.class);
        context.insertInto(USER_TODO).set(USER_TODO.USER_ID, username).set(USER_TODO.TODO_ID, newTodoItem
                .id()).execute();
        return newTodoItem;
    }


    public TodoItem update(String username, CreateTodoItemRequest todoItem, int id) {
        return context.update(TODO_ITEM).set(TODO_ITEM.CONTENT, todoItem.content()).set(TODO_ITEM.DUE_DATE, todoItem
                .dueDate()).where(TODO_ITEM.ID.eq(id)).and(String.valueOf(username.equals(context
                .select(USER_TODO.USER_ID).from(USER_TODO).where(USER_TODO.TODO_ID.eq(id)).fetchOneInto(String.class))))
                .returningResult(TODO_ITEM.ID, TODO_ITEM.CONTENT, TODO_ITEM.DUE_DATE).fetchOneInto(TodoItem.class);
    }
}
