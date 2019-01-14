/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.vladimir.model;

import javax.json.JsonObject;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Vladimir
 */
@XmlRootElement
public class TurnResponse {
    public static final int NOT_YOUR_TURN = 1;
    public static final int NO_RECEIVER = 2;
    public static final int SUCCESS = 3;
    public static final int OPPONENT_HAS_GONE = 4;
    public static final int GAME_IS_FINISHED = 5;
    private JsonObject gameData;
    private int result;

    public TurnResponse() {
    }
    
    
    public TurnResponse(int result){
        this.result = result;
    }

    public TurnResponse(JsonObject gameData, int result) {
        this.gameData = gameData;
        this.result = result;
    }

    public JsonObject getGameData() {
        return gameData;
    }

    public void setGameData(JsonObject gameData) {
        this.gameData = gameData;
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }
    
    
    
}
