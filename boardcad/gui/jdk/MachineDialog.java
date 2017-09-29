package boardcad.gui.jdk;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import boardcad.FileTools;
import boardcad.i18n.LanguageResource;
import boardcam.MachineConfig;

public class MachineDialog extends JFrame implements KeyEventDispatcher,
		WindowListener {
	MachineView mMachineView = null;
	MachineConfig mConfig = null;

	/**
	 * This method initializes
	 * 
	 */
	public MachineDialog(MachineConfig config) {
		super();

		mConfig = config;

		// Before initialize to set up config right
		initialize();

		mConfig.setMachineView(this.mMachineView);

		KeyboardFocusManager.getCurrentKeyboardFocusManager()
				.addKeyEventDispatcher(this);

		this.addWindowListener(this);
	}

	/**
	 * This method initializes this
	 * 
	 */
	private void initialize() {

		this.setTitle(" " + LanguageResource.getString("MACHINETITLE_STR"));
		this.setMinimumSize(new Dimension(640, 480));
		this.setSize(new Dimension(1024, 768));
		this.setLocationRelativeTo(null);
		this.setLayout(new BorderLayout());
		this.setPreferredSize(new Dimension(1024, 768));
		//this.setAlwaysOnTop(true);
		final MachineDialog myDialog = this;

		// CNC JDialog Icon
		try {
			ImageIcon icon = new ImageIcon(getClass().getResource(
					"icons/mill png 16x16.png"));
			super.setIconImage(icon.getImage());

		} catch (Exception e) {
			System.out.println("CNC bezier JDialog icon error:\n"
					+ e.getMessage());
		}

		JButton generateButton = new JButton();
		generateButton
				.setText(LanguageResource.getString("GENERATEBUTTON_STR"));
		generateButton.addActionListener(new java.awt.event.ActionListener() {

			public void actionPerformed(java.awt.event.ActionEvent e) {
				final JFileChooser fc = new JFileChooser();

				fc.setCurrentDirectory(new File(BoardCAD.defaultDirectory));
				try{
					fc.setSelectedFile(new File(BoardCAD.defaultDirectory
							+ FileTools.getFilename(BoardCAD.getInstance()
									.getCurrentBrd().getFilename())));
				}
				catch(Exception ex)
				{
					//Do nothing, just trying to set a file to start with anyway
				}

				int returnVal = fc.showSaveDialog(myDialog);
				if (returnVal != JFileChooser.APPROVE_OPTION)
					return;

				File file = fc.getSelectedFile();

				String filename = file.getPath(); // Load and display
				// selection
				if (filename == null)
					return;

				BoardCAD.defaultDirectory = file.getPath();

				try {
					filename = FileTools.setExtension(filename, "dnc");
					mConfig.getToolpathGenerator().writeToolpath(filename,
							mConfig.getBoard(), mConfig.getBlank());
				} catch (Exception ex) {
					String str = "Failed to write g-code file :"
							+ ex.toString();
					JOptionPane.showMessageDialog(BoardCAD.getInstance()
							.getFrame(), str, "Error when writing g-code file",
							JOptionPane.ERROR_MESSAGE);

				}
				mConfig.putPreferences();
			}
		});

		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
		buttonPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		buttonPane.add(Box.createHorizontalGlue());
		buttonPane.add(generateButton);

		mMachineView = new MachineView(mConfig);
		this.add(mMachineView, BorderLayout.CENTER);
		this.add(buttonPane, BorderLayout.PAGE_END);

		this.setVisible(true);

		mMachineView.fit_all();
	}

	public MachineView getMachineView() {
		return mMachineView;
	}

	public boolean dispatchKeyEvent(KeyEvent event) {
		if(getFocusOwner() != null)
		{
			BrdInputCommand cmd = (BrdInputCommand) mMachineView.get2DView().getCurrentCommand();
			return cmd.onKeyEvent(mMachineView.get2DView(), event);
		}
		return false;
	}

	@Override
	public void windowActivated(WindowEvent e) {
	}

	@Override
	public void windowClosed(WindowEvent e) {
	}

	@Override
	public void windowClosing(WindowEvent e) {
		mConfig.putPreferences();
		KeyboardFocusManager.getCurrentKeyboardFocusManager()
		.removeKeyEventDispatcher(this);

	}

	@Override
	public void windowDeactivated(WindowEvent e) {
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
	}

	@Override
	public void windowIconified(WindowEvent e) {
	}

	@Override
	public void windowOpened(WindowEvent e) {
	}

} // @jve:decl-index=0:visual-constraint="10,10"
