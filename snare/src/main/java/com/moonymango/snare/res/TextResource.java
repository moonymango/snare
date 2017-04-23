package com.moonymango.snare.res;

import android.content.res.AssetManager;
import android.content.res.Resources;

import com.moonymango.snare.game.IGame;
import com.moonymango.snare.game.SnareGame;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

public class TextResource extends BaseResource {
    
    public TextResource(IGame game, IAssetName asset)
    {
        super(game, asset);
    }


    public TextResource(IGame game, IResourceName descr) {
        super(game, descr);
    }
    
    
    private TextResHandle buildString(InputStream in) {
        StringBuilder text = new StringBuilder();
        String NL = System.getProperty("line.separator");
        Scanner scanner;
        
        scanner = new Scanner(in);
        try {
            while (scanner.hasNextLine()){
                text.append(scanner.nextLine()).append(NL);
            }
        } finally{
            scanner.close();
        } 
        
        return new TextResHandle(this, text.toString());
    }
    
    
    @Override
    protected BaseResHandle createHandleByResID(Resources res) {
        InputStream in = res.openRawResource(mResID);
        return buildString(in);
    }
    
    @Override
    protected BaseResHandle createHandleByAsset(AssetManager am) {
        InputStream in;
        TextResHandle handle;
        try {
            in = am.open(mName);
            handle = buildString(in);
        } catch (IOException e) {
            return null;
        }
        return handle;
    }

    public TextResHandle getHandle() {
        return (TextResHandle) getHandle(SnareGame.get().getResourceCache());
    }
    
    
    

}
