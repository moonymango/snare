package com.moonymango.snare.ui.scene3D;

import com.moonymango.snare.game.BaseSnareClass;
import com.moonymango.snare.game.GameObj;
import com.moonymango.snare.game.GameObj.ComponentType;
import com.moonymango.snare.game.IGame;
import com.moonymango.snare.game.logic.BaseComponent;
import com.moonymango.snare.opengl.GLObjDescriptor;
import com.moonymango.snare.opengl.GLObjDescriptor.GLObjType;
import com.moonymango.snare.opengl.GLState;
import com.moonymango.snare.opengl.ProgramObj;
import com.moonymango.snare.opengl.ProgramObj.ILocationHolder;

/**
 * Effects focus on shader handling and the actual drawing. Since
 * effects may have a state, every drawable needs its own effect instance.
 * 
 * NOTE: When implementing method extractLocations of {@link ILocationHolder} 
 * always store locations in static variables. Multiple instances of an effect can
 * exist, but for only one of them extractLocations() gets actually called. 
 * So by using static variables the extracted locations are visible to all instances.  
 *
 */
public abstract class BaseEffect extends BaseComponent 
        implements ILocationHolder {
    
    private final RenderContext mContext;
        
    /** Constructor. */
    protected BaseEffect(RenderContext context)
    {
        super(context.mGame, ComponentType.EFFECT);
        mContext = context;
    }
    
    /** 
     * Effect component is controlled by BaseSceneDrawable and shall not
     * do anything on it's own in onInit.
     */
    @Override
    public final void onInit() {super.onInit();}
    /** 
     * Effect component is controlled by BaseSceneDrawable and shall not
     * do anything on it's own in onShutdown.
     */
    public final void onShutdown() {super.onShutdown();}
    

    public void loadToGpu() {
        if (mContext.mProgObj != null)
            return;
        
        final ProgramObj o = (ProgramObj) mContext.mProgramDescr.getHandle(); 
        if (!o.isConfigured()) {
            o.configure(mContext.mVertexShaderText, 
                    mContext.mFragmentShaderText, 
                    this);
        }
        mContext.mProgObj = o; 
    }
    
    public void unloadFromGpu() {
        mContext.mProgramDescr.releaseHandle(mContext.mProgObj);
        mContext.mProgObj = null;
    }
    
    /**
     * @return Render context for the effect.
     */
    public RenderContext getContext() {return mContext;}
    
    /**
     * Apply effect in the given scene. When this is called the
     * {@link RenderContext} is already in place (see getContext()) and
     * also the textures have already been bound to their tex unit. 
     * @param scene
     * @param pass
     * @return False, if the effect does not want to be rendered again, true otherwise.
     */
    public abstract boolean render(Scene3D scene, BaseMesh mesh,
                                   Material material, GameObj obj, RenderPass pass);
    
    /**
     * Check if effect can be applied using a specified mesh and material.
     * If no then this should throw a runtime exception. 
     * @param mesh
     * @param mat
     */
    protected abstract void check(BaseMesh mesh, Material mat);
    
    
    /**
     * Context consisting of shader program and general GL state. 
     * This is used by {@link Scene3D} to group drawables within their 
     * respective render passes.
     */
    public static class RenderContext extends BaseSnareClass
    {
        private final String mName;
        private final String mVertexShaderText;
        private final String mFragmentShaderText;
        private final GLState mState;
        /** Reference to actual GL object representing the shader. */
        private ProgramObj mProgObj;
        private final GLObjDescriptor mProgramDescr;
        private final int mHash;
        
        /**
         * Constructs RenderContext.
         * @param name Shader name.
         * @param vertexShader Vertex shader text.
         * @param fragmentShader Fragment shader text.
         * @param state GlState to use with the shader (state has to be locked)   
         */
        public RenderContext(IGame game, String name, String vertexShader, String fragmentShader, GLState state)
        {
            super(game);
            mName = name;
            mVertexShaderText = vertexShader;
            mFragmentShaderText = fragmentShader;
            mState = state;
            mProgramDescr = new GLObjDescriptor(game, name, GLObjType.PROGRAM);
            
            // calculate hash
            final int prime = 31;
            int result = 1;
            result = prime * result + mName.hashCode();
            result = prime * result + mState.hashCode();
            mHash = result;
        }
        
        /** Applies context. */
        public void begin() {
            mState.apply(mProgObj.getID());
        }
        
        /** Ends the context. Currently nothing happens here. */
        public void end() {
            
        }
        
        /** 
         * @return Context name (also shader program name)
         */
        public String getName() {return mName;}
        /**
         * @return GLState used by the context.
         */
        public GLState getGlState() {return mState;}

        @Override
        public int hashCode() {
            return mHash;
        }

        /** Context are equal if they use same shader and GLState settings.*/
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            RenderContext other = (RenderContext) obj;
            
            return mHash == other.mHash;
        }
        
        
    }
}
