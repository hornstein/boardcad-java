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

public class EndClampsBlankHoldingSystem extends AbstractBlankHoldingSystem {

	static String CLAMP_WIDTH = "ClampWidth";
	static String CLAMP_HEIGHT = "ClampHeight";
	static String CLAMP_LENGTH = "ClampLength";
	static String CLAMP_OVERLAP = "ClampOverlap";
	static String BLANK_VERTICAL_OFFSET = "BlankVerticalOffset";
	static String CLAMP_SAFEZONE = "ClampSafeZone";

	MachineConfig mConfig = null;

	double mRotatedLength = 0;

	IndexedQuadArray mNoseClampBox;
	IndexedQuadArray mTailClampBox;

	Shape3D mNoseClampShape;
	Shape3D mTailClampShape;

	AxisAlignedBoundingBox mNoseBox;
	AxisAlignedBoundingBox mTailBox;

	AxisAlignedBoundingBox mPastNoseBox;
	AxisAlignedBoundingBox mPastTailBox;

	private boolean mCollideWithNoseClamp = false;
	private boolean mCollideWithTailClamp = false;
	private boolean mPastNoseClamp = false;
	private boolean mPastTailClamp = false;

	private boolean mPreviousClampCollision = false;

	private Point3d mPreviousPos = null;

	public EndClampsBlankHoldingSystem(MachineConfig config) {
		super.init();
		mConfig = config;
		System.out.printf(
				"EndClampsBlankHoldingSystem mConfig set to %s in %s\n",
				mConfig.toString(), this.toString());

		Settings supportsSettings = mConfig.addCategory(LanguageResource
				.getString("BLANKHOLDINGSYSTEMCATEGORY_STR"));
		supportsSettings.putPreferences(); // Save previous
		supportsSettings.clear();
		SettingChangedCallback cb = new Settings.SettingChangedCallback() {
			public void onSettingChanged(Object object) {
				calcBlankOffset();
				update3DModel();
			}
		};

		supportsSettings.addMeasurement(CLAMP_LENGTH, 80,
				LanguageResource.getString("CLAMPLENGTH_STR"), cb);
		supportsSettings.addMeasurement(CLAMP_HEIGHT, 20,
				LanguageResource.getString("CLAMPHEIGHT_STR"), cb);
		supportsSettings.addMeasurement(CLAMP_WIDTH, 20,
				LanguageResource.getString("CLAMPWIDTH_STR"), cb);
		supportsSettings.addMeasurement(CLAMP_SAFEZONE, 5,
				LanguageResource.getString("CLAMPSAFEZONE_STR"), cb);

		supportsSettings.addMeasurement(CLAMP_OVERLAP, 8,
				LanguageResource.getString("CLAMPOVERLAP_STR"), cb);
		supportsSettings.addMeasurement(BLANK_VERTICAL_OFFSET, 10,
				LanguageResource.getString("BLANKVERTICALOFFSET_STR"), cb);

		SettingChangedCallback blankChange = new Settings.SettingChangedCallback() {
			public void onSettingChanged(Object object) {
				calcBlankOffset();
				update3DModel();
				updateBoundingBoxes();
			}
		};

		Settings generalSettings = mConfig.getCategory(LanguageResource
				.getString("GENERALCATEGORY_STR"));
		Setting blankSetting = generalSettings.getSetting(LanguageResource
				.getString("BLANK_STR"));
		blankSetting.addCallback(blankChange);

		//
		init3DModel();
	}

	public void setBoardDeckOffsetPos(Vector3d offset) {
		super.setBoardDeckOffsetPos(offset);
		mBoardBottomOffset.set(offset.x, offset.y, -offset.z);
	}

	public void setBoardDeckOffsetAngle(double angle) {
		super.setBoardDeckOffsetAngle(angle);
		mBoardBottomRotation = -angle;
	}

	public Vector3d getBoardDeckOffsetPos() {
		mPreviousClampCollision = false; // TODO: this is a "Hack" to reset
											// before starting deck cutting, fix
											// with a reset function
		mPreviousPos = null;

		return mBoardDeckOffset;
	}

	public Vector3d getBoardBottomOffsetPos() {
		mPreviousClampCollision = false; // TODO: this is a "Hack" to reset
											// before starting bottom cutting,
											// fix with a reset function
		mPreviousPos = null;

		return mBoardBottomOffset;
	}

