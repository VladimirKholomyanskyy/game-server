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
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PUT;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.vladimir.namebindings.Secured;
import org.vladimir.queries.PlayerQueries;

/**
 * REST Web Service
 *
 * @author Vladimir
 */



@Path("players")
public class PlayersResource {

    @Context
    private UriInfo context;

    /**
     * Creates a new instance of PlayersResource
     */
    public PlayersResource() {
    }

    
    
    
    
    @GET
    @Path("login")
    @Secured
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(){
        return Response.ok().build();
    }
    /**
     * PUT method for updating or creating an instance of PlayersResource
     * @param name
     * @param oldPassword
     * @param newPassword
     * @return 
     */
    
    @PUT
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON) 
    public Response updatePassword (@FormParam("Name")
                                    @NotNull
                                    @Size(min = 3, max = 50)
                                    @Pattern(regexp = "[a-zA-Z]\\w*")
                                    String name,
                                    @FormParam("OldPassword")
                                    @NotNull
                                    @Size(min = 6, max = 30)
                                    @Pattern(regexp = "\\w*")
                                    String oldPassword,
                                    @FormParam("NewPassword")
                                    @NotNull
                                    @Size(min = 6, max = 30)
                                    @Pattern(regexp = "\\w*")
                                    String newPassword) {
        
        Response response;
        try {
            PlayerQueries query = new PlayerQueries();
            String playerPasswordInDB = query.getPlayerPassword(name);
            if(oldPassword.equals(playerPasswordInDB)){
                query.updatePlayerPassword(name, newPassword);
                response = Response.status(Response.Status.NO_CONTENT).build();              
            }
            else{
                response = Response.status(Response.Status.UNAUTHORIZED).build();
            }
        } catch (SQLException | NamingException ex) {
            Logger.getLogger(PlayersResource.class.getName()).log(Level.SEVERE, null, ex);
            response = Response.serverError().build();
        }
        return response;
        
    }
    
    /**
     *
     * @param name
     * @param password
     * @return
     */
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)        
    public Response register(@FormParam("Name")
                             @NotNull
                             @Size(min = 3, max = 50)
                             @Pattern(regexp = "[a-zA-Z]\\w*")
                             String name,
                             @FormParam("Password")
                             @NotNull
                             @Size(min = 6, max = 30)
                             @Pattern(regexp = "\\w*")
                             String password){
        
        Response response;
        try {
            PlayerQueries query = new PlayerQueries();
            int result = query.addPlayer(name, password);
            if(result != 0){
                response = Response.status(Response.Status.CREATED).build();
            }else{
                response = Response.status(Response.Status.CONFLICT).build();
            }
            
        } catch (SQLException | NamingException ex) {
            Logger.getLogger(GameServerResource.class.getName()).log(Level.SEVERE, null, ex);
            response = Response.serverError().build();
        }
        return response;
    }
}
