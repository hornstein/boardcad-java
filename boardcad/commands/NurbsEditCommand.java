package boardcad.commands;

import board.NurbsBoard;
import boardcad.gui.jdk.BoardCAD;
import boardcad.gui.jdk.BoardHandler;
import boardcad.i18n.LanguageResource;

public class NurbsEditCommand extends BrdCommand
{
	NurbsBoard oldBoard;
	NurbsBoard newBoard;
	BoardHandler board_handler;
	
	public NurbsEditCommand()
	{
		NurbsBoard board;
		board_handler=BoardCAD.getInstance().getBoardHandler();
		board=board_handler.getActiveBoard();
		if(board!=null)
			oldBoard = (NurbsBoard)board.clone();
	}

	public void execute()
	{
		NurbsBoard board;
		board=board_handler.getActiveBoard();
		if(board!=null)
			newBoard = (NurbsBoard)board.clone();
		super.execute();
	}

	public void redo()
	{
		board_handler.setActiveBoard(newBoard);
	}

	public void undo()
	{
		board_handler.setActiveBoard(oldBoard);
	}

	public String getCommandString()
	{
		return LanguageResource.getString("MACROCMD_STR");
	}
}