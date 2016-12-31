package com.moonymango.snare.util;


/** Bresenham line algorithm. */
public class Bresenham 
{    
    /** 
     * Finds coordinates of points to form a line. The coordinates are entered
     * into an array of ints. The caller has to make sure that the array
     * is of sufficient size.
     *    
     * @param x0 x of start point
     * @param y0 y of start point
     * @param x1 x of end point
     * @param y1 y of end point
     * @param points result array.
     * @return Number of points on the line.
     */
    public static int line(int x0, int y0, int x1, int y1, 
            int[][] points) 
    {
        boolean steep = Math.abs(y1 - y0) > Math.abs(x1 - x0);
        final int _x0 = steep ? y0 : x0;
        final int _y0 = steep ? x0 : y0;
        final int _x1 = steep ? y1 : x1;
        final int _y1 = steep ? x1 : y1;
        final int deltax = Math.abs(_x1 - _x0);
        final int deltay = Math.abs(_y1 - _y0);
        final int xstep = _x0 < _x1 ? 1 : -1;
        final int ystep = _y0 < _y1 ? 1 : -1; 
        int error = deltax / 2;
        int y = _y0;
        int x = _x0;
        
        for (int i = 0; i < deltax; i++)
        {
            points[i][0] = steep ? y : x;
            points[i][1] = steep ? x : y;             
            error -= deltay;
            if (error < 0) 
            {
                y += ystep;
                error += deltax;
            }
            x += xstep;
        }
        
        // also add target point
        points[deltax][0] = steep ? _y1 : _x1;
        points[deltax][1] = steep ? _x1 : _y1;
        return deltax+1;
    }
}
