/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.vladimir.filters;

import java.sql.SQLException;
import java.util.Base64;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.NamingException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import org.vladimir.namebindings.Secured;
import org.vladimir.queries.PlayerQueries;

/**
 *
 * @author Vladimir
 */
@Provider
@Secured
public class SecurityFilter implements ContainerRequestFilter {
    private static final String AUTHORIZATION_HEADER_KEY = "Authorization";
    private static final String AUTHORIZATION_HEADER_PREFIX = "Basic ";
    private static final Logger LOGGER = Logger.getLogger(SecurityFilter.class.getName());
    
    @Override
    public void filter(ContainerRequestContext requestContext) {
        
        String authHeader = requestContext.getHeaderString(AUTHORIZATION_HEADER_KEY);
        if(!(authHeader == null || "".equals(authHeader))){
            
            try {
                authHeader = authHeader.replaceFirst(AUTHORIZATION_HEADER_PREFIX, "");
                String decodedString = new String (Base64.getDecoder().decode(authHeader));
                StringTokenizer tokenizer = new StringTokenizer(decodedString, ":");
                String name = tokenizer.nextToken();
                String password = tokenizer.nextToken();
                LOGGER.info(String.format("Method: filter; name = %s; password = %s", name,password));
                PlayerQueries query = new PlayerQueries();
                if(!password.equals(query.getPlayerPassword(name))){
                    LOGGER.info(String.format("Method: filter; name = %s; password = %s; FORBIDDEN", name,password));
                    requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
                } else {
                }
            } catch (SQLException | NamingException ex) {
                Logger.getLogger(SecurityFilter.class.getName()).log(Level.SEVERE, null, ex);
                requestContext.abortWith(Response.status(Response.Status.INTERNAL_SERVER_ERROR).build());
            }catch (NoSuchElementException e){
                requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
            }

            
        }
        else{
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
        }
    }   
    
}
