package net.kennux.cubicworld.test;

import junit.framework.TestCase;
import net.kennux.cubicworld.CubicWorldGame;
import net.kennux.cubicworld.input.GameInputProcessor;
import net.kennux.cubicworld.input.IKeyInputHandler;
import net.kennux.cubicworld.input.IMouseInputHandler;
import net.kennux.cubicworld.input.InputManager;

import org.easymock.EasyMock;
import org.junit.Test;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;

public class InputSystemTest extends TestCase
{
	/**
	 * Tests the game input processor
	 */
	@Test
	public void testGameInputProcessor()
	{
		// Init
		CubicWorldGame cwGame = EasyMock.createStrictMock(CubicWorldGame.class);
		InputManager inputManager = new InputManager();
		cwGame.inputManager = inputManager;

		// Create test handler mock
		IKeyInputHandler testInputHandler = EasyMock.createStrictMock(IKeyInputHandler.class);
		IMouseInputHandler testMouseInputHandler = EasyMock.createStrictMock(IMouseInputHandler.class);

		// Attach to input manager
		inputManager.addInputAction(Input.Keys.A, testInputHandler);
		inputManager.setMouseInputHandler(testMouseInputHandler);

		// Create game input processor
		GameInputProcessor inputProcessor = new GameInputProcessor(cwGame);

		// Record expected input handler behaviour
		testInputHandler.keyPressed(cwGame);
		testInputHandler.keyReleased(cwGame);
		testMouseInputHandler.mouseMoved(new Vector2(10, 10), new Vector2(10, 10));

		// Replay
		EasyMock.replay(testInputHandler);
		EasyMock.replay(testMouseInputHandler);

		// Fire test calls to processor
		inputProcessor.keyDown(Input.Keys.A);
		inputProcessor.keyUp(Input.Keys.A);
		inputProcessor.mouseMoved(10, 10);

		// Verify the mock
		EasyMock.verify(testInputHandler);
	}

	/**
	 * Tests the input manager
	 */
	@Test
	public void testInputManager()
	{
		// Init
		InputManager inputManager = new InputManager();

		// Create test handler
		IKeyInputHandler testInputHandler = new IKeyInputHandler()
		{
			@Override
			public void keyPressed(CubicWorldGame cubicWorld)
			{
			}

			@Override
			public void keyReleased(CubicWorldGame cubicWorld)
			{
			}
		};

		// Attach to input manager
		inputManager.addInputAction(Input.Keys.A, testInputHandler);

		// Test
		assertEquals(testInputHandler, inputManager.getInputActionForKey(Input.Keys.A));
	}
}
