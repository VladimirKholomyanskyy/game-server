
package org.vladimir.service;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.StringTokenizer;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.HttpHeaders;
import org.vladimir.model.Player;

/**
 *  Service class with useful methods
 * @author Vladimir
 */
public class ServiceClass {
    private static final String AUTHORIZATION_HEADER_KEY = "Authorization";
    private static final String AUTHORIZATION_HEADER_PREFIX = "Basic ";
    
    
    public static Player extractPlayerFromAuthHeader(HttpHeaders header){
        Player player = null;
        String authHeader = header.getHeaderString(AUTHORIZATION_HEADER_KEY);
        Map<String,String> map = extractFromString(authHeader);
        if(!map.isEmpty()){
            player = new Player(map.get("name"));
        }
        return player;
    }
    
   
    
    
    /**
     * Extracts from name of player and his password from string
     * @param authHeader - string from authorization header
     * @return player name and password
     */
    private static Map<String,String> extractFromString(String authHeader){
        Map<String,String> map = new HashMap<>();
        if(authHeader != null){
            authHeader = authHeader.replaceFirst(AUTHORIZATION_HEADER_PREFIX, "");
            String decodedString = new String (Base64.getDecoder().decode(authHeader));
            StringTokenizer tokenizer = new StringTokenizer(decodedString, ":");
            String name = tokenizer.nextToken();
            String password = tokenizer.nextToken();
            map.put("name", name);
            map.put("password", password);
        }
        return map;
    }
}
