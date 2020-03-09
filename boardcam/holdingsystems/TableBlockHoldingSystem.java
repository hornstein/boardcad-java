package boardcam.holdingsystems;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import javax.media.j3d.Appearance;
import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.IndexedQuadArray;
import javax.media.j3d.LineArray;
import javax.media.j3d.Shape3D;
import javax.media.j3d.TransparencyAttributes;

import cadcore.BezierSpline;
import cadcore.UnitUtils;
import cadcore.AxisAlignedBoundingBox;

import com.sun.j3d.utils.geometry.Box;

import javax.vecmath.Point2d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import board.AbstractBoard;
import board.BezierBoard;
import boardcad.settings.Setting;
import boardcad.settings.Settings;
import boardcad.settings.Settings.SettingChangedCallback;
import boardcam.MachineConfig;
import boardcam.cutters.AbstractCutter;
import boardcad.i18n.LanguageResource;

public class TableBlockHoldingSystem extends AbstractBlankHoldingSystem {

		static String BLOCK_WIDTH = "BlockWidth";
		static String BLOCK_THICKNESS = "BlockHeight";
		static String BLOCK_LENGTH = "BlockLength";

		MachineConfig mConfig = null;
		
		IndexedQuadArray mBlockBox;
		Shape3D mBlockShape;

		public TableBlockHoldingSystem(MachineConfig config) {
			super.init();
			mConfig = config;
			System.out.printf(
					"TableBlockHoldingSystem mConfig set to %s in %s\n",
					mConfig.toString(), this.toString());

			Settings blockSettings = mConfig.addCategory(LanguageResource
					.getString("BLANKHOLDINGSYSTEMCATEGORY_STR"));
			blockSettings.putPreferences(); // Save previous
			blockSettings.clear();
			SettingChangedCallback cb = new Settings.SettingChangedCallback() {
				public void onSettingChanged(Object object) {
					calcBlankOffset();
					update3DModel();
				}
			};

			blockSettings.addMeasurement(BLOCK_LENGTH, 244,
					LanguageResource.getString("BLOCKLENGTH_STR"), cb);
			blockSettings.addMeasurement(BLOCK_THICKNESS, 15,
					LanguageResource.getString("BLOCKTHICKNESS_STR"), cb);
			blockSettings.addMeasurement(BLOCK_WIDTH, 60,
					LanguageResource.getString("BLOCKWIDTH_STR"), cb);

			setBoardDeckOffsetPos(getBoardDeckOffsetPos());
			//
			init3DModel();
		}

		public void setBoardDeckOffsetPos(Vector3d offset) {
			super.setBoardDeckOffsetPos(offset);
			String holdingSystemStr = LanguageResource.getString("BLANKHOLDINGSYSTEMCATEGORY_STR");
			Settings holdingSystemSettings = mConfig.getCategory(holdingSystemStr);
			double blockThickness = holdingSystemSettings.getMeasurement(BLOCK_THICKNESS);
			mBoardBottomOffset.set(offset.x, offset.y, blockThickness-offset.z);
		}

		public void setBoardDeckOffsetAngle(double angle) {
			super.setBoardDeckOffsetAngle(angle);
			mBoardBottomRotation = -angle;
		}

		public Vector3d getBoardDeckOffsetPos() {
			return mBoardDeckOffset;
		}

		public Vector3d getBoardBottomOffsetPos() {
			return mBoardBottomOffset;
		}

		public void calcBlankOffset() {
		}

		public boolean checkCollision(Point3d pos, AbstractCutter cutter) {
			return false;
		}

		public ArrayList<double[]> handleCollision(Point3d point, AbstractCutter cutter, AbstractBoard board) {

			ArrayList<double[]> res = new ArrayList<double[]>();
			
			return res;
		}

