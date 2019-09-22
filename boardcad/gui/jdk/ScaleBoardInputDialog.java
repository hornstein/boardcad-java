package boardcad.gui.jdk;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Rectangle;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import cadcore.UnitUtils;
import boardcad.i18n.LanguageResource;

public class ScaleBoardInputDialog extends JDialog {

	private static final long serialVersionUID = 1L;

	private JPanel mContentPane = null;
	private JLabel MessageText = null;	
	private JLabel FactorMessageText = null;
	private JLabel LengthLabel = null;
	private JLabel WidthLabel = null;	
	private JLabel ThickLabel = null;	
	private JLabel FactorLabel = null;	
	private JCheckBox ScaleFinsCheckBox = null;	
	private JCheckBox ScaleFinsCheckBoxFactor = null;
	private JCheckBox ConstraintProportionsCheckBox = null;	
	private JCheckBox ScaleBottomRockerCheckBox = null;	
	private JButton OkButton = null;
	private JButton CancelButton = null;
	private JRadioButton OverCurveRadioButton = null;
	private JRadioButton StraightLineRadioButton = null;
	private JTextField LengthTextField = null;
	private JTextField WidthTextField = null;	
	private JTextField ThickTextField = null;	
	private JTextField FactorTextField = null;
	private boolean mWasCancelled = true;	
	private boolean mScaleThroughFactor = false;
	
	private double original_length;
	private double original_width;
	private double original_thickness;
		
	/**
	 * @param owner
	 */
	public ScaleBoardInputDialog(Frame owner) {
		super(owner);
		initialize();
	}

	/**
	 * This method initializes this
	 *
	 * @return void
	 */
	private void initialize() {
		this.setSize(308, 388);		//this.setSize(349, 236);
		this.setResizable(false);
		this.setContentPane(getmContentPane ());
		this.setLocationRelativeTo(null);
	}

