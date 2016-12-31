package com.moonymango.snare.res.data;


// TODO prioD: make this a general purpose vector class??
public class Vec {

    protected final float x;
    protected final float y;
    protected final float z;
    private Vec normal;
        
        
    public Vec(Vec v) {
        x = v.x;
        y = v.y;
        z = v.z;
    }
    
    public Vec(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    protected Vec normalize() {
        if (normal == null) {
            final float f = (float) Math.sqrt(x*x + y*y + z*z);
            normal = new Vec(x/f, y/f, z/f);
        }
        return normal; 
    }
         
    protected Vec subtract(Vec v) { 
        return new Vec( x - v.x, y - v.y, z - v.z);
    }
    
    protected Vec add(Vec v) {
        return new Vec(x + v.x, y + v.y, z + v.z);
    }
    
    protected float dot(Vec v) {
        return x*v.x + y*v.y + z*v.z;
    }
    
    protected Vec cross(Vec v) {
        final float resultX = y*v.z - z*v.y;
        final float resultY = z*v.x - x*v.z;
        final float resultZ = x*v.y - y*v.x;
        return new Vec(resultX, resultY, resultZ);
    }
    
    
}
