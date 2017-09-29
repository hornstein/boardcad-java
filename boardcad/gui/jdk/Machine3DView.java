package boardcad.gui.jdk;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.media.j3d.AmbientLight;
import javax.media.j3d.Appearance;
import javax.media.j3d.Background;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.DirectionalLight;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.Group;
import javax.media.j3d.LineArray;
import javax.media.j3d.LineStripArray;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Switch;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.ViewPlatform;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

import cadcore.UnitUtils;

import com.sun.j3d.utils.behaviors.vp.OrbitBehavior;
import com.sun.j3d.utils.universe.SimpleUniverse;
import com.sun.j3d.utils.universe.ViewingPlatform;
import boardcad.i18n.LanguageResource;


public class Machine3DView extends JComponent{
	static final long serialVersionUID=1L;
	TransformGroup mScale;
	Transform3D mTransform;
	double mNormalsScale;
	
	Background mBackgroundNode;
	
	TransformGroup mToolpathsGroup;
	Transform3D mToolpathsTransform;
	
	Switch mDeckOrBottomToolpathSwitch;
	Group mDeckToolpathGroup;
	Group mBottomToolpathGroup;

	Switch mDeckToolpathsSwitch;
	Shape3D mDeckToolpaths;
	Switch mBottomToolpathsSwitch;
	Shape3D mBottomToolpaths;

	TransformGroup mModelTransformGroup;
	Transform3D mModelTransform;
	
	Switch mDeckOrBottomModelSwitch;
	Group mDeckModelGroup;
	Group mBottomModelGroup;
	
	Switch mDeckSurfacePathsSwitch;
	Switch mDeckNormalsSwitch;

	Switch mBottomSurfacePathsSwitch;
	Switch mBottomSurfaceNormalsSwitch;

	Shape3D mDeckSurfacePaths;
	Shape3D mDeckSurfaceNormals;
	
	Shape3D mBottomSurfacePaths;
	Shape3D mBottomSurfaceNormals;
	
	BranchGroup mBlankHoldingSystemModel;
	
	BranchGroup mCutter;
	TransformGroup mCutterTransformGroup;
	Transform3D mCutterTransform;

	
	LineStripArray mDeckSurfacePathsArray;
	LineArray mDeckNormalsArray;
	LineStripArray mDeckToolpathsArray;
	
	int mDeckCurrentSurfacePathIndex = 0;
	int mDeckCurrentNormalIndex = 0;
	int mDeckCurrentToolpathIndex = 0;

	LineStripArray mBottomSurfacePathsArray;
	LineArray mBottomNormalsArray;
	LineStripArray mBottomToolpathsArray;
	
	int mBottomCurrentSurfacePathIndex = 0;
	int mBottomCurrentNormalIndex = 0;
	int mBottomCurrentToolpathIndex = 0;
	
	public Machine3DView()
	{
		JPopupMenu.setDefaultLightWeightPopupEnabled(false);
		final JPopupMenu menu = new JPopupMenu();

		final AbstractAction toggleDeckBottom = new AbstractAction() {
			static final long serialVersionUID=1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("TOGGLEDECKBOTTOM_STR"));
			};

