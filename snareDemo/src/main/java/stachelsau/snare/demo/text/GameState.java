package stachelsau.snare.demo.text;

import stachelsau.snare.demo.Asset;
import stachelsau.snare.events.EventManager.IEventListener;
import stachelsau.snare.events.IEvent;
import stachelsau.snare.events.ITouchEvent;
import stachelsau.snare.game.Game;
import stachelsau.snare.game.IGameState;
import stachelsau.snare.game.IGameStateLogic;
import stachelsau.snare.res.xml.BMFont;
import stachelsau.snare.res.xml.BMFontXMLHandler;
import stachelsau.snare.res.xml.XMLResHandle;
import stachelsau.snare.res.xml.XMLResource;
import stachelsau.snare.ui.PlayerGameView;
import stachelsau.snare.ui.TouchAction;
import stachelsau.snare.ui.widgets.Text;

class GameState implements IGameState, IGameStateLogic,
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
    
    
    @Override
    public boolean handleEvent(IEvent event) {
        final ITouchEvent e = (ITouchEvent) event;
        if (!e.getTouchAction().equals(TouchAction.DOWN)) 
            return false;
        
        //final int angle = Game.get().getRandomInt(-90, 90);
        //final int size = Game.get().getRandomInt(10, 40);
        //final int idx = Game.get().getRandomInt(0, 4);
        final int idx = 4;
        final int size = 30;
        final int angle = 5;
        
        final PlayerGameView v = Game.get().getPrimaryView();        
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
        mFontRes[0] = new XMLResource<BMFont>(Asset.HIGHLIGHT, new BMFontXMLHandler());
        mFontRes[1] = new XMLResource<BMFont>(Asset.BROADWAY, new BMFontXMLHandler());
        mFontRes[2] = new XMLResource<BMFont>(Asset.COURIER, new BMFontXMLHandler());
        mFontRes[3] = new XMLResource<BMFont>(Asset.SQUARE, new BMFontXMLHandler());
        mFontRes[4] = new XMLResource<BMFont>(Asset.EMBOSSED, new BMFontXMLHandler());
        
        for (int i = 0; i < 5; i++) {
            mFontHnd[i] = mFontRes[i].getHandle();
        }
        
        Game.get().getEventManager().addListener(ITouchEvent.EVENT_TYPE, this);
        Game.get().showToast("touch to add text");
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
    public void setNextState(IGameState next) {
                
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
