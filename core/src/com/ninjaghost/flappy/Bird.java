package com.ninjaghost.flappy;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.ArrayList;

public class Bird {

    public ArrayList<Texture> frames;
    int activeFrame = 0;

    Bird() {
        frames = new ArrayList<Texture>();
        reload();
    }

    private void reload() {
        // Reload bird frames
        frames.clear();
        frames.add(new Texture("sprites/yellowbird-upflap.png"));
        frames.add(new Texture("sprites/yellowbird-midflap.png"));
        frames.add(new Texture("sprites/yellowbird-downflap.png"));
    }

    public void Update(float deltaTime) {

    }

    public void Render(SpriteBatch spriteBatch) {
        spriteBatch.draw(frames.get(activeFrame), 0, 0);
    }

}
