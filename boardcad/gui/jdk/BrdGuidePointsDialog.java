package boardcad.gui.jdk;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import cadcore.UnitUtils;
import boardcad.i18n.LanguageResource;

public class BrdGuidePointsDialog extends JDialog {
	static final long serialVersionUID=1L;

	private JScrollPane mScrollPane = null;
	private JTable mGuidePointsTable = null;

	/**
	 * This method initializes 
	 * 
	 */
	public BrdGuidePointsDialog() {
		super();
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 */
	private void initialize() {
		this.setSize(new Dimension(566, 338));
		this.setTitle("Guidepoints");
		this.setContentPane(getScrollPane());
	}

	/**
	 * This method initializes scrll	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getScrollPane() {
		if (mScrollPane == null) {
			mScrollPane = new JScrollPane();
			mScrollPane.setViewportView(getGuidePointsTable());
		}
		return mScrollPane;
	}

	/**
	 * This method initializes GuidePointsTable	
	 * 	
	 * @return javax.swing.JTable	
	 */
	private JTable getGuidePointsTable() {
		if (mGuidePointsTable == null) {
			mGuidePointsTable = new JTable();
// JAVA 6			GuidePointsTable.setFillsViewportHeight(true);
			mGuidePointsTable.setModel(new AbstractTableModel() {
				static final long serialVersionUID=1L;
				public String getColumnName(int col) {
					switch(col)
					{
					case 0:
						return "#";
					case 1:
						return LanguageResource.getString("GUIDEPOINTXCOORDCOLUMN_STR");
					case 2:
						return LanguageResource.getString("GUIDEPOINTYCOORDCOLUMN_STR");
					default:
						return "";
					}
				}
				public int getRowCount() { return BoardCAD.getInstance().getSelectedEdit().getGuidePoints().size(); }
				public int getColumnCount() { return 3; }
				public Object getValueAt(int row, int col) {

					switch(col)
					{
					case 0:
						return Integer.toString(row);
					case 1:
					case 2:
						Point2D.Double pnt = BoardCAD.getInstance().getSelectedEdit().getGuidePoints().get(row);

						return UnitUtils.convertLengthToCurrentUnit((col== 1)?pnt.x:pnt.y, true);
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
						return true;
					}
				}
				public void setValueAt(Object value, int row, int col) {
					Point2D.Double pnt = BoardCAD.getInstance().getSelectedEdit().getGuidePoints().get(row);
					double val = UnitUtils.convertInputStringToInternalLengthUnit((String)value);

					switch(col)
					{
					case 1:
						pnt.x = val;
						break;
					case 2:
						pnt.y = val;
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

					BoardCAD.getInstance().getSelectedEdit().getGuidePoints().add(new Point2D.Double());
					repaint();
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
					repaint();
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
		}

		return mGuidePointsTable;
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"
