package com.moonymango.snare.res;

public interface IResourceName {

    int getID();
    String getName();
    String getQualifier();
    Class<? extends BaseResource> getType();
}
