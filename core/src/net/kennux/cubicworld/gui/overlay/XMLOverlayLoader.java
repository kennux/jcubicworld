package net.kennux.cubicworld.gui.overlay;

import java.io.IOException;
import java.util.HashMap;
import java.util.zip.DataFormatException;

import net.kennux.cubicworld.gui.GuiHelper;
import net.kennux.cubicworld.util.ConsoleHelper;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;

/**
 * <pre>
 * This class handles gui overlay loading from the assets folder.
 * It can load xml files in the cubicworld's gui xml format.
 * 
 * </pre>
 * 
 * @author kennux
 *
 */
public class XMLOverlayLoader
{
	/**
	 * <pre>
	 * Loads a gui overlay from the given gdx filehandle.
	 * Static attributes:
	 * 
	 * - id (String) Identifier
	 * - width (float) Width in percentage
	 * - height (float) Height in percentage
	 * - position-x (float) Position x in percentage
	 * - position-y (float) Position y in percentage
	 * 
	 * </pre>
	 * 
	 * @param fileHandle
	 * @return
	 * @throws IOException
	 */
	public static Overlay loadOverlay(FileHandle fileHandle) throws IOException, DataFormatException
	{
		// Read and parse file
		XmlReader xmlReader = new XmlReader();
		Element rootNode = xmlReader.parse(fileHandle);

		// Is this the rootnode?
		if (rootNode.getName().equals("overlay"))
		{
			ObjectMap<String, String> rootAttributes = rootNode.getAttributes();

			// Start interpreting
			float widthPercent = Float.parseFloat(rootAttributes.get("width").replace("%", ""));
			float heightPercent = Float.parseFloat(rootAttributes.get("height").replace("%", ""));

			// Calculate absolute position
			Vector2 absoluteSize = GuiHelper.getAbsoluteFromPercentagePosition(new Vector2(widthPercent, heightPercent));
			Vector2 absolutePosition = new Vector2(0, 0);

			boolean alignCenter = false;
			if (rootAttributes.containsKey("align"))
				if (rootAttributes.get("align").equals("center"))
					alignCenter = true;

			// Calculate position if aligning
			if (alignCenter)
			{
				absolutePosition = new Vector2((Gdx.graphics.getWidth() / 2) - (absoluteSize.x / 2), (Gdx.graphics.getHeight() / 2) - (absoluteSize.y / 2));
			}

			Overlay overlay = new Overlay();

			// Set overlay settings
			overlay.setSize(absoluteSize);
			overlay.setPosition(absolutePosition);

			// Interpret all elements
			for (int i = 0; i < rootNode.getChildCount(); i++)
			{
				// Get element
				Element element = rootNode.getChild(i);
				ObjectMap<String, String> elementAttributes = element.getAttributes();

				// Get static attributes
				String id = elementAttributes.get("id");
				float elementWidthPercentage = Float.parseFloat(elementAttributes.get("width").replace("%", ""));
				float elementHeightPercentage = Float.parseFloat(elementAttributes.get("height").replace("%", ""));
				float positionXPercentage = Float.parseFloat(elementAttributes.get("position-x").replace("%", ""));
				float positionYPercentage = Float.parseFloat(elementAttributes.get("position-y").replace("%", ""));

				// Calculate absolute values
				Vector2 elementAbsoluteSize = new Vector2(elementWidthPercentage, elementHeightPercentage);

				// Calculate position relative to the gui overlay's origin
				Vector2 elementRelativePosition = GuiHelper.getAbsoluteFromPercentagePosition(new Vector2(positionXPercentage, positionYPercentage), (int) absoluteSize.x, (int) absoluteSize.y);

				// Add the overlay's position to the relative position to get the absolute position
				Vector2 elementAbsolutePosition = new Vector2(elementRelativePosition).add(absolutePosition);

				// Calculate element absolute size by multiplying it with absoluteSize of the overlay
				elementAbsoluteSize.x *= (absoluteSize.x / 100);
				elementAbsoluteSize.y *= (absoluteSize.y / 100);

				// Create element rectangle
				Rectangle absoluteElementRectangle = new Rectangle(elementAbsolutePosition.x, elementAbsolutePosition.y, elementAbsoluteSize.x, elementAbsoluteSize.y);
				Rectangle relativeElementRectangle = new Rectangle(elementRelativePosition.x, elementRelativePosition.y, elementAbsoluteSize.x, elementAbsoluteSize.y);

				/*
				 * switch (element.getName())
				 * {
				 * case "image":
				 * {
				 * // Get values
				 * String texturePath = elementAttributes.get("texture");
				 * 
				 * // Add element to gui overlay
				 * GuiImage img = new GuiImage(absoluteElementRectangle, relativeElementRectangle, texturePath);
				 * 
				 * overlay.addElement(id, img);
				 * 
				 * break;
				 * }
				 * case "textbox":
				 * {
				 * // Add element to gui overlay
				 * Textbox textbox = new Textbox(absoluteElementRectangle, relativeElementRectangle);
				 * 
				 * overlay.addElement(id, textbox);
				 * 
				 * break;
				 * }
				 * case "button":
				 * {
				 * }
				 * case "inventoryslot":
				 * {
				 * int inventorySlotId = Integer.parseInt(elementAttributes.get("slot-id"));
				 * String inventory = elementAttributes.get("inventory");
				 * 
				 * InventorySlot inventorySlot = new InventorySlot(overlay, absoluteElementRectangle, relativeElementRectangle, inventorySlotId, inventory);
				 * 
				 * overlay.addElement(id, inventorySlot);
				 * }
				 * case "inventoryview":
				 * {
				 * // Get additional attributes
				 * int startSlotId = Integer.parseInt(elementAttributes.get("start-slot-id"));
				 * int endSlotId = Integer.parseInt(elementAttributes.get("end-slot-id"));
				 * float cellspacing = Float.parseFloat(elementAttributes.get("cellspacing").replace("%", ""));
				 * int slotsPerRow = Integer.parseInt(elementAttributes.get("slots-per-row"));
				 * String inventory = elementAttributes.get("inventory");
				 * 
				 * InventoryView inventoryView = new InventoryView(overlay, absoluteElementRectangle, relativeElementRectangle, id, startSlotId, endSlotId, slotsPerRow, inventory, cellspacing);
				 * overlay.addElement(id, inventoryView);
				 * }
				 * }
				 */

				// Got loader for this kind of object?
				IXMLObjectLoader objectLoader = objectLoaders.get(element.getName());

				if (objectLoader != null)
					overlay.addElement(id, objectLoader.load(elementAttributes, absoluteElementRectangle, relativeElementRectangle, id, overlay));
				else
				{
					ConsoleHelper.writeLog("ERROR", "Missing object loader for XML GUI Object " + element.getName(), "XMLOverlayLoader");
				}
			}

			return overlay;
		}
		else
			throw new DataFormatException("Illegal rootnode tag in overlay: " + fileHandle.path());

	}

	/**
	 * Registers an object loader for the given xml object name.
	 * 
	 * @param objectName
	 * @param objectLoader
	 */
	public static void registerObjectLoader(String objectName, IXMLObjectLoader objectLoader)
	{
		if (!objectLoaders.containsKey(objectName))
			objectLoaders.put(objectName, objectLoader);
		else
		{
			ConsoleHelper.writeLog("ERROR", "Tried to register duplicate key xml object loader: " + objectName, "XMLOverlayLoader");
		}
	}

	private static HashMap<String, IXMLObjectLoader> objectLoaders = new HashMap<String, IXMLObjectLoader>();
}
