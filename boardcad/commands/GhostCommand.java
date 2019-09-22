package boardcad.commands;

import boardcad.i18n.LanguageResource;

public class GhostCommand extends BrdInputCommand
{

	public GhostCommand()
	{
		mCanUndo = false;
	}

	public String getCommandString()
	{
		return LanguageResource.getString("GHOSTCMD_STR");
	}
}

