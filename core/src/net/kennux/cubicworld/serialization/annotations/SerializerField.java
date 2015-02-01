package net.kennux.cubicworld.serialization.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.kennux.cubicworld.serialization.SerializationTypes;

/**
 * Annotation for adding fields for serialization.
 * @author KennuX
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface SerializerField
{
	/**
	 * The serialization order.
	 * @return
	 */
	public int order();
	
	/**
	 * The type from SerializationTypes.xxx
	 * @return
	 */
	public SerializationTypes type() default SerializationTypes.BOOLEAN;
}
