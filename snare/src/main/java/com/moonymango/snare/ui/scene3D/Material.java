package com.moonymango.snare.ui.scene3D;

import com.moonymango.snare.game.GameObj;
import com.moonymango.snare.game.GameObj.ComponentType;
import com.moonymango.snare.game.logic.BaseComponent;
import com.moonymango.snare.opengl.TextureObj.TextureUnit;
import com.moonymango.snare.ui.ColorWrapper;
import com.moonymango.snare.ui.ColorWrapper.IColorSeqListener;
import android.util.SparseArray;

/**
 * Container for all material related information (colors and textures).
 * Textures are loaded to GPU in onInit() and removed in onShutdown().
 */
public class Material extends BaseComponent implements IColorSeqListener {
    
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
    
    private int mHash;
    
    /** Constructs material with max. 8 texture units. */
    public Material() {
        this(8);
    }
    
    public Material(int maxTexUnits) {
        super(ComponentType.MATERIAL);
        mTexUnits = new TextureUnit[maxTexUnits];
    }
    
    /** 
     * Material component is controlled by BaseSceneDrawable and shall not
     * do anything on it's own in onInit.
     */
    @Override
    public final void onInit() {super.onInit();}
    /** 
     * Material component is controlled by BaseSceneDrawable and shall not
     * do anything on it's own in onShutdown.
     */
    public final void onShutdown() {super.onShutdown();}
    
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
      
    public Material addTextureUnit(TextureUnit t) {
        final GameObj obj = getGameObj();
        if (obj != null && getGameObj().isInitialized()) {
            throw new IllegalStateException("Cannot add texture to an " +
                    "active game object.");
        }
        
        if (mTexUnits[t.unit] != null) {
            throw new IllegalStateException("Texture unit " + t.unit + 
                    " already registered.");
        }
        
        mTexUnits[t.unit] = t;
        
        // recalc hash, consider texture name and unit
        final int prime = 31;
        mHash = 1;
        for (int i = 0; i < mTexUnits.length; i++)  {
            final TextureUnit tu = mTexUnits[i];
            if (tu == null) continue;
            final String s = tu.res.getQName();
            mHash = prime * mHash + s.hashCode();
            mHash = prime * mHash + tu.unit;
        }
        return this;
    }
    
    /** Gets qualified name of specified texture. */
    public String getTextureName(int unit) {
        return mTexUnits[unit].res.getQName();    
    }
    
    protected void bindTextures() {
        for (int i = 0; i < mTexUnits.length; i++) {
            if (mTexUnits[i] != null) {
                mTexUnits[i].bind();
            }
        }
    }
    
    public void loadToGpu() {
        for (int i = 0; i < mTexUnits.length; i++) {
            if (mTexUnits[i] != null) {
                mTexUnits[i].load();
            }
        }
    }

    public void unloadFromGpu() {
        for (int i = 0; i < mTexUnits.length; i++) {
            if (mTexUnits[i] != null) {
                mTexUnits[i].unload();
            }
        }
    }

    @Override
    public int hashCode() {
        return mHash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Material other = (Material) obj;
        return mHash == other.mHash;
    }
    
}
