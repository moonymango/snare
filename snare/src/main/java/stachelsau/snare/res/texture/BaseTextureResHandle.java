package stachelsau.snare.res.texture;

import stachelsau.snare.opengl.TextureObjOptions;
import stachelsau.snare.res.BaseResHandle;

public abstract class BaseTextureResHandle  extends BaseResHandle {

    public BaseTextureResHandle(BaseTextureResource res) {
        super(res);
    }
    
    public abstract int loadToGPU(TextureObjOptions options);
    
}