	public void calcBlankOffset() {
		// Get parameters
		String holdingSystemStr = LanguageResource
				.getString("BLANKHOLDINGSYSTEMCATEGORY_STR");
		Settings holdingSystemSettings = mConfig.getCategory(holdingSystemStr);
		double clampLength = holdingSystemSettings.getMeasurement(CLAMP_LENGTH);
		double clampHeight = holdingSystemSettings.getMeasurement(CLAMP_HEIGHT);
		double clampWidth = holdingSystemSettings.getMeasurement(CLAMP_WIDTH);
		double clampOverlap = holdingSystemSettings
				.getMeasurement(CLAMP_OVERLAP);
		double blankVertOffset = holdingSystemSettings
				.getMeasurement(BLANK_VERTICAL_OFFSET);
		double clampSafeZone = holdingSystemSettings
				.getMeasurement(CLAMP_SAFEZONE);

		// Get blank and calculate angle
		BezierBoard blank = mConfig.getBlank();

		if (blank != null && !blank.isEmpty()) {
			Point2d tailMaxPos = blank.getMaxDeckAtTailPos();
			Point2d noseMaxPos = blank.getMaxDeckAtNosePos();

			double tailBottom = blank.getBottomAtPos(0.0, 0.0);
			double noseBottom = blank.getBottomAt(blank.getLength()
					- BezierSpline.ZERO, 0.0);

			double rotAngle = Math.atan2(noseMaxPos.y - tailMaxPos.y,
					noseMaxPos.x - tailMaxPos.x);

			mBlankDeckRotation = rotAngle;
			mBlankBottomRotation = -rotAngle;

			double vertOffset = Math.cos(rotAngle) * tailMaxPos.y
					- Math.sin(rotAngle) * tailMaxPos.x;
			vertOffset -= blankVertOffset;

			double horOffset = -Math.sin(rotAngle) * tailMaxPos.y;

			mBlankDeckOffset = new Vector3d(horOffset - clampOverlap, 0,
					-vertOffset);
			mBlankBottomOffset = new Vector3d(horOffset - clampOverlap, 0,
					vertOffset);

			mRotatedLength = (Math.sin(rotAngle) * (noseBottom - tailBottom))
					+ (Math.cos(rotAngle) * blank.getLength());
		}
	}

	public boolean checkCollision(Point3d pos, AbstractCutter cutter) {
		mCollideWithNoseClamp = false;
		mCollideWithTailClamp = false;
		mPastNoseClamp = false;
		mPastTailClamp = false;

		// The cutter is not allowed further back than the back end of the
		// clamps
		// Check collision against clamps
		if (cutter.checkCollision(pos, mPastNoseBox)) {
			mPastNoseClamp = true;
			return true;
		} else if (cutter.checkCollision(pos, mPastTailBox)) {
			mPastTailClamp = true;
			return true;
		}

		if (cutter.checkCollision(pos, mNoseBox))// Check collision against nose
													// clamp
		{
			mCollideWithNoseClamp = true;
			return true;
		}

		if (cutter.checkCollision(pos, mTailBox))// Check collision against tail
													// clamp
		{
			mCollideWithTailClamp = true;
			return true;
		}

		if (mPreviousClampCollision == true) {
			return true;
		}

		mPreviousPos = pos;
		return false;
	}

