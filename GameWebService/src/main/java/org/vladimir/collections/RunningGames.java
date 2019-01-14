/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.vladimir.collections;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import javax.ws.rs.container.AsyncResponse;
import org.vladimir.model.Player;
import org.vladimir.model.RunningGame;

/**
 *
 * @author Vladimir
 */
public class RunningGames {
    private final AtomicInteger index;
    private final Map<String,ConcurrentHashMap<Integer,RunningGame>> runningGames;
    private static final Logger LOGGER = Logger.getLogger(RunningGames.class.getName());
    
    public RunningGames() {
        this.runningGames = new HashMap<>();
        index = new AtomicInteger(1);
    }
    
    public int addGame(String gameName, Player firstPlayer, Player secondPlayer){
        int gameID = index.getAndIncrement();
        Player nextTurn = whoGoesFirst(firstPlayer, secondPlayer);
        RunningGame game = new RunningGame(firstPlayer, secondPlayer,nextTurn);
        ConcurrentHashMap<Integer,RunningGame> map = runningGames.get(gameName);
        if(map == null){
            map = new ConcurrentHashMap<>();
            map.put(gameID, game);
            runningGames.put(gameName, map);
        }else{
            map.put(gameID, game);
        }
        LOGGER.info(String.format("RunningGames.addGame(gameName = %s, firstPlayer = %s,"
                + " secondPlayer = %s, nextTurn = %s) {map.size = %d; return: gameID = %d}", gameName,firstPlayer.getName(),
                secondPlayer.getName(),nextTurn.getName(),gameID,map.size()));
        return gameID;
    }
    
    public boolean setAsyncresponseToGame(String gameName,int gameID,AsyncResponse asyncResponse){
        ConcurrentHashMap<Integer,RunningGame> map = runningGames.putIfAbsent(gameName, new ConcurrentHashMap<Integer,RunningGame>());
        RunningGame game = map.get(gameID);
        LOGGER.info(String.format("RunningGames.setAsyncresponse(gameName = %s,"
                + " gameID = %d) {map.size = %d}", gameName,gameID,map.size()));
        if(game != null) 
            game.setAsyncResponse(asyncResponse);
        return false;
    }
    
    public RunningGame getGame(String gameName,int gameID){
        ConcurrentHashMap<Integer,RunningGame> map = runningGames.putIfAbsent(gameName, new ConcurrentHashMap<Integer,RunningGame>());
        LOGGER.info(String.format("RunningGames.getGame(gameName = %s,"
                + " gameID = %d) {map.size = %d}", gameName,gameID,map.size()));
        return map.get(gameID);
    }
    
    public RunningGame removeGame(String gameName,int gameID){
        ConcurrentHashMap<Integer,RunningGame> map = runningGames.putIfAbsent(gameName, new ConcurrentHashMap<Integer,RunningGame>());
        LOGGER.info(String.format("RunningGames.removeGameBefore gameName = %s,"
                + " gameID = %d) {map.size = %d}", gameName,gameID,map.size()));
        RunningGame remove = map.remove(gameID);
        LOGGER.info(String.format("RunningGames.removeGameAfter (gameName = %s,"
                + " gameID = %d) {map.size = %d}", gameName,gameID,map.size()));
        return remove;  
    }
    
    public int gamesInQueue(String gameName){
        ConcurrentHashMap<Integer,RunningGame> map = runningGames.putIfAbsent(gameName, new ConcurrentHashMap<Integer,RunningGame>());
        return map.size();
    }
    
    private Player whoGoesFirst(Player p1, Player p2){
        Random random = new Random();
        int result = random.nextInt(2);
        if(result == 0) return p1;
        return p2;
    }
}
