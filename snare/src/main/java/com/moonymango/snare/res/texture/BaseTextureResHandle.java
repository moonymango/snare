package com.moonymango.snare.res.texture;

import com.moonymango.snare.opengl.TextureObjOptions;
import com.moonymango.snare.res.BaseResHandle;

public abstract class BaseTextureResHandle  extends BaseResHandle {

    public BaseTextureResHandle(BaseTextureResource res) {
        super(res);
    }
    
    public abstract int loadToGPU(TextureObjOptions options);
    
}
