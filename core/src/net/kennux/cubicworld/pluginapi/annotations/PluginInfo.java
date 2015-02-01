package net.kennux.cubicworld.pluginapi.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The plugin notation can get used to provide information about a plugin.
 * 
 * @author KennuX
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
// on class level
public @interface PluginInfo
{
	public String author() default "Unknown";

	public String pluginName() default "Unknown";
}
