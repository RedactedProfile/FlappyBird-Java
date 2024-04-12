package com.ninjaghost.flappy;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;

import java.util.ArrayList;

/**
 * Plan:
 * 1. we should get the infinitely scrolling floor first
 * 2. let's infinitely spawn some pipe pairs
 * 3. let's then make the bird fall
 * 4. get some input in to jump the bird up
 * 5. Make things smoother (rotate bird)
 * 6. "Start menu"
 * 7. "Game over"
 * 8. Counter
 */


public class MyGdxGame extends ApplicationAdapter {
	SpriteBatch batch;

	Sprite backgroundSprite;
	Sprite floorSprite;

	Sprite greenPipeHigh;
	Sprite greenPipeLow;

	Bird bird;


	ArrayList<Integer> floorOffsets = new ArrayList<>();

	@Override
	public void create () {
		batch = new SpriteBatch();

		backgroundSprite = new Sprite(new Texture("sprites/background-day.png"));
		floorSprite = new Sprite(new Texture("sprites/base.png"));
		greenPipeHigh = new Sprite(new Texture("sprites/pipe-green.png"));
		greenPipeLow = new Sprite(new Texture("sprites/pipe-green.png")); greenPipeLow.flip(false, true);

		floorOffsets.add(0);
		floorOffsets.add((int)floorSprite.getWidth());
		floorOffsets.add((int)floorSprite.getWidth() * 2);

		bird = new Bird();
	}

	@Override
	public void render () {
		this.update(Gdx.graphics.getDeltaTime());
		ScreenUtils.clear(0, 0, 0.75f, 1);
		batch.begin();

		batch.draw(backgroundSprite, 0, 0);

		for (int floorOffset : floorOffsets) {
			batch.draw(floorSprite, floorOffset, 0);
		}


		batch.draw(greenPipeHigh, 0, 0);
		batch.draw(greenPipeLow, 0, 0);

		bird.Render(batch);

		batch.end();
	}

	public void update (float delta) {
		bird.Update(delta);
	}
	
	@Override
	public void dispose () {
		batch.dispose();
		bird.dispose();
		backgroundSprite.getTexture().dispose();
		floorSprite.getTexture().dispose();
		greenPipeHigh.getTexture().dispose();
		greenPipeLow.getTexture().dispose();
	}



}
