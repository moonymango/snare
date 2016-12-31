package com.moonymango.snare.ui.scene3D;

import com.moonymango.snare.game.GameObj;
import com.moonymango.snare.game.GameObj.ComponentType;
import com.moonymango.snare.game.logic.BaseComponent;
import com.moonymango.snare.opengl.TextureObj.TextureUnit;
import com.moonymango.snare.ui.ColorWrapper;
import com.moonymango.snare.ui.ColorWrapper.IColorSeqListener;
import android.util.SparseArray;

public class MaterialManager 
{
    public static final int INVALID_MATERIAL_KEY = Integer.MAX_VALUE;
    
    private static final Material DEFAUL_MATERIAL = new Material();
    private final SparseArray<Material> mMat = new SparseArray<Material>();
    
    private int mActualKey;
    
    /**
     * Adds {@link Material} to manager and loads material textures to Gpu. 
     * @param mat Material
     * @param key Key which may used for later reference to the material.
     */
    public void load(Material m, int key)
    {
        if (key == INVALID_MATERIAL_KEY)
            throw new IllegalArgumentException("Invalid key provided.");
        if (mMat.get(key) != null || m.mManager != null)
            throw new IllegalStateException("Material already loaded.");
        
        mMat.append(key, m);
       
        // load all included textures to Gpu
        for (int i = 0; i < m.mTexUnits.length; i++) {
            if (m.mTexUnits[i] != null) {
                m.mTexUnits[i].load();
            }
        }
    }
    
    /**
     * Removes 
     * @param key
     */
    public void unload(int key)
    {
        final Material m = mMat.get(key);
        if (m == null)
            throw new IllegalStateException("Material wasn't loaded before.");
        
        mMat.delete(key);
        
        // unload textures from Gpu
        for (int i = 0; i < m.mTexUnits.length; i++) {
            if (m.mTexUnits[i] != null) {
                m.mTexUnits[i].unload();
            }
        }
    }
    
    /**
     * @param key
     * @return Material for specified key or null in case of unknown key.
     */
    public Material getMaterial(int key)
    {
        return mMat.get(key);
    }
    
    
    protected void onPreDraw()
    {
        mActualKey = INVALID_MATERIAL_KEY;
    }
    
    /**
     * Prepares material mapped to specified component for drawing. In case
     * of unknown material a default is prepared.
     * @param c
     */
    protected void prepare(MaterialComponent c)
    {
        Material m = mMat.get(c.mKey);
        if (m == null) 
            m = DEFAUL_MATERIAL;
     
        // bind all included textures
        for (int i = 0; i < m.mTexUnits.length; i++) {
            if (m.mTexUnits[i] != null) {
                m.mTexUnits[i].bind();
            }
        }  
    }
    
    
    /**
     * {@link MaterialComponent} maps a {@link GameObj} to a {@link Material}.
     */
    public static class MaterialComponent extends BaseComponent 
    {
        private int mKey; 
        
        private MaterialComponent() 
        {
            super(ComponentType.MATERIAL);
        }
        
        public void mapMaterial(int key)
        {
            mKey = key;
        }  
    }

    /**
     * Container for all material related information (colors and textures).
     */
    public static class Material implements IColorSeqListener {
        
        MaterialManager mManager;
        
        public static final int AMBIENT_COLOR_IDX = 0;
        public static final int DIFFUSE_COLOR_IDX = 1;
        public static final int SPECULAR_COLOR_IDX = 2;
        public static final int OUTLINE_COLOR_IDX = 3;
        public static final int LINE_COLOR_IDX = 4;
        
        private SparseArray<float[]> mColors = new SparseArray<float[]>();
        private SparseArray<ColorWrapper> mPalettes = new SparseArray<ColorWrapper>();
        private final float[] mOutlineScale = {1.05f, 1.05f, 1.05f, 0};
        private float mTexCoord;
        
        private final TextureUnit[] mTexUnits;
        
        /** Constructs material with max. 8 texture units. */
        private Material() {
            this(8);
        }
        
        private Material(int maxTexUnits) {
            mTexUnits = new TextureUnit[maxTexUnits];
        }
        
        public Material setColor(int idx, float r, float g, float b, float a) {
            float[] c = mColors.get(idx);
            if (c == null) {
                c = new float[4];
                mColors.append(idx, c);
            }
            c[0] = r;
            c[1] = g;
            c[2] = b;
            c[3] = a;
            return this;
        }
        
        /** Sets color at specified index. */
        public Material setColor(int idx, float[] color) {
            float[] c = mColors.get(idx);
            if (c == null) {
                c = new float[4];
                mColors.append(idx, c);
            }
            for (int i = 0; i < 4; i++) {
                c[i] = color[i];
            }
            return this;
        }
        
        /** Sets scale of outline decoration for each dimension. */
        public Material setOutlineScale(float x, float y, float z) {
            mOutlineScale[0] = x;
            mOutlineScale[1] = y;
            mOutlineScale[2] = z;
            return this;
        }
        
        /** Sets scale of outline decoration for each dimension. */
        public Material setOutlineScale(float[] scale) {
            mOutlineScale[0] = scale[0];
            mOutlineScale[1] = scale[1];
            mOutlineScale[2] = scale[2];
            return this;
        }
        
        public float[] getOutlineScale() {
            return mOutlineScale;
        }
        
        public void setTexCoord(float c) {
            mTexCoord = c;
        }
        
        public float getTexCoord() {
            return mTexCoord;
        }
         
        public float[] getColor(int idx) {
            return mColors.get(idx);
        }
        
        /** 
         * Register color wrapper for specified color index.
         * ATTENTION: A single color wrapper must not be registered to multiple
         *              color indices.
         */
        public Material registerPalette(int colorIdx, ColorWrapper cp) {
            if (mColors.get(colorIdx) == null) {
                final float[] c = new float[4];
                mColors.append(colorIdx, c);
            }
            final ColorWrapper old = mPalettes.get(colorIdx);
            if (old != null) {
                old.removeListener(this);
            }
            mPalettes.append(colorIdx, cp);
            cp.addListener(this);
            return this;
        }
        
        @Override
        public void onColorChange(ColorWrapper cp) {
             
            final int idx = mPalettes.indexOfValue(cp);
            if (idx < 0) {
                return;
            }
                 
            final float[] c = cp.getActualColor();
            final float[] color = mColors.get(idx);
            for (int i = 0; i < 4; i++) {
                color[i] = c[i];
            }
        }
        
        public boolean hasTextureUnit(int unit) {
            return mTexUnits[unit] != null;
        }
          
        public Material addTextureUnit(TextureUnit t)
        {    
            if (mManager != null) {
                throw new IllegalStateException("Cannot add texture to an " +
                        "already loaded material.");
            }
            
            if (mTexUnits[t.unit] != null) {
                throw new IllegalStateException("Texture unit " + t.unit + 
                        " already registered.");
            }
            
            mTexUnits[t.unit] = t;
            
            return this;
        }
        
        /** Gets qualified name of specified texture. */
        public String getTextureName(int unit) {
            return mTexUnits[unit].res.getQName();    
        }
                
    }

    
}
