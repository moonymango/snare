package com.moonymango.snareTest;


import android.support.test.runner.AndroidJUnit4;

import com.moonymango.snare.game.GameObj;
import com.moonymango.snare.physics.BaseBoundingVolume;
import com.moonymango.snare.physics.BaseBoundingVolume.IntersectionDistance;
import com.moonymango.snare.physics.BaseBoundingVolume.VolumeType;
import com.moonymango.snare.physics.BaseSimpleBoundingVolume;
import com.moonymango.snare.physics.IPhysics;
import com.moonymango.snare.physics.SimpleAABB;
import com.moonymango.snare.physics.SimpleCollisionPair;
import com.moonymango.snare.physics.SimpleCylinderBoundingVolume;
import com.moonymango.snare.physics.SimpleCylinderBoundingVolume.AxisOrientation;
import com.moonymango.snare.physics.SimplePhysics;
import com.moonymango.snare.physics.SimpleSphereBoundingVolume;
import com.moonymango.snare.physics.SimpleSquareBoundingVolume;
import com.moonymango.snare.ui.scene3D.mesh.SquareMesh;
import com.moonymango.snare.util.Geometry;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(AndroidJUnit4.class)
public class BoundingVolumeTest {
    
    public static final int OBJECT_LAYER = 1;
    
    BaseSimpleBoundingVolume mBVA;
    GameObj mObjA;
    BaseSimpleBoundingVolume mBVB;
    GameObj mObjB;
    GameStub mGame;
    final float[] mS = new float[4];
    final float[] mV = new float[4];
    IPhysics mPhysics;
    
    @Before
    public void setUp() throws Exception {
        mGame = new GameStub();
        mPhysics = new SimplePhysics(mGame);
        mPhysics.enableCollisionChecking(true);
    }

    @Test
    public void testRayAABBIntersection() {
        final float[] min = {-1, -1, -1, 1};
        final float[] max = {1, 1, 1, 1};
                
        // define box with dimensions 2, 4, 1 at point (5, 0, 0)
        mBVA = (BaseSimpleBoundingVolume) mPhysics.createBoundingVolume(VolumeType.BOX);
        ((SimpleAABB) mBVA).setDimensions(min, max);
        mObjA = new GameObj(mGame, "bounding_volume");
        mObjA.addComponent(mBVA);
        mObjA.setPosition(5, 0, 0);
        mObjA.setScale(1, 2, 0.5f);
        mObjA.onUpdateTransform(0, 0, 0);
        
        // ray at position (5, 0, 2) along neg. z axis
        mS[0] = 5;
        mS[1] = 0;
        mS[2] = 2;
        mS[3] = 1;
        mV[0] = 0;
        mV[1] = 0;
        mV[2] = -1;
        mV[3] = 0;
        
        BaseBoundingVolume.IntersectionDistance d = mBVA.getRayDistance(mS, mV);
        
        // box is oriented so that the ray sees z dimension of box
        // > distance: 2 - 1/2 = 1.5
        assertEquals(true, d != null);
        assertEquals(1.5f, d.MIN, Geometry.PRECISION);
        
        // rotate box so that y dimension is oriented along ray direction
        // > distance: 2 - 4/2 = 0
        mObjA.resetRotation();
        mObjA.rotate(1, 0, 0, 90);
        mObjA.onUpdateTransform(0, 0, 0);
        d = mBVA.getRayDistance(mS, mV);
        assertEquals(true, d != null);
        assertEquals(0, d.MIN, Geometry.PRECISION);
        
        // rotate box so that x dimension is oriented along ray direction
        // > distance: 2 - 2/2 = 1 
        mObjA.resetRotation();
        mObjA.rotate(0, 1, 0, 90);
        mObjA.onUpdateTransform(0, 0, 0);
        d = mBVA.getRayDistance(mS, mV);
        assertEquals(true, d != null);
        assertEquals(1, d.MIN, Geometry.PRECISION);
        
        // test ray that misses the box
        mS[0] = 0;
        mS[1] = 0;
        mS[2] = 0;
        mS[3] = 1;
        mV[0] = 0;
        mV[1] = 1;
        mV[2] = 0;
        mV[3] = 0;
        d = mBVA.getRayDistance(mS, mV);
        assertEquals(true, d == null);
        
        // test ray that starts within the box
        mS[0] = 5;
        mS[1] = 0.5f;
        mS[2] = 0;
        mS[3] = 1;
        mV[0] = 0;
        mV[1] = 1;
        mV[2] = 0;
        mV[3] = 0;
        d = mBVA.getRayDistance(mS, mV);
        assertEquals(true, d != null);
        // box spans from -2 to 2 in y direction, ray origin y is 0.5
        // min = -2 - 0.5 = -2.5, max = 2 - 0.5 = 1.5 
        assertEquals(-2.5f, d.MIN, Geometry.PRECISION);
        assertEquals(1.5f, d.MAX, Geometry.PRECISION);
        
    }

