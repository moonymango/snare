package com.moonymango.snareDemo.text;

import com.moonymango.snare.events.EventManager.IEventListener;
import com.moonymango.snare.events.IEvent;
import com.moonymango.snare.events.ITouchEvent;
import com.moonymango.snare.game.BaseSnareClass;
import com.moonymango.snare.game.IGame;
import com.moonymango.snare.game.IGameState;
import com.moonymango.snare.game.IGameStateLogic;
import com.moonymango.snare.res.xml.BMFont;
import com.moonymango.snare.res.xml.BMFontXMLHandler;
import com.moonymango.snare.res.xml.XMLResHandle;
import com.moonymango.snare.res.xml.XMLResource;
import com.moonymango.snare.ui.PlayerGameView;
import com.moonymango.snare.ui.TouchAction;
import com.moonymango.snare.ui.widgets.Text;
import com.moonymango.snareDemo.Asset;

class GameState extends BaseSnareClass implements IGameState, IGameStateLogic,
        IEventListener {

    @SuppressWarnings({"unchecked"})
    private final XMLResource<BMFont> mFontRes[] = new XMLResource[5];
    @SuppressWarnings({"unchecked"})
    private final XMLResHandle<BMFont> mFontHnd[] = new XMLResHandle[5];
    
    private static final String string = "Woe to you, Oh Earth and Sea, \n" +
                                         "for the Devil sends the beast with wrath, \n" +
                                         "because he knows the time is short... \n" +
                                         "Let him who hath understanding reckon \n" +
                                         "the number of the beast for it is a human number, \n" +
                                         "it's number is Six hundred and sixty six \n";


    public GameState(IGame game)
    {
        super(game);
    }

    @Override
    public boolean handleEvent(IEvent event) {
        final ITouchEvent e = (ITouchEvent) event;
        if (!e.getTouchAction().equals(TouchAction.DOWN)) 
            return false;
        
        //final int angle = mGame.getRandomInt(-90, 90);
        //final int size = mGame.getRandomInt(10, 40);
        //final int idx = mGame.getRandomInt(0, 4);
        final int idx = 4;
        final int size = 30;
        final int angle = 5;
        
        final PlayerGameView v = mGame.getPrimaryView();        
        Text text = new Text(mFontHnd[idx].getContent(), string, null);
        text.setTextSize(size);
        text.setOutlineColor(0, 0, 1, 1).setColor(1, 0, 0, 1);
        
        text.setPosition(e.getTouchX(), e.getTouchY());
        text.setAngle(angle);
        v.pushScreenElement(text);
        
        return false;
    }

    @Override
    public IGameState onUpdate(long realTime, float realDelta,
            float virtualDelta) {
        return null;
    }

    @Override
    public void onActivate(IGameState previous) {
        mFontRes[0] = new XMLResource<BMFont>(Asset.HIGHLIGHT, new BMFontXMLHandler(mGame));
        mFontRes[1] = new XMLResource<BMFont>(Asset.BROADWAY, new BMFontXMLHandler(mGame));
        mFontRes[2] = new XMLResource<BMFont>(Asset.COURIER, new BMFontXMLHandler(mGame));
        mFontRes[3] = new XMLResource<BMFont>(Asset.SQUARE, new BMFontXMLHandler(mGame));
        mFontRes[4] = new XMLResource<BMFont>(Asset.EMBOSSED, new BMFontXMLHandler(mGame));
        
        for (int i = 0; i < 5; i++) {
            mFontHnd[i] = mFontRes[i].getHandle();
        }
        
        mGame.getEventManager().addListener(ITouchEvent.EVENT_TYPE, this);
        mGame.showToast("touch to add text");
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
}
