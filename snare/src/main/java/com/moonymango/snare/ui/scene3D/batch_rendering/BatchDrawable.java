package com.moonymango.snare.ui.scene3D.batch_rendering;

import com.moonymango.snare.ui.scene3D.BaseSceneDrawable;
import com.moonymango.snare.ui.scene3D.RenderPass;
import com.moonymango.snare.ui.scene3D.Scene3D;
import com.moonymango.snare.ui.scene3D.Scene3D.DrawBundle;

/**
 * Idea for batch drawing:
 *  - BatchDrawable manages buffer objects (vertices and indices)
 *  - BatchDrawable is added to a dummy game object 
 *  - all other objects that should be rendered in the batch are registered
 *    with the BatchDrawable:
 *          + buffers will be modified and updated on GPU
 *          + during draw the BatchDrawable collect matrices from all
 *            registered objects and passes them as uniforms
 *            
 *  requires new effect classes to support this
 */
public class BatchDrawable extends BaseSceneDrawable {


    public BatchDrawable(RenderPass pass) {
        super(pass);
    }

    @Override
    public void draw(Scene3D scene, DrawBundle bundle, RenderPass pass) {
        // TODO Auto-generated method stub

    }

    

}
