package stachelsau.snare.demo.touch;

import stachelsau.snare.audio.DemoAudioComponent;
import stachelsau.snare.audio.SoundResource;
import stachelsau.snare.demo.Asset;
import stachelsau.snare.events.EventManager;
import stachelsau.snare.game.Game;
import stachelsau.snare.game.GameObj;
import stachelsau.snare.game.logic.TouchLogicComponent;
import stachelsau.snare.game.logic.TouchLogicComponent.ITouchInputHandler;
import stachelsau.snare.physics.BaseBoundingVolume;
import stachelsau.snare.physics.BaseBoundingVolume.VolumeType;
import stachelsau.snare.physics.IPhysics;
import stachelsau.snare.res.data.MeshResHandle;
import stachelsau.snare.res.data.MeshResource;
import stachelsau.snare.ui.TouchAction;
import stachelsau.snare.ui.scene3D.Material;
import stachelsau.snare.ui.scene3D.RenderPass;
import stachelsau.snare.ui.scene3D.mesh.Mesh;
import stachelsau.snare.ui.scene3D.rendering.DiffuseLightingEffect;
import stachelsau.snare.ui.scene3D.rendering.SceneDrawable;
import stachelsau.snare.util.Pool;

public class DemoObjPool extends Pool<GameObj> {
    
    private int mObjCnt;

    @Override
    protected GameObj allocatePoolItem() {        
        final GameObj obj = new GameObj("obj_" + mObjCnt++, GameState.mMonkeyLayer);
        
        // render component
        final MeshResource meshRes = new MeshResource(Asset.MONKEY3DS_MESH);
        obj.addComponent(new DiffuseLightingEffect());
        //obj.addComponent(new OutlineEffect());
        obj.addComponent(new Mesh(meshRes)); 
        obj.addComponent(new SceneDrawable(RenderPass.DYNAMIC));
        Material mat = new Material();
        mat.setColor(Material.DIFFUSE_COLOR_IDX, 0.8f, 0, 0, 1);
        mat.setColor(Material.AMBIENT_COLOR_IDX, 0, 0, 0.2f, 1);
        //mat.setColor(Material.OUTLINE_COLOR_IDX, 0, 0, 1, 1);
        obj.addComponent(mat);
        obj.setScale(0.4f, 0.4f, 0.4f);
        
        // set up bounding box, so that object is "touchable"
        final IPhysics p = Game.get().getPhysics();
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
                    final EventManager em = Game.get().getEventManager();
                    final IDemoObjCatchedEvent e = (IDemoObjCatchedEvent) em.obtain(IDemoObjCatchedEvent.EVENT_TYPE);
                    e.setGameObj(obj);
                    em.queueEvent(e);
                }
            }
        };
        obj.addComponent(new TouchLogicComponent(handler));
        
        // audio component
        obj.addComponent(new DemoAudioComponent(new SoundResource(Asset.BLASTER_SOUND)));
        
        return obj;
    }

}
