package uk.co.autotrader.springpractice.domain;

import java.time.LocalDate;

public record TodoItem(int id, String content, LocalDate dueDate) {

}