			public void actionPerformed(ActionEvent arg0) {
				toggleDeckOrBottom();
			}
		};
		menu.add(toggleDeckBottom);
		
		menu.addSeparator();

		final AbstractAction toggleSurfacePaths = new AbstractAction() {
			static final long serialVersionUID=1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("TOGGLESURFACEPATHS_STR"));
			};

			public void actionPerformed(ActionEvent arg0) {
				mDeckSurfacePathsSwitch.setWhichChild((mDeckSurfacePathsSwitch.getWhichChild()==Switch.CHILD_ALL)?Switch.CHILD_NONE:Switch.CHILD_ALL);
				mBottomSurfacePathsSwitch.setWhichChild((mBottomSurfacePathsSwitch.getWhichChild()==Switch.CHILD_ALL)?Switch.CHILD_NONE:Switch.CHILD_ALL);
			}
		};
		menu.add(toggleSurfacePaths);

		final AbstractAction toggleNormals = new AbstractAction() {
			static final long serialVersionUID=1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("TOGGLENORMALS_STR"));
			};

			public void actionPerformed(ActionEvent arg0) {
				mDeckNormalsSwitch.setWhichChild((mDeckNormalsSwitch.getWhichChild()==Switch.CHILD_ALL)?Switch.CHILD_NONE:Switch.CHILD_ALL);
				mBottomSurfaceNormalsSwitch.setWhichChild((mBottomSurfaceNormalsSwitch.getWhichChild()==Switch.CHILD_ALL)?Switch.CHILD_NONE:Switch.CHILD_ALL);
			}
		};
		menu.add(toggleNormals);

		final AbstractAction toggleToolpaths = new AbstractAction() {
			static final long serialVersionUID=1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("TOGGLETOOLPATHS_STR"));
			};

			public void actionPerformed(ActionEvent arg0) {
				mDeckToolpathsSwitch.setWhichChild((mDeckToolpathsSwitch.getWhichChild()==Switch.CHILD_ALL)?Switch.CHILD_NONE:Switch.CHILD_ALL);
				mBottomToolpathsSwitch.setWhichChild((mBottomToolpathsSwitch.getWhichChild()==Switch.CHILD_ALL)?Switch.CHILD_NONE:Switch.CHILD_ALL);
			}
		};
		menu.add(toggleToolpaths);

		add(menu);	
		
		setLayout(new BorderLayout());

		final Canvas3D canvas = new Canvas3D(SimpleUniverse.getPreferredConfiguration());
		add("Center", canvas);

		canvas.addMouseListener(new MouseAdapter()
		{
			public void mouseClicked(MouseEvent e)
			{
				if(e.isMetaDown())
					menu.show(canvas, e.getX(), e.getY());
			}

		});

		SimpleUniverse universe = new SimpleUniverse(canvas);

		// add mouse behaviors to the ViewingPlatform
		ViewingPlatform viewingPlatform = universe.getViewingPlatform();
		viewingPlatform.setNominalViewingTransform();
		ViewPlatform platform = viewingPlatform.getViewPlatform();

		// add orbit behavior to the ViewingPlatform
		OrbitBehavior orbit = new OrbitBehavior(canvas);
		BoundingSphere bounds = new BoundingSphere(new Point3d(0.0, 0.0, 0.0), 100.0);
		orbit.setSchedulingBounds(bounds);
		orbit.setZoomFactor(0.2);
		orbit.setProportionalZoom(true);
		viewingPlatform.setViewPlatformBehavior(orbit);

		// Create a simple scene and attach it to the virtual universe
		BranchGroup scene = createSceneGraph();
		universe.addBranchGraph(scene);		
		
		canvas.getView().setFrontClipPolicy(javax.media.j3d.View.VIRTUAL_EYE);
		canvas.getView().setFrontClipDistance(0.01);
		
	}
		
	private void initialize() {
			
	}

	private BranchGroup createSceneGraph() 
	{
		BranchGroup branchRoot = new BranchGroup();

		// Create a bounds for the background and lights
		BoundingSphere bounds = new BoundingSphere(new Point3d(0.0,0.0,0.0), 1000.0);

		// Set up the background
		Color3f bgColor = new Color3f(BoardCAD.getInstance().getBackgroundColor());
		mBackgroundNode = new Background(bgColor);
		mBackgroundNode.setApplicationBounds(bounds);
		mBackgroundNode.setCapability(Background.ALLOW_COLOR_WRITE);
		branchRoot.addChild(mBackgroundNode);

		// Set up the ambient light
		Color3f ambientColor = new Color3f(0.2f, 0.2f, 0.2f);
		AmbientLight ambientLightNode = new AmbientLight(ambientColor);
		ambientLightNode.setInfluencingBounds(bounds);
		branchRoot.addChild(ambientLightNode);

		// Set up the directional lights
		Color3f light1Color = new Color3f(1.0f, 1.0f, 0.9f);
		Vector3f light1Direction  = new Vector3f(0.0f, 1.0f, 0.0f);
		Color3f light2Color = new Color3f(1.0f, 1.0f, 0.9f);
		Vector3f light2Direction  = new Vector3f(0.0f, -1.0f, 0.0f);

		DirectionalLight light1 = new DirectionalLight(light1Color, light1Direction);
		light1.setInfluencingBounds(bounds);
		branchRoot.addChild(light1);

		DirectionalLight light2 = new DirectionalLight(light2Color, light2Direction);
		light2.setInfluencingBounds(bounds);
		branchRoot.addChild(light2);


		// Create an Appearance and set up the material properties
		Appearance surfacePathsApperance = new Appearance();
		ColoringAttributes surfacePathsColor = new ColoringAttributes();
		surfacePathsColor.setColor (1.0f, 1.0f, 1.0f);
		surfacePathsApperance.setColoringAttributes(surfacePathsColor);

		Appearance normalsApperance = new Appearance();
		ColoringAttributes normalsColor = new ColoringAttributes();
		normalsColor.setColor (0.5f, 0.5f, 0.9f);
		normalsApperance.setColoringAttributes(normalsColor);
		
		Appearance toolpathsApperance = new Appearance();
		ColoringAttributes toolpathsColor = new ColoringAttributes();
		toolpathsColor.setColor (0.1f, 0.1f, 0.3f);
		toolpathsApperance.setColoringAttributes(toolpathsColor);

		Appearance supportStructureApperance = new Appearance();
		ColoringAttributes supportStructtureColor = new ColoringAttributes();
		supportStructtureColor.setColor (0.1f, 0.1f, 0.3f);
		supportStructureApperance.setColoringAttributes(supportStructtureColor);

		Appearance cutterApperance = new Appearance();
		ColoringAttributes cutterApperanceColor = new ColoringAttributes();
		cutterApperanceColor.setColor (0.1f, 0.1f, 0.3f);
		cutterApperance.setColoringAttributes(cutterApperanceColor);
		
		// Create a Transformgroup to scale all objects so they
		// appear in the scene.
		mScale = new TransformGroup();
		mScale.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		mScale.setCapability(Group.ALLOW_CHILDREN_WRITE);
		mScale.setCapability(Group.ALLOW_CHILDREN_EXTEND);
		mTransform = new Transform3D();
		mTransform.setScale(0.001);
		mScale.setTransform(mTransform);
		branchRoot.addChild(mScale);

		//Toolpaths
		mToolpathsGroup = new TransformGroup();
		mToolpathsGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		mToolpathsGroup.setCapability(TransformGroup.ALLOW_CHILDREN_EXTEND);
		mToolpathsGroup.setCapability(TransformGroup.ALLOW_CHILDREN_WRITE);
		mToolpathsTransform = new Transform3D();
		mToolpathsTransform.setIdentity();
		mToolpathsGroup.setTransform(mToolpathsTransform);
		mScale.addChild(mToolpathsGroup);

		LineArray referencePointArray = new LineArray(6, LineArray.COORDINATES); 
		referencePointArray.setCapability(GeometryArray.ALLOW_COORDINATE_WRITE);

		double size = 40.0;
		referencePointArray.setCoordinates(mDeckCurrentNormalIndex, new Point3d[]{new Point3d(-size, 0.0, 0.0), new Point3d(size, 0.0, 0.0), new Point3d(0.0, -size, 0.0),new Point3d(0.0, size, 0.0),new Point3d(0.0, 0.0, -size),new Point3d(0.0, 0.0, size)});
		referencePointArray.setValidVertexCount(6);
		
		Appearance toolpathsReferencePointApperance = new Appearance();
		ColoringAttributes toolpathsReferencePointColor = new ColoringAttributes();
		toolpathsReferencePointColor.setColor (0.0f, 1.0f, 0.0f);
		toolpathsReferencePointApperance.setColoringAttributes(toolpathsReferencePointColor);
		
		Shape3D toolpathsReferencePoint = new Shape3D();
		toolpathsReferencePoint.setGeometry(referencePointArray);
		toolpathsReferencePoint.setAppearance(normalsApperance);
		mToolpathsGroup.addChild(toolpathsReferencePoint);
		
		//Cutter
		mCutterTransformGroup = new TransformGroup();
		mCutterTransformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		mCutterTransformGroup.setCapability(TransformGroup.ALLOW_CHILDREN_EXTEND);
		mCutterTransformGroup.setCapability(TransformGroup.ALLOW_CHILDREN_WRITE);
		mCutterTransform = new Transform3D();
		mCutterTransform.setIdentity();
		mCutterTransformGroup.setTransform(mCutterTransform);
		mToolpathsGroup.addChild(mCutterTransformGroup);
		
		mDeckOrBottomToolpathSwitch = new Switch();
		mDeckOrBottomToolpathSwitch.setCapability(Switch.ALLOW_SWITCH_READ);
		mDeckOrBottomToolpathSwitch.setCapability(Switch.ALLOW_SWITCH_WRITE);
		mDeckOrBottomToolpathSwitch.setWhichChild(Switch.CHILD_ALL);
		mToolpathsGroup.addChild(mDeckOrBottomToolpathSwitch);

		mDeckToolpathGroup = new Group();
		mDeckToolpathGroup.setCapability(Switch.ALLOW_SWITCH_READ);
		mDeckToolpathGroup.setCapability(Switch.ALLOW_SWITCH_WRITE);
		mDeckOrBottomToolpathSwitch.addChild(mDeckToolpathGroup);

		mDeckToolpathsSwitch = new Switch();
		mDeckToolpathsSwitch.setCapability(Switch.ALLOW_SWITCH_READ);
		mDeckToolpathsSwitch.setCapability(Switch.ALLOW_SWITCH_WRITE);
		mDeckToolpathsSwitch.setWhichChild(Switch.CHILD_ALL);
		mDeckToolpathGroup.addChild(mDeckToolpathsSwitch);
		
		mDeckToolpaths = new Shape3D();
		mDeckToolpaths.setCapability(Shape3D.ALLOW_GEOMETRY_WRITE);
//		mDeckToolpaths.setCapability(Shape3D.ALLOW_GEOMETRY_READ);
		mDeckToolpaths.setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);
