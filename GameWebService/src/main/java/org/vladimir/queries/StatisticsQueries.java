/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.vladimir.queries;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import org.vladimir.model.PlayerStatistic;

/**
 *
 * @author Vladimir
 */
public class StatisticsQueries {
    private final DataSource dataSource;
    private static final String URL ="jdbc/WebServerDB";
    public static final int TIE = 0;
    public static final int WIN = 1;
    public static final int LOSS = 2;
    
public StatisticsQueries() throws NamingException{  
    Context context = new InitialContext();
    dataSource = (DataSource) context.lookup(URL);
}
    

public int updateStatistics(String playerName, String gameName, int gameResult, int playerScore,
        int oppScore) throws SQLException{
    int result = 0;
    System.out.println("org.vladimir.queries.StatisticsQueries.updateStatistics()");
    
    Connection connection = null;
    
        try {
            PlayerStatistic oldPS = getStatistics(playerName,gameName);
            if(oldPS == null){
                addPlayer(playerName, gameName);
                oldPS = getStatistics(playerName,gameName);
            }
            oldPS.gamesAmount++;
            switch (gameResult) {
                case WIN:
                    oldPS.wins++;
                    oldPS.lastGameResult = "WIN";
                    break;
                case LOSS:
                    oldPS.losses++;
                    oldPS.lastGameResult = "LOSS";
                    break;
                case TIE:
                    oldPS.ties++;
                    oldPS.lastGameResult = "TIE";
                    break;
                default:
                    break;
            }
            if(oldPS.highestScore<playerScore) oldPS.highestScore = playerScore;

            java.util.Date currentDate = new java.util.Date();
            Timestamp timestamp = new Timestamp(currentDate.getTime());
            System.out.println("DATE = "+currentDate);
            System.out.println("SQLDATE = "+timestamp);
            System.out.println("SQLDATE++ = "+oldPS.lastTimePlayed);
            
            oldPS.lastGamePlayerScore = playerScore;
            oldPS.lastGameOppScore = oppScore;

            connection = dataSource.getConnection();
            PreparedStatement updateStatement = connection.prepareStatement(
                    "UPDATE STATISTICS SET GAMES_AMOUNT = ?, WINS = ?, LOSSES = ?, TIES = ?, "
                            + "HIGHEST_SCORE = ?, LAST_PLAYED = ?, LAST_GAME_RESULT = ?, "
                            + "LAST_GAME_PLAYER_SCORE = ?, LAST_GAME_OPP_SCORE = ? "
                            + "WHERE PLAYER = ? AND GAME = ?");
            updateStatement.setInt(1, oldPS.gamesAmount);
            updateStatement.setInt(2, oldPS.wins);
            updateStatement.setInt(3, oldPS.losses);
            updateStatement.setInt(4, oldPS.ties);
            updateStatement.setInt(5, oldPS.highestScore);
            updateStatement.setTimestamp(6, timestamp);
            updateStatement.setString(7, oldPS.lastGameResult);
            updateStatement.setInt(8, oldPS.lastGamePlayerScore);
            updateStatement.setInt(9, oldPS.lastGameOppScore);
            updateStatement.setString(10, oldPS.playerName);
            updateStatement.setString(11, oldPS.gameName);
            result = updateStatement.executeUpdate();
            
            
        } finally{
            close(connection);
            
        }
        
        return result;
}

public int addPlayer(String playerName, String gameName) throws SQLException{
    Connection connection = null;
    int result = 0;
    try{
        connection = dataSource.getConnection();
        PreparedStatement addStatement = connection.prepareStatement("INSERT "
                + "INTO STATISTICS (PLAYER, GAME, GAMES_AMOUNT, WINS, LOSSES, TIES) "
                +"VALUES (?, ?, ?, ?, ?, ?)");
        addStatement.setString(1, playerName);
        addStatement.setString(2, gameName);
        addStatement.setInt(3, 0);
        addStatement.setInt(4, 0);
        addStatement.setInt(5, 0);
        addStatement.setInt(6, 0);
    
        addStatement.executeUpdate();
    }finally{
        close(connection);
    }
    return result;
}
    
        
        
public PlayerStatistic getStatistics(String playerName, String gameName) throws SQLException{
    PlayerStatistic ps = null;
    ResultSet resultSet = null;
    Connection connection = null;
        try {
            connection = dataSource.getConnection();
            PreparedStatement getStats = connection.prepareStatement("SELECT * "
                    + "FROM STATISTICS WHERE PLAYER = ? AND GAME = ?");
            getStats.setString(1,playerName);
            getStats.setString(2,gameName);
            resultSet = getStats.executeQuery();
            if(resultSet.next()){
                ps = new PlayerStatistic();
                ps.playerName = playerName;
                ps.gameName = gameName;
                ps.gamesAmount = resultSet.getInt("GAMES_AMOUNT");
                ps.wins = resultSet.getInt("WINS");
                ps.losses = resultSet.getInt("LOSSES");
                ps.ties = resultSet.getInt("TIES");
                ps.highestScore = resultSet.getInt("HIGHEST_SCORE");
                ps.lastTimePlayed = resultSet.getTimestamp("LAST_PLAYED");
                
                ps.lastGameResult = resultSet.getString("LAST_GAME_RESULT");
                ps.lastGamePlayerScore = resultSet.getInt("LAST_GAME_PLAYER_SCORE");
                ps.lastGameOppScore = resultSet.getInt("LAST_GAME_OPP_SCORE");
                
            }
        } finally{
            if(resultSet!= null)
                resultSet.close();
            close(connection);
        }
        
    
    return ps;
}
    
private void close(Connection connection) {
        try {
            connection.close();
        } catch (SQLException ex) {
            Logger.getLogger(PlayerQueries.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
}
