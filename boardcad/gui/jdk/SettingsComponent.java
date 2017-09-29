package boardcad.gui.jdk;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Map;

import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import boardcad.settings.*;
import boardcad.settings.Settings.*;
import boardcad.i18n.LanguageResource;


class SettingsComponent extends JComponent
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 7619906218377501536L;

	private Settings mSettings = null;

	private JScrollPane mScrollPane = null;

	/**
	 * This method initializes 
	 * 
	 */
	public SettingsComponent(final Settings settings)
	{
		super();
		mSettings = settings;
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 */
	private void initialize() 
	{
		this.setSize(new Dimension(352, 221));
		this.setLayout(new BorderLayout());
		this.add(getScrollPane(), BorderLayout.CENTER);
	}

	/**
	 * This method initializes scrollPane	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getScrollPane() {
		if (mScrollPane == null) 
		{
			mScrollPane = new JScrollPane();

			
			final JTable settingsTable = new JTable()
			{
				/**
				 * 
				 */
				private static final long serialVersionUID = -7686188963845803173L;

				@Override
				synchronized public TableCellEditor getCellEditor(int row, int column)
				{
					if(column != 1) {
						return super.getCellEditor(row, column);
					}

					Class c =  getValueAt(row, column).getClass();
					String className = c.getName();

					if(className.compareTo(Color.class.getName()) == 0)
					{
						return new ColorEditor();
					}
					else if(className.compareTo(FileName.class.getName()) == 0)
					{
						return new FileNameEditor();
					}
					else if(className.compareTo(SettingsAction.class.getName()) == 0)
					{
						return new ActionEditor();
					}
					else if(className.compareTo(Enumeration.class.getName()) == 0)
					{
						return new EnumEditor();
					}
					else
					{
						return super.getDefaultEditor(c);			
					}

				}

				@Override
				synchronized public TableCellRenderer getCellRenderer(int row, int column) {

					if(column != 1) {
						return super.getCellRenderer(row, column);
					}

					Class theClass = getValueAt(row, column).getClass();
					String className = theClass.getName();

					if(className.compareTo(Color.class.getName()) == 0)
					{
						return new ColorRenderer();			
					}						
					else
					{
						return super.getDefaultRenderer(theClass);
					}
				}



			};

// JAVA 6			settingsTable.setAutoCreateRowSorter(true);
			
			settingsTable.setModel(new AbstractTableModel(){
				/**
				 * 
				 */
				private static final long serialVersionUID = 6677672269943294175L;
				@Override
				public String getColumnName(final int col) 
				{
					switch(col)
					{
					case 0:
						return LanguageResource.getString("SETTINGSDESCRIPTIONCOLUMN_STR");
					case 1:
						return LanguageResource.getString("SETTINGSVALUECOLUMNCOLUMN_STR");
					default:
						return "";
					}
				}
				public int getRowCount() { return mSettings.size(); }
				public int getColumnCount() { return 2; }
				public Object getValueAt(final int row, final int col) 
				{
					if(mSettings.isHidden(row))
					{
						return "";
					}

					switch(col)
					{
					case 0:
						return mSettings.getDescription(row);
					case 1:
						return mSettings.getValue(row);
					default:
						return "";

					}

				}
				@Override
				public boolean isCellEditable(final int row, final int col)
				{   
					switch(col)
					{
					default:
						return false;
					case 1:
						return !mSettings.isDisabled(row);
					}
				}
				@Override
				public void setValueAt(Object value, final int row, final int col) {

					final Object oldVal = mSettings.getValue(row);
					final String oldClassName = oldVal.getClass().getName();
//						final String newClassName = value.getClass().getName();
//						DEBUG					System.out.println("old: " + oldClassName + " new: "+ newClassName);

					try{
						if(oldClassName.compareTo(Double.class.getName()) == 0)
						{
							value = Double.parseDouble((String)value);
						}
						else if(oldClassName.compareTo(Integer.class.getName()) == 0)
						{
							value = Integer.parseInt((String)value);
						}
						else if(oldClassName.compareTo(Measurement.class.getName()) == 0)
						{
							value = mSettings.new Measurement((String)value);
						}
						mSettings.set(row, value);
						if(mSettings.getCallbacks(row).size() > 0)
						{
							for (SettingChangedCallback cb : mSettings.getCallbacks(row))
							{
								cb.onSettingChanged(value);					
							}
						}
						else if(mSettings.getDefaultCallback() != null)
						{
							mSettings.getDefaultCallback().onSettingChanged(value);
						}
					}
					catch(final Exception e)
					{
						System.out.print(e.toString());
					}
				}
			});
// JAVA 6			settingsTable.getRowSorter().toggleSortOrder(0);	//Sorts the list on first column

			mScrollPane.setViewportView(settingsTable);

		}
		return mScrollPane;
	}
}
	

class ColorRenderer extends JLabel implements TableCellRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6357536161048788938L;

	public ColorRenderer() {
		setOpaque(true); //MUST do this for background to show up.
	}

	public Component getTableCellRendererComponent(
			final JTable table, final Object color,
			final boolean isSelected, final boolean hasFocus,
			final int row, final int column) 
	{
		if(color.getClass().getSimpleName().compareTo("Color")==0)
		{
			setBackground((Color)color);
		}
		//setBorder(Solid black border);
		//setToolTipText(...); //Discussed in the following section
		return this;
	}
}

