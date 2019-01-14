/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.vladimir.gamewebservice;

import java.net.URI;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PUT;
import javax.ws.rs.PathParam;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.CompletionCallback;
import javax.ws.rs.container.ConnectionCallback;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.container.TimeoutHandler;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.vladimir.collections.GameQueue;
import org.vladimir.collections.RunningGames;
import org.vladimir.model.OpenedGame;
import org.vladimir.model.Player;
import org.vladimir.model.RunningGame;
import org.vladimir.model.TurnResponse;
import org.vladimir.namebindings.Secured;
import org.vladimir.service.ServiceClass;

/**
 * REST Web Service
 *
 * @author Vladimir
 */
@Path("gameserver")
public class GameServerResource {
    private static GameQueue gameQueue = new GameQueue();
    private static RunningGames runningGames = new RunningGames();
    private static final int TIME_FOR_TURN = 60; //sec
    private static final int CONNECTION_TIMEOUT = 5;//min
    
    private static final int NOT_YOUR_TURN = 1;
    private static final int NO_RECEIVER = 2;
    private static final int SUCCESS = 3;
    private static final int OPPONENT_HAS_GONE = 4;
    private static final int GAME_IS_FINISHED = 5;
    private static final Logger LOGGER = Logger.getLogger(GameServerResource.class.getName());
    
    @Context
    private UriInfo context;
    @Context
    private HttpHeaders headers;

    /**
     * Creates a new instance of GameServerResource
     */
    public GameServerResource() {
        
    }

    /**
     *
     * @param gameName
     * @return
     */
    @GET
    @Path("games/{gameName}")
    @Secured
    @Produces(MediaType.APPLICATION_JSON)
    public Response joinGame(@PathParam("gameName") String gameName) {
           
        Player secondPlayer = ServiceClass.extractPlayerFromAuthHeader(headers);
        
        LOGGER.info(String.format("GameServer.joinGame(gameName = %s, secondPlayer = %s)",gameName, secondPlayer.getName()));
        
        if(gameQueue.isPlayerInQueue(gameName, secondPlayer))
            return Response.status(Response.Status.CONFLICT).build();
        
        OpenedGame openedGame = gameQueue.pollGame(gameName);
        while(openedGame!=null){
            Player host = openedGame.getHostPlayer();
            if(openedGame.isOpen()){              
                int gameID = runningGames.addGame(gameName, host, secondPlayer);

                if(gameID != 0){
                    URI uri = context.getAbsolutePathBuilder().path(""+gameID).build();
//                    String serverURl = "http://10.0.0.4:8080";
//                    URI u = URI.create(serverURl+uri.getPath());
                    
                    LOGGER.info(uri.toASCIIString());
                    LOGGER.info(uri.toString());
                    LOGGER.info(uri.getPath());
                    
                    Response response = Response.ok(runningGames.getGame(gameName, gameID), MediaType.APPLICATION_JSON)
                            .link(uri, "GameLink").build();
                    openedGame.sent(response);
                    return Response.ok(runningGames.getGame(gameName, gameID), MediaType.APPLICATION_JSON)
                        .link(uri, "GameLink").build();
                }else{
                     LOGGER.severe(String.format("GameServer.joinGame(gameName = %s){host = %s; secondPlayer = %s; gameID = %d}",
                            gameName,host,secondPlayer,gameID));
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
                }
            }
            openedGame = gameQueue.pollGame(gameName);
        }
        
        LOGGER.info(String.format("GameServer.joinGame(gameName = %s){openedGame = NULL}",gameName));
        return Response.status(Response.Status.NOT_FOUND).build();
    }
   
    
    
