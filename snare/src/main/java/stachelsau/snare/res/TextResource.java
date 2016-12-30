package stachelsau.snare.res;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

import stachelsau.snare.game.Game;

import android.content.res.AssetManager;
import android.content.res.Resources;

public class TextResource extends BaseResource {
    
    public TextResource(IAssetName asset) {
        super(asset);
    }


    public TextResource(IResourceName descr) {
        super(descr);
    }
    
    
    private TextResHandle buildString(InputStream in) {
        StringBuilder text = new StringBuilder();
        String NL = System.getProperty("line.separator");
        Scanner scanner;
        
        scanner = new Scanner(in);
        try {
            while (scanner.hasNextLine()){
                text.append(scanner.nextLine() + NL);
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
        return (TextResHandle) getHandle(Game.get().getResourceCache());
    }
    
    
    

}
