package com.moonymango.snare.res.texture;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;

import com.moonymango.snare.opengl.TextureObj.TextureSize;
import com.moonymango.snare.res.BaseResHandle;
import com.moonymango.snare.res.IAssetName;
import com.moonymango.snare.res.texture.BitmapTextureResource.ITextureChannelSource.Channel;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;

public class BitmapTextureResource extends BaseTextureResource {

    private final boolean mGen;
    private TextureSize mSize;
    private ITextureChannelSource mR;
    private ITextureChannelSource mG;
    private ITextureChannelSource mB;
    private ITextureChannelSource mA;
    private Bitmap.Config mConfig;
    
    /**
     * Constructs procedural bitmap ALPHA_8 resource.
     * Not yet not supported! 
     * @param name
     * @param source
     */
    public BitmapTextureResource(String name, TextureSize size, 
            ITextureChannelSource alpha) {
        this(name, size, null, null, null, alpha);
        mConfig = Config.ALPHA_8;
    }
    
    /**
     * Construct procedural RGB_565 bitmap resource.
     * @param name
     * @param size
     * @param red
     * @param green
     * @param blue
     */
    public BitmapTextureResource(String name, TextureSize size, 
            ITextureChannelSource red, ITextureChannelSource green, 
            ITextureChannelSource blue) {
        this(name, size, red, green, blue, null);
        mConfig = Config.RGB_565;
    }
    
    /**
     * Constructs procedural ARGB_8888 bitmap resource.
     * @param name
     * @param size
     * @param red
     * @param green
     * @param blue
     * @param alpha
     */
    public BitmapTextureResource(String name, TextureSize size, 
            ITextureChannelSource red, ITextureChannelSource green, 
            ITextureChannelSource blue, ITextureChannelSource alpha) {
        super(name);
        mGen = true;
        mR = red;
        mG = green;
        mB = blue;
        mA = alpha;
        mSize = size;
        mConfig = Config.ARGB_8888;
    }
    
    /**
     * Creates texture resource using file location into asset folder.
     * @param name file location
     */
    public BitmapTextureResource(String name) {
        super(name);
        mGen = false;
    }
    
    /**
     * Creates texture resource using file location into asset folder and
     * also adds region information.
     * @param name
     * @param provider
     */
    public BitmapTextureResource(String name, ITextureRegionProvider provider) {
        super(name, provider);
        mGen = false;
    }
    
    /**
     * Creates texture resource using {@link IAssetName} description. 
     * @param asset
     */
    public BitmapTextureResource(IAssetName asset) {
        super(asset);
        mGen = false;
    }
     
    /**
     * Creates texture resource using {@link IAssetName} description and 
     * also adds region information.
     * @param asset
     * @param provider
     */
    public BitmapTextureResource(IAssetName asset, ITextureRegionProvider provider) {
        super(asset, provider);
        mGen = false;
    }
   
    @Override
    protected BaseResHandle createHandleByAsset(AssetManager am) {
        // generate bitmap
        if (mGen) {
            switch(mConfig) {
            case RGB_565:
                return new BitmapTextureResHandle(this, generate565());
            case ARGB_8888:
                return new BitmapTextureResHandle(this, generate8888());
            default:
                throw new IllegalArgumentException("Unsupported bitmap config.");
            }    
        }
        
        // load bitmap from asset file
        Bitmap b;
        try {
            BufferedInputStream in = new BufferedInputStream(am.open(mName));
            b = BitmapFactory.decodeStream(in);
            in.close();
        } catch (IOException e) {
            return null;
        } 
        if (b == null) {
            return null;
        }
        return new BitmapTextureResHandle(this, b);
    }

    
    @Override
    protected BaseResHandle createHandleByResID(Resources res) {
        // load bitmap from resource file
        BufferedInputStream in = new BufferedInputStream(res.openRawResource(mResID));
        Bitmap b = BitmapFactory.decodeStream(in);
        return new BitmapTextureResHandle(this, b);
    }
    
    
    private Bitmap generate565() {
        final int size = mSize.value();
        if (mR != null) mR.init(mSize);
        if (mG != null) mG.init(mSize);
        if (mB != null) mB.init(mSize);
        ShortBuffer sb = ShortBuffer.allocate(size * size);
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                final float r = mR != null ? mR.getPixel(Channel.R, x, y) : 0;
                final float g = mG != null ? mG.getPixel(Channel.G, x, y) : 0;
                final float b = mB != null ? mB.getPixel(Channel.B, x, y) : 0;
                final int rr = (int) (r*31);
                final int gg = (int) (g*63);
                final int bb = (int) (b*31);
                final short s = (short) (rr << 11 | gg << 5 | bb);
                sb.put(s);
            }
        }
        
        final Bitmap bm = Bitmap.createBitmap(size, size, mConfig);
        bm.copyPixelsFromBuffer(sb.rewind());
        return bm;
    }
    
    private Bitmap generate8888() {
        final int size = mSize.value();
        if (mR != null) mR.init(mSize);
        if (mG != null) mG.init(mSize);
        if (mB != null) mB.init(mSize);
        if (mA != null) mA.init(mSize);
        ByteBuffer bb = ByteBuffer.allocate(size * size * 4);
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                final float r = mR != null ? mR.getPixel(Channel.R, x, y) : 0;
                final float g = mG != null ? mG.getPixel(Channel.G, x, y) : 0;
                final float b = mB != null ? mB.getPixel(Channel.B, x, y) : 0;
                final float a = mA != null ? mA.getPixel(Channel.A, x, y) : 0;
                bb.put((byte) (r*255));
                bb.put((byte) (g*255));
                bb.put((byte) (b*255));
                bb.put((byte) (a*255));
            }
        }
        
        final Bitmap bm = Bitmap.createBitmap(size, size, mConfig);
        bm.copyPixelsFromBuffer(bb.rewind());
        return bm;
    }
    
    /**
     * Interface for procedural bitmap generators.
     */
    public interface ITextureChannelSource {
        enum Channel {R, G, B, A}
        /** 
         * Init source. This happen prior to any calls on getPixel()
         * @return Does not matter. 
         */
        boolean init(TextureSize size);
        /** 
         * Pixel data
         * @return Channel value in range [0..1] 
         */
        float getPixel(Channel c, int x, int y);
    }

}
