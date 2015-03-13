package net.kennux.cubicworld.pluginapi.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <pre>
 * The event notation can get used to register event handler functions.
 * 
 * Currently implemented Events:
 * - update(boolean isServer)
 * </pre>
 * 
 * @author KennuX
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
// on class level
public @interface Event
{
	public String eventName() default "";
}
