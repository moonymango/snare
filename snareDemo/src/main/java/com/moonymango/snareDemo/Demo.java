package com.moonymango.snareDemo;

import com.moonymango.snareDemo.camera.CameraDemo;
import com.moonymango.snareDemo.particles.ParticlesDemo;
import com.moonymango.snareDemo.physics.SimplePhysicsDemo;
import com.moonymango.snareDemo.playground.Playground;
import com.moonymango.snareDemo.procedural.ProceduralDemo;
import com.moonymango.snareDemo.resolution.ResolutionDemo;
import com.moonymango.snareDemo.scene.SceneDemo;
import com.moonymango.snareDemo.text.TextDemo;
import com.moonymango.snareDemo.textures.TextureDemo;
import com.moonymango.snareDemo.touch.SceneTouchDemo;
import com.moonymango.snareDemo.widgets.WidgetDemo;

public enum Demo {

    WIDGETS_CREATE_DESTROY  ("Button widgets",                      WidgetDemo.class),
    TEXT                    ("Text widgets",                        TextDemo.class),
    PROCEDURAL              ("Procedural textures",                  ProceduralDemo.class),
    SCENE                   ("Basic scene (object, camera, light)", SceneDemo.class),
    SCENE_CAMERA            ("Camera movement",                     CameraDemo.class),
    SCENE_PARTICLES         ("Particle effects",                    ParticlesDemo.class),
    SCENE_PLAYGROUND        ("Playground",                          Playground.class),
    SCENE_TOUCH             ("Kill the Blender monkey",             SceneTouchDemo.class),
    SCENE_TEXTURE           ("Textured objects",                    TextureDemo.class),
    PHYSICS_AND_AI          ("Simple pong",                         SimplePhysicsDemo.class),
    RESOLUTION              ("Rendering resolution",                ResolutionDemo.class);
    
    public final String mMSG;
    public final Class<?> mCLZ;
    private Demo(String msg, Class<?> clz) {
        mMSG = msg;
        mCLZ = clz;
    }
    
    public String getName() {
        return mMSG;
    }
    
    public static int getCount() {
        return values().length;
    }
    
    public static Demo get(int position) {
        return values()[position];
    }
}
