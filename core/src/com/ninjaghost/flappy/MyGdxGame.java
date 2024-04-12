package com.ninjaghost.flappy;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;

public class MyGdxGame extends ApplicationAdapter {
	SpriteBatch batch;
	Texture img;
	Texture backgroundSprite;
	Texture floorSprite;

	Bird bird;


	@Override
	public void create () {
		batch = new SpriteBatch();
		img = new Texture("badlogic.jpg");
		backgroundSprite = new Texture("sprites/background-day.png");
		floorSprite = new Texture("sprites/base.png");

		bird = new Bird();
	}

	@Override
	public void render () {
		this.update(Gdx.graphics.getDeltaTime());
		ScreenUtils.clear(0, 0, 0.75f, 1);
		batch.begin();
//		batch.draw(img, 0, 0);

		batch.draw(backgroundSprite, 0, 0);
		batch.draw(floorSprite, 0, 0);

		bird.Render(batch);
		batch.end();
	}
	
	@Override
	public void dispose () {
		batch.dispose();
		img.dispose();
	}

	public void update (float delta) {
		bird.Update(delta);
	}

}
