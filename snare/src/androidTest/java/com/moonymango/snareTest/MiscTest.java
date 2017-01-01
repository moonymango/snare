package com.moonymango.snareTest;

import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.moonymango.snare.game.Game;
import com.moonymango.snare.opengl.TextureObj.TextureSize;
import com.moonymango.snare.ui.ColorWrapper;
import com.moonymango.snare.util.Bresenham;
import com.moonymango.snare.util.Geometry;
import com.moonymango.snare.util.MatrixAF;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class MiscTest  {

    @Test
    public void testTextureSize() {

        assertTrue(TextureSize.fit(45, 45) == TextureSize.S_64);
        assertTrue(TextureSize.fit(-45, 45) == null);
        assertTrue(TextureSize.fit(1500, 45) == TextureSize.S_2048);
        assertTrue(TextureSize.fit(45, 1500) == TextureSize.S_2048);
        assertTrue(TextureSize.fit(10000, 45) == null);
        assertTrue(TextureSize.max(TextureSize.S_128, TextureSize.S_256) == TextureSize.S_256);
    }

    @Test
    public void testNegPow2() {
        assertEquals(Geometry.negPow2(0), 1, Geometry.PRECISION);
        assertEquals(Geometry.negPow2(1), 0.5f, Geometry.PRECISION);
        assertEquals(Geometry.negPow2(2), 0.25f, Geometry.PRECISION);

    }

    @Test
    public void testBresenham() {
        final int[][] points = new int[10][2];

        int result = Bresenham.line(0, 0, 5, 5, points);
        assertEquals(6, result);

        result = Bresenham.line(1, 7, 4, 0, points);
        assertEquals(8, result);

        result = Bresenham.line(1, -7, 4, 0, points);
        assertEquals(8, result);

        result = Bresenham.line(4, 4, 4, 4, points);
        assertEquals(1, result);

        result = Bresenham.line(4, 4, 5, 5, points);
        assertEquals(2, result);

    }

    @Test
    public void testColorWrapper() {
        final ColorWrapper cw = new ColorWrapper();
        cw.setColorHSV(0, 1, 1, 1);
        float[] c = cw.getActualColor();
        Log.e(Game.ENGINE_NAME, "r " + c[0] + " g " + c[1] + " b " + c[2] + " a " + c[3]);
        cw.setColorHSV(243, 1, 1, 1);
        c = cw.getActualColor();
        Log.e(Game.ENGINE_NAME, "r " + c[0] + " g " + c[1] + " b " + c[2] + " a " + c[3]);

    }

    @Test
    public void testPlaneTransform()
    {
        float[] plane = new float[4];
        float[] mat = new float[16];
        MatrixAF.setIdentityM(mat, 0);
        MatrixAF.translateM(mat, 0, 3, 0, 0);
        //MatrixAF.multiplyMM(sTmpMatA, 0, toWorld, 0, rotation, 0);
        //MatrixAF.scaleM(toWorld, 0, sTmpMatA, 0, scale[0], scale[1], scale[2]);

        plane[0] = 1;
        plane[1] = 0;
        plane[2] = 0;
        plane[3] = 0;

        MatrixAF.multiplyMV(mat, plane);

        plane[0] = 1;
        plane[1] = 0;
        plane[2] = 0;
        plane[3] = 0;

    }
}
