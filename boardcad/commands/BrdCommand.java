package boardcad.commands;

import boardcad.gui.jdk.BoardCAD;
import boardcad.gui.jdk.BoardEdit;

public abstract class BrdCommand extends Object implements Cloneable{
	boolean mCanUndo = true;
	BoardEdit mSource = null;
	BrdCommand mPreviousCommand = null;

	public void setPreviousCommand(BrdCommand previousCommand)
	{
		mPreviousCommand = previousCommand;
	}

	public void doAction()
	{

	}

	public void execute()
	{
		if(canUndo())
		{
			BrdCommandHistory.getInstance().addCommand((BrdCommand)this.clone());
		}

		if(mPreviousCommand != null)
		{
			BoardCAD.getInstance().setCurrentCommand(mPreviousCommand);
		}

	}; 	//Do command including user interactions

	public void setSource(BoardEdit source)
	{
		mSource = source;
	}

	public void redo()
	{
		if(mSource != null)
		{
			BoardCAD.getInstance().setSelectedEdit(mSource);
			mSource.onBrdChanged();
			BoardCAD.getInstance().onControlPointChanged();
		}

	}

	public void undo()
	{
		if(mSource != null)
		{
			BoardCAD.getInstance().setSelectedEdit(mSource);
			mSource.onBrdChanged();
			BoardCAD.getInstance().onControlPointChanged();
		}
	}
	public boolean canUndo(){return mCanUndo;};

	public Object clone(){
		try {
			return super.clone();
		} catch(CloneNotSupportedException e) {
			System.out.println("BoardComand.clone() Exception: " + e.toString());
			throw new Error("CloneNotSupportedException in BrdCommand");
		}
	}

	public void onSetCurrent()
	{

	}

	public void onCurrentChanged()
	{

	}

	public abstract String getCommandString();
}