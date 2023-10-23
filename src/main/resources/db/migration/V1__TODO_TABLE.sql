create table todo_item(
    id serial primary key,
    content varchar(100),
    due_date date
);

create table users(
    username varchar(100) primary key,
    password varchar(500)
);

create table user_todo(
    id serial primary key,
    user_id varchar(100),
    todo_id serial
);

create table user_stats(
    username varchar(100) primary key,
    completed_task_count int
);


