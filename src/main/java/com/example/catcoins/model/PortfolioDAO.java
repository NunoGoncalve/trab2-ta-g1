package com.example.catcoins.model;

import com.example.catcoins.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PortfolioDAO implements DAO<Portfolio> {

    public Portfolio GetByID(int id) throws SQLException {
        /*String sql = "SELECT WalletID, Balance, PendingBalance FROM UserClientWallet WHERE ID = ?;";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return null;
            } else {
                System.out.println("No wallet found with ID: " + id);
                return null;
            }

        }*/
        return  null;
    }

    public List<Portfolio> GetAll(){
        return null;
    }

    public Portfolio Add(Portfolio object){
        return null;
    }

    public boolean Update(Portfolio prtf) throws SQLException{
        String UpdatePortfolio = "Update Portfolio Set Amount = ? where WalletID = ? and CoinID = ?"; // `Order` -> to escape word Order
        try (Connection conn = DatabaseConnection.getInstance().getConnection()){
            PreparedStatement stmt = conn.prepareStatement(UpdatePortfolio);
            stmt.setInt(1, prtf.getAmount());
            stmt.setInt(2, prtf.getUserWallet().getID());
            stmt.setInt(3, prtf.getCryptoCoin().getID());
            stmt.executeUpdate();
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    public ArrayList<Portfolio> GetWalletPortfolio(Wallet wlt) throws SQLException {
        CoinDAO CnDao = new CoinDAO();
        String query = "SELECT CoinID, Amount, VarianceCalc(CoinID) as variance, (Select Value from CoinHistory Where Coin=id ORDER BY Date DESC LIMIT 1) as value, Name " +
                "FROM Portfolio INNER join Coin on CoinID = Coin.ID where WalletID = ?";
        ArrayList<Portfolio> PortfolioList = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            PreparedStatement Stmt = conn.prepareStatement(query);
            Stmt.setInt(1, wlt.getID());
            ResultSet rs = Stmt.executeQuery();
            while (rs.next()) {
                Portfolio Prtf= new Portfolio(
                        wlt,
                        CnDao.GetByID(conn,rs.getInt("CoinID")),
                        rs.getInt("Amount")
                );
                PortfolioList.add(Prtf);
            }
            return PortfolioList;
        }
    }

}