//		mDeckToolpaths.setCapability(Shape3D.ALLOW_APPEARANCE_READ);

		mDeckToolpathsArray = new LineStripArray(200000, LineStripArray.COORDINATES, new int[]{200000});
		mDeckToolpathsArray.setCapability(GeometryArray.ALLOW_COORDINATE_WRITE);
		mDeckToolpaths.setGeometry(mDeckToolpathsArray);
		mDeckToolpaths.setAppearance(toolpathsApperance);
		mDeckToolpathsSwitch.addChild(mDeckToolpaths);
		
		mDeckToolpathsArray.setCoordinates(3, new double[]{-1,-1,-1,0,0,0,1,1,1});
		
		mBottomToolpathGroup = new Group();
		mBottomToolpathGroup.setCapability(Switch.ALLOW_SWITCH_READ);
		mBottomToolpathGroup.setCapability(Switch.ALLOW_SWITCH_WRITE);
		mDeckOrBottomToolpathSwitch.addChild(mBottomToolpathGroup);

		mBottomToolpathsSwitch = new Switch();
		mBottomToolpathsSwitch.setCapability(Switch.ALLOW_SWITCH_READ);
		mBottomToolpathsSwitch.setCapability(Switch.ALLOW_SWITCH_WRITE);
		mBottomToolpathsSwitch.setWhichChild(Switch.CHILD_ALL);
		mBottomToolpathGroup.addChild(mBottomToolpathsSwitch);

		mBottomToolpaths = new Shape3D();
		mBottomToolpaths.setCapability(Shape3D.ALLOW_GEOMETRY_WRITE);
		mBottomToolpaths.setCapability(Shape3D.ALLOW_GEOMETRY_READ);
		mBottomToolpaths.setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);
		mBottomToolpaths.setCapability(Shape3D.ALLOW_APPEARANCE_READ);

		mBottomToolpathsArray = new LineStripArray(200000, LineStripArray.COORDINATES, new int[]{200000});
		mBottomToolpathsArray.setCapability(GeometryArray.ALLOW_COORDINATE_WRITE);
		mBottomToolpaths.setGeometry(mBottomToolpathsArray);
		mBottomToolpaths.setAppearance(toolpathsApperance);
		mBottomToolpathsSwitch.addChild(mBottomToolpaths);
		
		mBottomToolpathsArray.setCoordinates(3, new double[]{-1,-1,-1,0,0,0,1,1,1});
		
		//Model
		mModelTransformGroup = new TransformGroup();
		mModelTransformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		mModelTransformGroup.setCapability(TransformGroup.ALLOW_CHILDREN_EXTEND);
		mModelTransformGroup.setCapability(TransformGroup.ALLOW_CHILDREN_WRITE);
		mModelTransform = new Transform3D();
		mModelTransform.setIdentity();
		mModelTransformGroup.setTransform(mModelTransform);
		mScale.addChild(mModelTransformGroup);

		Appearance modelReferencePointApperance = new Appearance();
		ColoringAttributes modelReferencePointColor = new ColoringAttributes();
		modelReferencePointColor.setColor (1.0f, 1.0f, 1.0f);
		modelReferencePointApperance.setColoringAttributes(modelReferencePointColor);
		
		Shape3D modelReferencePoint = new Shape3D();
		modelReferencePoint.setGeometry(referencePointArray);
		modelReferencePoint.setAppearance(modelReferencePointApperance);
		mModelTransformGroup.addChild(modelReferencePoint);

		mDeckOrBottomModelSwitch = new Switch();
		mDeckOrBottomModelSwitch.setCapability(Switch.ALLOW_SWITCH_READ);
		mDeckOrBottomModelSwitch.setCapability(Switch.ALLOW_SWITCH_WRITE);
		mDeckOrBottomModelSwitch.setWhichChild(Switch.CHILD_ALL);
		mModelTransformGroup.addChild(mDeckOrBottomModelSwitch);

		mDeckModelGroup = new Group();
		mDeckModelGroup.setCapability(Switch.ALLOW_SWITCH_READ);
		mDeckModelGroup.setCapability(Switch.ALLOW_SWITCH_WRITE);
		mDeckOrBottomModelSwitch.addChild(mDeckModelGroup);
		
		mDeckSurfacePathsSwitch = new Switch();