	/**
	 * This method initializes mContentPane
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getmContentPane () {
		if (mContentPane == null) {
			MessageText = new JLabel();
			MessageText.setBounds(new Rectangle(11, 20, 275, 30));
			MessageText.setHorizontalAlignment(SwingConstants.CENTER);
			MessageText.setText(LanguageResource.getString("SCALEMSG_STR") );
			MessageText.setFont(new Font("Dialog", Font.BOLD, 14));
			
			LengthLabel = new JLabel();
			LengthLabel.setBounds(new Rectangle(45, 120, 97, 20));
			LengthLabel.setHorizontalAlignment(SwingConstants.RIGHT);
			LengthLabel.setText(LanguageResource.getString("LENGTHLABEL_STR") );
			
			WidthLabel = new JLabel();
			WidthLabel.setBounds(new Rectangle(45, 150, 97, 20));
			WidthLabel.setHorizontalAlignment(SwingConstants.RIGHT);
			WidthLabel.setText(LanguageResource.getString("MAXWIDTHLABEL_STR") );
			
			ThickLabel = new JLabel();
			ThickLabel.setBounds(new Rectangle(45, 180, 97, 20));
			ThickLabel.setHorizontalAlignment(SwingConstants.RIGHT);
			ThickLabel.setText(LanguageResource.getString("MAXTHICKLABEL_STR") );
			
			FactorMessageText = new JLabel();
			FactorMessageText.setText(LanguageResource.getString("FACTORSCALEMSG_STR") );
			FactorMessageText.setHorizontalAlignment(SwingConstants.CENTER);
			FactorMessageText.setFont(new Font("Dialog", Font.BOLD, 14));
			FactorMessageText.setBounds(new Rectangle(15, 15, 271, 30));
			FactorLabel = new JLabel();
			FactorLabel.setBounds(new Rectangle(60, 120, 97, 20));
			FactorLabel.setHorizontalAlignment(SwingConstants.RIGHT);
			FactorLabel.setText(LanguageResource.getString("FACTORLABEL_STR") );
			
			mContentPane = new JPanel();
			mContentPane.setLayout(new BorderLayout());
			//mContentPane .setLayout(null);

			JPanel panel1=new JPanel();
			panel1.setLayout(null);
			JPanel panel2=new JPanel();
			panel2.setLayout(null);
			JPanel panel3=new JPanel();
			
			JTabbedPane myTabbedPane = new JTabbedPane();
			myTabbedPane.add(LanguageResource.getString("MEASURESTAB_STR"),panel1); 
			myTabbedPane.add(LanguageResource.getString("FACTORTAB_STR"),panel2); 
			
			mContentPane.add(myTabbedPane, BorderLayout.CENTER);
			ChangeListener changeListener = new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent changeEvent) {
        				JTabbedPane sourceTabbedPane = (JTabbedPane) changeEvent.getSource();
				        if(sourceTabbedPane.getSelectedIndex()==0)
				        {
				        	mScaleThroughFactor = false;
				        }
				        else
				        {
				        	mScaleThroughFactor = true;
				        }
				      }
			};
    			myTabbedPane.addChangeListener(changeListener);
			
			
			panel1 .add(MessageText, null);
			panel1 .add(LengthLabel, null);
			panel1 .add(WidthLabel, null);
			panel1 .add(ThickLabel, null);
			panel1 .add(getScaleFinsCheckBox(), null);
			panel1 .add(getLengthTextField(), null);
			panel1 .add(getWidthTextField(), null);
			panel1 .add(getThickTextField(), null);
			panel1 .add(getOverCurveRadioButton(), null);
			panel1 .add(getStraightLineRadioButton(), null);
			panel1 .add(getConstraintProportionsCheckBox(), null);
			panel1 .add(getScaleBottomRockerCheckBox(), null);

			panel2 .add(FactorLabel, null);
			panel2 .add(FactorMessageText, null);
			panel2 .add(getFactorTextField(), null);
			panel2 .add(getScaleFinsCheckBoxFactor(), null);

			panel3.add(getOkButton(), null);
			panel3.add(getCancelButton(), null);
			
			mContentPane.add(panel3, BorderLayout.SOUTH);
			
			final ButtonGroup choiceButtonGroup = new ButtonGroup();
			choiceButtonGroup.add(getOverCurveRadioButton());
			choiceButtonGroup.add(getStraightLineRadioButton());
			getStraightLineRadioButton().setSelected(true);
	}
		return mContentPane ;
	}

	private JRadioButton getOverCurveRadioButton() {
			if (OverCurveRadioButton == null) {
				OverCurveRadioButton = new JRadioButton();
				OverCurveRadioButton.setBounds(new Rectangle(41, 68, 145, 20));
				OverCurveRadioButton.setText(LanguageResource.getString("OVERCURVERADIOBUTTON_STR") );
				OverCurveRadioButton.addActionListener(new java.awt.event.ActionListener() {
					@Override
					public void actionPerformed(java.awt.event.ActionEvent e) {
						OverCurveRadioButton.setSelected(true);
						setVisible(true);
						setBoardLength(BoardCAD.getInstance().getCurrentBrd().getLengthOverCurve());
					}
				});
			}
			return OverCurveRadioButton;
	}

	private JRadioButton getStraightLineRadioButton() {
				if (StraightLineRadioButton == null) {
					StraightLineRadioButton = new JRadioButton();
					StraightLineRadioButton.setBounds(new Rectangle(41, 86, 145, 20));
					StraightLineRadioButton.setText(LanguageResource.getString("STRAIGHTLINERADIOBUTTON_STR") );
					StraightLineRadioButton.addActionListener(new java.awt.event.ActionListener() {
						@Override
						public void actionPerformed(java.awt.event.ActionEvent e) {
							StraightLineRadioButton.setSelected(true);
							setVisible(true);
							setBoardLength(BoardCAD.getInstance().getCurrentBrd().getLength());
						}
					});
				}
			return StraightLineRadioButton;
	}

	private JCheckBox getConstraintProportionsCheckBox() {
		if (ConstraintProportionsCheckBox == null) {
			ConstraintProportionsCheckBox = new JCheckBox();
			ConstraintProportionsCheckBox.setBounds(new Rectangle(45, 210, 170, 20));
			ConstraintProportionsCheckBox.setText(LanguageResource.getString("CONSTRAINTPROPORTIONSMSG"));
			ConstraintProportionsCheckBox.setToolTipText(LanguageResource.getString("CONSTRAINTPROPORTIONSMSGDETAILED"));
			ConstraintProportionsCheckBox.setSelected(false);
		}
		return ConstraintProportionsCheckBox ;
	}
	
	private JCheckBox getScaleFinsCheckBox() {
		if (ScaleFinsCheckBox == null) {
			ScaleFinsCheckBox = new JCheckBox();
			ScaleFinsCheckBox.setBounds(new Rectangle(45, 270, 200, 20));
			ScaleFinsCheckBox.setText(LanguageResource.getString("SCALEFINSMSG"));
			ScaleFinsCheckBox.setToolTipText(LanguageResource.getString("SCALEFINSMSGDETAILED"));
			ScaleFinsCheckBox.setSelected(true);
			ScaleFinsCheckBox.addItemListener(new java.awt.event.ItemListener() {
				@Override
				public void itemStateChanged(java.awt.event.ItemEvent e) {
					//setVisible(false);
					//mWasCancelled = false;
					//((BrdEditCommand)BoardCAD.getInstance().getCurrentCommand()).setContinous(BoardCAD.getInstance().getSelectedEdit(),   e.getStateChange() == java.awt.event.ItemEvent.SELECTED);
				}
			});
		}
		return ScaleFinsCheckBox ;
	}

	private JCheckBox getScaleBottomRockerCheckBox() {
		if (ScaleBottomRockerCheckBox == null) {
			ScaleBottomRockerCheckBox = new JCheckBox();
			ScaleBottomRockerCheckBox.setBounds(new Rectangle(45, 240, 180, 20));
			ScaleBottomRockerCheckBox.setText(LanguageResource.getString("SCALEBOTTOMROCKERMSG"));
			ScaleBottomRockerCheckBox.setSelected(true);
		}
		return ScaleBottomRockerCheckBox ;
	}
	
	private JCheckBox getScaleFinsCheckBoxFactor() {
		if (ScaleFinsCheckBoxFactor == null) {
			ScaleFinsCheckBoxFactor = new JCheckBox();
			ScaleFinsCheckBoxFactor.setBounds(new Rectangle(60, 195, 200, 20));
			ScaleFinsCheckBoxFactor.setText(LanguageResource.getString("SCALEFINSMSG"));
			ScaleFinsCheckBoxFactor.setToolTipText(LanguageResource.getString("SCALEFINSMSGDETAILED"));
			ScaleFinsCheckBoxFactor.setSelected(true);
			ScaleFinsCheckBoxFactor.addItemListener(new java.awt.event.ItemListener() {
				@Override
				public void itemStateChanged(java.awt.event.ItemEvent e) {
					//setVisible(false);
					//mWasCancelled = false;
					//((BrdEditCommand)BoardCAD.getInstance().getCurrentCommand()).setContinous(BoardCAD.getInstance().getSelectedEdit(),   e.getStateChange() == java.awt.event.ItemEvent.SELECTED);
				}
			});
		}
		return ScaleFinsCheckBoxFactor ;
	}
	
	/**
	 * This method initializes OkButton
	 *
	 * @return javax.swing.JButton
	 */

