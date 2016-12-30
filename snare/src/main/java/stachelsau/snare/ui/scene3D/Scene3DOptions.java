package stachelsau.snare.ui.scene3D;

import stachelsau.snare.game.GameObj;
import stachelsau.snare.game.GameObj.GameObjLayer;


public class Scene3DOptions {
    
    public int SCENE_MATRIX_STACK_SIZE = 16;
    public boolean ENABLE_FRUSTRUM_CULLING = false;
    /** 
     * Layer mask to be used by scene for touch event raycasts.
     */
    public GameObjLayer DEFAULT_LAYER_MASK = GameObj.DEFAULT_OBJ_LAYER;
    /** Default material for 3D objects */
    public final Material DEFAULT_MATERIAL = new Material();
    public boolean SORT_DRAWABLES = false;
    
}