	public ArrayList<double[]> handleCollision(Point3d point,
			AbstractCutter cutter, AbstractBoard board) {
		ArrayList<double[]> res = new ArrayList<double[]>();

		if (mPastNoseClamp || mPastTailClamp) {
			// System.out.printf("Collided with past clamp\n");

			// if(mCollideWithClamp && !mPreviousClampCollision && mPreviousPos
			// != null)
			// {
			// //Straight up from last good pos
			// res.add(new double[]{mPreviousPos.x, mPreviousPos.y,
			// mNoseBox.getTop() + 10.0});
			// }
			//
			// //Move to x from pastbox
			// AxisAlignedBoundingBox cutterBox = cutter.getBoundingBox(point);
			// res.add(new double[]{mPastNoseClamp?mPastNoseBox.getMaxX() +
			// cutterBox.getLength()/2.0:mPastTailBox.getMinX() -
			// cutterBox.getLength()/2.0, point.y,
			// (mCollideWithClamp?mNoseBox.getTop() + 10.0:point.z)});
			//
			// if(mCollideWithClamp)
			// {
			// mPreviousClampCollision = true;
			// }

		} else if (mCollideWithNoseClamp || mCollideWithTailClamp) {
			// System.out.printf("Collided with %s clamp\n",
			// mCollideWithNoseClamp ? "nose" : "tail");

			// Calc above edge of clamp at the collision position
			String holdingSystemStr = LanguageResource
					.getString("BLANKHOLDINGSYSTEMCATEGORY_STR");
			Settings holdingSystemSettings = mConfig
					.getCategory(holdingSystemStr);
			double clampOverlap = holdingSystemSettings
					.getMeasurement(CLAMP_OVERLAP)
					* UnitUtils.MILLIMETER_PR_CENTIMETER;
			double clampSafeZone = holdingSystemSettings
					.getMeasurement(CLAMP_SAFEZONE)
					* UnitUtils.MILLIMETER_PR_CENTIMETER;

			double clampEdgeX = mCollideWithNoseClamp ? clampSafeZone
					: mRotatedLength * 10 - clampOverlap * 2 - clampSafeZone; // NOTE:
																				// Tail
																				// clamp
																				// and
																				// nose
																				// clamp
																				// is
																				// mixed
																				// up
			Vector3d normal = new Vector3d(mCollideWithNoseClamp ? 1.0 : -1.0,
					0.0, 0.0);

			// System.out.printf("point.x:%f clampEdgeX:%f", point.x,
			// clampEdgeX);

			double safeHeight = mNoseBox.getTop() + 25.0;

			Point3d pos = new Point3d(clampEdgeX, point.y, safeHeight);

			System.out
					.printf("--- Collision occured, limiting position to collisionpoint: %f,%f,%f\n",
							pos.x, pos.y, pos.z);

			double[] calculated = cutter.calcOffset(pos, normal, board);

			// System.out.printf("--- Collison offset by cutter point: %f,%f,%f\n",
			// calculated[0], calculated[1], calculated[2]);

			if (!mPreviousClampCollision && mPreviousPos != null) {
				// Straight up from last good pos
				res.add(new double[] { mPreviousPos.x, mPreviousPos.y,
						calculated[2] });
			}

			res.add(calculated);

			mPreviousClampCollision = true;

			mPreviousPos = new Point3d(calculated[0], calculated[1],
					calculated[2]);
		} else if (mPreviousClampCollision) {
			// System.out.printf("Previous collided\n");

			// Move from previous colliding position to above new non colliding
			// pos. Moving directly (without going above clamp) might result in
			// collision
			res.add(new double[] { point.x, point.y, mPreviousPos.z });

			// The move to the good position
			res.add(new double[] { point.x, point.y, point.z });

			mPreviousClampCollision = false;
		}

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

		double mulX = 1;
		double mulY = -1; // Flip y

		// Get parameters
		String holdingSystemStr = LanguageResource
				.getString("BLANKHOLDINGSYSTEMCATEGORY_STR");
		Settings holdingSystemSettings = mConfig.getCategory(holdingSystemStr);
		double clampLength = holdingSystemSettings.getMeasurement(CLAMP_LENGTH);
		double clampHeight = holdingSystemSettings.getMeasurement(CLAMP_HEIGHT);
		double clampWidth = holdingSystemSettings.getMeasurement(CLAMP_WIDTH);
		double clampOverlap = holdingSystemSettings
				.getMeasurement(CLAMP_OVERLAP);
		double blankVertOffset = holdingSystemSettings
				.getMeasurement(BLANK_VERTICAL_OFFSET);
		double clampSafeZone = holdingSystemSettings
				.getMeasurement(CLAMP_SAFEZONE);

		// Draw clamps
		Rectangle2D tailClampRect = new Rectangle2D.Double();
		tailClampRect.setFrame(-clampLength, -clampHeight / 2, clampLength,
				clampHeight);

		Rectangle2D noseClampRect = new Rectangle2D.Double();
		noseClampRect.setFrame(mRotatedLength - clampOverlap * 2,
				-clampHeight / 2, clampLength, clampHeight);

		// Draw safe zone
		Rectangle2D tailSafeZoneRect = new Rectangle2D.Double();
		tailSafeZoneRect.setFrame(-clampLength, -clampHeight / 2
				- clampSafeZone, clampLength + clampSafeZone, clampHeight
				+ clampSafeZone * 2);

		Rectangle2D noseSafeZoneRect = new Rectangle2D.Double();
		noseSafeZoneRect.setFrame(mRotatedLength - clampOverlap * 2
				- clampSafeZone, -clampHeight / 2 - clampSafeZone, clampLength
				+ clampSafeZone, clampHeight + clampSafeZone * 2);

		// Draw offset line
		Line2D line = new Line2D.Double();

		// Draw offset line
		line.setLine(tailClampRect.getMaxX(), showDeck ? -blankVertOffset
				: blankVertOffset, noseClampRect.getMinX(),
				showDeck ? -blankVertOffset : blankVertOffset);
		g2d.setColor(Color.WHITE);
		g2d.draw(line);

		// Draw center reference line
		line.setLine(tailClampRect.getMaxX(), 0, noseClampRect.getMinX(), 0);
		g2d.setColor(Color.GRAY);
		g2d.draw(line);

		g2d.setStroke(new BasicStroke((float) (2.0 / scale)));
		g2d.setColor(Color.BLACK);
		g2d.draw(noseClampRect);
		g2d.draw(tailClampRect);

		g2d.setColor(Color.BLUE);
		g2d.setStroke(new BasicStroke((float) (1.0 / scale)));
		g2d.draw(noseSafeZoneRect);
		g2d.draw(tailSafeZoneRect);

		g2d.setTransform(savedTransform);

		super.draw(g2d, offsetX, offsetY, scale, showDeck);
	}

