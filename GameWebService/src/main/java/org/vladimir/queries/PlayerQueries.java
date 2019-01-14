
package org.vladimir.queries;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 * Class for accessing player data base. Player data base contains player name
 * and password. The class access data base through DataSource class, this allows 
 * to use connection from connection pool.
 * @author Vladimir
 */
public class PlayerQueries {
    private final DataSource dataSource;
    private static final String URL ="jdbc/WebServerDB";
        
    /**
     * Looks up for data base GameServer
     * @throws SQLException
     * @throws NamingException
     */
    public PlayerQueries() throws SQLException, NamingException{
        Context context = new InitialContext();
        dataSource = (DataSource)context.lookup(URL);
    }
    
    /**
     * Adds new player to data base
     * @param name - player name, maximum length is 50
     * @param password - player password, maximum length is 30
     * @return
     * @throws SQLException 
     */
    public int addPlayer(String name, String password) throws SQLException{
        int result = 0;
        Connection connection = null;
        try{
            connection = dataSource.getConnection();
            PreparedStatement insertNewPlayer = connection.prepareStatement("INSERT INTO PLAYERS " +
               "(NAME, PASSWORD) " +
               "VALUES (?, ?)");
            
            insertNewPlayer.setString(1, name);
            insertNewPlayer.setString(2, password);
            if(getPlayerPassword(name)==null)
                result = insertNewPlayer.executeUpdate();
            
        }finally{
            close(connection);
        }
        
        return result;
    }

    public String getPlayerPassword(String name) throws SQLException{
        String password = null;
        ResultSet resultSet = null;
        Connection connection = null;
        
        try{
            connection = dataSource.getConnection();
            PreparedStatement getPlayerPassword = connection.prepareStatement(
                    "SELECT PASSWORD FROM PLAYERS WHERE NAME = ?");
            getPlayerPassword.setString(1, name);
            resultSet = getPlayerPassword.executeQuery();
            if(resultSet.next())
                password = resultSet.getString("PASSWORD");
        } 
        finally{
            if(resultSet !=null) resultSet.close();
            close(connection);
        }    
        return password;
    }
    
    public int updatePlayerPassword(String name, String newPassword) throws SQLException{
        int result = 0;
        Connection connection = null;
        try{
            connection = dataSource.getConnection();
            PreparedStatement updatePassword = connection.prepareStatement(
                    "UPDATE PLAYERS SET PASSWORD = ? WHERE NAME = ?");
            updatePassword.setString(1, newPassword);
            updatePassword.setString(2, name);
            result = updatePassword.executeUpdate();
        }
        finally{
            close(connection);
        }
        return result;
    }
    
    
    private void close(Connection connection) {
        try {
            connection.close();
        } catch (SQLException ex) {
            Logger.getLogger(PlayerQueries.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
}
