package com.moonymango.snare.ui.scene3D;


public enum RenderPass {
    // order of enum elements is also order of rendering
    
    DYNAMIC,
    STATIC,
    ENVIRONMENT,
    ALPHA;
   
    ///////////////////////////////////////////////////////////
    
    public static final int COUNT = RenderPass.values().length;
    // avoid allocations
    public static final RenderPass[] VALUES = RenderPass.values(); 
    
    public static RenderPass getFirst() {
        return VALUES[0];
    }
    
    public RenderPass getNext() {
        int ord = this.ordinal();
        if (++ord >= COUNT)
            return null;
        return VALUES[ord];
    }
} 