		public void draw(Graphics2D g2d, double offsetX, double offsetY,
				double scale, boolean showDeck) {
			// Setup for drawing
			AffineTransform savedTransform = g2d.getTransform();

			g2d.setColor(Color.BLACK);

			Stroke stroke = new BasicStroke((float) (1.0 / scale));
			g2d.setStroke(stroke);

			AffineTransform at = new AffineTransform();

			at.setToTranslation(offsetX, offsetY);

			g2d.transform(at);

			at.setToScale(scale, scale);

			g2d.transform(at);

			// Get parameters
			String holdingSystemStr = LanguageResource.getString("BLANKHOLDINGSYSTEMCATEGORY_STR");
			Settings holdingSystemSettings = mConfig.getCategory(holdingSystemStr);
			double blockLength = holdingSystemSettings.getMeasurement(BLOCK_LENGTH);
			double blockThickness = holdingSystemSettings.getMeasurement(BLOCK_THICKNESS);
			double blockWidth = holdingSystemSettings.getMeasurement(BLOCK_WIDTH);
			
			// Draw offset line
			Line2D line = new Line2D.Double();

			// Draw center reference line
			line.setLine(-blockLength*0.05, 0, blockLength*1.1, 0);
			g2d.setColor(Color.GRAY);
			g2d.draw(line);
			
			// Draw blocks
			Rectangle2D blockRect = new Rectangle2D.Double();
			blockRect.setFrame(0, -blockThickness, blockLength, blockThickness);

			g2d.setColor(Color.WHITE);
			g2d.setStroke(new BasicStroke((float) (1.0 / scale)));
			g2d.draw(blockRect);

			g2d.setTransform(savedTransform);

			super.draw(g2d, offsetX, offsetY, scale, showDeck);
		}

		void init3DModel() {

			mBlockBox = new IndexedQuadArray(8, GeometryArray.COORDINATES,
					6 * 4);

			mBlockBox.setCapability(GeometryArray.ALLOW_COORDINATE_WRITE);

			mBlockBox.setCoordinateIndex(0, 0); // Front end
			mBlockBox.setCoordinateIndex(1, 1);
			mBlockBox.setCoordinateIndex(2, 2);
			mBlockBox.setCoordinateIndex(3, 3);

			mBlockBox.setCoordinateIndex(4, 7); // Back end
			mBlockBox.setCoordinateIndex(5, 6);
			mBlockBox.setCoordinateIndex(6, 5);
			mBlockBox.setCoordinateIndex(7, 4);

			mBlockBox.setCoordinateIndex(8, 0); // Top
			mBlockBox.setCoordinateIndex(9, 3);
			mBlockBox.setCoordinateIndex(10, 7);
			mBlockBox.setCoordinateIndex(11, 4);

			mBlockBox.setCoordinateIndex(12, 1); // Bottom
			mBlockBox.setCoordinateIndex(13, 5);
			mBlockBox.setCoordinateIndex(14, 6);
			mBlockBox.setCoordinateIndex(15, 2);

			mBlockBox.setCoordinateIndex(16, 0); // Left
			mBlockBox.setCoordinateIndex(17, 4);
			mBlockBox.setCoordinateIndex(18, 5);
			mBlockBox.setCoordinateIndex(19, 1);

			mBlockBox.setCoordinateIndex(20, 7); // Right
			mBlockBox.setCoordinateIndex(21, 3);
			mBlockBox.setCoordinateIndex(22, 2);
			mBlockBox.setCoordinateIndex(23, 6);

			mBlockShape = new Shape3D(mBlockBox);

			Appearance blockApperance = new Appearance();
			ColoringAttributes blockColor = new ColoringAttributes();
			blockColor.setColor(0.3f, 0.3f, 0.5f);
			TransparencyAttributes transparency = new TransparencyAttributes();
			transparency.setTransparency(0.5f);
			transparency.setTransparencyMode(TransparencyAttributes.BLENDED);
			blockApperance.setColoringAttributes(blockColor);
			blockApperance.setTransparencyAttributes(transparency);
			mBlockShape.setAppearance(blockApperance);

			mModelRoot.addChild(mBlockShape);

			update3DModel();

		}

