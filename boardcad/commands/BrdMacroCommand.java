package boardcad.commands;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

import boardcad.i18n.LanguageResource;

public class BrdMacroCommand extends BrdCommand {

	LinkedList<BrdCommand> mBrdCommand;

	public BrdMacroCommand()
	{
		mBrdCommand = new LinkedList<BrdCommand>();
	}

	public void add(BrdCommand cmd)
	{
		mBrdCommand.add(cmd);
	}

	public void execute()
	{
		for(Iterator i = mBrdCommand.iterator(); i.hasNext();)
		{
			((BrdCommand)i.next()).doAction();
		}
		super.execute();
	}
	public void redo()
	{
		for(Iterator i = mBrdCommand.iterator(); i.hasNext();)
			((BrdCommand)i.next()).redo();

		super.redo();
	}

	public void undo()
	{
//		Java 6		for(Iterator i = mBrdCommand.descendingIterator(); i.hasNext();)
//		((BrdCommand)i.next()).undo();

		for(ListIterator i = mBrdCommand.listIterator(mBrdCommand.size()); i.hasPrevious();)
		{
			((BrdCommand)i.previous()).undo();
		}

		super.undo();
	}

	public String getCommandString()
	{
		return LanguageResource.getString("MACROCMD_STR");
	}

}