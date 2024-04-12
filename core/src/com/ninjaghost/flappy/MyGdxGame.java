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
import java.util.Random;

/**
 * Plan:
 * 1. ✔️ we should get the infinitely scrolling floor first
 * 2. ✔️ let's infinitely spawn some pipe pairs
 * 3. ✔️ Background needs to infinitely scroll too
 * 4. let's then make the bird fall
 * 5. get some input in to jump the bird up
 * 6. Make things smoother (rotate bird)
 * 7. "Start menu"
 * 8. "Game over"
 * 9. Counter
 */


public class MyGdxGame extends ApplicationAdapter {
	SpriteBatch batch;

	Sprite backgroundSprite;
	Sprite floorSprite;

	Sprite greenPipeHigh;
	Sprite greenPipeLow;

	Bird bird;


	ArrayList<Float> floorOffsets = new ArrayList<>();
	ArrayList<Float> bgOffsets = new ArrayList<>();
	ArrayList<Float[]> pipeOffsets = new ArrayList<>();
	float pipeSpawnTimer = 0;

	@Override
	public void create () {
		batch = new SpriteBatch();

		backgroundSprite = new Sprite(new Texture("sprites/background-day.png"));
		floorSprite = new Sprite(new Texture("sprites/base.png"));
		greenPipeHigh = new Sprite(new Texture("sprites/pipe-green.png")); greenPipeHigh.flip(false, true);
		greenPipeLow = new Sprite(new Texture("sprites/pipe-green.png"));

		for (int i = 0; i < 3; i++) {
			floorOffsets.add(floorSprite.getWidth() * i);
		}

		for (int i = 0; i < 4; i++) {
			bgOffsets.add(backgroundSprite.getWidth() * i);
		}

		bird = new Bird();
	}

	float offset = 0;
	@Override
	public void render () {
		this.update(Gdx.graphics.getDeltaTime());

		ScreenUtils.clear(0, 0, 0.75f, 1);
		batch.begin();

		// Draw the background first
		for (float bgOffset : bgOffsets) {
			batch.draw(backgroundSprite, bgOffset, 0);
		}

		// Draw the ground on top
		for (float floorOffset : floorOffsets) {
			batch.draw(floorSprite, floorOffset, 0);
		}

		// Draw the pipes
		float gapSize = 100f;
		for (Float[] pipeOffset : pipeOffsets) {
			// offset starts by rendering the pipe at the top of the screen (screen height - pipe height)
			// 		we then subtract the floor height which gives us a nice baseline
			float offset = Gdx.graphics.getHeight() - greenPipeHigh.getHeight() + floorSprite.getHeight();

			// Here, we'll add the vertical value provided in this pipeset
			offset += pipeOffset[1];

			batch.draw(greenPipeHigh, pipeOffset[0], offset);

			// the lower pipe simple needs to render the defined gap size lower and then continue on for however high the sprite is
			batch.draw(greenPipeLow, pipeOffset[0], offset - greenPipeLow.getHeight() - gapSize);
		}

		// Draw the bird
		bird.Render(batch);

		// Send it home
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
			for (int i = 0; i < 3; i++) {
				floorOffsets.set(i, floorSprite.getWidth() * i);
			}
		}

		// update bg positions
		bgOffsets.replaceAll(aFloat -> aFloat - 60f * delta);
		if(bgOffsets.get(0) <= -backgroundSprite.getWidth()) {
//			System.out.println("bg cycle");
			for (int i = 0; i < 4; i++) {
				bgOffsets.set(i, backgroundSprite.getWidth() * i);
			}
		}

		// Update pipe positions
		for (int i = 0; i < pipeOffsets.size(); i++) {
			Float[] val = pipeOffsets.get(i);
			val[0] -= 60f * delta;

			// delete if we're off screen
			if(val[0] < -Gdx.graphics.getWidth()) {
				pipeOffsets.remove(pipeOffsets.get(i));
				System.out.println("Deleting pipe at " + val[0].toString() + " pipes left: " + pipeOffsets.size());
				continue;
			}

			pipeOffsets.set(i, val);
		}

		bird.Update(delta);
	}

	private void spawnPipe() {
		float screenWidth = (float) Gdx.graphics.getWidth();

		// the opening can be a randomized value based on some clamps
		float randomFloat = (new Random()).nextFloat();
		float maxLow = -50.0f;
		float maxHigh = 145.0f;
		float randomOpening = maxLow + (maxHigh - (maxLow)) * randomFloat;

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
