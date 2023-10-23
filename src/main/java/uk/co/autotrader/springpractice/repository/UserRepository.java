package uk.co.autotrader.springpractice.repository;

import org.flywaydb.core.internal.util.JsonUtils;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;
import uk.co.autotrader.springpractice.domain.User;

import static uk.co.autotrader.generated.Tables.*;
import static uk.co.autotrader.generated.Tables.TODO_ITEM;

@Repository
public class UserRepository {

    private final DSLContext context;

    public UserRepository(DSLContext context) {
        this.context = context;
    }

    public boolean userExists(String username){
        return context.fetchExists(context.select().from(USERS).where(USERS.USERNAME.eq(username)));
    }
    public boolean addUser(User user){
            context.insertInto(USERS).set(USERS.USERNAME, user.username()).set(USERS.PASSWORD, user.password()).execute();
            context.insertInto(USER_STATS).set(USER_STATS.USERNAME, user.username()).set(USER_STATS.COMPLETED_TASK_COUNT, 0).execute();
            return true;

    }

    public String getPassword(User user){
        return context.select(USERS.PASSWORD).from(USERS).where(USERS.USERNAME.eq(user.username())).fetchOneInto(String.class);
    }

    public boolean usernameExists(String username){
        return context.fetchExists(context.select(USERS.USERNAME).from(USERS).where(USERS.USERNAME.eq(username)));
    }

    public Integer getCompletedTasks(String username){
        return context.select(USER_STATS.COMPLETED_TASK_COUNT).from(USER_STATS).where(USER_STATS.USERNAME.eq(username)).fetchOneInto(Integer.class);
    }

    public boolean completeTodo(int todoId, String username){
        context.deleteFrom(TODO_ITEM).where(TODO_ITEM.ID.eq(todoId)).execute();
        context.deleteFrom(USER_TODO).where(USER_TODO.ID.eq(todoId)).and(USER_TODO.USER_ID.eq(username)).execute();
        Integer completedTaskCount = context.select(USER_STATS.COMPLETED_TASK_COUNT).from(USER_STATS).where(USER_STATS.USERNAME.eq(username)).fetchOneInto(Integer.class);
        System.out.println(completedTaskCount);
        context.update(USER_STATS).set(USER_STATS.COMPLETED_TASK_COUNT, completedTaskCount + 1).where(USER_STATS.USERNAME.eq(username)).execute();
        return true;
    }



}
