package net.kennux.cubicworld.plugins.baseplugin.input;

import net.kennux.cubicworld.CubicWorldGame;
import net.kennux.cubicworld.gui.hud.BlockSelectorGui;
import net.kennux.cubicworld.input.IKeyInputHandler;

public class BlockSelectorHudHandler implements IKeyInputHandler
{
	private int id;

	public BlockSelectorHudHandler(int id)
	{
		this.id = id;
	}

	@Override
	public void keyPressed(CubicWorldGame cubicWorld)
	{
		BlockSelectorGui.instance.currentSelected = this.id;
	}

	@Override
	public void keyReleased(CubicWorldGame cubicWorld)
	{
		// TODO Auto-generated method stub

	}

}
