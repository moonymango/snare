package stachelsau.snare.demo;

import stachelsau.snare.demo.camera.CameraDemo;
import stachelsau.snare.demo.particles.ParticlesDemo;
import stachelsau.snare.demo.physics.SimplePhysicsDemo;
import stachelsau.snare.demo.playground.Playground;
import stachelsau.snare.demo.procedural.ProceduralDemo;
import stachelsau.snare.demo.resolution.ResolutionDemo;
import stachelsau.snare.demo.scene.SceneDemo;
import stachelsau.snare.demo.text.TextDemo;
import stachelsau.snare.demo.textures.TextureDemo;
import stachelsau.snare.demo.touch.SceneTouchDemo;
import stachelsau.snare.demo.widgets.WidgetDemo;

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
