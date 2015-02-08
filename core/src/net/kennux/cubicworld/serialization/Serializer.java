package net.kennux.cubicworld.serialization;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import net.kennux.cubicworld.serialization.annotations.SerializerField;
import net.kennux.cubicworld.util.ConsoleHelper;

/**
 * <pre>
 * The serializer base class.
 * This serializer can get used to serialize object whose classes got the @SerializerField annotation on it's members.
 * </pre>
 * 
 * @author KennuX
 *
 */
@SuppressWarnings("rawtypes")
public class Serializer
{
	/**
	 * The field used to store serializer field info.
	 * 
	 * @author KennuX
	 *
	 */
	private static class SerializerFieldInfo
	{
		public Field field;
		public SerializationTypes type;

		public SerializerFieldInfo(Field field, SerializationTypes type)
		{
			this.field = field;
			this.type = type;
		}
	}

	/**
	 * Holds all serializer instances.
	 */
	private static HashMap<Class, ISerializer> serializers = new HashMap<Class, ISerializer>();

	/**
	 * Generates a serializer just in time and saves it to serializers.
	 * If there is already a serializer, this function will return the existing one.
	 * 
	 * @param clazz
	 * @return
	 */
	private static ISerializer getSerializer(final Class clazz)
	{
		// The serializer
		ISerializer serializer = serializers.get(clazz);

		// was the serializer already in list?
		if (serializer == null)
		{
			// Get all annotations
			Field[] fields = clazz.getFields();

			// All serializer fields with their order as key
			HashMap<Integer, Field> serializerFields = new HashMap<Integer, Field>();
			ArrayList<Integer> annotationOrders = new ArrayList<Integer>();

			// Iterate through all fields
			for (Field f : fields)
			{
				if (f.isAnnotationPresent(SerializerField.class))
				{
					f.setAccessible(true);
					SerializerField annotation = f.getAnnotation(SerializerField.class);

					if (annotationOrders.contains(annotation.order()))
					{
						ConsoleHelper.writeLog("ERROR", "Duplicate serializer field entry: " + f.getName(), "Serializer");
					}

					serializerFields.put(annotation.order(), f);
					annotationOrders.add(annotation.order());
				}
			}

			// Create annotation order list and sort it
			int[] orders = new int[annotationOrders.size()];
			int counter = 0;
			for (Integer i : annotationOrders)
			{
				orders[counter] = i.intValue();
				counter++;
			}

			Arrays.sort(orders);

			// Create array for saving serialization info
			final SerializerFieldInfo[] serializerFieldInfo = new SerializerFieldInfo[orders.length];

			// Now iterate through all serializer fields
			for (int i : orders)
			{
				Field field = serializerFields.get(i);
				SerializerField annotation = field.getAnnotation(SerializerField.class);
				serializerFieldInfo[i] = new SerializerFieldInfo(field, annotation.type());
			}

			// Create anonymous serializer
			serializer = new ISerializer()
			{
				@Override
				public void serialize(BitWriter writer, Object object)
				{
					// Iterate through every serializer field info
					for (SerializerFieldInfo fieldInfo : serializerFieldInfo)
					{
						try
						{
							writer.writeField(fieldInfo.type, fieldInfo.field.get(object));
						}
						catch (IllegalArgumentException | IllegalAccessException e)
						{
							ConsoleHelper.writeLog("ERROR", "Error while serializing object of type " + clazz.getName(), "Serializer");
						}
					}
				}

				@Override
				public Object deserialize(BitReader reader)
				{
					// Instantiate new object
					Object newObject = null;
					try
					{
						newObject = clazz.newInstance();
					}
					catch (InstantiationException | IllegalAccessException e)
					{
						ConsoleHelper.writeLog("ERROR", "Error while instantiating object of type " + clazz.getName() + ". No constuctor without parameters?", "Serializer");
						return null;
					}

					// Iterate through every serializer field info
					for (SerializerFieldInfo fieldInfo : serializerFieldInfo)
					{
						try
						{
							fieldInfo.field.set(newObject, reader.readField(fieldInfo.type));
						}
						catch (IllegalArgumentException | IllegalAccessException e)
						{
							ConsoleHelper.writeLog("ERROR", "Error while deserializing object of type " + clazz.getName(), "Serializer");
						}
					}

					return newObject;
				}

			};

			serializers.put(clazz, serializer);
		}

		return serializer;
	}

	/**
	 * Serializes the given object.
	 * 
	 * @param object
	 * @return
	 */
	public static <T> void serialize(BitWriter writer, T object)
	{
		ISerializer serializer = getSerializer(object.getClass());
		serializer.serialize(writer, object);
	}

	/**
	 * Serializes the given object.
	 * 
	 * @param object
	 * @return
	 */
	public static <T> T deserialize(BitReader reader, Class<T> typeClass)
	{
		ISerializer serializer = getSerializer(typeClass);
		return (T) serializer.deserialize(reader);
	}
}