	void init3DModel() {

		mNoseClampBox = new IndexedQuadArray(8, GeometryArray.COORDINATES,
				6 * 4);
		mTailClampBox = new IndexedQuadArray(8, GeometryArray.COORDINATES,
				6 * 4);

		mNoseClampBox.setCapability(GeometryArray.ALLOW_COORDINATE_WRITE);
		mTailClampBox.setCapability(GeometryArray.ALLOW_COORDINATE_WRITE);

		mNoseClampBox.setCoordinateIndex(0, 0); // Front end
		mNoseClampBox.setCoordinateIndex(1, 1);
		mNoseClampBox.setCoordinateIndex(2, 2);
		mNoseClampBox.setCoordinateIndex(3, 3);

		mNoseClampBox.setCoordinateIndex(4, 7); // Back end
		mNoseClampBox.setCoordinateIndex(5, 6);
		mNoseClampBox.setCoordinateIndex(6, 5);
		mNoseClampBox.setCoordinateIndex(7, 4);

		mNoseClampBox.setCoordinateIndex(8, 0); // Top
		mNoseClampBox.setCoordinateIndex(9, 3);
		mNoseClampBox.setCoordinateIndex(10, 7);
		mNoseClampBox.setCoordinateIndex(11, 4);

		mNoseClampBox.setCoordinateIndex(12, 1); // Bottom
		mNoseClampBox.setCoordinateIndex(13, 5);
		mNoseClampBox.setCoordinateIndex(14, 6);
		mNoseClampBox.setCoordinateIndex(15, 2);

		mNoseClampBox.setCoordinateIndex(16, 0); // Left
		mNoseClampBox.setCoordinateIndex(17, 4);
		mNoseClampBox.setCoordinateIndex(18, 5);
		mNoseClampBox.setCoordinateIndex(19, 1);

		mNoseClampBox.setCoordinateIndex(20, 7); // Right
		mNoseClampBox.setCoordinateIndex(21, 3);
		mNoseClampBox.setCoordinateIndex(22, 2);
		mNoseClampBox.setCoordinateIndex(23, 6);

		mTailClampBox.setCoordinateIndex(0, 0); // Front end
		mTailClampBox.setCoordinateIndex(1, 1);
		mTailClampBox.setCoordinateIndex(2, 2);
		mTailClampBox.setCoordinateIndex(3, 3);

		mTailClampBox.setCoordinateIndex(4, 7); // Back end
		mTailClampBox.setCoordinateIndex(5, 6);
		mTailClampBox.setCoordinateIndex(6, 5);
		mTailClampBox.setCoordinateIndex(7, 4);

		mTailClampBox.setCoordinateIndex(8, 0); // Top
		mTailClampBox.setCoordinateIndex(9, 3);
		mTailClampBox.setCoordinateIndex(10, 7);
		mTailClampBox.setCoordinateIndex(11, 4);

		mTailClampBox.setCoordinateIndex(12, 1); // Bottom
		mTailClampBox.setCoordinateIndex(13, 5);
		mTailClampBox.setCoordinateIndex(14, 6);
		mTailClampBox.setCoordinateIndex(15, 2);

		mTailClampBox.setCoordinateIndex(16, 0); // Left
		mTailClampBox.setCoordinateIndex(17, 4);
		mTailClampBox.setCoordinateIndex(18, 5);
		mTailClampBox.setCoordinateIndex(19, 1);

		mTailClampBox.setCoordinateIndex(20, 7); // Right
		mTailClampBox.setCoordinateIndex(21, 3);
		mTailClampBox.setCoordinateIndex(22, 2);
		mTailClampBox.setCoordinateIndex(23, 6);

		mNoseClampShape = new Shape3D(mNoseClampBox);
		mTailClampShape = new Shape3D(mTailClampBox);

		Appearance supportStructureApperance = new Appearance();
		ColoringAttributes supportStructureColor = new ColoringAttributes();
		supportStructureColor.setColor(0.1f, 0.1f, 0.5f);
		supportStructureApperance.setColoringAttributes(supportStructureColor);
		mNoseClampShape.setAppearance(supportStructureApperance);
		mTailClampShape.setAppearance(supportStructureApperance);

		mModelRoot.addChild(mNoseClampShape);
		mModelRoot.addChild(mTailClampShape);

		update3DModel();

	}

