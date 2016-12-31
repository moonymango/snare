package com.moonymango.snare.audio;

/**
 * Plays sound when game object gets attached to the game.
 * Intended for short lived objects like explosion effects.
 */

public class EffectAudioComponent extends AudioComponent {

	public EffectAudioComponent(SoundResource descr) {
        super(descr);
    }

	@Override
	public void onInit() {
		super.onInit();
		play();
	}
	
	
	
}
