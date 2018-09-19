package jas.core;

import java.lang.annotation.*;

/**
 * Created by Jiachen on 3/27/18.
 * Invocation of @Mutating methods alters the original
 */
@Documented // This way it shows up in JavaDoc
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Mutating {

}
