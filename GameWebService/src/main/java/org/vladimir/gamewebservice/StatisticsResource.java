/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.vladimir.gamewebservice;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.NamingException;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.vladimir.model.PlayerStatistic;
import org.vladimir.namebindings.Secured;
import org.vladimir.queries.StatisticsQueries;

/**
 * REST Web Service
 *
 * @author Vladimir
 */
@Path("playersstats")
public class StatisticsResource {
    private static final Logger LOGGER = Logger.getLogger(StatisticsResource.class.getName());
    @Context
    private UriInfo context;

    /**
     * Creates a new instance of StatisticsResource
     */
    public StatisticsResource() {
    }

    /**
     * Retrieves representation of an instance of org.vladimir.gamewebservice.StatisticsResource
     * @param playerName
     * @param gameName
     * @return an instance of java.lang.String
     */
    @GET
    @Path("{gameName}/{playerName}")
    @Secured
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPlayerStats(@PathParam("playerName") String playerName,
            @PathParam("gameName") String gameName) {
        Response response;
        LOGGER.info(String.format("getPlayerStats(playerName = %s, gameName = %s)", playerName,gameName));
        try {
            StatisticsQueries queries = new StatisticsQueries();
            //queries.updateStatistics(playerName, gameName, StatisticsQueries.WIN, 50, 15);
            PlayerStatistic statistics = queries.getStatistics(playerName, gameName);
            if(statistics == null){
                queries.addPlayer(playerName, gameName);
                statistics = queries.getStatistics(playerName, gameName);
            }
            response = Response.ok(statistics, MediaType.APPLICATION_JSON).build();
        } catch (NamingException | SQLException ex) {
            Logger.getLogger(StatisticsResource.class.getName()).log(Level.SEVERE, null, ex);
            response = Response.serverError().build();
        }
        return response;
        
    }
    
    @PUT
    @Path("{gameName}")
    @Secured
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response updatePlayerStats(@PathParam("gameName") String gameName,
            @FormParam("playerName") @NotNull String playerName, 
            @FormParam("gameResult") @NotNull String gameResult,
            @FormParam("playerScore") @NotNull int playerScore,
            @FormParam("oppScore") @NotNull int oppScore){
       
        
        LOGGER.info(String.format("updatePlayerStats(playerName = %s,"
                + " gameName = %s, gameResult = %s, playerScore = %d, oppScore = %d)",
                playerName,gameName,gameResult, playerScore,oppScore));
        Response response;
        int gResult;
       
        if("WIN".equals(gameResult)) gResult = StatisticsQueries.WIN;
        else if("TIE".equals(gameResult)) gResult = StatisticsQueries.TIE;
        else if ("LOSS".equals(gameResult)) gResult = StatisticsQueries.LOSS;
        else return Response.status(Response.Status.BAD_REQUEST).build();
        
        
        try {
            StatisticsQueries sq = new StatisticsQueries();
            sq.updateStatistics(playerName, gameName, gResult, playerScore, oppScore);
            response = Response.ok().build();
        } catch (SQLException | NamingException ex) {
            response = Response.serverError().build();
            Logger.getLogger(StatisticsResource.class.getName()).log(Level.SEVERE, null, ex);
        }
        return response;
    }

   
}
