
package org.vladimir.model;

import java.sql.Timestamp;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Representation of player statistics
 * @author Vladimir
 */
@XmlRootElement
public class PlayerStatistic {
    public String playerName;
    public String gameName;
    public int gamesAmount;
    public int wins;
    public int losses;
    public int ties;
    public int highestScore;
    public Timestamp lastTimePlayed;
    public String lastGameResult;
    public int lastGamePlayerScore;
    public int lastGameOppScore;

    public PlayerStatistic() {
    }
    
    
    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public int getGamesAmount() {
        return gamesAmount;
    }

    public void setGamesAmount(int gamesAmount) {
        this.gamesAmount = gamesAmount;
    }

    public int getWins() {
        return wins;
    }

    public void setWins(int wins) {
        this.wins = wins;
    }

    public int getLosses() {
        return losses;
    }

    public void setLosses(int losses) {
        this.losses = losses;
    }

    public int getTies() {
        return ties;
    }

    public void setTies(int ties) {
        this.ties = ties;
    }

    public int getHighestScore() {
        return highestScore;
    }

    public void setHighestScore(int highestScore) {
        this.highestScore = highestScore;
    }

    public Timestamp getLastTimePlayed() {
        return lastTimePlayed;
    }

    public void setLastTimePlayed(Timestamp lastTimePlayed) {
        this.lastTimePlayed = lastTimePlayed;
    }

    public String getLastGameResult() {
        return lastGameResult;
    }

    public void setLastGameResult(String lastGameResult) {
        this.lastGameResult = lastGameResult;
    }

    public int getLastGamePlayerScore() {
        return lastGamePlayerScore;
    }

    public void setLastGamePlayerScore(int lastGamePlayerScore) {
        this.lastGamePlayerScore = lastGamePlayerScore;
    }

    public int getLastGameOppScore() {
        return lastGameOppScore;
    }

    public void setLastGameOppScore(int lastGameOppScore) {
        this.lastGameOppScore = lastGameOppScore;
    }
    
    
    
   
}
