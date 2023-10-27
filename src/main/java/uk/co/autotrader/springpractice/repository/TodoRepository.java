package uk.co.autotrader.springpractice.repository;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;
import uk.co.autotrader.springpractice.domain.CreateTodoItemRequest;
import uk.co.autotrader.springpractice.domain.TodoItem;

import java.util.ArrayList;
import java.util.List;

import static uk.co.autotrader.generated.Tables.*;


@Repository
public class TodoRepository {

    private final DSLContext context;

    public TodoRepository(DSLContext context) {
        this.context = context;
    }

    public TodoItem getById(String username, int id) {
        return context.select().from(TODO_ITEM)
                .where(TODO_ITEM.ID.eq(context.select(USER_TODO.TODO_ID).from(USER_TODO).where(USER_TODO.USER_ID
                        .eq(username)).and(USER_TODO.TODO_ID.eq(id)).fetchOneInto(Integer.class))).fetchOneInto(TodoItem
                        .class);
    }

    public void deleteById(String username, int id) {
        context.deleteFrom(USER_TODO).where(USER_TODO.TODO_ID.eq(id)).and(USER_TODO.USER_ID.eq(username)).execute();
        context.deleteFrom(TODO_ITEM).where(TODO_ITEM.ID.eq(id)).execute();
    }

    public void deleteAll(String username) {
        List<TodoItem> todoitems = this.getAll(username);
        context.deleteFrom(USER_TODO).where(USER_TODO.USER_ID.eq(username)).execute();
        for (TodoItem todoItem : todoitems) {
            context.deleteFrom(TODO_ITEM).where(TODO_ITEM.ID.eq(todoItem.id())).execute();
        }
    }

    public List<TodoItem> getAll(String username) {
        List<TodoItem> todoItems = new ArrayList<>();
        List<Integer> todoIds = context.select(USER_TODO.TODO_ID).from(USER_TODO).where(USER_TODO.USER_ID.eq(username)).fetchInto(Integer.class);
        for (Integer id : todoIds) {
            todoItems.add(context.select().from(TODO_ITEM).where(TODO_ITEM.ID.eq(id)).fetchOneInto(TodoItem.class));
        }
        return todoItems;
    }

    public int add(String username, CreateTodoItemRequest todoitem) {
        TodoItem created = context.insertInto(TODO_ITEM)
                .set(TODO_ITEM.CONTENT, todoitem.content())
                .set(TODO_ITEM.DUE_DATE, todoitem.dueDate())
                .returningResult(TODO_ITEM.ID, TODO_ITEM.CONTENT, TODO_ITEM.DUE_DATE).fetchOneInto(TodoItem.class);
        context.insertInto(USER_TODO).set(USER_TODO.USER_ID, username).set(USER_TODO.TODO_ID, created
                .id()).execute();
        return created.id();
    }

    public TodoItem update(String username, CreateTodoItemRequest todoItem, int id) {
        return context.update(TODO_ITEM).set(TODO_ITEM.CONTENT, todoItem.content()).set(TODO_ITEM.DUE_DATE, todoItem
                        .dueDate()).where(TODO_ITEM.ID.eq(id)).and(String.valueOf(username.equals(context
                        .select(USER_TODO.USER_ID).from(USER_TODO).where(USER_TODO.TODO_ID.eq(id)).fetchOneInto(String.class))))
                .returningResult(TODO_ITEM.ID, TODO_ITEM.CONTENT, TODO_ITEM.DUE_DATE).fetchOneInto(TodoItem.class);
    }

    public Integer getCompletedCount(String username) {
        return context.select(USER_STATS.COMPLETED_TASK_COUNT).from(USER_STATS).where(USER_STATS.USERNAME.eq(username)).fetchOneInto(Integer.class);
    }

    public void completeTodo(int todoId, String username) {
        context.deleteFrom(TODO_ITEM).where(TODO_ITEM.ID.eq(todoId)).execute();
        context.deleteFrom(USER_TODO).where(USER_TODO.TODO_ID.eq(todoId)).and(USER_TODO.USER_ID.eq(username)).execute();
        Integer completedTaskCount = context.select(USER_STATS.COMPLETED_TASK_COUNT).from(USER_STATS).
                where(USER_STATS.USERNAME.eq(username)).fetchOneInto(Integer.class);
        context.update(USER_STATS).set(USER_STATS.COMPLETED_TASK_COUNT, completedTaskCount + 1)
                .where(USER_STATS.USERNAME.eq(username)).execute();
    }
}