//		mDeckSurfacePathsSwitch.setCapability(Switch.ALLOW_SWITCH_READ);
		mDeckSurfacePathsSwitch.setCapability(Switch.ALLOW_SWITCH_WRITE);
		mDeckSurfacePathsSwitch.setWhichChild(Switch.CHILD_ALL);
		mDeckModelGroup.addChild(mDeckSurfacePathsSwitch);
		
		mDeckSurfacePaths = new Shape3D();
		mDeckSurfacePaths.setCapability(Shape3D.ALLOW_GEOMETRY_WRITE);
//		mDeckSurfacePaths.setCapability(Shape3D.ALLOW_GEOMETRY_READ);
		mDeckSurfacePaths.setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);
//		mDeckSurfacePaths.setCapability(Shape3D.ALLOW_APPEARANCE_READ);

		mDeckSurfacePathsArray = new LineStripArray(200000, LineStripArray.COORDINATES, new int[]{200000});
		mDeckSurfacePathsArray.setCapability(GeometryArray.ALLOW_COORDINATE_WRITE);
		mDeckSurfacePaths.setGeometry(mDeckSurfacePathsArray);
		mDeckSurfacePaths.setAppearance(surfacePathsApperance);
		mDeckSurfacePathsSwitch.addChild(mDeckSurfacePaths);

		mDeckNormalsSwitch = new Switch();
		mDeckNormalsSwitch.setCapability(Switch.ALLOW_SWITCH_READ);
		mDeckNormalsSwitch.setCapability(Switch.ALLOW_SWITCH_WRITE);
		mDeckNormalsSwitch.setWhichChild(Switch.CHILD_ALL);
		mDeckModelGroup.addChild(mDeckNormalsSwitch);

		mDeckSurfaceNormals = new Shape3D();
		mDeckSurfaceNormals.setCapability(Shape3D.ALLOW_GEOMETRY_WRITE);
