package stachelsau.snare.res.data;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import stachelsau.snare.game.Game;
import stachelsau.snare.res.BaseResHandle;
import stachelsau.snare.res.BaseResource;
import stachelsau.snare.res.IAssetName;
import android.content.res.AssetManager;
import android.content.res.Resources;

public class MeshResource extends BaseResource {
    
    private final BaseImportTransform mTransform;

    public MeshResource(IAssetName asset) {
        this(asset, null);
    }
    
    /**
     * TODO prioD: transform should be part of resource name to enable  
       import of an asset using different transforms
     * @param asset
     * @param transform
     */
    public MeshResource(IAssetName asset, BaseImportTransform transform) { 
        super(asset);
        if (asset.getQualifier() == null) {
            throw new IllegalArgumentException("Missing mesh name.");
        }
        mTransform = transform;
    }
    
    @Override
    protected BaseResHandle createHandleByAsset(AssetManager am) {
        
        final int i = mName.lastIndexOf(".");
        final String fileExt = (i > 0) ? mName.substring(i) : "";
        
        BaseMeshParser parser = null;
        if (fileExt.equals(".3ds")) {
            parser = new Max3DSParser();
        }
        if (parser == null) {
            throw new UnsupportedOperationException("Unsupported file type " + fileExt);
        }
        
        InputStream in;
        try {
            in = am.open(mName);
            boolean success = parser.parse(in, mQualifier);
            in.close();
            if (!success) {
                return null;
            } 
        } catch (IOException e) {
            return null;
        }
        
        return new MeshResHandle(this, parser, mTransform);
    }

    @Override
    protected BaseResHandle createHandleByResID(Resources res) {
        // never called because there isn't a constructor that takes a resource
        return null;
    }

    public MeshResHandle getHandle() {
        return (MeshResHandle) getHandle(Game.get().getResourceCache());
    }
    
    /** Transformation operation performed during mesh import. */
    public static abstract class BaseImportTransform 
    {    
        private BaseImportTransform mNext;
        /** Allows for adding next transformation to apply multiple transforms. */
        public void setNext(BaseImportTransform next) {mNext = next;}
        public BaseImportTransform getNext() {return mNext;}
        
        /** Transforms vertices. */
        abstract void transform(ArrayList<Vertex> vertices, 
                ArrayList<Face> faces);
    }    
}
