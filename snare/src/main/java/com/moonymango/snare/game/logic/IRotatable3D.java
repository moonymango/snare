package com.moonymango.snare.game.logic;

public interface IRotatable3D {
    IRotatable3D rotate(float vecX, float vecY, float vecZ, float angle);
    IRotatable3D resetRotation();
}