	private JButton getOkButton() {
		if (OkButton == null) {
			OkButton = new JButton();
			OkButton.setBounds(new Rectangle(95, 167, 110, 25));
			OkButton.setText(LanguageResource.getString("OKBUTTON_STR"));
			OkButton.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					setVisible(false);
					mWasCancelled = false;
				}
			});
		}
		return OkButton;
	}

	/**
	 * This method initializes CancelButton
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getCancelButton() {
		if (CancelButton == null) {
			CancelButton = new JButton();
			CancelButton.setBounds(new Rectangle(216, 370, 110, 25));
			CancelButton.setText(LanguageResource.getString("CANCELBUTTON_STR"));
			CancelButton.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					setVisible(false);
				}
			});
		}
		return CancelButton;
	}

	/**
	 * This method initializes LengthTextField
	 *
	 * @return javax.swing.JTextField
	 */
	private JTextField getLengthTextField() {
		if (LengthTextField == null) {
			LengthTextField = new JTextField();
			LengthTextField.setBounds(new Rectangle(149, 120, 80, 20));
			LengthTextField.addFocusListener(new java.awt.event.FocusListener() {
				  @Override
				public void focusLost(java.awt.event.FocusEvent e) {
				        if(ConstraintProportionsCheckBox.isSelected())
				        {
						double new_length=UnitUtils.convertInputStringToInternalLengthUnit(LengthTextField.getText());
				        	setBoardThick(original_thickness*new_length/original_length);
				        	setBoardWidth(original_width*new_length/original_length);
				        	mContentPane.repaint();
				        }
				  }
				  @Override
				public void focusGained(java.awt.event.FocusEvent e) {
				  	original_length=UnitUtils.convertInputStringToInternalLengthUnit(LengthTextField.getText());
				  	original_width=UnitUtils.convertInputStringToInternalLengthUnit(WidthTextField.getText());
				  	original_thickness=UnitUtils.convertInputStringToInternalLengthUnit(ThickTextField.getText());
				  } 
				  });
		}
		return LengthTextField;
	}

	/**
	 * This method initializes WidthTextField
	 *
	 * @return javax.swing.JTextField
	 */
	private JTextField getWidthTextField() {
		if (WidthTextField == null) {
			WidthTextField = new JTextField();
			WidthTextField.setBounds(new Rectangle(149, 150, 80, 20));
			WidthTextField.addFocusListener(new java.awt.event.FocusListener() {
				  @Override
				public void focusLost(java.awt.event.FocusEvent e) {
				        if(ConstraintProportionsCheckBox.isSelected())
				        {

						double new_width=UnitUtils.convertInputStringToInternalLengthUnit(WidthTextField.getText());
				        	setBoardLength(original_length*new_width/original_width);
				        	setBoardThick(original_thickness*new_width/original_width);
				        	mContentPane.repaint();
				        }
				  }
				  @Override
				public void focusGained(java.awt.event.FocusEvent e) {
				  	original_length=UnitUtils.convertInputStringToInternalLengthUnit(LengthTextField.getText());
				  	original_width=UnitUtils.convertInputStringToInternalLengthUnit(WidthTextField.getText());
				  	original_thickness=UnitUtils.convertInputStringToInternalLengthUnit(ThickTextField.getText());
				  } 
				  });
		}
		return WidthTextField;
	}

	/**
	 * This method initializes ThickTextField
	 * @return  javax.swing.JTextField
	 * @uml.property  name="thickTextField"
	 */
	private JTextField getThickTextField() {
		if (ThickTextField == null) {
			ThickTextField = new JTextField();
			ThickTextField.setBounds(new Rectangle(149, 180, 80, 20));
			ThickTextField.addFocusListener(new java.awt.event.FocusListener() {
				  @Override
				public void focusLost(java.awt.event.FocusEvent e) {
				        if(ConstraintProportionsCheckBox.isSelected())
				        {
						double new_thickness=UnitUtils.convertInputStringToInternalLengthUnit(ThickTextField.getText());
				        	setBoardLength(original_length*new_thickness/original_thickness);
				        	setBoardWidth(original_width*new_thickness/original_thickness);
				        	mContentPane.repaint();
				        }
				  }
				  @Override
				public void focusGained(java.awt.event.FocusEvent e) {
				  	original_length=UnitUtils.convertInputStringToInternalLengthUnit(LengthTextField.getText());
				  	original_width=UnitUtils.convertInputStringToInternalLengthUnit(WidthTextField.getText());
				  	original_thickness=UnitUtils.convertInputStringToInternalLengthUnit(ThickTextField.getText());
				  } 
				  });
		}
		return ThickTextField;
	}
	
	/**
	 * This method initializes FactorTextField
	 * @return  javax.swing.JTextField
	 * @uml.property  name="factorTextField"
	 */
	private JTextField getFactorTextField() {
		if (FactorTextField == null) {
			FactorTextField = new JTextField();
			FactorTextField.setBounds(new Rectangle(164, 120, 80, 20));
		}
		return FactorTextField;
	}
	
	public void setBoardLength(double length)
	{
		LengthTextField.setText(UnitUtils.convertLengthToCurrentUnit(length, true));
	}

	public void setBoardWidth(double width)
	{
		WidthTextField.setText(UnitUtils.convertLengthToCurrentUnit(width, true));
	}
	
	public void setBoardThick(double thick)
	{
		ThickTextField.setText(UnitUtils.convertLengthToCurrentUnit(thick, true));
	}
	
	public double getBoardLength()
	{
		return UnitUtils.convertInputStringToInternalLengthUnit(LengthTextField.getText());
	}

	public double getBoardWidth()
	{
		return UnitUtils.convertInputStringToInternalLengthUnit(WidthTextField.getText());
	}
	
	public double getBoardThick()
	{
		return UnitUtils.convertInputStringToInternalLengthUnit(ThickTextField.getText());
	}

	public double getFactor()
	{
		return Double.parseDouble(FactorTextField.getText());
	}
	
	public boolean useOverCurve()
	{
		return OverCurveRadioButton.isSelected();
	}
	
	public boolean scaleFins()
	{
		return ScaleFinsCheckBox.isSelected();
	}
	
	public boolean scaleBottomRocker()
	{
		return ScaleBottomRockerCheckBox.isSelected();
	}
	
	public boolean scaleFinsFactor()
	{
		return ScaleFinsCheckBoxFactor.isSelected();
	}
	
	public boolean wasCancelled()
	{
		return mWasCancelled;
	}
	
	public boolean scaleThroughFactor()
	{
		return mScaleThroughFactor;
	}


}  //  @jve:decl-index=0:visual-constraint="12,8"