	public void update3DModel() {
		String holdingSystemStr = LanguageResource
				.getString("BLANKHOLDINGSYSTEMCATEGORY_STR");
		Settings holdingSystemSettings = mConfig.getCategory(holdingSystemStr);
		double clampLength = holdingSystemSettings.getMeasurement(CLAMP_LENGTH)
				* UnitUtils.MILLIMETER_PR_CENTIMETER;
		double clampHeight = holdingSystemSettings.getMeasurement(CLAMP_HEIGHT)
				* UnitUtils.MILLIMETER_PR_CENTIMETER;
		double clampWidth = holdingSystemSettings.getMeasurement(CLAMP_WIDTH)
				* UnitUtils.MILLIMETER_PR_CENTIMETER;
		double clampOverlap = holdingSystemSettings
				.getMeasurement(CLAMP_OVERLAP)
				* UnitUtils.MILLIMETER_PR_CENTIMETER;

		mNoseClampBox.setCoordinate(0, new Point3d(-clampLength,
				clampWidth / 2.0, clampHeight / 2));
		mNoseClampBox.setCoordinate(1, new Point3d(-clampLength,
				clampWidth / 2.0, -clampHeight / 2));
		mNoseClampBox.setCoordinate(2, new Point3d(-clampLength,
				-clampWidth / 2.0, -clampHeight / 2));
		mNoseClampBox.setCoordinate(3, new Point3d(-clampLength,
				-clampWidth / 2.0, clampHeight / 2));

		mNoseClampBox.setCoordinate(4, new Point3d(0, clampWidth / 2.0,
				clampHeight / 2));
		mNoseClampBox.setCoordinate(5, new Point3d(0, clampWidth / 2.0,
				-clampHeight / 2));
		mNoseClampBox.setCoordinate(6, new Point3d(0, -clampWidth / 2.0,
				-clampHeight / 2));
		mNoseClampBox.setCoordinate(7, new Point3d(0, -clampWidth / 2.0,
				clampHeight / 2));

		mTailClampBox.setCoordinate(0, new Point3d(mRotatedLength * 10
				- clampOverlap * 2, clampWidth / 2.0, clampHeight / 2));
		mTailClampBox.setCoordinate(1, new Point3d(mRotatedLength * 10
				- clampOverlap * 2, clampWidth / 2.0, -clampHeight / 2));
		mTailClampBox.setCoordinate(2, new Point3d(mRotatedLength * 10
				- clampOverlap * 2, -clampWidth / 2.0, -clampHeight / 2));
		mTailClampBox.setCoordinate(3, new Point3d(mRotatedLength * 10
				- clampOverlap * 2, -clampWidth / 2.0, clampHeight / 2));

		mTailClampBox.setCoordinate(4, new Point3d(mRotatedLength * 10
				+ clampLength - clampOverlap * 2, clampWidth / 2.0,
				clampHeight / 2));
		mTailClampBox.setCoordinate(5, new Point3d(mRotatedLength * 10
				+ clampLength - clampOverlap * 2, clampWidth / 2.0,
				-clampHeight / 2));
		mTailClampBox.setCoordinate(6, new Point3d(mRotatedLength * 10
				+ clampLength - clampOverlap * 2, -clampWidth / 2.0,
				-clampHeight / 2));
		mTailClampBox.setCoordinate(7, new Point3d(mRotatedLength * 10
				+ clampLength - clampOverlap * 2, -clampWidth / 2.0,
				clampHeight / 2));
	}

