package com.ninjaghost.flappy;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.ArrayList;

public class Bird {

    public ArrayList<Texture> frameTextures;
    public ArrayList<Sprite> frames;
    int activeFrame = 0;

    Bird() {
        frameTextures = new ArrayList<Texture>();
        frames = new ArrayList<Sprite>();
        reload();
    }

    private void reload() {
        // Reload bird frames
        frameTextures.clear();
        frames.clear();
        frameTextures.add(new Texture("sprites/yellowbird-upflap.png"));
        frameTextures.add(new Texture("sprites/yellowbird-midflap.png"));
        frameTextures.add(new Texture("sprites/yellowbird-downflap.png"));

        frames.add(new Sprite(frameTextures.get(0)));
        frames.add(new Sprite(frameTextures.get(1)));
        frames.add(new Sprite(frameTextures.get(2)));
    }

    public void Update(float deltaTime) {

    }

    public void Render(SpriteBatch spriteBatch) {
        spriteBatch.draw(frames.get(activeFrame), 0, 0);
    }

    public void dispose() {
        for (Texture frame : frameTextures) {
            frame.dispose();
        }
    }

}