		public void update3DModel() {
			String holdingSystemStr = LanguageResource
					.getString("BLANKHOLDINGSYSTEMCATEGORY_STR");
			Settings holdingSystemSettings = mConfig.getCategory(holdingSystemStr);
			double blockLength = holdingSystemSettings.getMeasurement(BLOCK_LENGTH) * UnitUtils.MILLIMETER_PR_CENTIMETER;
			double blockThickness = holdingSystemSettings.getMeasurement(BLOCK_THICKNESS) * UnitUtils.MILLIMETER_PR_CENTIMETER;
			double blockWidth = holdingSystemSettings.getMeasurement(BLOCK_WIDTH) * UnitUtils.MILLIMETER_PR_CENTIMETER;

			mBlockBox.setCoordinate(0, new Point3d(0, blockWidth / 2.0, blockThickness ));
			mBlockBox.setCoordinate(1, new Point3d(0, blockWidth / 2.0, 0.0));
			mBlockBox.setCoordinate(2, new Point3d(0, -blockWidth / 2.0, 0.0));
			mBlockBox.setCoordinate(3, new Point3d(0, -blockWidth / 2.0, blockThickness));

			mBlockBox.setCoordinate(4, new Point3d(blockLength, blockWidth / 2.0, blockThickness));
			mBlockBox.setCoordinate(5, new Point3d(blockLength, blockWidth / 2.0, 0.0));
			mBlockBox.setCoordinate(6, new Point3d(blockLength, -blockWidth / 2.0, 0.0));
			mBlockBox.setCoordinate(7, new Point3d(blockLength, -blockWidth / 2.0, blockThickness));

		}

		public void updateBoundingBoxes() {
			String holdingSystemStr = LanguageResource
					.getString("BLANKHOLDINGSYSTEMCATEGORY_STR");
			Settings holdingSystemSettings = mConfig.getCategory(holdingSystemStr);
			double blockLength = holdingSystemSettings.getMeasurement(BLOCK_LENGTH) * UnitUtils.MILLIMETER_PR_CENTIMETER;
			double blockThickness = holdingSystemSettings.getMeasurement(BLOCK_THICKNESS) * UnitUtils.MILLIMETER_PR_CENTIMETER;
			double blockWidth = holdingSystemSettings.getMeasurement(BLOCK_WIDTH) * UnitUtils.MILLIMETER_PR_CENTIMETER;

			// TODO: No collision checks
		}

		public String toString(boolean isDeckCut) {
			String holdingSystemStr = LanguageResource
					.getString("BLANKHOLDINGSYSTEMCATEGORY_STR");
			Settings holdingSystemSettings = mConfig.getCategory(holdingSystemStr);
			double blockLength = holdingSystemSettings.getMeasurement(BLOCK_LENGTH) * UnitUtils.MILLIMETER_PR_CENTIMETER;
			double blockThickness = holdingSystemSettings.getMeasurement(BLOCK_THICKNESS) * UnitUtils.MILLIMETER_PR_CENTIMETER;
			double blockWidth = holdingSystemSettings.getMeasurement(BLOCK_WIDTH) * UnitUtils.MILLIMETER_PR_CENTIMETER;

			String str = new String();
			str.concat(LanguageResource.getString("ENDBLOCKS_STR"));
			str.concat(" ");
			str.concat(holdingSystemStr);
			str.concat("\n");

			String details = String.format(
					"%s:%f, %s:%f, %s:%f\n",
					LanguageResource.getString("BLOCKLENGTH_STR"), blockLength,
					LanguageResource.getString("BLOCKHEIGHT_STR"), blockThickness,
					LanguageResource.getString("BLOCKWIDTH_STR"), blockWidth);
			str.concat(details);

			str.concat(super.toString(isDeckCut));

			return str;
		}

	}
