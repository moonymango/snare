package com.moonymango.snare.opengl;

import android.opengl.ETC1Util;

import com.moonymango.snare.util.Logger;
import com.moonymango.snare.util.Logger.LogSource;

import static android.opengl.GLES20.GL_ALIASED_LINE_WIDTH_RANGE;
import static android.opengl.GLES20.GL_ALIASED_POINT_SIZE_RANGE;
import static android.opengl.GLES20.GL_EXTENSIONS;
import static android.opengl.GLES20.GL_MAX_FRAGMENT_UNIFORM_VECTORS;
import static android.opengl.GLES20.GL_MAX_RENDERBUFFER_SIZE;
import static android.opengl.GLES20.GL_MAX_TEXTURE_IMAGE_UNITS;
import static android.opengl.GLES20.GL_MAX_TEXTURE_SIZE;
import static android.opengl.GLES20.GL_MAX_VARYING_VECTORS;
import static android.opengl.GLES20.GL_MAX_VERTEX_ATTRIBS;
import static android.opengl.GLES20.GL_MAX_VERTEX_TEXTURE_IMAGE_UNITS;
import static android.opengl.GLES20.GL_MAX_VERTEX_UNIFORM_VECTORS;
import static android.opengl.GLES20.GL_SHADING_LANGUAGE_VERSION;
import static android.opengl.GLES20.glGetFloatv;
import static android.opengl.GLES20.glGetIntegerv;
import static android.opengl.GLES20.glGetString;


public class GLInfo
{
    private int MAX_VERTEX_ATTRIBUTES;
    private int MAX_VERTEX_UNIFORM_VECTORS;
    private int MAX_VARYING_VECTORS;
    private int MAX_VERTEX_TEXTURE_UNITS;
    private int MAX_FRAGMENT_UNIFORM_VECTORS;
    private int MAX_TEXTURE_UNITS;
    private int MAX_TEXTURE_SIZE;
    private int MAX_RENDERBUFFER_SIZE;
    private float POINT_SIZE_RANGE;
    private float LINE_WIDTH_RANGE;
    private String GLSL_VERSION;
    private boolean ETC1_SUPPORT;
    private String EXTENSIONS;
    
    
    
    public void collectValues() {
        int[] parami = new int[4];
        float[] paramf = new float[4];
        
        glGetIntegerv(GL_MAX_VERTEX_ATTRIBS, parami, 0);
        MAX_VERTEX_ATTRIBUTES = parami[0];
        
        glGetIntegerv(GL_MAX_VERTEX_UNIFORM_VECTORS, parami, 0);
        MAX_VERTEX_UNIFORM_VECTORS = parami[0];
        
        glGetIntegerv(GL_MAX_VARYING_VECTORS, parami, 0);
        MAX_VARYING_VECTORS = parami[0];
        
        glGetIntegerv(GL_MAX_VERTEX_TEXTURE_IMAGE_UNITS, parami, 0);
        MAX_VERTEX_TEXTURE_UNITS = parami[0];
        
        glGetIntegerv(GL_MAX_FRAGMENT_UNIFORM_VECTORS, parami, 0);
        MAX_FRAGMENT_UNIFORM_VECTORS = parami[0];
        
        glGetIntegerv(GL_MAX_TEXTURE_IMAGE_UNITS, parami, 0);
        MAX_TEXTURE_UNITS = parami[0];
        
        glGetIntegerv(GL_MAX_TEXTURE_SIZE, parami, 0);
        MAX_TEXTURE_SIZE = parami[0];
        
        glGetIntegerv(GL_MAX_RENDERBUFFER_SIZE, parami, 0);
        MAX_RENDERBUFFER_SIZE = parami[0];
        
        glGetFloatv(GL_ALIASED_POINT_SIZE_RANGE, paramf, 0);
        POINT_SIZE_RANGE = paramf[1];
        
        glGetFloatv(GL_ALIASED_LINE_WIDTH_RANGE, paramf, 0);
        LINE_WIDTH_RANGE = paramf[1];
        
        GLSL_VERSION = glGetString(GL_SHADING_LANGUAGE_VERSION);
        
        ETC1_SUPPORT = ETC1Util.isETC1Supported();
        
        EXTENSIONS = glGetString(GL_EXTENSIONS);
        
        logInfo();
    }
    
    public void logInfo() {
        Logger.i(LogSource.OPENGL, "GL_MAX_VERTEX_ATTRIBS = " + MAX_VERTEX_ATTRIBUTES);
        Logger.i(LogSource.OPENGL, "GL_MAX_VERTEX_UNIFORM_VECTORS = " + MAX_VERTEX_UNIFORM_VECTORS);
        Logger.i(LogSource.OPENGL, "GL_MAX_VARYING_VECTORS = " + MAX_VARYING_VECTORS);
        Logger.i(LogSource.OPENGL, "GL_MAX_VERTEX_TEXTURE_UNITS = " + MAX_VERTEX_TEXTURE_UNITS);
        Logger.i(LogSource.OPENGL, "GL_MAX_VERTEX_FRAGMENT_UNIFORM_VECTORS = " + MAX_FRAGMENT_UNIFORM_VECTORS);
        Logger.i(LogSource.OPENGL, "GL_MAX_TEXTURE_UNITS = " + MAX_TEXTURE_UNITS);
        Logger.i(LogSource.OPENGL, "GL_MAX_TEXTURE_SIZE = " + MAX_TEXTURE_SIZE);
        Logger.i(LogSource.OPENGL, "GL_MAX_RENDERBUFFER_SIZE = " + MAX_RENDERBUFFER_SIZE);
        Logger.i(LogSource.OPENGL, "GL_ALIASED_POINT_SIZE_RANGE = " + POINT_SIZE_RANGE);
        Logger.i(LogSource.OPENGL, "GL_ALIASED_LINE_WIDTH_RANGE = " + LINE_WIDTH_RANGE);
        Logger.i(LogSource.OPENGL, "GL_SHADING_LANGUAGE_VERSION = " + GLSL_VERSION);
        Logger.i(LogSource.OPENGL, "ETC1 support = " + ETC1_SUPPORT);
        Logger.i(LogSource.OPENGL, EXTENSIONS);
    }
    
    public int getMaxVertexAttributes() {return MAX_VERTEX_ATTRIBUTES;}
    public int getMaxVertexUniformVectors() {return MAX_VERTEX_UNIFORM_VECTORS;}
    public int getMaxVaryingVectors() {return MAX_VARYING_VECTORS;}
    public int getMaxVertexTextureUnits() {return MAX_VERTEX_TEXTURE_UNITS;}
    public int getMaxFragmentUniformVectors() {return MAX_FRAGMENT_UNIFORM_VECTORS;}
    public int getMaxTextureUnits() {return MAX_TEXTURE_UNITS;}
    public int getMaxTextureSize() {return MAX_TEXTURE_SIZE;}
    public int getMaxRenderbufferSize() {return MAX_RENDERBUFFER_SIZE;}
    public boolean hasETC1Support() {return ETC1_SUPPORT;}
    public String getExtensions() {return EXTENSIONS;}
}
