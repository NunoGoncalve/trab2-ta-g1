package com.example.catcoins.model;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface DAO<T> {
    T GetByID(int id) throws SQLException;
    List<T> GetAll() throws SQLException;
    T Add(T object) throws SQLException;
    boolean Update(T object) throws SQLException;
}
