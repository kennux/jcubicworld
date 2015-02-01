package net.kennux.cubicworld.gui.overlay;

import net.kennux.cubicworld.gui.AGuiOverlay;

public class Overlay extends AGuiOverlay
{
	private OverlayData overlayData;

	/**
	 * Returns a reference to the overlay data.
	 * If you modify the overlay data use the setOverlayData() function.
	 */
	public OverlayData getOverlayData()
	{
		return this.overlayData;
	}

	@Override
	protected void initialize()
	{
		// TODO Auto-generated method stub

	}

	/**
	 * @param overlayData
	 *            the overlayData to set
	 */
	public void setOverlayData(OverlayData overlayData)
	{
		this.overlayData = overlayData;
	}

	@Override
	public void update()
	{
		// TODO Auto-generated method stub

	}

}
