package com.moonymango.snareDemo;

import com.moonymango.snare.audio.LoopResource;
import com.moonymango.snare.audio.SoundResource;
import com.moonymango.snare.res.BaseResource;
import com.moonymango.snare.res.IAssetName;
import com.moonymango.snare.res.data.MeshResource;
import com.moonymango.snare.res.texture.BitmapTextureResource;
import com.moonymango.snare.res.texture.ETC1TextureResource;
import com.moonymango.snare.res.xml.XMLResource;

public enum Asset implements IAssetName {

    GRADIENT_RED__PKM   ("textures/gradient_red.pkm",   null, ETC1TextureResource.class),
    BEAM_TEX            ("textures/beam.png",           null, BitmapTextureResource.class),
    SUN_TEX             ("textures/sun.png",           null, BitmapTextureResource.class),
    NEPTUNE_TEX         ("textures/neptune.png",           null, BitmapTextureResource.class),
    SPACE2_TEX           ("textures/space2.png",           null, BitmapTextureResource.class),
    
    XML_IMAGESET        ("textures/texture_atlas.xml", "texture_atlas", XMLResource.class),

    COURIER             ("fonts/courier.fnt",           null, XMLResource.class),
    BROADWAY            ("fonts/broadway.fnt",          null, XMLResource.class),
    HIGHLIGHT           ("fonts/highlightLET.fnt",      null, XMLResource.class),
    SQUARE              ("fonts/square721.fnt",         null, XMLResource.class),
    IMPACT              ("fonts/impact.fnt",            null, XMLResource.class),
    EMBOSSED            ("fonts/embossed.fnt",          null, XMLResource.class),
    
    CUBE3DS_MESH        ("mesh/cube.3ds",               "Cube",     MeshResource.class),
    SPHERE_MESH         ("mesh/sphere.3ds",             "Sphere",   MeshResource.class),
    SPHERE_UV_MESH         ("mesh/sphere_uv.3ds",             "Sphere",   MeshResource.class),
    MONKEY3DS_MESH      ("mesh/monkey.3ds",             "Monkey",   MeshResource.class),
    SKULL_MESH          ("mesh/skull.3ds",              "Skull",    MeshResource.class),

    BLASTER_SOUND       ("sounds/blaster.mp3",          null, SoundResource.class),
    DRIP_SOUND          ("sounds/drip.ogg",             null, SoundResource.class),

    ;

    private final String mName;
    private final String mQualifier;
    private final Class<? extends BaseResource> mClz;
    private Asset(String name, String qualifier, Class<? extends BaseResource> clz) {
        mName = name;
        mQualifier = qualifier;
        mClz = clz;
    }
    
    @Override
    public String getName() {
        return mName;
        
    }

    @Override
    public Class<? extends BaseResource> getType() {
        return mClz;
    }

    @Override
    public String getQualifier() {
        return mQualifier;
    }

}
