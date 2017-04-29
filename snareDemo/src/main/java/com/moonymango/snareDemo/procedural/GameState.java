package com.moonymango.snareDemo.procedural;

import com.moonymango.snare.events.EventManager.IEventListener;
import com.moonymango.snare.events.IEvent;
import com.moonymango.snare.events.IWidgetTouchedEndEvent;
import com.moonymango.snare.game.BaseSnareClass;
import com.moonymango.snare.game.IGame;
import com.moonymango.snare.game.IGameState;
import com.moonymango.snare.game.IGameStateLogic;
import com.moonymango.snare.opengl.TextureObj.TextureSize;
import com.moonymango.snare.opengl.TextureObjOptions;
import com.moonymango.snare.res.texture.BitmapTextureResource;
import com.moonymango.snare.res.texture.GradientGenerator;
import com.moonymango.snare.res.texture.GradientGenerator.ColorSamples;
import com.moonymango.snare.res.texture.NoiseGenerator;
import com.moonymango.snare.res.texture.NoiseGenerator.NoiseFunction;
import com.moonymango.snare.res.texture.ShapeGenerator;
import com.moonymango.snare.res.texture.ShapeGenerator.Shape;
import com.moonymango.snare.ui.BaseFont;
import com.moonymango.snare.ui.PlayerGameView;
import com.moonymango.snare.ui.widgets.BaseTouchWidget;
import com.moonymango.snare.ui.widgets.BaseTouchWidget.TouchSetting;
import com.moonymango.snare.ui.widgets.BaseWidget.PositionAlignment;
import com.moonymango.snare.ui.widgets.Rectangle;
import com.moonymango.snare.ui.widgets.Text;

class GameState extends BaseSnareClass implements IGameState, IGameStateLogic, IEventListener {
    
    private float mX;
    private Rectangle mShape;
    private Rectangle mGradient;
    private Rectangle mPlain;
    private Rectangle mPerlin;
    private final ColorSamples mSamples = new ColorSamples(3, 3);
   
    public GameState(IGame game)
    {
        super(game);
        // initialize color samples: blue column in middle of texture
        mSamples.setAll(1, 1, 1, 1)
            .setColumn(1, 0, 0, 1, 1);
    }
    
    @Override
    public IGameState onUpdate(long realTime, float realDelta,
            float virtualDelta) {
        // move texture slowly
        mX += realDelta * 0.00001f;
        if (mGradient != null) {
           mGradient.setTextureOffset(mX, mX);
        }
        return null;
    }

    @Override
    public void onActivate(IGameState previous) {
        genShape(true);
        gen2DGradient(true);
        genPlain(true);
        genPerlin(true);
        mGame.showToast("touch to re-generate textures");

        mGame.getEventManager().addListener(IWidgetTouchedEndEvent.EVENT_TYPE, this);
    }

    @Override
    public void onDeactivate(IGameState next) {}

    @Override
    public void onInit() {}

    @Override
    public void onShutdown() {}

    @Override
    public IGameStateLogic getGameStateLogic() {return this;}

    @Override
    public boolean equals(IGameState state) {return state == this;}

    public String getName() {return GameState.class.getName();}

    @Override
	public boolean handleEvent(IEvent event) {
		final IWidgetTouchedEndEvent e = (IWidgetTouchedEndEvent) event;
		final BaseTouchWidget widget = e.getWidget();
    	
        final PlayerGameView v = mGame.getPrimaryView();
        if (widget == mShape) {
            v.removeScreenElement(mShape);
            mShape = null;
            genShape(false);
        }
        if (widget == mGradient) {
            v.removeScreenElement(mGradient);
            mGradient = null;
            randomColors();
            gen2DGradient(false);
        }
        if (widget == mPlain) {
            v.removeScreenElement(mPlain);
            mPlain = null;
            genPlain(false);
        }
        if (widget == mPerlin) {
            v.removeScreenElement(mPerlin);
            mPerlin = null;
            genPerlin(false);
        }     
        
        return true;
    }
    
    private void genShape(boolean text) {
        final PlayerGameView v = mGame.getPrimaryView();
        final BaseFont font = mGame.getSystemFont();
        int w = (int) (v.getScreenWidth() * 0.5f);
        int h = (int) (v.getScreenHeight() * 0.4f);
        int textSize = (int) (v.getScreenHeight() * 0.025f);
        
        final float f0 = mGame.getRandomFloat(0, 1);
        final float f1 = mGame.getRandomFloat(0, 1);
        ShapeGenerator gs = new ShapeGenerator(Shape.CIRCLE, f0, f0>f1);
        
        BitmapTextureResource bm = new BitmapTextureResource(mGame, "shape"
                + mGame.getRandomString(), TextureSize.S_32, gs, gs, gs);
        mShape = new Rectangle(mGame, bm, TextureObjOptions.NEAREST_MIRRORED_REPEAT,
        		TouchSetting.TOUCHABLE);
        mShape.setAnimation(null)
            .setPosition(0, (int) (v.getScreenHeight() * 0.75f))
            .setPositionAlignment(PositionAlignment.LEFT_X_CENTERED_Y)
            .setSize(w, h);         
        v.pushScreenElement(mShape); 
        
        if (text) {
            Text t = new Text(font, "Circular shape, 32x32 pixel, GL_NEAREST", 
                    null);
            t.setTextSize(textSize)
                .setPositionAlignment(PositionAlignment.LEFT_X_BOTTOM_Y)
                .setPosition(0, (int) (v.getScreenHeight() * 0.5f));
            v.pushScreenElement(t);
        }
    }
    
