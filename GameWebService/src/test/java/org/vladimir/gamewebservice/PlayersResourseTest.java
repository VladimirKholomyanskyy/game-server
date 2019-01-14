/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.vladimir.gamewebservice;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Assert;
import org.junit.Test;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
/**
 *
 * @author Vladimir
 */
public class PlayersResourseTest extends JerseyTest {

    
    @Override
    protected Application configure() {
        return new ResourceConfig(PlayersResource.class);
    }
    
    
    @Test
    public void testLogin(){
//        Client client = ClientBuilder.newClient();
//        Response result = client.target("http://localhost:8080/GameWebService/webresources/players/login").request(MediaType.APPLICATION_JSON).get();
//        Response expected = Response.status(Response.Status.UNAUTHORIZED).build();
//        
//        Assert.assertEquals(expected.getStatus(), result.getStatus());
//        System.out.println("helll");
    }
}
