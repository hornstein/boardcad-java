package boardcad.gui.jdk;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import javax.swing.JComponent;
import javax.swing.JFileChooser;

import board.BezierBoard;
import board.readers.*;

import cadcore.BezierSpline;
import cadcore.UnitUtils;
import boardcad.FileTools;
import boardcad.i18n.LanguageResource;

public final class BoardPreview extends JComponent implements PropertyChangeListener {
		private BezierBoard mBrd = null;
        private File mFile = null;
    	private Font mFont = new Font("Dialog", Font.PLAIN, 10);

        public BoardPreview(JFileChooser fc) 
        {
            setPreferredSize(new Dimension(250, 400));
            fc.addPropertyChangeListener(this);
        }

        public void loadBoard() {
            if (mFile == null) {
                mBrd = null;
                return;
            }
            
            String extension = FileTools.getExtension(mFile);
            if (extension != null) {
                if (extension.equals("srf")) {
                	mBrd = new BezierBoard();
                    SrfReader.loadFile(mBrd, mFile.getAbsolutePath());
                } 
                else if (extension.equals("brd")){
                	mBrd = new BezierBoard();
                    BrdReader.loadFile(mBrd, mFile.getAbsolutePath());
                } 
                else if (extension.equals("s3d")) {
                	mBrd = new BezierBoard();
                    S3dReader.loadFile(mBrd, mFile.getAbsolutePath());
                } 
 
		else if (extension.equals("stp") || (extension.equals("step"))) {

   	            	mBrd = new BezierBoard();
			StepReader.loadPreview(mBrd, mFile.getAbsolutePath());

		}
                else if (extension.equals("cad")){
                	//TODO: implement
                }            
            }
            
        }

        public void propertyChange(PropertyChangeEvent e)
        {
            boolean update = false;
            String prop = e.getPropertyName();
            
            if (JFileChooser.DIRECTORY_CHANGED_PROPERTY.equals(prop)) {//If the directory changed, don't show an image.
                mFile = null;
                mBrd = null;
                update = true;
            } 
            else if (JFileChooser.SELECTED_FILE_CHANGED_PROPERTY.equals(prop)) {	//If a file became selected, find out which one.
                mFile = (File) e.getNewValue();
                mBrd = null;
                loadBoard();
                update = true;
            }

            //Update the preview accordingly.
            if (update) {
                if (isShowing()) {
                    repaint();
                }
            }
        }

        protected void paintComponent(Graphics gr) 
        {
        	Graphics2D g = (Graphics2D)gr;
    		FontMetrics fontMetrics = g.getFontMetrics(mFont);

        	double leftMargin = 5.0;
    		double topMargin = 3.0;
    		double bottomMargin = 3.0;

    		double height = getHeight();
        	double width = getWidth();
        	
            g.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    		
    		g.setBackground(new Color(1.0f,1.0f,1.0f,1.0f));
    		g.clearRect(0, 0, (int)width, (int)height);
    		
    		//Draw frames
    		double frameMargins = 2.0;
    		double lowerFrameHeight = fontMetrics.getHeight()*5 + topMargin + bottomMargin;
    		double upperFrameHeight = height - lowerFrameHeight - frameMargins;
    		
    		g.setColor(Color.GRAY);
    		g.drawRect((int)frameMargins, (int)0, (int)(width-frameMargins-1), (int)upperFrameHeight);    		
    		g.drawRect((int)frameMargins, (int)(upperFrameHeight + frameMargins), (int)(width-frameMargins-1), (int)lowerFrameHeight-1);
    		

    		if (mBrd == null) 
            {
                loadBoard();
            }
    
            if (mBrd != null) 
            {        		        		
            	double scale = (upperFrameHeight-topMargin-bottomMargin)/mBrd.getLength();

            	JavaDraw jd = new JavaDraw(g);
        		
        		//Vertical
        		BasicStroke stroke = new BasicStroke(1.0f/(float)scale);
        		
        		double outlineDrawPos = width/2 - (mBrd.getThickness()*scale + 5.0*scale)/2;
        		
        		BezierBoardDrawUtil.paintBezierSplines(jd, outlineDrawPos, upperFrameHeight-bottomMargin, scale, Color.BLACK, stroke, new BezierSpline[]{mBrd.getOutline()}, BezierBoardDrawUtil.Vertical | BezierBoardDrawUtil.FlipY | BezierBoardDrawUtil.MirrorX, true);	  		
        		BezierBoardDrawUtil.paintBezierSplines(jd, outlineDrawPos +  mBrd.getMaxWidth()*scale/2+ mBrd.getThickness()*scale + 5.0*scale, upperFrameHeight-bottomMargin, scale , Color.BLACK, stroke, new BezierSpline[]{mBrd.getDeck(), mBrd.getBottom()}, BezierBoardDrawUtil.Vertical | BezierBoardDrawUtil.FlipX | BezierBoardDrawUtil.FlipY, true);	  		

            	//Text
        		double strLength = fontMetrics.stringWidth(mFile.getName());
        		g.drawString(mFile.getName(), (int)(width-strLength)/2, (int)((upperFrameHeight + topMargin + fontMetrics.getHeight())));

        		String modelStr = ((!mBrd.getModel().isEmpty())?(LanguageResource.getString("MODEL_STR") + mBrd.getModel()):"");
        		strLength = fontMetrics.stringWidth(modelStr);
        		g.drawString(modelStr, (int)(width-strLength)/2, (int)((upperFrameHeight + topMargin + fontMetrics.getHeight()*2)));
        		
        		String dimStr = UnitUtils.convertLengthToCurrentUnit(mBrd.getLength(), true) + "(" + UnitUtils.convertLengthToCurrentUnit(mBrd.getLengthOverCurve(), true) + ") x " + UnitUtils.convertLengthToCurrentUnit(mBrd.getMaxWidth(), false) + " x " + UnitUtils.convertLengthToCurrentUnit(mBrd.getThickness(), false);
        		strLength = fontMetrics.stringWidth(dimStr);
        		g.drawString(dimStr, (int)(width-strLength)/2, (int)((upperFrameHeight + topMargin + fontMetrics.getHeight()*3)));

        		String volumeStr = LanguageResource.getString("VOLUME_STR") + UnitUtils.convertVolumeToCurrentUnit(mBrd.getVolume());
        		strLength = fontMetrics.stringWidth(volumeStr);
        		g.drawString(volumeStr, (int)(width-strLength)/2, (int)((upperFrameHeight + topMargin + fontMetrics.getHeight()*4)));

        		String shaperAndSurferStr = ((!mBrd.getDesigner().isEmpty())?(LanguageResource.getString("DESIGNED_BY_STR") + mBrd.getDesigner() + " "):"") + ((!mBrd.getSurfer().isEmpty())?(LanguageResource.getString("FOR_STR") + mBrd.getSurfer()):"");
        		strLength = fontMetrics.stringWidth(shaperAndSurferStr);
        		g.drawString(shaperAndSurferStr, (int)(width-strLength)/2, (int)((upperFrameHeight + topMargin + fontMetrics.getHeight()*5)));
            }
        }

}
