package uk.co.autotrader.springpractice.domain;

import java.time.LocalDate;

public record CreateTodoItemRequest(String content, LocalDate dueDate) {
}
