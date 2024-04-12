package com.ninjaghost.flappy;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.sun.org.slf4j.internal.Logger;
import com.sun.tools.javac.util.Pair;

import java.util.ArrayList;

/**
 * Plan:
 * 1. ✔️ we should get the infinitely scrolling floor first
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


	ArrayList<Float> floorOffsets = new ArrayList<>();
	ArrayList<Float[]> pipeOffsets = new ArrayList<>();
	float pipeSpawnTimer = 0;

	@Override
	public void create () {
		batch = new SpriteBatch();

		backgroundSprite = new Sprite(new Texture("sprites/background-day.png"));
		floorSprite = new Sprite(new Texture("sprites/base.png"));
		greenPipeHigh = new Sprite(new Texture("sprites/pipe-green.png")); greenPipeHigh.flip(false, true);
		greenPipeLow = new Sprite(new Texture("sprites/pipe-green.png"));

		floorOffsets.add(0f);
		floorOffsets.add(floorSprite.getWidth());
		floorOffsets.add(floorSprite.getWidth() * 2);

		bird = new Bird();
	}

	float offset = 0;
	@Override
	public void render () {
		this.update(Gdx.graphics.getDeltaTime());

		ScreenUtils.clear(0, 0, 0.75f, 1);
		batch.begin();

		batch.draw(backgroundSprite, 0, 0);

		for (float floorOffset : floorOffsets) {
			batch.draw(floorSprite, floorOffset, 0);
		}

		float gapSize = 35f;
		for (Float[] pipeOffset : pipeOffsets) {
			batch.draw(greenPipeHigh, pipeOffset[0], Gdx.graphics.getHeight() - greenPipeHigh.getHeight());
			batch.draw(greenPipeLow, pipeOffset[0], 0);
		}

//		batch.draw(greenPipeHigh, 60f, Gdx.graphics.getHeight() - greenPipeHigh.getHeight());

		bird.Render(batch);

		batch.end();
	}

	public void update (float delta) {
		pipeSpawnTimer += delta;

		if(pipeSpawnTimer > 5) {
			spawnPipe();
			pipeSpawnTimer = 0;
		}

		// update floor position
        floorOffsets.replaceAll(aFloat -> aFloat - 60f * delta);
		if(floorOffsets.get(0) <= -floorSprite.getWidth()) {
//			System.out.println("floor cycle");
			floorOffsets.set(0, 0f);
			floorOffsets.set(1, floorSprite.getWidth());
			floorOffsets.set(2, floorSprite.getWidth() * 2);
		}

		// Update pipe positions
		for (int i = 0; i < pipeOffsets.size(); i++) {
			Float[] val = pipeOffsets.get(i);
			val[0] -= 60f * delta;
			pipeOffsets.set(i, val);
		}

		bird.Update(delta);
	}

	private void spawnPipe() {
		float screenWidth = (float) Gdx.graphics.getWidth();
		float randomOpening = (float) Gdx.graphics.getHeight() / 2;
		pipeOffsets.add(new Float[] { screenWidth, randomOpening });
		System.out.println("spawn pipe");
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
