package com.ninjaghost.flappy;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

import java.security.Key;
import java.util.ArrayList;

public class Bird {

    public ArrayList<Sprite> frames;
    int activeFrame = 0;

    Bird() {
        frames = new ArrayList<Sprite>();
        reload();
    }

    private void reload() {
        // Reload bird frames
        frames.clear();

        for(String file : new String[] {
                "sprites/yellowbird-upflap.png",
                "sprites/yellowbird-midflap.png",
                "sprites/yellowbird-downflap.png"}) {
            frames.add(new Sprite(new Texture(file)));
        }
    }

    public void Update(float deltaTime) {
        if(Gdx.input.isKeyPressed(Input.Keys.I)) {
            System.out.println("I pressed");
        }
    }

    public Rectangle box() {
        return frames.get(activeFrame).getBoundingRectangle();
    }

    public void Render(SpriteBatch spriteBatch) {
        spriteBatch.draw(frames.get(activeFrame), 0, 0);
    }

    public void dispose() {
        for (Sprite frame : frames) {
            frame.getTexture().dispose();
        }
    }

}
