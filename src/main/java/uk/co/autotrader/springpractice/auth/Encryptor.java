package uk.co.autotrader.springpractice.auth;

import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import uk.co.autotrader.springpractice.domain.TodoItem;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;


@Component
public class Encryptor {
        public String encrypt(String text) {
            return String.valueOf(Hex.encode(text.getBytes(StandardCharsets.UTF_8)));
        }
        public String decrypt(String encryptedText) {
            byte[] decoded = Hex.decode(encryptedText);
            return new String(decoded, StandardCharsets.UTF_8);
        }
        public List<TodoItem> decryptTodoList(List<TodoItem> todoItems){
            List<TodoItem> unencryptedTodoItems = new ArrayList<>();
            for (TodoItem todoItem : todoItems){
                unencryptedTodoItems.add(new TodoItem(todoItem.id(), this.decrypt(todoItem.content()), todoItem.dueDate()));
            }
            return unencryptedTodoItems;
        }
    };