    @POST
    @Path("games/{gameName}")
    @Secured
    @Produces(MediaType.APPLICATION_JSON)
    public void createGame(@PathParam("gameName") final String gameName,
            @Suspended  AsyncResponse asyncResponse){
        
        final Player host = ServiceClass.extractPlayerFromAuthHeader(headers);
        
        LOGGER.info(String.format("GameServer.createGame(gameName = %s, host = %s)",gameName,host.getName()));
        
        asyncResponse.register(new ConnectionCallback() {
            @Override
            public void onDisconnect(AsyncResponse ar) {
                LOGGER.info(String.format("GameServer.createGame(gameName = %s, host = %s).onDisconnect()",gameName,host.getName()));
                gameQueue.removeGameFromTheQueue(gameName, host);
                
                ar.cancel();
            }
        });
        
        asyncResponse.register(new CompletionCallback() {
            @Override
            public void onComplete(Throwable thrwbl) {
                if(thrwbl!=null){
                    LOGGER.info(String.format("GameServer.createGame(gameName = %s,host = %s).onComplete()"
                            + " {message = %s}",gameName,host.getName(), thrwbl.getMessage()));
                    
                    gameQueue.removeGameFromTheQueue(gameName, host).close();
                }                 
            }
        });
        
        asyncResponse.register(new TimeoutHandler() {
            @Override
            public void handleTimeout(AsyncResponse ar) {
                gameQueue.removeGameFromTheQueue(gameName, host);
                ar.resume(Response.status(Response.Status.SERVICE_UNAVAILABLE).build());
            }
        });
        
        try{
            int timeout = Integer.parseInt(headers.getHeaderString("timeout"));
            asyncResponse.setTimeout(timeout, TimeUnit.SECONDS);
        }catch(NumberFormatException e){
            asyncResponse.setTimeout(CONNECTION_TIMEOUT, TimeUnit.MINUTES);
        }
        LOGGER.info(String.format("GameServer.createGame(gameName = %s){ host = %s} END",gameName,host.getName()));
        
        gameQueue.addGameToGameQueue(gameName, host, asyncResponse);
    }
    
    
    @DELETE
    @Path("games/{gameName}")
    @Secured
    public Response exitGameQueue(@PathParam("gameName") String gameName){
        Response response;
        Player host = ServiceClass.extractPlayerFromAuthHeader(headers);
        OpenedGame openedGame = gameQueue.removeGameFromTheQueue(gameName, host);
        if(openedGame == null){
            LOGGER.info(String.format("GameServer.exitGameQueue(gameName = %s) "
                    + "{host = %s; openedGame = null}", gameName,host.getName()));
            response = Response.status(Response.Status.NOT_FOUND).build();
        }else{
            LOGGER.info(String.format("GameServer.exitGameQueue(gameName = %s) "
                    + "{host = %s} Game removed from the queue", gameName,host.getName()));
            
            openedGame.close();
            response = Response.ok().build();
        }
        return response;
    }
    
    
    @DELETE
    @Path("games/{gameName}/{gameID}")
    @Secured
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response endGame(@PathParam("gameName") String gameName,
            @PathParam("gameID") int gameID){
        
       
        Player player = ServiceClass.extractPlayerFromAuthHeader(headers);
        RunningGame game = runningGames.getGame(gameName, gameID);
        if(game!=null && game.playerInGame(player)){
            RunningGame removeGame = runningGames.removeGame(gameName, gameID);
            if(removeGame.isPlayerTurn(player)){               
                if(removeGame.isOpponentAwaits()){
                    removeGame.sent(new TurnResponse(TurnResponse.OPPONENT_HAS_GONE));
                }
            }else{
                removeGame.close();
            } 
            return Response.ok(new TurnResponse(TurnResponse.GAME_IS_FINISHED)).build();
        }else{
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        
    }
    
    
    @GET
    @Path("games/{gameName}/{gameID}/turns")
    @Secured
    @Produces(MediaType.APPLICATION_JSON)
    public void getTurn(@Suspended AsyncResponse asyncresponse,
            @PathParam("gameName") final String gameName,
            @PathParam("gameID") final int gameID){
        Player player = ServiceClass.extractPlayerFromAuthHeader(headers);
        RunningGame game = runningGames.getGame(gameName, gameID);
        
        LOGGER.info(String.format("GameServer.getTurn(gameName = %s, gameID = %d) ",gameName,gameID));
        
        if(game == null){
            LOGGER.info(String.format("GameServer.getTurn(gameName = %s, gameID = %d) "
                    + "{player = %s}; Game not found",gameName,gameID,player.getName()));
            asyncresponse.resume(Response.status(Response.Status.NOT_FOUND).build());
        }else{
            LOGGER.info(String.format("GameServer.getTurn(gameName = %s, gameID = %d) "
                    + "{player = %s}{game = %s};",gameName,gameID,player.getName(),game));
            
            if(game.isPlayerInGameAndCanRequest(player)){
                
                LOGGER.info(String.format("GameServer.getTurn(gameName = %s, gameID = %d) "
                    + "{player = %s}; Game has been found",gameName,gameID,player.getName()));
                
            asyncresponse.register(new ConnectionCallback() {
                @Override
                public void onDisconnect(AsyncResponse ar) {
                    LOGGER.info(String.format("GameServer.getTurn(gameName = %s, gameID = %d).onDisconnect() ",
                        gameName,gameID));
                    ar.cancel();
                }
            });
            asyncresponse.register(new TimeoutHandler() {
                @Override
                public void handleTimeout(AsyncResponse ar) {
                    runningGames.removeGame(gameName, gameID);
                    LOGGER.info(String.format("Method: getTurn/handleTimeout; gameName = %s; gameID = %d; "
                    + "game removed",gameName,gameID));
                    ar.resume(new TurnResponse(TurnResponse.OPPONENT_HAS_GONE));
                }
            });
            asyncresponse.register(new CompletionCallback() {
                    @Override
                    public void onComplete(Throwable thrwbl) {
                        if(thrwbl != null){
                            runningGames.removeGame(gameName, gameID);
                            LOGGER.info(String.format("Method: getTurn/CompletionCallback; gameName = %s; gameID = %d; "
                    + "game removed",gameName,gameID));
                        }
                    }
                });
             try{
                 LOGGER.info(String.format("Timeout set to = %s", headers.getHeaderString("timeout")));
                 int timeout = Integer.parseInt(headers.getHeaderString("timeout"));
                 asyncresponse.setTimeout(timeout, TimeUnit.SECONDS);
                 LOGGER.info(String.format("Timeout set to = %d", timeout));
             }catch(NumberFormatException e){
                 asyncresponse.setTimeout(TIME_FOR_TURN, TimeUnit.SECONDS);
             }
            
            
            game.setAsyncResponse(asyncresponse);
            }
            else{
                asyncresponse.resume(Response.status(Response.Status.CONFLICT).build());
            }
        }
        
    }
    
    
    @PUT
    @Path("games/{gameName}/{gameID}/turns")
    @Secured
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response makeTurn(JsonObject gameData,
            @PathParam("gameName") String gameName,
            @PathParam("gameID") int gameID) {
        
        LOGGER.info(String.format("GameServer.makeTurn(gameName = %s, gameID = %d) ",gameName,gameID));
        Response response;
        RunningGame game = runningGames.getGame(gameName, gameID);
        Player player = ServiceClass.extractPlayerFromAuthHeader(headers);
        
        if(game != null){
            if(game.isPlayerTurn(player)){
                LOGGER.info(String.format("GameServer.makeTurn(gameName = %s; gameID = %d)"
                        + "{player = %s}{gameData = %s} Game is found", gameName,
                        gameID,player.getName(),gameData.toString()));
                
                if(game.isOpponentAwaits()){
                    game.switchNextPlayer();
                    JsonObject jsonObject = Json.createObjectBuilder()
                            .add("gameData", gameData).add("result", TurnResponse.SUCCESS).build();
                    game.sent(jsonObject);
                    
                    response = Response.ok(jsonObject,
                            MediaType.APPLICATION_JSON).build();
                }else{
                    response = Response.ok(new TurnResponse(TurnResponse.NO_RECEIVER),
                            MediaType.APPLICATION_JSON).build();
                }
                
            }else{
                response = Response.ok(new TurnResponse(TurnResponse.NOT_YOUR_TURN),
                        MediaType.APPLICATION_JSON).build();
            }
        }
        else{
            LOGGER.info(String.format("GameServer.makeTurn(gameName = %s; gameID = %d)"
                    + "{player = %s} Game not found", gameName, gameID,player.getName()));
            response = Response.status(Response.Status.NOT_FOUND).build();
        }
        return response;
        
    }  
}
