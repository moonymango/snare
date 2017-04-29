package com.moonymango.snareDemo.touch;

import com.moonymango.snare.audio.DemoAudioComponent;
import com.moonymango.snare.audio.SoundResource;
import com.moonymango.snare.events.EventManager;
import com.moonymango.snare.game.GameObj;
import com.moonymango.snare.game.IGame;
import com.moonymango.snare.game.logic.TouchLogicComponent;
import com.moonymango.snare.game.logic.TouchLogicComponent.ITouchInputHandler;
import com.moonymango.snare.physics.BaseBoundingVolume;
import com.moonymango.snare.physics.BaseBoundingVolume.VolumeType;
import com.moonymango.snare.physics.IPhysics;
import com.moonymango.snare.res.data.MeshResHandle;
import com.moonymango.snare.res.data.MeshResource;
import com.moonymango.snare.ui.TouchAction;
import com.moonymango.snare.ui.scene3D.Material;
import com.moonymango.snare.ui.scene3D.RenderPass;
import com.moonymango.snare.ui.scene3D.mesh.Mesh;
import com.moonymango.snare.ui.scene3D.rendering.DiffuseLightingEffect;
import com.moonymango.snare.ui.scene3D.rendering.SceneDrawable;
import com.moonymango.snare.util.Pool;
import com.moonymango.snareDemo.Asset;

public class DemoObjPool extends Pool<GameObj>
{
    private int mObjCnt;

    public DemoObjPool(IGame game)
    {
        super(game);
    }

    @Override
    protected GameObj allocatePoolItem() {        
        final GameObj obj = new GameObj(mGame, "obj_" + mObjCnt++, GameState.mMonkeyLayer);
        
        // render component
        final MeshResource meshRes = new MeshResource(mGame, Asset.MONKEY3DS_MESH);
        obj.addComponent(new DiffuseLightingEffect(mGame));
        //obj.addComponent(new OutlineEffect());
        obj.addComponent(new Mesh(meshRes)); 
        obj.addComponent(new SceneDrawable(mGame, RenderPass.DYNAMIC));
        Material mat = new Material(mGame);
        mat.setColor(Material.DIFFUSE_COLOR_IDX, 0.8f, 0, 0, 1);
        mat.setColor(Material.AMBIENT_COLOR_IDX, 0, 0, 0.2f, 1);
        //mat.setColor(Material.OUTLINE_COLOR_IDX, 0, 0, 1, 1);
        obj.addComponent(mat);
        obj.setScale(0.4f, 0.4f, 0.4f);
        
        // set up bounding box, so that object is "touchable"
        final IPhysics p = mGame.getPhysics();
        final BaseBoundingVolume bv = p.createBoundingVolume(VolumeType.SPHERE);
        bv.setCollisionCheck(false); // don't need collision checking between objs
        final MeshResHandle meshHnd = meshRes.getHandle();
        bv.setDimensions(meshHnd);
        meshRes.releaseHandle(meshHnd);
        obj.addComponent(bv);
        
        // input component: handle touch
        final ITouchInputHandler handler = new ITouchInputHandler() {
            
            @Override
            public void handleTouch(GameObj obj, TouchAction action) {
                if (action.equals(TouchAction.DOWN)) {
                    // send "catch" event
                    final EventManager em = mGame.getEventManager();
                    final IDemoObjCatchedEvent e = (IDemoObjCatchedEvent) em.obtain(IDemoObjCatchedEvent.EVENT_TYPE);
                    e.setGameObj(obj);
                    em.queueEvent(e);
                }
            }
        };
        obj.addComponent(new TouchLogicComponent(mGame, handler));
        
        // audio component
        obj.addComponent(new DemoAudioComponent(new SoundResource(mGame, Asset.BLASTER_SOUND)));
        
        return obj;
    }

}
