package com.moonymango.snare.res;

public interface IAssetName {

    String getName();
    String getQualifier();
    Class<? extends BaseResource> getType();
}
