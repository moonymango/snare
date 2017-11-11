package com.moonymango.snareDemo.resolution;

import com.moonymango.snare.events.EventManager;
import com.moonymango.snare.events.EventManager.IEventListener;
import com.moonymango.snare.events.IEvent;
import com.moonymango.snare.events.IFlingEvent;
import com.moonymango.snare.events.IScaleEvent;
import com.moonymango.snare.events.IScrollEvent;
import com.moonymango.snare.events.ITouchEvent;
import com.moonymango.snare.game.BaseSnareClass;
import com.moonymango.snare.game.GameObj;
import com.moonymango.snare.game.IGame;
import com.moonymango.snare.game.IGameState;
import com.moonymango.snare.game.IGameStateLogic;
import com.moonymango.snare.opengl.VarResolutionRenderer;
import com.moonymango.snare.res.data.MeshResource;
import com.moonymango.snare.res.xml.BMFont;
import com.moonymango.snare.res.xml.BMFontXMLHandler;
import com.moonymango.snare.res.xml.XMLResHandle;
import com.moonymango.snare.res.xml.XMLResource;
import com.moonymango.snare.ui.PlayerGameView;
import com.moonymango.snare.ui.TouchAction;
import com.moonymango.snare.ui.scene3D.Light;
import com.moonymango.snare.ui.scene3D.Light.LightType;
import com.moonymango.snare.ui.scene3D.Material;
import com.moonymango.snare.ui.scene3D.PerspectiveCamera;
import com.moonymango.snare.ui.scene3D.RenderPass;
import com.moonymango.snare.ui.scene3D.Scene3D;
import com.moonymango.snare.ui.scene3D.mesh.Mesh;
import com.moonymango.snare.ui.scene3D.rendering.DiffuseLightingEffect;
import com.moonymango.snare.ui.scene3D.rendering.SceneDrawable;
import com.moonymango.snare.ui.widgets.Text;
import com.moonymango.snare.util.VectorAF;
import com.moonymango.snareDemo.Asset;

class GameState extends BaseSnareClass implements IGameState, IGameStateLogic, IEventListener
{

    private PerspectiveCamera mCam;
    private GameObj mObj;
    private int mResX;
    private int mResY;
    private int mResXBeforeScale;
    private int mResYBeforeScale;


    public GameState(IGame game)
    {
        super(game);
    }

    @Override
    public IGameState onUpdate(long realTime, float realDelta,
                               float virtualDelta)
    {
        return null;
    }

