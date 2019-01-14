/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.vladimir.model;


import javax.ws.rs.container.AsyncResponse;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * Represents game between two players. 
 * Players exchange information about theirs move through the asyncResponse.
 * One player waits on asyncResponse for info from second player. When second 
 * player finishes his turn he sends info about his turn to first player through
 * asyncResponce. And after that he waits on asyncResponse for first player turn.
 * @author Vladimir
 */
@XmlRootElement
public class RunningGame{
    private Player firstPlayer;
    private Player secondPlayer;
    private Player nextTurn;
   
    @XmlTransient
    private AsyncResponse asyncResponse; 

    public RunningGame() {}

    
    public RunningGame(Player firstPlayer, Player secondPlayer,Player nextTurn) {   
        this.firstPlayer = firstPlayer;
        this.secondPlayer = secondPlayer;
        this.nextTurn = nextTurn;
            }

    public void setFirstPlayer(Player firstPlayer) {
        this.firstPlayer = firstPlayer;
    }

    public void setSecondPlayer(Player secondPlayer) {
        this.secondPlayer = secondPlayer;
    }

    public void setNextTurn(Player nextTurn) {
        this.nextTurn = nextTurn;
    }
  
    public Player getFirstPlayer() {
        return firstPlayer;
    }

    public Player getSecondPlayer() {
        return secondPlayer;
    }

    public Player getNextTurn() {
        return nextTurn;
    }

    public void switchNextPlayer(){      
        if(firstPlayer.equals(nextTurn)){
            nextTurn = secondPlayer;
        }else{
            nextTurn = firstPlayer;
        }
    }
    
    public boolean isOpponentAwaits(){
        return !(asyncResponse == null || !asyncResponse.isSuspended());
    }
    
    public boolean isPlayerInGameAndCanRequest(Player player){
        return (!nextTurn.equals(player)
                && (firstPlayer.equals(player) 
                || secondPlayer.equals(player)));
    }
    
    
    public boolean playerInGame(Player player){
        return firstPlayer.equals(player)||secondPlayer.equals(player);
    }
       
    public void close(){
        if(asyncResponse!=null)
            asyncResponse.cancel();
    }
    
    public void sent(Object object){
        asyncResponse.resume(object);
    }
    
    
    public boolean isPlayerTurn(Player player){
        return nextTurn.equals(player);
    }
    
    
    
    public void setAsyncResponse(AsyncResponse asyncResponse) {
        this.asyncResponse = asyncResponse;
    }

         

    @Override
    public String toString() {
        return "RunningGame{" + "firstPlayer=" + firstPlayer + ", secondPlayer=" + secondPlayer + ", nextTurn=" + nextTurn  + '}';
    }
    
}
