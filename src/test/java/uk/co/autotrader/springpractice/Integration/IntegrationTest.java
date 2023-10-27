package uk.co.autotrader.springpractice.Integration;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.jooq.DSLContext;
import org.junit.jupiter.api.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import uk.co.autotrader.springpractice.domain.TodoItem;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.co.autotrader.generated.Tables.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class IntegrationTest {

    @Autowired
    private DSLContext context;

    private static final HttpClient client =HttpClient.newHttpClient();

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private static String VALID_AUTH_TOKEN;

    @BeforeEach
     void beforeEach() throws URISyntaxException, IOException, InterruptedException {
        context.deleteFrom(TODO_ITEM).execute();
        context.deleteFrom(USER_TODO).execute();
        context.deleteFrom(USER_STATS).execute();
        context.deleteFrom(USERS).execute();
        String RegisterRequestBodyJson = """
                {
                "username" : "TodoTests",
                "password" : "TodoTests"               
                }""";

        HttpRequest registerUserPostRequest = HttpRequest.newBuilder().POST(HttpRequest.BodyPublishers.ofString(RegisterRequestBodyJson))
                .uri(new URI("http://localhost:8080/user/register"))
                .headers("Content-Type", "application/json").build();
        client.send(registerUserPostRequest, HttpResponse.BodyHandlers.ofString());
        HttpRequest userLoginPostRequest = HttpRequest.newBuilder().POST(HttpRequest.BodyPublishers.ofString(RegisterRequestBodyJson))
                .uri(new URI("http://localhost:8080/user/validate"))
                .headers("Content-Type", "application/json").build();
        HttpResponse<String> userLoginResponse = client.send(userLoginPostRequest, HttpResponse.BodyHandlers.ofString());
        VALID_AUTH_TOKEN = userLoginResponse.body();
        context.alterSequence("todo_item_id_seq").restart().execute();
    }

    @AfterEach
     void afterEach() {
        context.deleteFrom(TODO_ITEM).execute();
        context.deleteFrom(USER_TODO).execute();
        context.deleteFrom(USER_STATS).execute();
        context.deleteFrom(USERS).execute();
        context.alterSequence("todo_item_id_seq").restart().execute();
    }

    @Test
    public void whenUserRegistered_ThenAccountExists() throws IOException, InterruptedException, URISyntaxException {
        String RegisterRequestBodyJson = """
                {
                                
                "username" : "Test",
                "password" : "Test"
                                
                }""";

        String accountExistsRequestBodyJson = """
                        {
                        
                        "username" : "Test"
                        
                        }
                            
                """;

        HttpRequest registerUserPostRequest = HttpRequest.newBuilder().POST(HttpRequest.BodyPublishers.ofString(RegisterRequestBodyJson))
                .uri(new URI("http://localhost:8080/user/register"))
                .headers("Content-Type", "application/json").build();
        client.send(registerUserPostRequest, HttpResponse.BodyHandlers.ofString());

        HttpRequest accountExistsPostRequest = HttpRequest.newBuilder().POST(HttpRequest.BodyPublishers.ofString(accountExistsRequestBodyJson))
                .uri(new URI("http://localhost:8080/user/validate/username"))
                .headers("Content-Type", "application/json").build();
        HttpResponse<String> accountExistsResponse = client.send(accountExistsPostRequest, HttpResponse.BodyHandlers.ofString());
        assertThat(objectMapper.readValue(accountExistsResponse.body(), boolean.class)).isTrue();


    }

    @Test
    public void whenUserRegistered_ThenUserAbleToLogin_AndJwtTokenIsValid() throws IOException, InterruptedException, URISyntaxException {
        String RegisterAndLoginRequestBodyJson = """
                {

                "username" : "Test",
                "password" : "Test"

                }""";


        HttpRequest registerUserPostRequest = HttpRequest.newBuilder().POST(HttpRequest.BodyPublishers.ofString(RegisterAndLoginRequestBodyJson))
                .uri(new URI("http://localhost:8080/user/register"))
                .headers("Content-Type", "application/json").build();
        client.send(registerUserPostRequest, HttpResponse.BodyHandlers.ofString());

        HttpRequest userLoginPostRequest = HttpRequest.newBuilder().POST(HttpRequest.BodyPublishers.ofString(RegisterAndLoginRequestBodyJson))
                .uri(new URI("http://localhost:8080/user/validate"))
                .headers("Content-Type", "application/json").build();
        HttpResponse<String> userLoginResponse = client.send(userLoginPostRequest, HttpResponse.BodyHandlers.ofString());
        String userToken = "{\n" +
                "\"token\" : \"" + userLoginResponse.body() + "\"\n" +
                "}";

        HttpRequest validateTokenRequest = HttpRequest.newBuilder().POST(HttpRequest.BodyPublishers.ofString(String.valueOf(userToken)))
                .uri(new URI("http://localhost:8080/user/validate/token"))
                .headers("Content-Type", "application/json").build();
        HttpResponse<String> validateTokenResponse = client.send(validateTokenRequest, HttpResponse.BodyHandlers.ofString());

        assertThat(objectMapper.readValue(validateTokenResponse.body(), boolean.class)).isTrue();

    }

    @Test
    public void whenGivenToken_ThenReturnAssociatedUsername() throws IOException, InterruptedException, URISyntaxException {
        String RegisterRequestBodyJson = """
                {
                                
                "username" : "Test",
                "password" : "Test"
                                
                }""";
        // creates user
        HttpRequest registerUserPostRequest = HttpRequest.newBuilder().POST(HttpRequest.BodyPublishers.ofString(RegisterRequestBodyJson))
                .uri(new URI("http://localhost:8080/user/register"))
                .headers("Content-Type", "application/json").build();
        client.send(registerUserPostRequest, HttpResponse.BodyHandlers.ofString());
        // retrives token
        HttpRequest userLoginPostRequest = HttpRequest.newBuilder().POST(HttpRequest.BodyPublishers.ofString(RegisterRequestBodyJson))
                .uri(new URI("http://localhost:8080/user/validate"))
                .headers("Content-Type", "application/json").build();
        HttpResponse<String> userToken = client.send(userLoginPostRequest, HttpResponse.BodyHandlers.ofString());
        HttpRequest retrieveUsernameFromToken = HttpRequest.newBuilder().GET()
                .uri(new URI("http://localhost:8080/username"))
                .headers("Content-Type", "application/json", "authorization", userToken.body()).build();
        HttpResponse<String> retrieveUsernameResponse = client.send(retrieveUsernameFromToken, HttpResponse.BodyHandlers.ofString());
        assertThat(retrieveUsernameResponse.body()).isEqualTo("Test");


    }

    @Test
    public void createAndGetAllTodos() throws IOException, InterruptedException, URISyntaxException {
        String json = """
                     {
                    "content" : "create and get all todos",
                    "dueDate" : "2023-10-16"

                    }
                """;
        String json2 = """
                     {
                    "content" : "create and get all todos",
                    "dueDate" : "2023-10-17"

                    }
                """;
        TodoItem expectedTodoItem = new TodoItem(1, "create and get all todos", LocalDate.of(2023, 10, 16));
        TodoItem expectedTodoItem2 = new TodoItem(2, "create and get all todos", LocalDate.of(2023, 10, 17));

        HttpRequest postRequest = HttpRequest.newBuilder().POST(HttpRequest.BodyPublishers.ofString(json))
                .uri(new URI("http://localhost:8080/todo")).headers("Content-Type", "application/json", "authorization", VALID_AUTH_TOKEN).build();
        HttpResponse<String> postResponse = client.send(postRequest, HttpResponse.BodyHandlers.ofString());
        HttpRequest postRequest2 = HttpRequest.newBuilder().POST(HttpRequest.BodyPublishers.ofString(json2))
                .uri(new URI("http://localhost:8080/todo")).headers("Content-Type", "application/json", "authorization", VALID_AUTH_TOKEN).build();
        HttpResponse<String> postResponse2 = client.send(postRequest2, HttpResponse.BodyHandlers.ofString());


        assertThat(objectMapper.readValue(postResponse2.body(), TodoItem.class)).isEqualTo(expectedTodoItem2);

        HttpRequest getRequest = HttpRequest.newBuilder().GET().uri(new URI("http://localhost:8080/todo"))
                .headers("Content-Type", "application/json", "authorization", VALID_AUTH_TOKEN).build();
        HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());

        List<LinkedHashMap> responseAsList = objectMapper.readValue(getResponse.body(), List.class);

        List<TodoItem> expectedOutput = new ArrayList<>(List.of(expectedTodoItem, expectedTodoItem2));

        List<TodoItem> items = new ArrayList<>();
        for (LinkedHashMap map : responseAsList) {
            items.add(new TodoItem((int) map.get("id"), (String) map.get("content"), LocalDate.parse(map.get("dueDate").toString())));
        }

        assertThat(items).isEqualTo(expectedOutput);
    }

    @Test
    public void createAndGetTodoById() throws IOException, InterruptedException, URISyntaxException {
        String json = """
                     {
                    "content" : "create and get by id",
                    "dueDate" : "2023-10-13"

                    }
                """;
        TodoItem expectedTodoItem = new TodoItem(1, "create and get by id",LocalDate.of(2023, 10, 13));

        HttpRequest postRequest = HttpRequest.newBuilder().POST(HttpRequest.BodyPublishers.ofString(json)).uri(new URI("http://localhost:8080/todo")).headers("Content-Type", "application/json", "authorization", VALID_AUTH_TOKEN).build();
        client.send(postRequest, HttpResponse.BodyHandlers.ofString());
        HttpRequest getRequest = HttpRequest.newBuilder().GET().uri(new URI("http://localhost:8080/todo/1")).headers("Content-Type", "application/json", "authorization", VALID_AUTH_TOKEN).build();
        HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());

        assertThat(objectMapper.readValue(getResponse.body(), TodoItem.class)).isEqualTo(expectedTodoItem);

    }

    @Test
    public void createAndUpdateTodo() throws IOException, InterruptedException, URISyntaxException{
        String json = """
                     {
                    "content" : "create and update",
                    "dueDate" : "2023-10-16"

                    }
                """;
        String putJson = """
                     {
                     "id" : 1,
                    "content" : "Updated!",
                    "dueDate" : "2023-10-17"

                    }
                """;


        TodoItem expectedTodoItem = new TodoItem(1, "Updated!",LocalDate.of(2023, 10, 17));

        HttpRequest postRequest = HttpRequest.newBuilder().POST(HttpRequest.BodyPublishers.ofString(json)).uri(new URI("http://localhost:8080/todo"))
                .headers("Content-Type", "application/json", "authorization", VALID_AUTH_TOKEN).build();
        HttpResponse<String> postResponse = client.send(postRequest, HttpResponse.BodyHandlers.ofString());

        HttpRequest putRequest = HttpRequest.newBuilder().PUT(HttpRequest.BodyPublishers.ofString(putJson)).uri(new URI("http://localhost:8080/todo/1"))
                .headers("Content-Type", "application/json", "authorization", VALID_AUTH_TOKEN).build();
        HttpResponse<String> putResponse = client.send(putRequest, HttpResponse.BodyHandlers.ofString());

        assertThat(objectMapper.readValue(putResponse.body(), TodoItem.class)).isEqualTo(expectedTodoItem);
    }

    @Test
    public void createAndDeleteTodoById() throws IOException, InterruptedException, URISyntaxException{
        String json = """
                     {
                    "content" : "create and delete by id",
                    "dueDate" : "2023-10-16"

                    }
                """;
        HttpRequest postRequest = HttpRequest.newBuilder().POST(HttpRequest.BodyPublishers.ofString(json))
                .uri(new URI("http://localhost:8080/todo")).headers("Content-Type", "application/json", "authorization", VALID_AUTH_TOKEN).build();
        HttpResponse<String> postResponse = client.send(postRequest, HttpResponse.BodyHandlers.ofString());
        HttpRequest deleteRequest = HttpRequest.newBuilder().DELETE().uri(new URI("http://localhost:8080/todo/1"))
                .headers("Content-Type", "application/json", "authorization", VALID_AUTH_TOKEN).build();
        HttpResponse<String> deleteResponse = client.send(deleteRequest, HttpResponse.BodyHandlers.ofString());
        TodoItem expectedTodoItem = new TodoItem(1, "create and delete by id",LocalDate.of(2023, 10, 16));
        assertThat(objectMapper.readValue(postResponse.body(), TodoItem.class)).isEqualTo(expectedTodoItem);
        assertThat(objectMapper.readValue(deleteResponse.body(), boolean.class)).isEqualTo(Boolean.valueOf("True"));
    }

    @Test
    public void createAndDeleteTodo() throws IOException, InterruptedException, URISyntaxException {

        String json = """
                     {
                    "content" : "create and delete",
                    "dueDate" : "2023-10-16"

                    }
                """;

        String json2 = """
                     {
                    "content" : "create and delete",
                    "dueDate" : "2023-10-17"

                    }
                """;

        HttpRequest postRequest = HttpRequest.newBuilder().POST(HttpRequest.BodyPublishers.ofString(json)).uri(new URI("http://localhost:8080/todo")).headers("Content-Type", "application/json", "authorization", VALID_AUTH_TOKEN).build();
        HttpResponse<String> postResponse = client.send(postRequest, HttpResponse.BodyHandlers.ofString());
        HttpRequest postRequest2 = HttpRequest.newBuilder().POST(HttpRequest.BodyPublishers.ofString(json2)).uri(new URI("http://localhost:8080/todo")).headers("Content-Type", "application/json", "authorization", VALID_AUTH_TOKEN).build();
        HttpResponse<String> postResponse2 = client.send(postRequest2, HttpResponse.BodyHandlers.ofString());
        HttpRequest deleteRequest = HttpRequest.newBuilder().DELETE().uri(new URI("http://localhost:8080/todo")).headers("Content-Type", "application/json", "authorization", VALID_AUTH_TOKEN).build();
        HttpResponse<String> deleteResponse = client.send(deleteRequest, HttpResponse.BodyHandlers.ofString());

        TodoItem expectedTodoItem = new TodoItem(1, "create and delete", LocalDate.of(2023, 10, 16));
        TodoItem expectedTodoItem2 = new TodoItem(2, "create and delete", LocalDate.of(2023, 10, 17));
        assertThat(objectMapper.readValue(postResponse2.body(), TodoItem.class)).isEqualTo(expectedTodoItem2);
        assertThat(objectMapper.readValue(postResponse.body(), TodoItem.class)).isEqualTo(expectedTodoItem);

        assertThat(objectMapper.readValue(deleteResponse.body(), boolean.class)).isEqualTo(Boolean.valueOf("True"));

        HttpRequest getRequest = HttpRequest.newBuilder().GET().uri(new URI("http://localhost:8080/todo")).headers("Content-Type", "application/json", "authorization", VALID_AUTH_TOKEN).build();
        HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());

        List<LinkedHashMap> responseAsList = objectMapper.readValue(getResponse.body(), List.class);

        List<TodoItem> items = new ArrayList<>();

        List<TodoItem> expectedOutput = new ArrayList<>();

        for (LinkedHashMap map : responseAsList) {
            items.add(new TodoItem((int) map.get("id"), (String) map.get("content"), LocalDate.parse(map.get("dueDate").toString())));
        }

        assertThat(items).isEqualTo(expectedOutput);
    }

    @Test
    public void createAndCompleteTodo() throws IOException, InterruptedException, URISyntaxException {

        String json = """
                     {
                    "content" : "create and complete",
                    "dueDate" : "2023-10-16"

                    }
                """;

        HttpRequest postRequest = HttpRequest.newBuilder().POST(HttpRequest.BodyPublishers.ofString(json)).uri(new URI("http://localhost:8080/todo")).headers("Content-Type", "application/json", "authorization", VALID_AUTH_TOKEN).build();
        client.send(postRequest, HttpResponse.BodyHandlers.ofString());

        HttpRequest completeTodoPostRequest = HttpRequest.newBuilder().POST(HttpRequest.BodyPublishers.ofString(json))
                .uri(new URI("http://localhost:8080/todo/complete/1")).headers("Content-Type", "application/json", "authorization", VALID_AUTH_TOKEN).build();
        client.send(completeTodoPostRequest, HttpResponse.BodyHandlers.ofString());

        HttpRequest getCompletedTodoCountRequest = HttpRequest.newBuilder().GET().uri(new URI("http://localhost:8080/todo/completed")).headers("Content-Type", "application/json", "authorization", VALID_AUTH_TOKEN).build();
        HttpResponse<String> getCompletedTodoCountResponse = client.send(getCompletedTodoCountRequest, HttpResponse.BodyHandlers.ofString());

        assertThat(objectMapper.readValue(getCompletedTodoCountResponse.body(), Integer.class)).isEqualTo(1);

    }

    @Test
    public void getUserProgress() throws IOException, InterruptedException, URISyntaxException {

        String json = """
                     {
                    "content" : "create and complete",
                    "dueDate" : "2023-10-16"

                    }
                """;

        HttpRequest postRequest = HttpRequest.newBuilder().POST(HttpRequest.BodyPublishers.ofString(json)).uri(new URI("http://localhost:8080/todo")).headers("Content-Type", "application/json", "authorization", VALID_AUTH_TOKEN).build();
        client.send(postRequest, HttpResponse.BodyHandlers.ofString());
        client.send(postRequest, HttpResponse.BodyHandlers.ofString());


        HttpRequest completeTodoPostRequest = HttpRequest.newBuilder().POST(HttpRequest.BodyPublishers.ofString(json))
                .uri(new URI("http://localhost:8080/todo/complete/1")).headers("Content-Type", "application/json", "authorization", VALID_AUTH_TOKEN).build();
        client.send(completeTodoPostRequest, HttpResponse.BodyHandlers.ofString());

        HttpRequest getUserProgressRequest = HttpRequest.newBuilder().GET().uri(new URI("http://localhost:8080/todo/progress")).headers("Content-Type", "application/json", "authorization", VALID_AUTH_TOKEN).build();
        HttpResponse<String> getCompletedTodoCountResponse = client.send(getUserProgressRequest, HttpResponse.BodyHandlers.ofString());

        assertThat(objectMapper.readValue(getCompletedTodoCountResponse.body(), Double.class)).isEqualTo(50.0);

    }
}