//		mDeckNormals.setCapability(Shape3D.ALLOW_GEOMETRY_READ);
		mDeckSurfaceNormals.setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);
//		mDeckNormals.setCapability(Shape3D.ALLOW_APPEARANCE_READ);

		mDeckNormalsArray = new LineArray(200000, LineArray.COORDINATES);
		mDeckNormalsArray.setCapability(GeometryArray.ALLOW_COORDINATE_WRITE);
		mDeckSurfaceNormals.setGeometry(mDeckNormalsArray);
		mDeckSurfaceNormals.setAppearance(normalsApperance);
		mDeckNormalsSwitch.addChild(mDeckSurfaceNormals);

		mBottomModelGroup = new Group();
		mBottomModelGroup.setCapability(Switch.ALLOW_SWITCH_READ);
		mBottomModelGroup.setCapability(Switch.ALLOW_SWITCH_WRITE);
		mDeckOrBottomModelSwitch.addChild(mBottomModelGroup);

		mBottomSurfacePathsSwitch = new Switch();
		mBottomSurfacePathsSwitch.setCapability(Switch.ALLOW_SWITCH_READ);
		mBottomSurfacePathsSwitch.setCapability(Switch.ALLOW_SWITCH_WRITE);
		mBottomSurfacePathsSwitch.setWhichChild(Switch.CHILD_ALL);
		mBottomModelGroup.addChild(mBottomSurfacePathsSwitch);

		mBottomSurfacePaths = new Shape3D();
		mBottomSurfacePaths.setCapability(Shape3D.ALLOW_GEOMETRY_WRITE);
