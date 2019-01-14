
package org.vladimir.namebindings;

/**
 * Name binding that binds all methods annotated with @Secured to
 * SecurityFilter, that checks if client is registered user of the server.
 * @author Vladimir
 */


import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import javax.ws.rs.NameBinding;


@NameBinding
@Retention(RetentionPolicy.RUNTIME)
public @interface Secured{}