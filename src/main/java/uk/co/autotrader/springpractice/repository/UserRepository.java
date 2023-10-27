package uk.co.autotrader.springpractice.repository;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;
import uk.co.autotrader.springpractice.domain.User;

import static uk.co.autotrader.generated.Tables.*;

@Repository
public class UserRepository {

    private final DSLContext context;

    public UserRepository(DSLContext context) {
        this.context = context;
    }

    public boolean userExists(String username) {
        return context.fetchExists(context.select().from(USERS).where(USERS.USERNAME.eq(username)));
    }

    public boolean addUser(User user) {
        context.insertInto(USERS).set(USERS.USERNAME, user.username()).set(USERS.PASSWORD, user.password()).execute();
        context.insertInto(USER_STATS).set(USER_STATS.USERNAME, user.username()).set(USER_STATS.COMPLETED_TASK_COUNT, 0).execute();
        return true;

    }

    public String getPassword(User user) {
        return context.select(USERS.PASSWORD).from(USERS).where(USERS.USERNAME.eq(user.username())).fetchOneInto(String.class);
    }

    public boolean usernameExists(String username) {
        return context.fetchExists(context.select(USERS.USERNAME).from(USERS).where(USERS.USERNAME.eq(username)));
    }



}
