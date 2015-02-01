package net.kennux.cubicworld.test;

import junit.framework.TestCase;
import net.kennux.cubicworld.inventory.BasicInventory;
import net.kennux.cubicworld.inventory.BlockInventory;
import net.kennux.cubicworld.inventory.IInventoryFilterRule;
import net.kennux.cubicworld.inventory.InventoryFilterRuleSet;
import net.kennux.cubicworld.item.ItemStack;
import net.kennux.cubicworld.item.ItemSystem;
import net.kennux.cubicworld.item.ItemType;

import org.junit.Before;
import org.junit.Test;

/**
 * This class contains testing functions for the BasicInventory and the ItemStack class.
 * 
 * @author KennuX
 *
 */
public class InventoryTest extends TestCase
{
	private BasicInventory inventoryInstance;
	private ItemType testItemType;
	private ItemType testItemType2;

	@Before
	public void setUp()
	{
		// Init inventory instance
		this.inventoryInstance = new BasicInventory(10);
		ItemSystem.initialize(0, 0);
		this.testItemType = ItemSystem.registerItemType("Test");
		this.testItemType2 = ItemSystem.registerItemType("Test2");
	}

	/**
	 * Tests adding and removing items.
	 */
	@Test
	public void testAddRemove()
	{
		ItemStack itemStack = new ItemStack(this.testItemType.getItemId(), 10);

		// Test adding to inventory
		assertTrue(this.inventoryInstance.setItemStackInSlot(2, itemStack));
		assertEquals(2, this.inventoryInstance.getFirstOccupiedSlotId());

		// Test retrieval
		assertEquals(itemStack, this.inventoryInstance.getItemStackInSlot(2));

		// Test removal
		assertTrue(this.inventoryInstance.removeItemsFromStack(2, 6));
		assertEquals(4, this.inventoryInstance.getItemStackInSlot(2).getItemCount());
		assertTrue(this.inventoryInstance.removeItemsFromStack(2, 4));
		assertEquals(null, this.inventoryInstance.getItemStackInSlot(2));

		// Test illegal removal
		itemStack = new ItemStack(this.testItemType.getItemId(), 10);
		assertTrue(this.inventoryInstance.setItemStackInSlot(2, itemStack));
		assertFalse(this.inventoryInstance.removeItemsFromStack(2, 60));

		this.inventoryInstance.setItemStackInSlot(2, null);
	}

	/**
	 * Tests filter rules.
	 */
	@Test
	public void testFilterRules()
	{
		// Init rule set
		IInventoryFilterRule testFilterRule = new IInventoryFilterRule()
		{
			@Override
			public boolean filter(ItemType itemType)
			{
				return itemType == testItemType;
			}
		};

		IInventoryFilterRule testFilterRule2 = new IInventoryFilterRule()
		{
			@Override
			public boolean filter(ItemType itemType)
			{
				return itemType == testItemType2;
			}
		};

		InventoryFilterRuleSet ruleSet = new InventoryFilterRuleSet();
		ruleSet.setRules(0, testFilterRule, testFilterRule2);
		ruleSet.setRules(1, testFilterRule);

		// Init block inventory
		BlockInventory blockInventory = new BlockInventory(10);
		blockInventory.setFilterRuleSet(ruleSet);

		// Init test stacks
		ItemStack itemStack = new ItemStack(this.testItemType.getItemId(), 10);
		ItemStack itemStack2 = new ItemStack(this.testItemType2.getItemId(), 10);

		// Test adding to inventory in filtered slots
		assertTrue(blockInventory.setItemStackInSlot(0, itemStack));
		assertTrue(blockInventory.setItemStackInSlot(0, itemStack2));
		assertTrue(blockInventory.setItemStackInSlot(1, itemStack));
		assertFalse(blockInventory.setItemStackInSlot(1, itemStack2));
		assertTrue(blockInventory.setItemStackInSlot(2, itemStack));
		assertTrue(blockInventory.setItemStackInSlot(2, itemStack2));

	}

	/**
	 * Tests item stack initialization and adding / removing items
	 */
	@Test
	public void testItemStack()
	{
		ItemStack itemStack = new ItemStack(this.testItemType.getItemId(), 10);

		// Test init
		assertEquals(this.testItemType, itemStack.getType());
		assertEquals(10, itemStack.getItemCount());

		// Test remove
		assertTrue(itemStack.removeItems(1));
		assertEquals(9, itemStack.getItemCount());
		assertFalse(itemStack.removeItems(11));
		assertEquals(9, itemStack.getItemCount());
		assertTrue(itemStack.removeItems(-1));
		assertEquals(10, itemStack.getItemCount());

		// Test add
		assertTrue(itemStack.addItems(10));
		assertFalse(itemStack.addItems(60));
		assertEquals(20, itemStack.getItemCount());
		assertTrue(itemStack.addItems(-1));
		assertEquals(19, itemStack.getItemCount());
	}

	/**
	 * Tests the inventory size.
	 */
	@Test
	public void testSize()
	{
		assertFalse(this.inventoryInstance.setItemStackInSlot(11, new ItemStack(this.testItemType.getItemId(), 1)));
		assertFalse(this.inventoryInstance.setItemStackInSlot(-1, new ItemStack(this.testItemType.getItemId(), 1)));
		assertEquals(null, this.inventoryInstance.getItemStackInSlot(11));
		assertEquals(null, this.inventoryInstance.getItemStackInSlot(-1));
	}
}
