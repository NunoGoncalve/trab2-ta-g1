package com.example.catcoins.model;

import com.example.catcoins.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class OrderDAO implements DAO<Order> {

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

    public Order GetByID(int id) throws SQLException {
        return null;
    }

    public ArrayList<Order> GetAll() throws SQLException {
        /*String sql = "SELECT id, Name, VarianceCalc(id) as Variance, (Select Value from CoinHistory Where Coin=id ORDER BY Date DESC LIMIT 1) as value FROM Coin where Status='Active'";
        ArrayList<Order> orders = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet UserResult = stmt.executeQuery();
            while (UserResult.next()) {
                orders.add(
                    new Order(
                        UserResult.getInt("ID"),
                        UserResult.getINt("Name"),
                        UserResult.getDouble("Value"),
                        UserResult.getDouble("Variance"),
                        UserResult.getTimestamp("Date")
                    )
                );
            }
            return orders;
        }*/
        return null;
    }

    public Order Add(Order ord) throws SQLException{
        String NewOrder = "INSERT INTO `Order` (Type, Wallet, Coin, Amount, Value) Values (?, ?, ?, ?, ?)"; // `Order` -> to escape word Order

        try (Connection conn = DatabaseConnection.getInstance().getConnection()){
            PreparedStatement stmt = conn.prepareStatement(NewOrder, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, ord.getType());
            stmt.setInt(2, ord.getOrderwallet().getID());
            stmt.setInt(3, ord.getCoin().getID());
            stmt.setInt(4, ord.getAmount());
            stmt.setDouble(5, ord.getValue());
            stmt.executeUpdate();
            ResultSet generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                int generatedId = generatedKeys.getInt(1);
                ord.setID(generatedId);
            } else {
                throw new SQLException("Creating user failed, no ID obtained.");
            }
            return ord;
        }
    }

    public ArrayList<Order> GetUserOrderHistory(Wallet wlt) throws SQLException {
        CoinDAO CnDao = new CoinDAO();
        ArrayList<Order> OrderHistoryList = new ArrayList<>();
        String sql = " SELECT O.ID,O.Type, O.Value,O.Date, O.Coin, O.Status ,Sum(Transaction.Amount) as Amount " +
                "FROM Transaction inner join `Order` O on Transaction.OrderID=O.ID " +
                "Where Wallet = ? group by OrderID Order by Date DESC";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, wlt.getID());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Order ord = new Order(
                        rs.getInt("ID"),
                        rs.getString("Type"),
                        null,
                        CnDao.GetByID(conn, rs.getInt("Coin")),
                        rs.getDouble("Value"),
                        rs.getInt("Amount"),
                        rs.getTimestamp("Date"), // Alterado para getTimestamp
                        OrderStatus.fromString(rs.getString("Status"))
                );
                if(ord.getValue()==0){
                    ord.setValue(ord.getCoin().getValue());
                }
                OrderHistoryList.add(ord);
            }
            return OrderHistoryList;
        }
    }

    public ArrayList<Order> GetUserActiveOrders(Wallet wlt) throws SQLException {
        ArrayList<Order> ActiveOrderList = new ArrayList<>();
        CoinDAO CnDao = new CoinDAO();
        String sql = "SELECT O.ID, O.Type, O.Value,O.Date, O.Status, Coin.Name as Coin, O.Amount as Amount,O.Coin as CoinID " +
                "FROM `Order` O inner join Coin on O.coin= Coin.ID Where `Wallet` = ? and O.Status = 'Open' group by O.ID Order by Date DESC";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, wlt.getID());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Order ord = new Order(
                        rs.getInt("ID"),
                        rs.getString("Type"),
                        null,
                        CnDao.GetByID(conn, rs.getInt("CoinID")),
                        rs.getDouble("Value"),
                        rs.getInt("Amount"),
                        rs.getTimestamp("Date"),
                        OrderStatus.fromString(rs.getString("Status"))
                );
                if(ord.getValue()==0){
                    ord.setValue(ord.getCoin().getValue());
                }
                ActiveOrderList.add(ord);
            }
            return ActiveOrderList;
        }
    }

    public ArrayList<Order> GetCoinOrders(int ID) throws SQLException {
        ArrayList<Order> orders = new ArrayList<>();
        String sql = "SELECT `Order`.ID,`Order`.Type,`Order`.Wallet, `Order`.Value,`Order`.Date, `Order`.Status ,Sum(Transaction.Amount) as Amount " +
                "FROM `Transaction` inner join `Order` on Transaction.OrderID=`Order`.ID " +
                "Where Coin = ? group by OrderID Order by Date DESC";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setInt(1, ID);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    orders.add(new Order(
                            rs.getInt("ID"),
                            rs.getString("Type"),
                            null,
                            null,
                            rs.getDouble("Value"),
                            rs.getInt("Amount"),
                            rs.getTimestamp("Date"),
                            OrderStatus.fromString(rs.getString("Status"))
                    ));
                }
            }
            return orders;
        } catch (SQLException e) {
            throw e;
        }
    }

    public MarketInfo OrderSearch(Order NewOrder) throws SQLException{
        String SearchOrder = "Call SaleSearch(?, ?, ?, ?, ?, ?,?, ?, ?, ?)";
        try(Connection conn = DatabaseConnection.getInstance().getConnection();
            CallableStatement stmts = conn.prepareCall(SearchOrder)){
            stmts.setString(1, NewOrder.getType().toString());
            stmts.setInt(2, NewOrder.getId());
            stmts.setInt(3, NewOrder.getOrderwallet().getID());
            stmts.setInt(4, NewOrder.getCoin().getID());
            stmts.setInt(5, NewOrder.getAmount());
            stmts.setDouble(6, NewOrder.getValue());
            stmts.registerOutParameter(7, Types.BOOLEAN);
            stmts.registerOutParameter(8, Types.BOOLEAN);
            stmts.registerOutParameter(9, Types.INTEGER);
            stmts.registerOutParameter(10, Types.INTEGER);
            stmts.execute();
            MarketInfo MkInfo = new MarketInfo(stmts.getBoolean(7),stmts.getBoolean(8),stmts.getInt(9), stmts.getInt(10));
            return MkInfo;
        }catch (SQLException e){
            throw e;
        }
    }

    public Map<String, Integer> GetUserOrderNum(Wallet wlt) throws SQLException {
        String sql = "SELECT Type, COUNT(ID) AS total_orders FROM `Order` WHERE Wallet = ? AND Type IN ('Buy', 'Sell') GROUP BY Type";
        Map<String, Integer> map = new HashMap<>();
        map.put("Buy", 0);
        map.put("Sell", 0);
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, wlt.getID());
            ResultSet rs = stmt.executeQuery();

            while(rs.next()) {
                if(rs.getString("Type").equals("Buy")) {
                    map.put("Buy", rs.getInt("total_orders"));
                }else{
                    map.put("Sell", rs.getInt("total_orders"));
                }
            }
            return map;
        }
    }

    public boolean CancelOrder(Order ord) throws SQLException{
        String CancelOrder = "Update `Order` Set Status = ? where ID =?"; // `Order` -> to escape word Order

        try (Connection conn = DatabaseConnection.getInstance().getConnection()){
            PreparedStatement stmt = conn.prepareStatement(CancelOrder);
            stmt.setString(1, ord.getStatus().toString());
            stmt.setInt(2, ord.getId());
            stmt.executeUpdate();
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    public boolean Update(Order object) throws SQLException{
        return false;
    }

}