//		mBottomSurfacePaths.setCapability(Shape3D.ALLOW_GEOMETRY_READ);
		mBottomSurfacePaths.setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);
//		mBottomSurfacePaths.setCapability(Shape3D.ALLOW_APPEARANCE_READ);

		mBottomSurfacePathsArray = new LineStripArray(200000, LineStripArray.COORDINATES, new int[]{200000});
		mBottomSurfacePathsArray.setCapability(GeometryArray.ALLOW_COORDINATE_WRITE);
		mBottomSurfacePaths.setGeometry(mBottomSurfacePathsArray);
		mBottomSurfacePaths.setAppearance(surfacePathsApperance);
		mBottomSurfacePathsSwitch.addChild(mBottomSurfacePaths);

		mBottomSurfaceNormalsSwitch = new Switch();
		mBottomSurfaceNormalsSwitch.setCapability(Switch.ALLOW_SWITCH_READ);
		mBottomSurfaceNormalsSwitch.setCapability(Switch.ALLOW_SWITCH_WRITE);
		mBottomSurfaceNormalsSwitch.setWhichChild(Switch.CHILD_ALL);
		mBottomModelGroup.addChild(mBottomSurfaceNormalsSwitch);

		mBottomSurfaceNormals = new Shape3D();
		mBottomSurfaceNormals.setCapability(Shape3D.ALLOW_GEOMETRY_WRITE);
//		mBottomNormals.setCapability(Shape3D.ALLOW_GEOMETRY_READ);
		mBottomSurfaceNormals.setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);
//		mBottomNormals.setCapability(Shape3D.ALLOW_APPEARANCE_READ);

		mBottomNormalsArray = new LineArray(200000, LineArray.COORDINATES);
		mBottomNormalsArray.setCapability(GeometryArray.ALLOW_COORDINATE_WRITE);
		mBottomSurfaceNormals.setGeometry(mBottomNormalsArray);
		mBottomSurfaceNormals.setAppearance(normalsApperance);
		mBottomSurfaceNormalsSwitch.addChild(mBottomSurfaceNormals);

//DEBUG
//		mDeckGroup.addChild(new com.sun.j3d.utils.geometry.Box(100.0f,100.0f, 100.0f, new Appearance()));
//		mBottomGroup.addChild(new com.sun.j3d.utils.geometry.Box());
		
		// Have Java 3D perform optimizations on this scene graph.