	public void updateBoundingBoxes() {
		String holdingSystemStr = LanguageResource
				.getString("BLANKHOLDINGSYSTEMCATEGORY_STR");
		Settings holdingSystemSettings = mConfig.getCategory(holdingSystemStr);
		double clampLength = holdingSystemSettings.getMeasurement(CLAMP_LENGTH)
				* UnitUtils.MILLIMETER_PR_CENTIMETER;
		double clampHeight = holdingSystemSettings.getMeasurement(CLAMP_HEIGHT)
				* UnitUtils.MILLIMETER_PR_CENTIMETER;
		double clampWidth = holdingSystemSettings.getMeasurement(CLAMP_WIDTH)
				* UnitUtils.MILLIMETER_PR_CENTIMETER;
		double clampOverlap = holdingSystemSettings
				.getMeasurement(CLAMP_OVERLAP)
				* UnitUtils.MILLIMETER_PR_CENTIMETER;
		double clampSafeZone = holdingSystemSettings
				.getMeasurement(CLAMP_SAFEZONE)
				* UnitUtils.MILLIMETER_PR_CENTIMETER;

		mNoseBox = new AxisAlignedBoundingBox(new Point3d(-clampLength,
				-clampWidth / 2.0 - clampSafeZone, -clampHeight / 2
						- clampSafeZone), new Point3d(0 + clampSafeZone,
				clampWidth / 2.0 + clampSafeZone, clampHeight / 2
						+ clampSafeZone));
		mTailBox = new AxisAlignedBoundingBox(new Point3d(mRotatedLength * 10
				- clampOverlap * 2 - clampSafeZone, -clampWidth / 2.0
				- clampSafeZone, -clampHeight / 2 - clampSafeZone),
				new Point3d(mRotatedLength * 10 - clampOverlap * 2
						- clampSafeZone + clampLength, clampWidth / 2.0
						+ clampSafeZone, clampHeight / 2 + clampSafeZone));

		mPastNoseBox = new AxisAlignedBoundingBox(new Point3d(
				-Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE),
				new Point3d((-clampLength), Double.MAX_VALUE, Double.MAX_VALUE));
		mPastTailBox = new AxisAlignedBoundingBox(new Point3d((mRotatedLength
				* 10 + clampLength - clampOverlap * 2), -Double.MAX_VALUE,
				-Double.MAX_VALUE), new Point3d(Double.MAX_VALUE,
				Double.MAX_VALUE, Double.MAX_VALUE));
	}

	public String toString(boolean isDeckCut) {
		String holdingSystemStr = LanguageResource
				.getString("BLANKHOLDINGSYSTEMCATEGORY_STR");
		Settings holdingSystemSettings = mConfig.getCategory(holdingSystemStr);
		double clampLength = holdingSystemSettings.getMeasurement(CLAMP_LENGTH)
				* UnitUtils.MILLIMETER_PR_CENTIMETER;
		double clampHeight = holdingSystemSettings.getMeasurement(CLAMP_HEIGHT)
				* UnitUtils.MILLIMETER_PR_CENTIMETER;
		double clampWidth = holdingSystemSettings.getMeasurement(CLAMP_WIDTH)
				* UnitUtils.MILLIMETER_PR_CENTIMETER;
		double clampOverlap = holdingSystemSettings
				.getMeasurement(CLAMP_OVERLAP)
				* UnitUtils.MILLIMETER_PR_CENTIMETER;
		double clampSafeZone = holdingSystemSettings
				.getMeasurement(CLAMP_SAFEZONE)
				* UnitUtils.MILLIMETER_PR_CENTIMETER;
		double blankVertOffset = holdingSystemSettings
				.getMeasurement(BLANK_VERTICAL_OFFSET)
				* UnitUtils.MILLIMETER_PR_CENTIMETER;

		String str = new String();
		str.concat(LanguageResource.getString("ENDCLAMPS_STR"));
		str.concat(" ");
		str.concat(holdingSystemStr);
		str.concat("\n");

		String details = String.format(
				"%s:%f, %s:%f, %s:%f, %s:%f, %s:%f, %s:%f\n",
				LanguageResource.getString("CLAMPLENGTH_STR"), clampLength,
				LanguageResource.getString("CLAMPHEIGHT_STR"), clampHeight,
				LanguageResource.getString("CLAMPWIDTH_STR"), clampWidth,
				LanguageResource.getString("CLAMPSAFEZONE_STR"), clampSafeZone,
				LanguageResource.getString("CLAMPOVERLAP_STR"), clampOverlap,
				LanguageResource.getString("BLANKVERTICALOFFSET_STR"),
				blankVertOffset);
		str.concat(details);

		str.concat(super.toString(isDeckCut));

		return str;
	}

}
