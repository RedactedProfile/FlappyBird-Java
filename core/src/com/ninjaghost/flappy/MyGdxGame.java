package com.ninjaghost.flappy;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;

public class MyGdxGame extends ApplicationAdapter {
	SpriteBatch batch;

	Sprite backgroundSprite;
	Sprite floorSprite;

	Sprite greenPipeHigh;
	Sprite greenPipeLow;

	Bird bird;


	@Override
	public void create () {
		batch = new SpriteBatch();

		backgroundSprite = new Sprite(new Texture("sprites/background-day.png"));
		floorSprite = new Sprite(new Texture("sprites/base.png"));
		greenPipeHigh = new Sprite(new Texture("sprites/pipe-green.png"));
		greenPipeLow = new Sprite(new Texture("sprites/pipe-green.png")); greenPipeLow.flip(false, true);

		bird = new Bird();
	}

	@Override
	public void render () {
		this.update(Gdx.graphics.getDeltaTime());
		ScreenUtils.clear(0, 0, 0.75f, 1);
		batch.begin();

		batch.draw(backgroundSprite, 0, 0);
		batch.draw(floorSprite, 0, 0);

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