    @Test
    public void testAABBPoint() {
        // box with edge length of 2 at center
        final float[] min = {-1, -1, -1, 1};
        final float[] max = {1, 1, 1, 1};
                
        mBVA = (BaseSimpleBoundingVolume) mPhysics.createBoundingVolume(VolumeType.BOX);
        ((SimpleAABB) mBVA).setDimensions(min, max);
        mObjA = new GameObj(mGame, "bounding_volume");
        mObjA.addComponent(mBVA);
       
        assertEquals(false, mBVA.isInVolume(1.1f, 0, 0)); 
        assertEquals(false, mBVA.isInVolume(2, 2, 2));
        
        mObjA.setPosition(2, 2, 2);
        mObjA.onUpdateTransform(0, 0, 0);
        assertEquals(true, mBVA.isInVolume(2, 2, 2));
        
    }

    @Test
    public void testAABBCollision() {
        final float[] min = {-1, -1, -1, 1};
        final float[] max = {1, 1, 1, 1};
                
        mBVA = (BaseSimpleBoundingVolume) mPhysics.createBoundingVolume(VolumeType.BOX);
        ((SimpleAABB) mBVA).setDimensions(min, max);
        mObjA = new GameObj(mGame, "bounding_volumeA");
        mObjA.addComponent(mBVA);
        
        // box at center
        mBVB = (BaseSimpleBoundingVolume) mPhysics.createBoundingVolume(VolumeType.BOX);
        ((SimpleAABB) mBVB).setDimensions(min, max);
        mObjB = new GameObj(mGame, "bounding_volumeB");
        mObjB.addComponent(mBVB);
        mObjB.setScale(5, 5, 5);
        mObjB.onUpdateTransform(0, 0, 0);
        
        final SimpleCollisionPair p = new SimpleCollisionPair(mGame);
        
        // no collision
        p.init(mBVA, mBVB);
        mObjA.setPosition(8, 8, 8);
        mObjA.onUpdateTransform(0, 0, 0);
        assertEquals(false, p.isCollision());
        
        // touch
        p.init(mBVA, mBVB);
        mObjA.setPosition(5.99999f, 5.99999f, 5.99999f);
        mObjA.onUpdateTransform(0, 0, 0);
        mObjA.onUpdateComponents(0, 0, 0);
        assertEquals(true, p.isCollision());
        // collision point is 5, 5, 5
        final float[] cp = p.getCollisionPoint();
        assertEquals(5, cp[0], Geometry.PRECISION);
        assertEquals(5, cp[1], Geometry.PRECISION);
        assertEquals(5, cp[1], Geometry.PRECISION);
        
        // collision
        p.init(mBVA, mBVB);
        mObjA.setPosition(5, 5, 5);
        mObjA.onUpdateTransform(0, 0, 0);
        mObjA.onUpdateComponents(0, 0, 0);
        assertEquals(true, p.isCollision());
        
        // objA inside objB, but center of objB still outside of objA
        p.init(mBVA, mBVB);
        mObjA.setPosition(3, 3, 3);
        mObjA.onUpdateTransform(0, 0, 0);
        mObjA.onUpdateComponents(0, 0, 0);
        assertEquals(true, p.isCollision());
        
        // objA inside objB, center of objB inside of objA
        p.init(mBVA, mBVB);
        mObjA.setPosition(0, 0, 0);
        mObjA.onUpdateTransform(0, 0, 0);
        mObjA.onUpdateComponents(0, 0, 0);
        assertEquals(true, p.isCollision());
        
    }

