/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.vladimir.collections;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;
import javax.ws.rs.container.AsyncResponse;
import org.vladimir.model.OpenedGame;
import org.vladimir.model.Player;

/**
 *
 * @author Vladimir
 */
public class GameQueue {
    private Map<String,ConcurrentLinkedQueue<OpenedGame>> queues;
    private static final Logger LOGGER = Logger.getLogger(GameQueue.class.getName());

    public GameQueue() {
        queues = new HashMap<>();
    }
    
    /**
     *
     * @param host
     * @param asyncResponse
     * @param gameName
     * @return
     */
    public boolean addGameToGameQueue(String gameName, Player host, AsyncResponse asyncResponse){
        OpenedGame openedGame = new OpenedGame(asyncResponse, host);
        ConcurrentLinkedQueue queue = queues.get(gameName);   
        if(queue == null){
            queue = new ConcurrentLinkedQueue<>();
            queues.put(gameName, queue);
            LOGGER.info(String.format("GameQueue.addGameToGameQueue(gameName = %s,"
                    + "host = %s){queue.size = %d}; game added to the queue", gameName, host.getName(),queue.size()));
            return queue.add(openedGame);
        }else {
            OpenedGame removedGameFromTheQueue = removeGameFromTheQueue(gameName, host);
            if(removedGameFromTheQueue != null)
                removedGameFromTheQueue.close();
              
            LOGGER.info(String.format("GameQueue.addGameToGameQueue(gameName = %s,"
                    + "host = %s){queue.size = %d}; game added to the queue", gameName, host.getName(),queue.size()));
            return queue.add(openedGame);
        }  
    }
    
    public OpenedGame pollGame(String gameName){
        ConcurrentLinkedQueue<OpenedGame> queue = queues.get(gameName);
        if(queue == null) return null;
        LOGGER.info(String.format("GameQueue.addGameToGameQueue(gameName = %s)"
                    + "{queue.size = %d}; game polled from the queue", gameName,queue.size()));
        return queue.poll();       
    }
    
    public OpenedGame removeGameFromTheQueue(String gameName,Player host){
        ConcurrentLinkedQueue<OpenedGame> queue = queues.get(gameName);
        if(queue == null)
            return null;
        OpenedGame openedGame = new OpenedGame(host);
        for (OpenedGame next : queue) {
            if(openedGame.equals(next)){
                openedGame = next;
                break;
            }          
        }
        if(queue.remove(openedGame)){
            LOGGER.info(String.format("GameQueue.removeGameFromTheQueue(gameName = %s;"
                    + "host = %s){queue.size = %d} game removed from the queue", gameName,host.getName(),queue.size()));
            return openedGame;
        }
        return null;
    }
    
    
    public boolean isPlayerInQueue(String gameName, Player host){
        ConcurrentLinkedQueue<OpenedGame> queue = queues.get(gameName);
        if(queue != null){
            OpenedGame openedGame = new OpenedGame(host);
            LOGGER.info(String.format("GameQueue.isPlayerInQueue(gameName = %s,"
                    + "host = %s){queue.size = %d}", gameName,host, queue.size()));
            return queue.contains(openedGame);
        }
        return false;
    }
    
//    public int gamesInQueue(String gameName){
//        ConcurrentLinkedQueue<OpenedGame> queue = queues.get(gameName);
//        if(queue == null) return 0;
//        LOGGER.info(String.format("GameQueue.gamesInQueue(gameName = %s){queue.size = %d}",
//                    gameName, queue.size()));
//        return queue.size();
//    }
}
