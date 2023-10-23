//package uk.co.autotrader.springpractice.Integration;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
//import org.jooq.DSLContext;
//import org.junit.jupiter.api.*;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import uk.co.autotrader.springpractice.domain.TodoItem;
//
//import java.io.IOException;
//import java.net.URI;
//import java.net.URISyntaxException;
//import java.net.http.HttpClient;
//import java.net.http.HttpRequest;
//import java.net.http.HttpResponse;
//import java.time.LocalDate;
//import java.util.ArrayList;
//import java.util.LinkedHashMap;
//import java.util.List;
//import java.util.Map;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static uk.co.autotrader.generated.Tables.TODO_ITEM;
//
//
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
//public class IntegrationTest {
//
//    @Autowired
//    private DSLContext context;
//
//    private HttpClient client =HttpClient.newHttpClient();
//    private ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
//
//    @BeforeEach
//    void setUp() {
//        context.deleteFrom(TODO_ITEM).execute();
//        context.alterSequence("todo_item_id_seq").restart().execute();
//    }
//
//    @AfterEach
//     void afterEach() {
//        context.deleteFrom(TODO_ITEM).execute();
//        context.alterSequence("todo_item_id_seq").restart().execute();
//
//    }
//
//    @Test
//    public void createAndGetAllTodos() throws IOException, InterruptedException, URISyntaxException {
//        String json = """
//                     {
//                    "content" : "create and get all todos",
//                    "dueDate" : "2023-10-16"
//
//                    }
//                """;
//        String json2 = """
//                     {
//                    "content" : "create and get all todos",
//                    "dueDate" : "2023-10-17"
//
//                    }
//                """;
//        HttpRequest postRequest = HttpRequest.newBuilder().POST(HttpRequest.BodyPublishers.ofString(json)).uri(new URI("http://localhost:8080/todo")).headers("Content-Type", "application/json").build();
//        HttpResponse<String> postResponse = client.send(postRequest, HttpResponse.BodyHandlers.ofString());
//        HttpRequest postRequest2 = HttpRequest.newBuilder().POST(HttpRequest.BodyPublishers.ofString(json2)).uri(new URI("http://localhost:8080/todo")).headers("Content-Type", "application/json").build();
//        HttpResponse<String> postResponse2 = client.send(postRequest2, HttpResponse.BodyHandlers.ofString());
//
//        // checks successful post request
//        TodoItem expectedTodoItem = new TodoItem(1, "create and get all todos", LocalDate.of(2023, 10, 16));
//        TodoItem expectedTodoItem2 = new TodoItem(2, "create and get all todos", LocalDate.of(2023, 10, 17));
//        assertThat(objectMapper.readValue(postResponse2.body(), TodoItem.class)).isEqualTo(expectedTodoItem2);
//        assertThat(objectMapper.readValue(postResponse.body(), TodoItem.class)).isEqualTo(expectedTodoItem);
//
//        // checks that the database is populated
//
//        HttpRequest getRequest = HttpRequest.newBuilder().GET().uri(new URI("http://localhost:8080/todo")).build();
//        HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
//
//        List<LinkedHashMap> responseAsList = objectMapper.readValue(getResponse.body(), List.class);
//
//        List<TodoItem> expectedOutput = new ArrayList<>(List.of(expectedTodoItem, expectedTodoItem2));
//
//        List<TodoItem> items = new ArrayList<>();
//        for (LinkedHashMap map : responseAsList) {
//            items.add(new TodoItem((int) map.get("id"), (String) map.get("content"), LocalDate.parse(map.get("dueDate").toString())));
//        }
//
//        assertThat(items).isEqualTo(expectedOutput);
//    }
//
//    @Test
//    public void createAndGetTodoById() throws IOException, InterruptedException, URISyntaxException {
//        String json = """
//                     {
//                    "content" : "create and get by id",
//                    "dueDate" : "2023-10-13"
//
//                    }
//                """;
//        HttpRequest postRequest = HttpRequest.newBuilder().POST(HttpRequest.BodyPublishers.ofString(json)).uri(new URI("http://localhost:8080/todo")).headers("Content-Type", "application/json").build();
//        HttpResponse<String> postResponse = client.send(postRequest, HttpResponse.BodyHandlers.ofString());
//        HttpRequest getRequest = HttpRequest.newBuilder().GET().uri(new URI("http://localhost:8080/todo/1")).build();
//        HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
//
//        TodoItem expectedTodoItem = new TodoItem(1, "create and get by id",LocalDate.of(2023, 10, 13));
//        assertThat(objectMapper.readValue(getResponse.body(), TodoItem.class)).isEqualTo(expectedTodoItem);
//
//    }
//    @Test
//    public void createAndDeleteTodoById() throws IOException, InterruptedException, URISyntaxException{
//        String json = """
//                     {
//                    "content" : "create and delete by id",
//                    "dueDate" : "2023-10-16"
//
//                    }
//                """;
//        HttpRequest postRequest = HttpRequest.newBuilder().POST(HttpRequest.BodyPublishers.ofString(json)).uri(new URI("http://localhost:8080/todo")).headers("Content-Type", "application/json").build();
//        HttpResponse<String> postResponse = client.send(postRequest, HttpResponse.BodyHandlers.ofString());
//        HttpRequest deleteRequest = HttpRequest.newBuilder().DELETE().uri(new URI("http://localhost:8080/todo/1")).build();
//        HttpResponse<String> deleteResponse = client.send(deleteRequest, HttpResponse.BodyHandlers.ofString());
//        // checks successful post request
//        TodoItem expectedTodoItem = new TodoItem(1, "create and delete by id",LocalDate.of(2023, 10, 16));
//        assertThat(objectMapper.readValue(postResponse.body(), TodoItem.class)).isEqualTo(expectedTodoItem);
//        // checks successful delete request
//        assertThat(objectMapper.readValue(deleteResponse.body(), boolean.class)).isEqualTo(Boolean.valueOf("True"));
//    }
//
//    @Test
//    public void createAndUpdateTodo() throws IOException, InterruptedException, URISyntaxException{
//        String json = """
//                     {
//                    "content" : "create and update",
//                    "dueDate" : "2023-10-16"
//
//                    }
//                """;
//        String putJson = """
//                     {
//                     "id" : 1,
//                    "content" : "create and update",
//                    "dueDate" : "2023-10-17"
//
//                    }
//                """;
//        HttpRequest postRequest = HttpRequest.newBuilder().POST(HttpRequest.BodyPublishers.ofString(json)).uri(new URI("http://localhost:8080/todo")).headers("Content-Type", "application/json").build();
//        HttpResponse<String> postResponse = client.send(postRequest, HttpResponse.BodyHandlers.ofString());
//
//        HttpRequest putRequest = HttpRequest.newBuilder().PUT(HttpRequest.BodyPublishers.ofString(putJson)).uri(new URI("http://localhost:8080/todo")).headers("Content-Type", "application/json").build();
//        HttpResponse<String> putResponse = client.send(putRequest, HttpResponse.BodyHandlers.ofString());
//        // checks successful post request
//        TodoItem expectedTodoItem = new TodoItem(1, "create and update",LocalDate.of(2023, 10, 16));
//        TodoItem expectedUpdatedTodoItem = new TodoItem(1, "create and update",LocalDate.of(2023, 10, 17));
//
//        assertThat(objectMapper.readValue(postResponse.body(), TodoItem.class)).isEqualTo(expectedTodoItem);
//        // checks successful put request
//        assertThat(objectMapper.readValue(putResponse.body(), TodoItem.class)).isEqualTo(expectedUpdatedTodoItem);
//    }
//
//
//    @Test
//    public void createAndDeleteTodo() throws IOException, InterruptedException, URISyntaxException {
//        String json = """
//                     {
//                    "content" : "create and delete",
//                    "dueDate" : "2023-10-16"
//
//                    }
//                """;
//        String json2 = """
//                     {
//                    "content" : "create and delete",
//                    "dueDate" : "2023-10-17"
//
//                    }
//                """;
//        HttpRequest postRequest = HttpRequest.newBuilder().POST(HttpRequest.BodyPublishers.ofString(json)).uri(new URI("http://localhost:8080/todo")).headers("Content-Type", "application/json").build();
//        HttpResponse<String> postResponse = client.send(postRequest, HttpResponse.BodyHandlers.ofString());
//        HttpRequest postRequest2 = HttpRequest.newBuilder().POST(HttpRequest.BodyPublishers.ofString(json2)).uri(new URI("http://localhost:8080/todo")).headers("Content-Type", "application/json").build();
//        HttpResponse<String> postResponse2 = client.send(postRequest2, HttpResponse.BodyHandlers.ofString());
//        HttpRequest deleteRequest = HttpRequest.newBuilder().DELETE().uri(new URI("http://localhost:8080/todo")).build();
//        HttpResponse<String> deleteResponse = client.send(deleteRequest, HttpResponse.BodyHandlers.ofString());
//
//        // checks successful post request
//        TodoItem expectedTodoItem = new TodoItem(1, "create and delete", LocalDate.of(2023, 10, 16));
//        TodoItem expectedTodoItem2 = new TodoItem(2, "create and delete", LocalDate.of(2023, 10, 17));
//        assertThat(objectMapper.readValue(postResponse2.body(), TodoItem.class)).isEqualTo(expectedTodoItem2);
//        assertThat(objectMapper.readValue(postResponse.body(), TodoItem.class)).isEqualTo(expectedTodoItem);
//
//        // checks successful delete request
//        assertThat(objectMapper.readValue(deleteResponse.body(), boolean.class)).isEqualTo(Boolean.valueOf("True"));
//
//        // checks that the database is empty
//        HttpRequest getRequest = HttpRequest.newBuilder().GET().uri(new URI("http://localhost:8080/todo")).build();
//        HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
//
//        List<LinkedHashMap> responseAsList = objectMapper.readValue(getResponse.body(), List.class);
//
//        List<TodoItem> items = new ArrayList<>();
//
//        List<TodoItem> expectedOutput = new ArrayList<>();
//
//        for (LinkedHashMap map : responseAsList) {
//            items.add(new TodoItem((int) map.get("id"), (String) map.get("content"), LocalDate.parse(map.get("dueDate").toString())));
//        }
//
//        assertThat(items).isEqualTo(expectedOutput);
//    }
//
//
//}
//
