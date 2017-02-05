package com.moonymango.snareDemo.widgets;

import com.moonymango.snare.audio.SoundHandle;
import com.moonymango.snare.audio.SoundResource;
import com.moonymango.snareDemo.Asset;
import com.moonymango.snare.events.EventManager.IEventListener;
import com.moonymango.snare.events.IEvent;
import com.moonymango.snare.events.IWidgetTouchedBeginEvent;
import com.moonymango.snare.game.Game;
import com.moonymango.snare.game.IGameState;
import com.moonymango.snare.game.IGameStateLogic;
import com.moonymango.snare.res.texture.BaseTextureResource;
import com.moonymango.snare.res.xml.CEGUIImageSetXMLHandler;
import com.moonymango.snare.res.xml.XMLResHandle;
import com.moonymango.snare.res.xml.XMLResource;
import com.moonymango.snare.ui.PlayerGameView;
import com.moonymango.snare.ui.widgets.BaseTouchWidget.TouchSetting;
import com.moonymango.snare.ui.widgets.Rectangle;
import com.moonymango.snare.ui.widgets.WidgetMotionModifier;
import com.moonymango.snare.ui.widgets.WidgetMotionModifier.MotionSettings;
import com.moonymango.snare.ui.widgets.WidgetRotationModifier;
import com.moonymango.snare.util.EasingProfile;

class GameState implements IGameState, IGameStateLogic, 
        IEventListener {

    private SoundResource mSoundRes;
    private SoundHandle mSoundHandle;
    
    @Override
    public IGameState onUpdate(long realTime, float realDelta,
            float virtualDelta) {
        return null;
    }

    @Override
    public void onActivate(IGameState previous) {
        mSoundRes = new SoundResource(Asset.DRIP_SOUND);
        mSoundHandle = mSoundRes.getHandle();
        
        // just add 6 widgets
        XMLResource<BaseTextureResource> xmlRes = new XMLResource<BaseTextureResource>
                (Asset.XML_IMAGESET, new CEGUIImageSetXMLHandler());
        XMLResHandle<BaseTextureResource> xmlHnd = xmlRes.getHandle();
        BaseTextureResource imageSet = xmlHnd.getContent();
        xmlRes.releaseHandle(xmlHnd);
               
        // static button
        final PlayerGameView v = Game.get().getPrimaryView();
        Rectangle r = new Rectangle(imageSet, TouchSetting.TOUCHABLE);
        int x = (int) (v.getScreenWidth() * 0.2f);
        int y = (int) (v.getScreenHeight() * 0.3f);
        r.setPosition(x, y);
        r.setSize(200, 200); 
        r.setTextureRegion("left");
        v.pushScreenElement(r); 
         
        // rotating button
        r = new Rectangle(imageSet, TouchSetting.TOUCHABLE);
        x = (int) (v.getScreenWidth() * 0.5f);
        y = (int) (v.getScreenHeight() * 0.3f);
        r.setPosition(x, y);
        r.setSize(200, 200); 
        r.setTextureRegion("left");
        v.pushScreenElement(r);
        
        WidgetRotationModifier m = new WidgetRotationModifier(r, 40, 0, null);
        m.run();

        // moving button
        final int startX = (int) (v.getScreenWidth() * 0.2f);
        final int targetX = (int) (v.getScreenWidth() * 0.8f);
        r = new Rectangle(imageSet, TouchSetting.TOUCHABLE);
        y = (int) (v.getScreenHeight() * 0.7f);
        r.setPosition(startX, y);
        r.setSize(200, 200); 
        r.setTextureRegion("left");
        v.pushScreenElement(r);
        
        MotionSettings s = new MotionSettings();
        s.widget = r;
        s.duration = 5000;
        s.startFromCurrentPos = false;
        s.startX = startX;
        s.startY = y;
        s.targetX = targetX;
        s.targetY = y;
        s.profile = EasingProfile.BOUNCE;
        WidgetMotionModifier toRight = new WidgetMotionModifier(s);
        s.startX = targetX;
        s.targetX = startX;
        WidgetMotionModifier toLeft = new WidgetMotionModifier(s);
        // make each other's successor to create endless animation loop
        toLeft.setNext(toRight);
        toRight.setNext(toLeft);
        toLeft.run();
        
        // moving and rotating button
        final int startY = (int) (v.getScreenHeight() * 0.3f);
        final int targetY = (int) (v.getScreenHeight() * 0.7f);
        r = new Rectangle(imageSet, TouchSetting.TOUCHABLE);
        x = (int) (v.getScreenWidth() * 0.8f);
        r.setPosition(x, startY);
        r.setSize(200, 200); 
        r.setTextureRegion("left");
        v.pushScreenElement(r);
        
        s.widget = r;
        s.duration = 1000;
        s.startX = x;
        s.startY = startY;
        s.targetX = x;
        s.targetY = targetY;
        s.profile = EasingProfile.SIN_IN;
        WidgetMotionModifier toTop = new WidgetMotionModifier(s);
        s.startY = targetY;
        s.targetY = startY;
        WidgetMotionModifier toBottom = new WidgetMotionModifier(s);
        toBottom.setNext(toTop);
        toTop.setNext(toBottom);
        toBottom.run();
        m = new WidgetRotationModifier(r, 60, 0, null);
        m.run();
         
        Game.get().showToast("touch widgets to change texture region");
        Game.get().getEventManager().addListener(IWidgetTouchedBeginEvent.EVENT_TYPE, this);
    }

    @Override
    public void onDeactivate(IGameState next) {
            
    }

    @Override
    public void onInit() {
        
    }

    @Override
    public void onShutdown() {
        
    }
    

    @Override
    public IGameStateLogic getGameStateLogic() {
        return this;
    }

    @Override
    public boolean equals(IGameState state) {
        return state == this;
    }

    public String getName() {
        return GameState.class.getName();
    }
    
    
    @Override
	public boolean handleEvent(IEvent event) {
		final IWidgetTouchedBeginEvent e = (IWidgetTouchedBeginEvent) event;
		final Rectangle r = (Rectangle) e.getWidget();
    	
        mSoundHandle.play();
        if (r.getTextureRegionName().equals("left")) {
            r.setTextureRegion("right");
        } else {
            r.setTextureRegion("left");
        }
        
        return true;
    }
    
}
