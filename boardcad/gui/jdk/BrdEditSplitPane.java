package boardcad.gui.jdk;

import javax.swing.JSplitPane;

class BrdEditSplitPane extends JSplitPane implements BrdEditParentContainer
{
	static final long serialVersionUID=1L;
	BoardEdit mActive = null;
	
	BrdEditSplitPane(int arg1, BoardEdit first, BoardEdit second)
	{
		super(arg1, first, second);
		mActive = first;
		first.mParentContainer = this;
		second.mParentContainer = this;
	}
	
	@Override
	public void setActive(BoardEdit edit)
	{
		mActive = edit;
		this.repaint();
	}
	
	@Override
	public boolean isActive(BoardEdit edit)
	{
		return (edit == mActive);
	}
	
	@Override
	public BoardEdit getActive()
	{
		return mActive;
	}	
}