    private void gen2DGradient(boolean text) {
        final PlayerGameView v = mGame.getPrimaryView();
        final BaseFont font = mGame.getSystemFont();
        int w = (int) (v.getScreenWidth() * 0.5f);
        int h = (int) (v.getScreenHeight() * 0.4f);
        int textSize = (int) (v.getScreenHeight() * 0.025f);
        
        // GradientGenerator can handle all channels at once, so 
        // we use this one as source for all channels
        GradientGenerator gg = new GradientGenerator(mSamples);
        
        // choose random name, so that we do not get the same texture from
        // the resource cache when reloading
        BitmapTextureResource bm = new BitmapTextureResource(mGame, "gradient"
                + mGame.getRandomString(), TextureSize.S_32, gg, gg, gg);
        
        mGradient = new Rectangle(mGame, bm,
                TextureObjOptions.LINEAR_MIRRORED_REPEAT, TouchSetting.TOUCHABLE);
        int y = (int) (v.getScreenHeight() * 0.25f);
        mGradient.setAnimation(null)
            .setPosition(0, y)
            .setPositionAlignment(PositionAlignment.LEFT_X_CENTERED_Y)
            .setSize(w, h); 
        v.pushScreenElement(mGradient);
        
        if (text) {
            Text t = new Text(font, "Color gradient, 32x32 pixel, GL_LINEAR, GL_MIRRORED_REPEAT", 
                    null);
            t.setTextSize(textSize)
                .setPositionAlignment(PositionAlignment.LEFT_X_BOTTOM_Y)
                .setPosition(0, 0);
            v.pushScreenElement(t);
        }
    }
    
    private void genPlain(boolean text)
    {
        final PlayerGameView v = mGame.getPrimaryView();
        final BaseFont font = mGame.getSystemFont();
        int w = (int) (v.getScreenWidth() * 0.5f);
        int h = (int) (v.getScreenHeight() * 0.4f);
        int textSize = (int) (v.getScreenHeight() * 0.025f);
       
        final float nx = mGame.getRandomFloat();
        final float ny = mGame.getRandomFloat();
        final float nw = mGame.getRandomFloat(1, 30);
        
        // use same generator for each color channel results in monochromatic
        // bitmap
        NoiseGenerator gr = new NoiseGenerator(NoiseFunction.PLAIN, 6, 
                nx, ny, nw, nw / v.getScreenRatio());
        BitmapTextureResource bm = new BitmapTextureResource(mGame, "noise"
                    + mGame.getRandomString(), TextureSize.S_128,
                    gr,gr, gr);
        
        mPlain = new Rectangle(mGame, bm, TextureObjOptions.LINEAR_MIRRORED_REPEAT,
                TouchSetting.TOUCHABLE);
        int x = (int) (v.getScreenWidth() * 0.5f);
        int y = (int) (v.getScreenHeight() * 0.75f);
        mPlain.setAnimation(null)
            .setPosition(x, y)
            .setPositionAlignment(PositionAlignment.LEFT_X_CENTERED_Y)
            .setSize(w, h); 
        v.pushScreenElement(mPlain);
        
        if (text) {
            Text t = new Text(font, "Simplex noise, 128x128 pixel, GL_LINEAR", 
                    null);
            t.setTextSize(textSize)
                .setPositionAlignment(PositionAlignment.LEFT_X_BOTTOM_Y)
                .setPosition(x, (int) (v.getScreenHeight() * 0.5f));
            v.pushScreenElement(t);
        }
    }
    
    private void genPerlin(boolean text) { 
        final PlayerGameView v = mGame.getPrimaryView();
        final BaseFont font = mGame.getSystemFont();
        int w = (int) (v.getScreenWidth() * 0.5f);
        int h = (int) (v.getScreenHeight() * 0.4f);
        int textSize = (int) (v.getScreenHeight() * 0.025f);
        
        final float nx = mGame.getRandomFloat();
        final float ny = mGame.getRandomFloat();
        final float nw = mGame.getRandomFloat(1, 10);
        
        // just specify generator for red channel to save computation time
        NoiseGenerator gr = new NoiseGenerator(NoiseFunction.PERLIN_C, 6, 
                nx, ny, nw, nw / v.getScreenRatio());
        BitmapTextureResource bm = new BitmapTextureResource(mGame, "noise"
                    + mGame.getRandomString(), TextureSize.S_128, gr,
                    null, null);
        
        mPerlin = new Rectangle(mGame, bm, TextureObjOptions.LINEAR_MIRRORED_REPEAT,
                TouchSetting.TOUCHABLE);
        int x = (int) (v.getScreenWidth() * 0.5f);
        int y = (int) (v.getScreenHeight() * 0.25f);
        mPerlin.setAnimation(null)
            .setPosition(x, y)
            .setPositionAlignment(PositionAlignment.LEFT_X_CENTERED_Y)
            .setSize(w, h); 
        v.pushScreenElement(mPerlin);
        
        if (text) {
            Text t = new Text(font, "Perlin fractal noise (6 octaves), 128x128 pixel, GL_LINEAR", 
                    null);
            t.setTextSize(textSize)
                .setPositionAlignment(PositionAlignment.LEFT_X_BOTTOM_Y)
                .setPosition(x, 0);
            v.pushScreenElement(t);
        }
    }
    
    private void randomColors() {
        final float[] c = new float[4];
        for (int x = 0; x < mSamples.getWidth(); x++) {
            for (int y = 0; y < mSamples.getHeight(); y++) {
                for (int i = 0; i < 4; i++) {
                    c[i] = mGame.getRandomFloat(0, 1);
                }
                mSamples.set(x, y, c);
            }
        }
    }
}
