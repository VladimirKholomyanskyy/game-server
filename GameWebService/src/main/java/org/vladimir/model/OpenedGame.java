
package org.vladimir.model;

import java.util.Objects;
import javax.ws.rs.container.AsyncResponse;

/**
 * Represents a game that doesn't have second player yet.
 * Player who has created a game waits on asynResponce until second player is
 * found or he cancels a game.
 * @author Vladimir
 */
public class OpenedGame {
    private final Player hostPlayer;
    private AsyncResponse asyncResponse;

    public OpenedGame(AsyncResponse asyncResponse, Player hostPlayer) {
        this.asyncResponse = asyncResponse;
        this.hostPlayer = hostPlayer;
    }

    public OpenedGame(Player hostPlayer) {

        this.hostPlayer = hostPlayer;
    }

    public Player getHostPlayer() {
        return hostPlayer;
    }
    
    public boolean isOpen(){
       return asyncResponse!=null && asyncResponse.isSuspended();
    }
       
    public void sent(Object object){  
         asyncResponse.resume(object);
    }
     
    public void close(){
        if(asyncResponse!=null)
            asyncResponse.cancel();
    }
        
    
    
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 19 * hash + Objects.hashCode(this.hostPlayer);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final OpenedGame other = (OpenedGame) obj;
        return Objects.equals(this.hostPlayer, other.hostPlayer);
    }

    
}
