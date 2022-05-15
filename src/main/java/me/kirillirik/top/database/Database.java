package me.kirillirik.top.database;

public interface Database {

    /**
     * Создание БД
     */
    void create();

    /**
     * Выполнения запроса
     * @param sql запрос
     */
    void execute(String sql);
}
