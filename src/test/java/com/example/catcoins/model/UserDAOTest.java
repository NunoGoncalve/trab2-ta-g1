package com.example.catcoins.model;

import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
class UserDAOTest {

    @Test
    void login() {
        UserDAO UsrDao = new UserDAO();
        try {
            assertNull(UsrDao.Login("invalid@email", "invalidpassword"));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}