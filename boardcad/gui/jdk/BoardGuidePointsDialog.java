package boardcad.gui.jdk;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.EventObject;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.text.JTextComponent;

import cadcore.UnitUtils;
import boardcad.i18n.LanguageResource;

public class BoardGuidePointsDialog extends JDialog{
	static final long serialVersionUID=1L;

	private JScrollPane mScrollPane = null;
	private JTable mGuidePointsTable = null;

	/**
	 * This constructs the class
	 * 
	 */
	public BoardGuidePointsDialog() {
		super();
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 */
	private void initialize() {
		this.setSize(new Dimension(566, 360));
		this.setTitle(LanguageResource.getString("GUIDEPOINTSTITLE_STR"));
		this.setContentPane(getScrll());
		this.setLocationRelativeTo(null);
	}
	/**
	 * This method initializes scrll	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getScrll() {
		if (mScrollPane == null) {
			mScrollPane = new JScrollPane();
			mScrollPane.setViewportView(getGuidePointsTable());
		}
		return mScrollPane;
	}
	
	public void update()
	{
		((AbstractTableModel)mGuidePointsTable.getModel()).fireTableDataChanged();
	}

	/**
	 * This method initializes GuidePointsTable	
	 * 	
	 * @return javax.swing.JTable	
	 */
	private JTable getGuidePointsTable() {
		final JTabbedPane mTabbedPane = BoardCAD.getInstance().getmTabbedPane();
		if (mGuidePointsTable == null) {
			mGuidePointsTable = new JTable()
			{
				/*
				 *  Override to provide Select All editing functionality
				 */
				public boolean editCellAt(int row, int column, EventObject e)
				{
					boolean result = super.editCellAt(row, column, e);

					selectAll(e);

					return result;
				}

				/*
				 * Select the text when editing on a text related cell is started
				 */
				private void selectAll(EventObject e)
				{
					final Component editor = getEditorComponent();

					if (editor == null
					|| ! (editor instanceof JTextComponent))
						return;

					if (e == null)
					{
						((JTextComponent)editor).selectAll();
						return;
					}

					//  Typing in the cell was used to activate the editor

					if (e instanceof KeyEvent)
					{
						((JTextComponent)editor).selectAll();
						return;
					}

					//  F2 was used to activate the editor

					if (e instanceof ActionEvent)
					{
						((JTextComponent)editor).selectAll();
						return;
					}

					//  A mouse click was used to activate the editor.
					//  Generally this is a double click and the second mouse click is
					//  passed to the editor which would remove the text selection unless
					//  we use the invokeLater()
					if (e instanceof MouseEvent)
					{
						SwingUtilities.invokeLater(new Runnable()
						{
							public void run()
							{
								((JTextComponent)editor).selectAll();
							}
						});
					}
				}				
			};
			//GuidePointsTable.setAutoCreateRowSorter(true); //...doesn't work on strings!

// JAVA 6			GuidePointsTable.setFillsViewportHeight(true);
			mGuidePointsTable.setModel(new AbstractTableModel() {
				static final long serialVersionUID=1L;
				public String getColumnName(int col) {
					switch(col)
					{
					case 0:
						return "#";
					case 1:
						return LanguageResource.getString("GUIDEPOINTXCOORD_STR");
					case 2:
						return LanguageResource.getString("GUIDEPOINTYCOORD_STR");
					case 3:
						if (mTabbedPane.getSelectedIndex() == 5
								|| (mTabbedPane.getSelectedIndex() == 2 
										&& BoardCAD.getInstance().getFourView().getActive().getName() == "QuadViewCrossSection" )) {

						}else{
							return LanguageResource.getString("GUIDEPOINTOCCOORD_STR");
						}
					default:
						return "";
					}
				}
				public int getRowCount() 
				{ 
					BoardEdit edit = BoardCAD.getInstance().getSelectedEdit();
					if(edit == null)
						return 0;
					
					return edit.getGuidePoints().size(); 
				}
				public int getColumnCount() 
				{

					if (mTabbedPane.getSelectedIndex() == 5	|| (mTabbedPane.getSelectedIndex() == 2 
							&& BoardCAD.getInstance().getFourView().getActive().getName() == "QuadViewCrossSection" )) 
					{
						return 3;
					}else{
						return 4;
					}
				}
				public Object getValueAt(int row, int col) {
					
					Point2D.Double pnt = BoardCAD.getInstance().getSelectedEdit().getGuidePoints().get(row);
					
					switch(col)
					{
					case 0:
						return Integer.toString(row);
					case 1:
					case 2:
						return UnitUtils.convertLengthToCurrentUnit((col== 1)?pnt.x:pnt.y, true);
					case 3:
/*						if (mTabbedPane.getSelectedIndex() == 5
								|| (mTabbedPane.getSelectedIndex() == 2 
									&& BoardCAD.getInstance().getFourView().getActive().getName() == "QuadViewCrossSection" )) 
						{

						}
						else{
*/
							return UnitUtils.convertLengthToCurrentUnit(BoardCAD.getInstance().getCurrentBrd().getFromTailOverBottomCurveAtPos(pnt.x), true);	
//						}
					default:
						return "";

					}

				}
				public boolean isCellEditable(int row, int col)
				{   
					switch(col)
					{
					default:
					case 0:
						return false;
					case 1:
					case 2:
					case 3:
						return true;
					}
				}
				public void setValueAt(Object value, int row, int col) {
					BoardEdit edit = BoardCAD.getInstance().getSelectedEdit();
					if(edit == null)
					{
						return;
					}
					if(edit.getGuidePoints().size() < row)
					{
						return;	//Invalid row
					}
					Point2D.Double pnt = edit.getGuidePoints().get(row);
					double val = UnitUtils.convertInputStringToInternalLengthUnit((String)value);

					switch(col)
					{
					case 1:
						pnt.x = val;
						break;
					case 2:
						pnt.y = val;
						break;
					case 3:
						pnt.x = BoardCAD.getInstance().getCurrentBrd().getXFromTailByOverBottomCurveLength(val);
						fireTableRowsUpdated(row,row);
						break;
					}
					BoardCAD.getInstance().getFrame().repaint();
				}
			});
			
			final JPopupMenu menu = new JPopupMenu();
//			JPopupMenu.setDefaultLightWeightPopupEnabled(false);
			JMenuItem add = new JMenuItem(new AbstractAction()
			{
				static final long serialVersionUID=1L;
				{
					this.putValue(Action.NAME, LanguageResource.getString("GUIDEPOINTADD_STR"));
//					this.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, 0));
				};

				public void actionPerformed(ActionEvent arg0) {

					addGuidePoint();
				}

			});
			menu.add(add);
			JMenuItem remove = new JMenuItem(new AbstractAction()
			{
				static final long serialVersionUID=1L;
				{
					this.putValue(Action.NAME, LanguageResource.getString("GUIDEPOINTREMOVE_STR"));
//					this.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
				};

				public void actionPerformed(ActionEvent arg0) {

					int[] ai = mGuidePointsTable.getSelectedRows();
					ArrayList<Point2D.Double> pntslist = new ArrayList<Point2D.Double>();
					for(int i = 0; i  < ai.length; i++)
					{
						pntslist.add(BoardCAD.getInstance().getSelectedEdit().getGuidePoints().get(ai[i]));	
					}
					
					for(int i = 0; i  < pntslist.size(); i++)
					{
						BoardCAD.getInstance().getSelectedEdit().getGuidePoints().remove(pntslist.get(i));	
					}
					mGuidePointsTable.revalidate();
					getParent().repaint();
				}

			});
			menu.add(remove);

			mGuidePointsTable.add(menu);
			mGuidePointsTable.addMouseListener(new MouseAdapter() {
				public void mousePressed(MouseEvent e) {
					showPopup(e);
				}
				public void mouseReleased(MouseEvent e) {
					showPopup(e);
				}
				private void showPopup(MouseEvent e) {
					if (e.isPopupTrigger()) {
						menu.show(e.getComponent(), e.getX(), e.getY());
					}
				}
			});
			
			mScrollPane.add(menu);
			mScrollPane.addMouseListener(new MouseAdapter() {
				public void mousePressed(MouseEvent e) {
					if (e.isPopupTrigger()) {
						showPopup(e);
					}
					else{
						addGuidePoint();
					}
				}
				public void mouseReleased(MouseEvent e) {
					showPopup(e);
				}
				private void showPopup(MouseEvent e) {
					if (e.isPopupTrigger()) {
						menu.show(e.getComponent(), e.getX(), e.getY());
					}
				}
			});
			
			
		}

		return mGuidePointsTable;
	}

	private void addGuidePoint() {
		BoardEdit edit = BoardCAD.getInstance().getSelectedEdit();
		if(edit == null)
			return;
		
		edit.getGuidePoints().add(new Point2D.Double());
		mGuidePointsTable.revalidate();
		getParent().repaint();
	}

	
	
}  //  @jve:decl-index=0:visual-constraint="10,10"
