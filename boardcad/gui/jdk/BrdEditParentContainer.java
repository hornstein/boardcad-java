package boardcad.gui.jdk;

interface BrdEditParentContainer
{
	public void setActive(BoardEdit edit);	
	public boolean isActive(BoardEdit edit);	
	public BoardEdit getActive();
}