package com.example.catcoins.model;

import com.example.catcoins.DatabaseConnection;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CoinDAO implements DAO<Coin> {

    public int GetId(int id) throws SQLException {
        String sql = "SELECT WalletID FROM UserClientWallet WHERE ID = ?;";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("WalletID");
            } else {
                System.out.println("No wallet found with ID: " + id);
                return 0;
            }

        } catch (SQLException e) {
            throw new SQLException(e);
        }
    }

    public Coin GetByID(int id) throws SQLException {
        String sql = "SELECT id, Name, VarianceCalc(id) as Variance, (Select Value from CoinHistory Where Coin=id ORDER BY Date DESC LIMIT 1) as value FROM Coin where Status='Active' and ID = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new Coin(rs.getInt("ID"), rs.getString("Name"), rs.getDouble("Value"), rs.getDouble("Variance"));
            } else {
                System.out.println("No Coin found with ID: " + id);
                return null;
            }

        } catch (SQLException e) {
            throw e;
        }
    }

    public Coin GetByID(Connection conn, int id) throws SQLException {
        String sql = "SELECT id, Name, VarianceCalc(id) as Variance, (Select Value from CoinHistory Where Coin=id ORDER BY Date DESC LIMIT 1) as value FROM Coin where Status='Active' and ID = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new Coin(rs.getInt("ID"), rs.getString("Name"), rs.getDouble("Value"), rs.getDouble("Variance"));
            } else {
                System.out.println("No Coin found with ID: " + id);
                return null;
            }

        } catch (SQLException e) {
            throw e;
        }
    }

    public ArrayList<Coin> GetAll() throws SQLException {
        String sql = "SELECT id, Name, VarianceCalc(id) as Variance, (Select Value from CoinHistory Where Coin=id ORDER BY Date DESC LIMIT 1) as Value FROM Coin where Status='Active'";
        ArrayList<Coin> coins = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet CoinResult = stmt.executeQuery();
            while (CoinResult.next()) {
                coins.add(
                    new Coin(
                        CoinResult.getInt("ID"),
                        CoinResult.getString("Name"),
                        CoinResult.getDouble("Value"),
                        CoinResult.getDouble("Variance")
                    )
                );
            }
            return coins;
        }

    }

    public XYChart.Series<String, Number> GetCoinHistory(int ID, String filter) throws SQLException {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection()){

            String sql = "SELECT XValue, Value FROM ( SELECT DATE_FORMAT(Date, '%H:%i') AS XValue, Value, Date FROM CoinHistory WHERE Date >= NOW() - INTERVAL 24 HOUR AND Coin = ? ORDER BY Date DESC LIMIT 24 ) sub ORDER BY Date ASC";
            PreparedStatement stmt=conn.prepareStatement(sql);
            stmt.setInt(1, ID);

            if(filter.equals("Week")){
                sql = "SELECT DAY(Date) AS XValue, AVG(Value) AS Value FROM CoinHistory WHERE Date >= CURDATE() - INTERVAL 7 DAY AND Date <= CURDATE() AND Coin=? GROUP BY Coin, XValue ORDER BY Coin, XValue";
                stmt = conn.prepareStatement(sql);
                stmt.setInt(1, ID);
            }else if(filter.equals("Month")){
                sql = "SELECT DAY(Date) AS XValue, AVG(Value) AS Value FROM CoinHistory WHERE MONTH(Date) = ? AND YEAR(Date) = ? AND Coin=? GROUP BY Coin, XValue ORDER BY Coin, XValue";
                stmt = conn.prepareStatement(sql);
                stmt.setInt(1, LocalDate.now().getMonthValue());
                stmt.setInt(2, LocalDate.now().getYear());
                stmt.setInt(3, ID);
            }
            ResultSet CoinResult = stmt.executeQuery();

            while (CoinResult.next()) {
                series.getData().add(new XYChart.Data<>(CoinResult.getString("XValue"), CoinResult.getDouble("Value")));
            }
            return series;
        }catch (SQLException e){
            throw e;
        }

    }

    public Coin Add(Coin object) throws SQLException{
        /*String sql = "INSERT INTO User (Name, Email, Password, Role, Status) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nome);
            stmt.setString(2, email);
            stmt.setString(3, password);
            stmt.setString(4, role);
            stmt.setString(5, status);
            stmt.executeUpdate();

        }*/
        return null;
    }

    public boolean Update(Coin object) throws SQLException{
        String sql = "Update User Set Name = ? WHERE ID = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, object.getName());
            stmt.setInt(2, object.getID());
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        }
    }

}