    @Test
    public void testSphereIntersection() {               
        // define sphere with radius 2 at point (5, 0, 0)
        // + object flattened along z axis
        mBVA = (BaseSimpleBoundingVolume) mPhysics.createBoundingVolume(VolumeType.SPHERE);
        ((SimpleSphereBoundingVolume) mBVA).setDimensions(2);
        mObjA = new GameObj(mGame, "bounding_volume");
        mObjA.addComponent(mBVA);
        mObjA.setPosition(5, 0, 0);
        mObjA.setScale(1, 1, 0.5f);
        mObjA.onUpdateTransform(0, 0, 0);
        mObjA.onUpdateComponents(0, 0, 0);
        
        // ray at position (5, 0, 2) along neg. z axis;
        // right through sphere's center
        mS[0] = 5;
        mS[1] = 0;
        mS[2] = 2;
        mS[3] = 1;
        mV[0] = 0;
        mV[1] = 0;
        mV[2] = -1;
        mV[3] = 0;
        IntersectionDistance d = mBVA.getRayDistance(mS, mV);
        
        // expected distance: 1 
        assertEquals(true, d != null);
        assertEquals(1, d.MIN, Geometry.PRECISION);
        assertEquals(3, d.MAX, Geometry.PRECISION);
        
        // ray at position (3, 0, 2) along neg. z axis:
        // tangent to sphere
        mS[0] = 3;
        mS[1] = 0;
        mS[2] = 2;
        mS[3] = 1;
        mV[0] = 0;
        mV[1] = 0;
        mV[2] = -1;
        mV[3] = 0;
        d = mBVA.getRayDistance(mS, mV);
        
        // expected distance: 2 (min equals max)
        assertEquals(true, d != null);
        assertEquals(2, d.MIN, Geometry.PRECISION);
        assertEquals(d.MAX, d.MIN, Geometry.PRECISION);
                
        // test ray that misses the sphere
        mS[0] = 0;
        mS[1] = 0;
        mS[2] = 0;
        mS[3] = 1;
        mV[0] = 0;
        mV[1] = 1;
        mV[2] = 0;
        mV[3] = 0;
        d = mBVA.getRayDistance(mS, mV);
        assertEquals(true, d == null);
        
        // test ray that starts within the sphere
        mS[0] = 5;
        mS[1] = 0.5f;
        mS[2] = 0;
        mS[3] = 1;
        mV[0] = 0;
        mV[1] = 1;
        mV[2] = 0;
        mV[3] = 0;
        d = mBVA.getRayDistance(mS, mV);
        assertEquals(true, d != null);
        // sphere spans from -1 to 1 in y direction, ray origin y is 0.5
        // min = -1 - 0.5 = -1.5, max = 1 - 0.5 = 0.5 
        assertEquals(-2.5f, d.MIN, Geometry.PRECISION);
        assertEquals(1.5f, d.MAX, Geometry.PRECISION);
    }

    @Test
    public void testSpherePoint() {
        // sphere with radius 2 at center
                
        mBVA = (BaseSimpleBoundingVolume) mPhysics.createBoundingVolume(VolumeType.SPHERE);
        ((SimpleSphereBoundingVolume) mBVA).setDimensions(2);
        mObjA = new GameObj(mGame, "bounding_volume");
        mObjA.addComponent(mBVA);
       
        assertEquals(false, mBVA.isInVolume(2.1f, 0, 0)); 
        assertEquals(false, mBVA.isInVolume(2f, 2f, 2f));
        
        mObjA.setPosition(2, 2, 2);
        mObjA.onUpdateTransform(0, 0, 0);
        mObjA.onUpdateComponents(0, 0, 0);
        assertEquals(true, mBVA.isInVolume(2, 2, 2));
        
    }

    @Test
    public void testSphereCollision() {
        // sphere radius 1
        mBVA = (BaseSimpleBoundingVolume) mPhysics.createBoundingVolume(VolumeType.SPHERE);
        ((SimpleSphereBoundingVolume) mBVA).setDimensions(1);
        mObjA = new GameObj(mGame, "bounding_volumeA");
        mObjA.addComponent(mBVA);
        
        // sphere radius 5
        mBVB = (BaseSimpleBoundingVolume) mPhysics.createBoundingVolume(VolumeType.SPHERE);
        ((SimpleSphereBoundingVolume) mBVB).setDimensions(1);
        mObjB = new GameObj(mGame, "bounding_volumeB");
        mObjB.addComponent(mBVB);
        mObjB.setScale(5, 5, 5);
        mObjB.onUpdateTransform(0, 0, 0);
        mObjA.onUpdateComponents(0, 0, 0);
        
        final SimpleCollisionPair p = new SimpleCollisionPair(mGame);
        
        // no collision
        p.init(mBVA, mBVB);
        mObjA.setPosition(8, 0, 0);
        mObjA.onUpdateTransform(0, 0, 0);
        mObjA.onUpdateComponents(0, 0, 0);
        assertEquals(false, p.isCollision());
        
        // touch
        p.init(mBVA, mBVB);
        mObjA.setPosition(5.99999f, 0, 0);
        mObjA.onUpdateTransform(0, 0, 0);
        mObjA.onUpdateComponents(0, 0, 0);
        assertEquals(true, p.isCollision());
        // collision point is 5, 0, 0
        final float[] cp = p.getCollisionPoint();
        assertEquals(5, cp[0], Geometry.PRECISION);
        assertEquals(0, cp[1], Geometry.PRECISION);
        assertEquals(0, cp[1], Geometry.PRECISION);
        
        // collision
        p.init(mBVA, mBVB);
        mObjA.setPosition(5, 0, 0);
        mObjA.onUpdateTransform(0, 0, 0);
        mObjA.onUpdateComponents(0, 0, 0);
        assertEquals(true, p.isCollision());
        
        // objA inside objB, but center of objB still outside of objA
        p.init(mBVA, mBVB);
        mObjA.setPosition(3, 0, 0);
        mObjA.onUpdateTransform(0, 0, 0);
        mObjA.onUpdateComponents(0, 0, 0);
        assertEquals(true, p.isCollision());
        
        // objA inside objB, center of objB inside of objA
        p.init(mBVA, mBVB);
        mObjA.setPosition(0, 0, 0);
        mObjA.onUpdateTransform(0, 0, 0);
        mObjA.onUpdateComponents(0, 0, 0);
        assertEquals(true, p.isCollision());
        
    }

