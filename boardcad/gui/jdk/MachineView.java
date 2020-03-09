package boardcad.gui.jdk;

import java.awt.BorderLayout;
import java.awt.ComponentOrientation;
import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;

import boardcad.i18n.LanguageResource;
import boardcam.MachineConfig;

public class MachineView extends JPanel implements AbstractEditor
{
	protected Machine2DView mMachine2DView;
	protected Machine3DView mMachine3DView;
	
	protected MachineConfig mConfig;

	public MachineView(MachineConfig config)
	{
		super();
		
		mConfig = config;
		
		initialize();

		setLayout(new BorderLayout());

		setPreferredSize(new Dimension(1000, 600));
		
		setMinimumSize(new Dimension(100, 50));

		init();
		
	}

	/**
	 * This method initializes this
	 * 
	 */
	private void initialize() {
        this.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        this.setSize(new Dimension(400, 400));		
	}

	public void init()
	{	
		JTabbedPane tabbedPane = new JTabbedPane();

		tabbedPane.setSize(new Dimension(70, 20));
		tabbedPane.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		
		mMachine2DView =  new Machine2DView(mConfig);
		mMachine2DView.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		mMachine2DView.setPreferredSize(new Dimension(400, 400));
		tabbedPane.add(LanguageResource.getString("POSITIONING_STR"),mMachine2DView);
		
		mMachine3DView = new Machine3DView();
		mMachine3DView.setEnabled(true);
		mMachine3DView.setPreferredSize(new Dimension(10, 10));
		mMachine3DView.setVisible(true);
		tabbedPane.add(LanguageResource.getString("3D_STR"), mMachine3DView);

		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tabbedPane, new MachineConfigComponent(mConfig));
		splitPane.setOneTouchExpandable(true);
		splitPane.setDividerLocation(700);
		splitPane.setEnabled(true);
		splitPane.setResizeWeight(0.7);

		splitPane.setComponentOrientation(ComponentOrientation.UNKNOWN);
		splitPane.setContinuousLayout(true);
		splitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
		splitPane.setDividerSize(10);
		splitPane.setVisible(true);
		
		add(splitPane,BorderLayout.CENTER);
	}

	@Override
	public void fit_all()
	{
		mMachine2DView.fit_all();
		mMachine3DView.fit_all();
	}
	
	public void update()
	{
		fit_all();
		mMachine2DView.repaint();
		mMachine3DView.update();
	}
	
	public Machine2DView get2DView()
	{
		return mMachine2DView;
	}
	
	public Machine3DView get3DView()
	{
		return mMachine3DView;
	}


}  //  @jve:decl-index=0:visual-constraint="15,13";
