package boardcad.commands;

import java.util.LinkedList;

public class BrdCommandHistory
{
	private LinkedList<BrdCommand> mCommandHistory;
	private int mCurrentCommandIndex;
	
	private static BrdCommandHistory mInstance = null;
	
	private BrdCommandHistory()
	{
		mCommandHistory = new LinkedList<BrdCommand>();
	}

	public static BrdCommandHistory getInstance()
	{
		if(mInstance == null) {
			mInstance = new BrdCommandHistory();
		}
		return mInstance;
	}

	public void clear()
	{
		mCommandHistory.clear();
		mCurrentCommandIndex = -1;
	}

	public void addCommand(BrdCommand command)
	{
		if(mCurrentCommandIndex>=0 && mCommandHistory.size() > mCurrentCommandIndex+1)
		{
			/*			Sometimes get java.util.ConcurrentModificationException with this code
			 * 			int size = mCommandHistory.size();	
			java.util.List<BrdCommand> toBeRemoved = mCommandHistory.subList(mCurrentCommand+1, size);
			mCommandHistory.removeAll(toBeRemoved);
			 */
			while(mCommandHistory.size() > mCurrentCommandIndex+1)
			{
				mCommandHistory.removeLast();
			}
		}
		mCommandHistory.add(command);
		mCurrentCommandIndex = mCommandHistory.size()-1;
	}

	public void undo()
	{
		if(mCurrentCommandIndex >= 0)
		{
			BrdCommand command = mCommandHistory.get(mCurrentCommandIndex--);
			if(command == null)
				return;
			command.undo();
		}
	}

	public void redo()
	{
		if(mCurrentCommandIndex < mCommandHistory.size()-1 && mCommandHistory.size() > 0)
		{
			BrdCommand command = mCommandHistory.get(++mCurrentCommandIndex);
			if(command == null)
				return;
			command.redo();
		}
	}
}