package com.ninjaghost.flappy;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;

public class MyGdxGame extends ApplicationAdapter {
	SpriteBatch batch;

	Texture background;
	Texture floor;
	Sprite backgroundSprite;
	Sprite floorSprite;

	Texture greenPipe;
	Sprite greenPipeHigh;
	Sprite greenPipeLow;

	Bird bird;


	@Override
	public void create () {
		batch = new SpriteBatch();

		background = new Texture("sprites/background-day.png");
		backgroundSprite = new Sprite(background);
		floor = new Texture("sprites/base.png");
		floorSprite = new Sprite(floor);
		greenPipe = new Texture("sprites/pipe-green.png");
		greenPipeHigh = new Sprite(greenPipe);
		greenPipeLow = new Sprite(greenPipe); greenPipeLow.flip(false, true);

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
		background.dispose();
		floor.dispose();
		greenPipe.dispose();
	}



}