    @Test
    public void testAABBSphereCollision() {
        mBVA = (BaseSimpleBoundingVolume) mPhysics.createBoundingVolume(VolumeType.SPHERE);
        ((SimpleSphereBoundingVolume) mBVA).setDimensions(1);
        mObjA = new GameObj(mGame, "bounding_volumeA");
        mObjA.addComponent(mBVA);
        
        final float[] min = {-1, -1, -1, 1};
        final float[] max = {1, 1, 1, 1};
        mBVB = (BaseSimpleBoundingVolume) mPhysics.createBoundingVolume(VolumeType.BOX);
        ((SimpleAABB) mBVB).setDimensions(min, max);
        mObjB = new GameObj(mGame, "bounding_volumeB");
        mObjB.addComponent(mBVB);
        mObjB.setScale(5, 5, 5);
        mObjB.onUpdateTransform(0, 0, 0);
        mObjA.onUpdateComponents(0, 0, 0);
        
        final SimpleCollisionPair p = new SimpleCollisionPair(mGame);
        
        // no collision
        p.init(mBVA, mBVB);
        mObjA.setPosition(8, 0, 0);
        mObjA.onUpdateTransform(0, 0, 0);
        mObjA.onUpdateComponents(0, 0, 0);
        assertEquals(false, p.isCollision());
        
        // touch
        p.init(mBVA, mBVB);
        mObjA.setPosition(5.99999f, 0, 0);
        mObjA.onUpdateTransform(0, 0, 0);
        mObjA.onUpdateComponents(0, 0, 0);
        assertEquals(true, p.isCollision());
        // collision point is 5, 0, 0
        final float[] cp = p.getCollisionPoint();
        assertEquals(5, cp[0], Geometry.PRECISION);
        assertEquals(0, cp[1], Geometry.PRECISION);
        assertEquals(0, cp[1], Geometry.PRECISION);
        
        // collision
        p.init(mBVA, mBVB);
        mObjA.setPosition(5, 0, 0);
        mObjA.onUpdateTransform(0, 0, 0);
        mObjA.onUpdateComponents(0, 0, 0);
        assertEquals(true, p.isCollision());
        
        // objA inside objB, but center of objB still outside of objA
        p.init(mBVA, mBVB);
        mObjA.setPosition(3, 0, 0);
        mObjA.onUpdateTransform(0, 0, 0);
        mObjA.onUpdateComponents(0, 0, 0);
        assertEquals(true, p.isCollision());
        
        // objA inside objB, center of objB inside of objA
        p.init(mBVA, mBVB);
        mObjA.setPosition(0, 0, 0);
        mObjA.onUpdateTransform(0, 0, 0);
        mObjA.onUpdateComponents(0, 0, 0);
        assertEquals(true, p.isCollision());
    }

    @Test
    public void testCylinderPoint() {
        
        mBVA = (BaseSimpleBoundingVolume) mPhysics.createBoundingVolume(VolumeType.CYLINDER);
        SimpleCylinderBoundingVolume cyl = (SimpleCylinderBoundingVolume) mBVA;
           
        cyl.setAxisOrientation(AxisOrientation.Z);
        cyl.setDimensions(2, 4);
        mObjA = new GameObj(mGame, "bounding_volume");
        mObjA.addComponent(mBVA);
       
        assertEquals(false, mBVA.isInVolume(0, 0, 2.1f)); 
        assertEquals(true, mBVA.isInVolume(0, 0, 1));
        assertEquals(false, mBVA.isInVolume(2, 2, 1));
        assertEquals(true, mBVA.isInVolume(2, 0, 1));
        
        mObjA.setPosition(0, 2, 0);
        mObjA.onUpdateTransform(0, 0, 0);
        mObjA.onUpdateComponents(0, 0, 0);
        assertEquals(true, mBVA.isInVolume(2f, 2, 1));
        
    }

