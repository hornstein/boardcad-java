package boardcad.gui.jdk;

import java.awt.GridLayout;

import javax.swing.JPanel;

class QuadView extends JPanel implements BrdEditParentContainer
{
	static final long serialVersionUID=1L;
	BoardEdit mActive = null;
	
	QuadView()
	{
		this.setLayout(new GridLayout(2,2,3,3));
	}
	
	@Override
	public void setActive(BoardEdit edit)
	{
		mActive = edit;
		this.repaint();
		BoardCAD.getInstance().getGuidePointsDialog().update();
	}
	
	@Override
	public boolean isActive(BoardEdit edit)
	{
		return (edit == mActive);
	}
	
	@Override
	public BoardEdit getActive()
	{
		if(mActive == null)
			return (BoardEdit)getComponent(0);
			
		return mActive;
	}	
	
}