class ColorEditor extends AbstractCellEditor implements TableCellEditor, ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4268145246290497799L;
	Color currentColor;
	JButton button;
	JColorChooser colorChooser;
	JDialog dialog;
	protected static final String EDIT = "edit";

	public ColorEditor() {
		button = new JButton();
		button.setActionCommand(EDIT);
		button.addActionListener(this);
		button.setBorderPainted(false);

//		Set up the dialog that the button brings up.
		colorChooser = new JColorChooser();
		dialog = JColorChooser.createDialog(button,
				LanguageResource.getString("COLORSETTINGPICK_STR"),
				true,  //modal
				colorChooser,
				this,  //OK button handler
				null); //no CANCEL button handler
	}

	public void actionPerformed(final ActionEvent e) {
		if (EDIT.equals(e.getActionCommand())) {
//			The user has clicked the cell, so
//			bring up the dialog.
			button.setBackground(currentColor);
			colorChooser.setColor(currentColor);
			dialog.setVisible(true);

			fireEditingStopped(); //Make the renderer reappear.

		} else { //User pressed dialog's "OK" button.
			currentColor = colorChooser.getColor();
		}
	}

//	Implement the one CellEditor method that AbstractCellEditor doesn't.
	public Object getCellEditorValue() {
		return currentColor;
	}

//	Implement the one method defined by TableCellEditor.
	public Component getTableCellEditorComponent(final JTable table,
			final Object value,
			final boolean isSelected,
			final int row,
			final int column) {
		currentColor = (Color)value;
		return button;
	}
}

class FileNameEditor extends AbstractCellEditor implements TableCellEditor, ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1054238490165960482L;
	String fileName;
	JButton button;
	JFileChooser fileChooser;
	JDialog dialog;
	protected static final String EDIT = "edit";

	public FileNameEditor() {
		button = new JButton();
		button.setActionCommand(EDIT);
		button.addActionListener(this);
		button.setBorderPainted(false);

//		Set up the dialog that the button brings up.
		fileChooser = new JFileChooser();

	}

	public void actionPerformed(final ActionEvent e) {
		if (EDIT.equals(e.getActionCommand())) {
//			The user has clicked the cell, so
//			bring up the dialog.

			fileChooser.setCurrentDirectory(new File(fileName));

			fileChooser.showOpenDialog(BoardCAD.getInstance().getFrame());

			fireEditingStopped(); //Make the renderer reappear.

		} else { //User pressed dialog's "OK" button.

		}
	}

//	Implement the one CellEditor method that AbstractCellEditor doesn't.
	public Object getCellEditorValue() {
		final File file = fileChooser.getSelectedFile();
		if(file == null)
			fileName = "";
		else
			fileName = file.getPath();    // Load and display selection

		return new Settings().new FileName(fileName);
	}
	
//	Implement the one method defined by TableCellEditor.
	public Component getTableCellEditorComponent(final JTable table,
			final Object value,
			final boolean isSelected,
			final int row,
			final int column) {
		fileName = ((FileName)value).toString();
		return button;
	}
}

class ActionEditor extends AbstractCellEditor implements TableCellEditor, ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1054238490165960482L;
	JButton button;
	protected static final String EDIT = "edit";
	Object value;

	public ActionEditor() {
		button = new JButton();
		button.setActionCommand(EDIT);
		button.addActionListener(this);
//		button.setBorderPainted(false);
		button.setBorderPainted(true);
	}

	public void actionPerformed(final ActionEvent e) {
		if (EDIT.equals(e.getActionCommand())) {
//			The user has clicked the cell, so
//			bring up the dialog.

			fireEditingStopped(); //Make the renderer reappear.

		} else { //User pressed dialog's "OK" button.

		}
	}

//	Implement the one CellEditor method that AbstractCellEditor doesn't.
	public Object getCellEditorValue() {
		return value;
	}

//	Implement the one method defined by TableCellEditor.
	public Component getTableCellEditorComponent(final JTable table,
			final Object value,
			final boolean isSelected,
			final int row,
			final int column) {
		this.value = ((SettingsAction)value);
		return button;
	}
}

class EnumEditor extends AbstractCellEditor implements TableCellEditor {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4268145246290497799L;
	JButton button;
	JComboBox comboBox;
	protected static final String EDIT = "edit";
	Enumeration enu;
	Integer result;

	public EnumEditor() {
		comboBox = new JComboBox();
		comboBox.setEditable(false);
		comboBox.addActionListener(new ActionListener()
		{
			    public void actionPerformed(ActionEvent e) {
			        fireEditingStopped();
			    }
		});
	}


//	Implement the one CellEditor method that AbstractCellEditor doesn't.
	public Object getCellEditorValue() {
		for(Map.Entry<Integer, String> entry : enu.getAlternatives().entrySet())
		{			
			if(entry.getValue() == comboBox.getSelectedItem())
			{
				result = entry.getKey();
				break;
			}
		}
		return new Settings().new Enumeration(result, enu.getAlternatives());
	}

//	Implement the one method defined by TableCellEditor.
	public Component getTableCellEditorComponent(final JTable table,
			final Object value,
			final boolean isSelected,
			final int row,
			final int column) {
		enu = (Enumeration)value;

		comboBox.removeAllItems();
		
		for(Map.Entry<Integer, String> entry : enu.getAlternatives().entrySet())
		{			
			comboBox.addItem(entry.getValue());
		}
		
		return comboBox;
	}
}