//		branchRoot.compile();	CANNOT ADD AND REMOVE NODES THEN

		return branchRoot;
	}
	
	public void reset()
	{
		mDeckCurrentSurfacePathIndex = 0;
		mDeckCurrentNormalIndex = 0;
		mDeckCurrentToolpathIndex = 0;		

		//TODO: Test refactor to only reset linestrips by setting count to zero
		mDeckSurfacePathsArray = new LineStripArray(200000, LineStripArray.COORDINATES, new int[]{200000});
		mDeckSurfacePathsArray.setCapability(GeometryArray.ALLOW_COORDINATE_WRITE);
		mDeckSurfacePathsArray.setCapability(GeometryArray.ALLOW_COUNT_WRITE);	
		mDeckSurfacePaths.setGeometry(mDeckSurfacePathsArray);

		mDeckNormalsArray = new LineArray(200000, LineArray.COORDINATES);
		mDeckNormalsArray.setCapability(GeometryArray.ALLOW_COORDINATE_WRITE);
		mDeckNormalsArray.setCapability(GeometryArray.ALLOW_COUNT_WRITE);	
		mDeckSurfaceNormals.setGeometry(mDeckNormalsArray);
	
		mDeckToolpathsArray = new LineStripArray(200000, LineStripArray.COORDINATES, new int[]{200000});
		mDeckToolpathsArray.setCapability(GeometryArray.ALLOW_COORDINATE_WRITE);
		mDeckToolpathsArray.setCapability(GeometryArray.ALLOW_COUNT_WRITE);	
		mDeckToolpaths.setGeometry(mDeckToolpathsArray);

		mBottomCurrentSurfacePathIndex = 0;
		mBottomCurrentNormalIndex = 0;
		mBottomCurrentToolpathIndex = 0;		

		mBottomSurfacePathsArray = new LineStripArray(200000, LineStripArray.COORDINATES, new int[]{200000});
		mBottomSurfacePathsArray.setCapability(GeometryArray.ALLOW_COORDINATE_WRITE);
		mBottomSurfacePathsArray.setCapability(GeometryArray.ALLOW_COUNT_WRITE);	
		mBottomSurfacePaths.setGeometry(mBottomSurfacePathsArray);

		mBottomNormalsArray = new LineArray(200000, LineArray.COORDINATES);
		mBottomNormalsArray.setCapability(GeometryArray.ALLOW_COORDINATE_WRITE);
		mBottomNormalsArray.setCapability(GeometryArray.ALLOW_COUNT_WRITE);	
		mBottomSurfaceNormals.setGeometry(mBottomNormalsArray);
	
		mBottomToolpathsArray = new LineStripArray(200000, LineStripArray.COORDINATES, new int[]{200000});
		mBottomToolpathsArray.setCapability(GeometryArray.ALLOW_COORDINATE_WRITE);
		mBottomToolpathsArray.setCapability(GeometryArray.ALLOW_COUNT_WRITE);	
		mBottomToolpaths.setGeometry(mBottomToolpathsArray);
	}
	
	void setOffset(double offset)
	{
		double scale = 0.0005;
	//	Vector3d deckOffset = BoardCAD.getInstance().getMachineView().getBoardDeckOffsetPos();
		Vector3d deckTranslation = new Vector3d();
	//	deckTranslation.sub(deckOffset);
		String generalStr = LanguageResource.getString("GENERALCATEGORY_STR");
//		deckTranslation.x -= (BoardCAD.getInstance().getCurrentBrd().getLength() + (.getCategory(generalStr).getDouble(MachineConfig.TAILSTOP_POS)/UnitUtils.MILLIMETER_PR_CENTIMETER))/2.0;
		deckTranslation.scale(scale*10);	//Since the board coordinates are in cm, scaled to millimeters used by g-code
		mTransform.set(scale, deckTranslation);
		mScale.setTransform(mTransform);

	}
	
	public void setBackgroundColor(Color color)
	{
		float[] components = color.getRGBComponents(null);
		mBackgroundNode.setColor(new Color3f(components[0], components[1], components[2]));
	}

	public void setDeckSurfaceStart(Point3d point)
	{
		mDeckCurrentSurfacePathIndex=0;
		mDeckSurfacePathsArray.setCoordinates(mDeckCurrentSurfacePathIndex++, new Point3d[]{point});
	}

	public void addDeckSurfaceLine(Point3d point)
	{
		mDeckSurfacePathsArray.setCoordinates(mDeckCurrentSurfacePathIndex++, new Point3d[]{point});
		mDeckSurfacePathsArray.setStripVertexCounts(new int[]{(mDeckCurrentSurfacePathIndex>2)?mDeckCurrentSurfacePathIndex:2});
	}

	public void addDeckNormal(Point3d location, Vector3d normal)
	{	
		final Point3d normalCopy = new Point3d(normal);
		final Point3d tmp = new Point3d(location);
		normalCopy.scale(40.0);
		tmp.add(normalCopy);
		mDeckNormalsArray.setCoordinates(mDeckCurrentNormalIndex, new Point3d[]{location,tmp});
		mDeckCurrentNormalIndex += 2;
		mDeckNormalsArray.setValidVertexCount(mDeckCurrentNormalIndex);
	}

	public void setDeckToolpathStart(Point3d point)
	{
		mDeckCurrentToolpathIndex = 0;
		mDeckToolpathsArray.setCoordinates(mDeckCurrentToolpathIndex++, new Point3d[]{point});		

		mCutterTransform.setIdentity();
		mCutterTransform.setTranslation(new Vector3d(point));
		mCutterTransformGroup.setTransform(mCutterTransform);
	}

	public void addDeckToolpathLine(Point3d point)
	{
		mDeckToolpathsArray.setCoordinates(mDeckCurrentToolpathIndex++, new Point3d[]{point});
		mDeckToolpathsArray.setStripVertexCounts(new int[]{(mDeckCurrentToolpathIndex>2)?mDeckCurrentToolpathIndex:2});
		
		mCutterTransform.setIdentity();
		mCutterTransform.setTranslation(new Vector3d(point));
		mCutterTransformGroup.setTransform(mCutterTransform);
	}

	public void setBottomSurfaceStart(Point3d point)
	{
		mBottomCurrentSurfacePathIndex=0;
		mBottomSurfacePathsArray.setCoordinates(mDeckCurrentSurfacePathIndex++, new Point3d[]{point});
	}

	public void addBottomSurfaceLine(Point3d point)
	{
		mBottomSurfacePathsArray.setCoordinates(mBottomCurrentSurfacePathIndex++, new Point3d[]{point});
		mBottomSurfacePathsArray.setStripVertexCounts(new int[]{(mBottomCurrentSurfacePathIndex>2)?mBottomCurrentSurfacePathIndex:2});
	}

	public void addBottomNormal(Point3d location, Vector3d normal)
	{	
		final Point3d normalCopy = new Point3d(normal);
		final Point3d tmp = new Point3d(location);
		normalCopy.scale(40.0);
		tmp.add(normalCopy);
		mBottomNormalsArray.setCoordinates(mBottomCurrentNormalIndex, new Point3d[]{location,tmp});
		mBottomCurrentNormalIndex += 2;
		mBottomNormalsArray.setValidVertexCount(mBottomCurrentNormalIndex);
	}

	public void setBottomToolpathStart(Point3d point)
	{
		mBottomCurrentToolpathIndex = 0;
		mBottomToolpathsArray.setCoordinates(mBottomCurrentToolpathIndex++, new Point3d[]{point});

		mCutterTransform.setTranslation(new Vector3d(point));
		mCutterTransformGroup.setTransform(mCutterTransform);
	}

	public void addBottomToolpathLine(Point3d point)
	{
		mBottomToolpathsArray.setCoordinates(mBottomCurrentToolpathIndex++, new Point3d[]{point});
		mBottomToolpathsArray.setStripVertexCounts(new int[]{(mBottomCurrentToolpathIndex>2)?mBottomCurrentToolpathIndex:2});

		mCutterTransform.setTranslation(new Vector3d(point));
		mCutterTransformGroup.setTransform(mCutterTransform);
	}

	public void toggleDeckOrBottom(){
		mDeckOrBottomToolpathSwitch.setWhichChild((mDeckOrBottomToolpathSwitch.getWhichChild()==0)?1:0);
		mDeckOrBottomModelSwitch.setWhichChild((mDeckOrBottomModelSwitch.getWhichChild()==0)?1:0);
	}

	public void setDeckActive(){
		mDeckOrBottomToolpathSwitch.setWhichChild(0);
		mDeckOrBottomModelSwitch.setWhichChild(0);
	}

	public void setBottomActive(){
		mDeckOrBottomToolpathSwitch.setWhichChild(1);
		mDeckOrBottomModelSwitch.setWhichChild(1);
	}

	public void setCutterModel(BranchGroup model){
		try{
			if(mCutter != null)
			{
				mCutterTransformGroup.removeChild(mCutter);
			}
			mCutter = model;
			if(model != null)
			{
				mCutterTransformGroup.addChild(mCutter);
			}
		}
		catch(Exception e)
		{
			System.out.println("Exception in Machine3Dview::setCutterModel(): " + e.toString());
		}
//		System.out.println("setCutterModel()\n");
	}

	public void setBlankHoldingSystemModel(BranchGroup model){
		try{
			if(mBlankHoldingSystemModel != null)
			{
				mModelTransformGroup.removeChild(mBlankHoldingSystemModel);	//remove old
			}
			mBlankHoldingSystemModel = model;
			if(model != null)
			{
				mModelTransformGroup.addChild(mBlankHoldingSystemModel);
			}
		}catch(Exception e)
		{
			System.out.println("Exception in Machine3DView::setBlankHoldingSystemModel(): " + e.toString());
		}
//		System.out.println("setBlankHoldingSystemModel()\n");
		
	}
	
	public void fit_all()
	{
		
	}

	public void setCutterpos(Point3d pos){
		mCutterTransform.setTranslation(new Vector3d(pos));
		mCutterTransformGroup.setTransform(mCutterTransform);
	}

	public void setCutterOffset(Point3d pos){
		mToolpathsTransform.setTranslation(new Vector3d(pos.x*UnitUtils.MILLIMETER_PR_CENTIMETER, pos.y*UnitUtils.MILLIMETER_PR_CENTIMETER, pos.z*UnitUtils.MILLIMETER_PR_CENTIMETER));
		mToolpathsGroup.setTransform(mToolpathsTransform);
	}
	
	public void update()
	{
		reset();
	}

	public void addCutterModel(Shape3D cutterModel)
	{
		mScale.addChild(cutterModel);
	}
	
	public void setModelRotation(double angle)
	{
		mModelTransform.setIdentity();
		mModelTransform.rotX(angle);
		mModelTransformGroup.setTransform(mModelTransform);
	}
}

