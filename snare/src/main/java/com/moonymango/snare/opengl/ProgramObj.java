package com.moonymango.snare.opengl;

import static android.opengl.GLES20.*;

public class ProgramObj extends BaseGLObj {
  
    private static final int sParams[] = new int[4];
    
    private String mVertexShaderString;
    private String mFragmentShaderString;
    private ILocationHolder mLocHolder;
    private int mVID; 
    private int mFID; 
    
    
    protected ProgramObj(GLObjDescriptor descriptor) {
        super(descriptor);
    }

    /**
     * Configure program object. Program objects must be configured only once!
     * @param vertexShader Shader code.
     * @param fragmentShader Shader code.
     * @param locHolder location holder for this program
     */
    public void configure(String vertexShader, String fragmentShader, ILocationHolder locHolder) {
        if (isConfigured()) {
            throw new IllegalStateException("GL object already configured.");
        }
        if (vertexShader == null || fragmentShader == null) {
            throw new IllegalArgumentException("Missing shaders.");
        }
        if (locHolder == null) {
            throw new IllegalArgumentException("Missing location holder.");
        }
        mVertexShaderString = vertexShader;
        mFragmentShaderString = fragmentShader;
        mLocHolder = locHolder;
                
        setState(GLObjState.TO_LOAD);
    }
    
    
    public ILocationHolder getLocationHolder() {
        return mLocHolder;
    }
    
    @Override
    public void onLoad() {
        if (!isConfigured()) {
            throw new IllegalStateException("Program not configured.");
        }   
        
        mVID = loadShader(GL_VERTEX_SHADER, mVertexShaderString);
        mFID = loadShader(GL_FRAGMENT_SHADER, mFragmentShaderString);
        mID = linkProgram(mVID, mFID);
        mLocHolder.extractLocations(mDescriptor.getQName(), mID);
    }

    @Override
    public void onUnload() {
        if (isLoaded()) {
            glUseProgram(0);
            glDeleteProgram(mID);
            glDeleteShader(mVID);
            glDeleteShader(mFID);
        }        
    }
    
    private static int loadShader(int type, String shaderCode){
        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        if (type != GL_VERTEX_SHADER && type != GL_FRAGMENT_SHADER) {
            throw new IllegalArgumentException("Unknown shader type.");
        }
        int shader = glCreateShader(type); 
        glShaderSource(shader, shaderCode);
        glCompileShader(shader);
        glGetShaderiv(shader, GL_COMPILE_STATUS, sParams, 0);
        if (sParams[0] != GL_TRUE) {
            String log = glGetShaderInfoLog(shader);
            String s = type == GL_VERTEX_SHADER ? "vertex shader" : "fragment shader";
            throw new IllegalArgumentException("Unable to compile " + s + ":\n"
                    + shaderCode + "\nshader info log:\n" + log);
        }       
        return shader;
    }
    
    private static int linkProgram(int vs, int fs) {
        int p = glCreateProgram();
        glAttachShader(p, vs);
        glAttachShader(p, fs);
        glLinkProgram(p);      
        glGetProgramiv(p, GL_LINK_STATUS, sParams, 0);
        if (sParams[0] != GL_TRUE) {
            throw new IllegalArgumentException("Unable to link program:\n"
                    + glGetProgramInfoLog(p));
        }
        return p;
    }
    
    public interface ILocationHolder {
        /**
         * Called after program linking to allow for collection of
         * uniform and attribute locations.
         * @param programName Name of program object, see {@link GLObjDescriptor}
         * @param prog GL id of the program.
         */
        void extractLocations(String programName, int prog);
    }
}