    @Override
    public void onActivate(IGameState previous)
    {
        final VarResolutionRenderer r = (VarResolutionRenderer) mGame.getRenderer();
        mResX = r.getMaxResolutionX();
        mResY = r.getMaxResolutionY();
        mResXBeforeScale = mResX;
        mResYBeforeScale = mResY;

        final Scene3D s = new Scene3D(mGame);

        mCam = new PerspectiveCamera();
        GameObj obj = new GameObj(mGame, "camera");
        obj.addComponent(mCam);
        obj.setPosition(0, 0, 7);
        mGame.addGameObj(obj);

        mCam.lookAt(0, 0, 0, 0, 1, 0);
        mCam.setNearFarPlane(2, 30);
        mCam.setFieldOfView(30);
        s.setCamera(mCam);

        obj = new GameObj(mGame, "directionalLight");
        Light light = new Light(LightType.DIRECTIONAL);
        light.setColor(1, 1, 1, 0);
        obj.addComponent(light);
        obj.rotate(0, 1, 0, 90);
        mGame.addGameObj(obj);

        obj = new GameObj(mGame, "ambientLight");
        light = new Light(LightType.AMBIENT);
        light.setColor(1, 1, 1, 0);
        obj.addComponent(light);
        mGame.addGameObj(obj);

        mGame.getPrimaryView().pushScreenElement(s);


        MeshResource meshResSkull = new MeshResource(mGame, Asset.MONKEY3DS_MESH);
        mObj = new GameObj(mGame, "mesh");
        //final SceneDrawableComponent c = new SceneDrawableComponent(new Mesh(meshResSkull), 
        //        new DiffuseLightingEffect(), RenderPass.DYNAMIC);
        mObj.addComponent(new DiffuseLightingEffect(mGame));
        mObj.addComponent(new Mesh(meshResSkull));
        mObj.addComponent(new SceneDrawable(mGame, RenderPass.DYNAMIC));
        final Material mat = new Material(mGame);
        mat.setColor(Material.AMBIENT_COLOR_IDX, 0, 0, 0.5f, 1);
        mat.setColor(Material.DIFFUSE_COLOR_IDX, 1, 0, 0, 1);
        mObj.addComponent(mat);
        //mObj.setScale(0.04f, 0.04f, 0.04f);
        mObj.setPosition(0, 0, 0);

        mGame.addGameObj(mObj);

        // register for fling and scroll gestures
        final EventManager em = mGame.getEventManager();
        em.addListener(IScrollEvent.EVENT_TYPE, this);
        em.addListener(IFlingEvent.EVENT_TYPE, this);
        em.addListener(ITouchEvent.EVENT_TYPE, this);
        em.addListener(IScaleEvent.EVENT_TYPE, this);

        // add some text widget in overlay view

        final XMLResource<BMFont> res = new XMLResource<>(Asset.EMBOSSED, new BMFontXMLHandler(mGame));
        final XMLResHandle<BMFont> hnd = res.getHandle();

        final PlayerGameView[] v = mGame.getRenderer().getPlayerViews();
        final PlayerGameView overlay = v[1];
        Text text = new Text(hnd.getContent(), "scale to change resolution", null);
        text.setTextSize(40);
        text.setOutlineColor(0, 0, 1, 1).setColor(1, 0, 0, 1);

        text.setPosition(overlay.getScreenCoordX(0.5f), 100);
        overlay.pushScreenElement(text);

    }

    @Override
    public void onDeactivate(IGameState next)
    {

    }

    @Override
    public void onInit()
    {

    }

    @Override
    public void onShutdown()
    {

    }

    @Override
    public IGameStateLogic getGameStateLogic()
    {
        return this;
    }

    @Override
    public boolean equals(IGameState state)
    {
        return state == this;
    }

    public String getName()
    {
        return GameState.class.getName();
    }

    @Override
    public boolean handleEvent(IEvent event)
    {
        if (event.getType() == IScrollEvent.EVENT_TYPE) {
            handleScroll((IScrollEvent) event);
        }
        /*if (event.getType() == IFlingEvent.EVENT_TYPE) {
            handleFling((IFlingEvent) event);
        }*/
        if (event.getType() == IScaleEvent.EVENT_TYPE) {
            handleScale((IScaleEvent) event);
        }

        if (event.getType() == ITouchEvent.EVENT_TYPE) {
            final ITouchEvent e = (ITouchEvent) event;
            if (e.getTouchAction() == TouchAction.DOUBLE_TAB) {
                handleDoubleTap();
            }
        }
        return false;
    }

    private void handleScroll(IScrollEvent e)
    {
        // rotate
        final float[] vec = {-e.getDistanceY(), -e.getDistanceX(), 0, 0};
        final float angle = 0.3f * VectorAF.normalize(vec);
        mObj.rotate(vec[0], vec[1], vec[2], angle);
    }

    private void handleDoubleTap()
    {
        // reset to initial orientation
        mObj.resetRotation();
    }

    private void handleScale(IScaleEvent e)
    {
        if (e.isNewScaleGesture()) {
            mResXBeforeScale = mResX;
            mResYBeforeScale = mResY;
        }

        final VarResolutionRenderer r = (VarResolutionRenderer) mGame.getRenderer();
        final int tempX = mResX;
        final int tempY = mResY;

        mResX = (int) (mResXBeforeScale * e.getScaleFactor() * e.getScaleFactor());
        mResY = (int) (mResYBeforeScale * e.getScaleFactor() * e.getScaleFactor());

        if (mResY < 5 || mResY > r.getMaxResolutionY()
                || mResX > r.getMaxResolutionX()) {
            mResX = tempX;
            mResY = tempY;
            return;
        }

        r.setResolution(mResX, mResY);
    }

}