    @Test
    public void testCylinderIntersection() {               
        // define cylinder with radius 5 at point (5, 0, 0)
        mBVA = (BaseSimpleBoundingVolume) mPhysics.createBoundingVolume(VolumeType.CYLINDER);
        SimpleCylinderBoundingVolume cyl = (SimpleCylinderBoundingVolume) mBVA;
        cyl.setAxisOrientation(AxisOrientation.Y);
        cyl.setDimensions(5, 5);
        mObjA = new GameObj(mGame, "bounding_volume");
        mObjA.addComponent(mBVA);
        mObjA.setPosition(5, 0, 0);
        mObjA.onUpdateTransform(0, 0, 0);
        mObjA.onUpdateComponents(0, 0, 0);
        
        // ray at position (5, 0, 10) along neg. z axis;
        // right through sphere's center
        mS[0] = 5;
        mS[1] = 0;
        mS[2] = 10;
        mS[3] = 1;
        mV[0] = 0;
        mV[1] = 0;
        mV[2] = -1;
        mV[3] = 0;
        IntersectionDistance d = mBVA.getRayDistance(mS, mV);
        // expect intersection at point (5, 0, 5) and (5, 0, -5)
        assertEquals(5, d.MIN, Geometry.PRECISION);
        assertEquals(15, d.MAX, Geometry.PRECISION);
        
        // ray at position (10, 2.5f, 10) along neg. z axis;
        // just grazing the cylinder
        mS[0] = 10;
        mS[1] = 2.5f;
        mS[2] = 10;
        mS[3] = 1;
        mV[0] = 0;
        mV[1] = 0;
        mV[2] = -1;
        mV[3] = 0;
        d = mBVA.getRayDistance(mS, mV);
        // expect single intersection at point (10, 2.5f, 0)
        assertEquals(10, d.MIN, Geometry.PRECISION);
        assertEquals(10, d.MAX, Geometry.PRECISION);
        
        // ray that misses the cylinder
        mS[0] = 5;
        mS[1] = 5;  // ray passes above cyl.
        mS[2] = 10;
        mS[3] = 1;
        mV[0] = 0;
        mV[1] = 0;
        mV[2] = -1;
        mV[3] = 0;
        d = mBVA.getRayDistance(mS, mV);
        assertEquals(d == null, true);
        
        // ray that starts within cylinder
        mS[0] = 2;
        mS[1] = 2f;
        mS[2] = 0;
        mS[3] = 1;
        mV[0] = 1;
        mV[1] = 0;
        mV[2] = 0;
        mV[3] = 0;
        d = mBVA.getRayDistance(mS, mV);
        // expect intersection at points (10, 2, 0) and (0, 2, 0)
        assertEquals(-2, d.MIN, Geometry.PRECISION);
        assertEquals(8, d.MAX, Geometry.PRECISION);
        
    }

    @Test
    public void testSquareIntersection()
    {
        SimpleSquareBoundingVolume bv = (SimpleSquareBoundingVolume) mPhysics.createBoundingVolume(VolumeType.SQUARE);
        SquareMesh m = new SquareMesh(mGame);
        bv.setDimensions(m);

        GameObj obj = new GameObj(mGame, "bv");
        obj.addComponent(bv);
        obj.setPosition(10, 0, 0);
        obj.setScale(1, 1, 2);
        obj.onUpdateTransform(0, 0, 0);
        obj.onUpdateComponents(0, 0, 0);

        // vertical ray outside of square
        mS[0] = 8;
        mS[1] = 1;
        mS[2] = -1.5f;
        mS[3] = 1;
        mV[0] = 0;
        mV[1] = 1;
        mV[2] = 0;
        mV[3] = 0;

        IntersectionDistance d = bv.getRayDistance(mS, mV);
        assertNull(d);

        // move s so that ray hits square
        mS[0] = 9.1f;
        d = bv.getRayDistance(mS, mV);
        assertEquals(-1, d.MIN, Geometry.PRECISION);

        // move s to another point with hit but different distance
        mS[0] = 10.9f;
        mS[1] = -5;
        mS[2] = 1.9f;
        d = bv.getRayDistance(mS, mV);
        assertEquals(5, d.MIN, Geometry.PRECISION);

        // change ray direction so that there is no hit
        mV[0] = 1;
        mV[1] = 0;
        d = bv.getRayDistance(mS, mV);
        assertNull(d);
    }

}
