package com.ninjaghost.flappy;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.utils.ScreenUtils;
import java.util.ArrayList;
import java.util.Random;

import static jdk.internal.org.jline.terminal.spi.TerminalProvider.Stream.Input;

/**
 * Plan:
 * ✔️ we should get the infinitely scrolling floor first
 * ✔️ let's infinitely spawn some pipe pairs
 * ✔️ Background needs to infinitely scroll too
 * ✔️ Bird debug move around mode
 * ✔️ collision detection for death state
 * ✔️ let's then make the bird fall
 * get some input in to jump the bird up
 * Make things smoother (rotate bird)
 * "Start menu"
 * "Game over"
 * Counter
 */


public class MyGdxGame extends ApplicationAdapter implements InputProcessor {
	SpriteBatch batch;

	Sprite backgroundSprite;
	Sprite floorSprite;

	Sprite greenPipeHigh;
	Sprite greenPipeLow;

	Bird bird;


	ArrayList<Rectangle> deathBoxes = new ArrayList<>();
	ArrayList<Float> floorOffsets = new ArrayList<>();
	ArrayList<Float> bgOffsets = new ArrayList<>();
	ArrayList<Float[]> pipeOffsets = new ArrayList<>();
	float pipeSpawnTimer = 0;
	float pipeGapSize = 100f;

	Sprite player;
	float playerVelocity = 0;
	float getPlayerVelocityFactor = 980;


	boolean cheat_freemove = true; // disables gravity, enables WASD movement
	boolean cheat_noclip = false; // when enabled, disables collision
	boolean cheat_drawboxes = true; // when enabled draws the bounding boxes

	// input state
	boolean iUp = false;
	boolean iDown = false;
	boolean iLeft = false;
	boolean iRight = false;

	@Override
	public void create () {
		Gdx.input.setInputProcessor(this);
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

		player = new Sprite(new Texture("sprites/yellowbird-midflap.png"));
		player.setPosition(25f, Gdx.graphics.getHeight() / 2);
	}

	@Override
	public void render () {
		this.update(Gdx.graphics.getDeltaTime());

		ScreenUtils.clear(0, 0, 0.75f, 1);
		batch.begin();

		// Draw the background first
		for (float bgOffset : bgOffsets) {
			batch.draw(backgroundSprite, bgOffset, 0);
		};

		// Draw the ground on top
		for (float floorOffset : floorOffsets) {
			batch.draw(floorSprite, floorOffset, 0);
		}

		// Draw the pipes
		for (Float[] pipeOffset : pipeOffsets) {
			// offset starts by rendering the pipe at the top of the screen (screen height - pipe height)
			// 		we then subtract the floor height which gives us a nice baseline
			float offset = Gdx.graphics.getHeight() - greenPipeHigh.getHeight() + floorSprite.getHeight();

			// Here, we'll add the vertical value provided in this pipeset
			offset += pipeOffset[1];

			batch.draw(greenPipeHigh, pipeOffset[0], offset);

			// the lower pipe simple needs to render the defined gap size lower and then continue on for however high the sprite is
			batch.draw(greenPipeLow, pipeOffset[0], offset - greenPipeLow.getHeight() - pipeGapSize);
		}

		// Draw the bird
//		bird.Render(batch);
		batch.draw(player, player.getX(), player.getY());

		// Send it home
		batch.end();


		if(cheat_drawboxes) {
			// DrawBoxes mode, outline in red all the deathboxes in the scene.
			// Requires a special drawing mode so the previous SpriteBatch needs to be ended first
			ShapeRenderer shapeRenderer = new ShapeRenderer();
			shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
			shapeRenderer.setColor(Color.RED);
			for (Rectangle bbox : deathBoxes) {
				shapeRenderer.rect(bbox.x, bbox.y, bbox.width, bbox.height);
			}
			shapeRenderer.end();
		}
	}

	public void update (float delta) {
		deathBoxes.clear(); // empty the death boxes

		pipeSpawnTimer += delta;

		if(pipeSpawnTimer > 5) {
			spawnPipe();
			pipeSpawnTimer = 0;
		}

		// update floor position
        floorOffsets.replaceAll(aFloat -> aFloat - 60f * delta);
		if(floorOffsets.get(0) <= -floorSprite.getWidth()) {
			for (int i = 0; i < 3; i++) {
				floorOffsets.set(i, floorSprite.getWidth() * i);
			}
		}
		// add generic rectangle for floor death
		deathBoxes.add((new Rectangle()).set(0, 0, floorSprite.getWidth(), floorSprite.getHeight()));

		// add generic rectangle for ceiling death
		deathBoxes.add((new Rectangle()).set(0, Gdx.graphics.getHeight(), floorSprite.getWidth(), floorSprite.getHeight()));


		// update bg positions
		bgOffsets.replaceAll(aFloat -> aFloat - 60f * delta);
		if(bgOffsets.get(0) <= -backgroundSprite.getWidth()) {
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

			// update the pipe offsets
			pipeOffsets.set(i, val);

			// add death boxes for pipes
			// offset starts by rendering the pipe at the top of the screen (screen height - pipe height)
			// 		we then subtract the floor height which gives us a nice baseline
			float offset = Gdx.graphics.getHeight() - greenPipeHigh.getHeight() + floorSprite.getHeight();

			// Here, we'll add the vertical value provided in this pipeset
			offset += val[1];

			// create the bounding boxes
			deathBoxes.add((new Rectangle()).set(val[0], offset, greenPipeHigh.getWidth(), greenPipeHigh.getHeight()));
			deathBoxes.add((new Rectangle()).set(val[0], offset - greenPipeLow.getHeight() - pipeGapSize, greenPipeLow.getWidth(), greenPipeLow.getHeight()));
		}

		// deal with input and calculate new position
		bird.Update(delta);

		float newX = player.getX();
		float newY = player.getY();

		if(cheat_freemove) {
			// FreeMove Mode disables gravity and lets the player move around with WASD. Useful for debugging collision detection.
			float moveBy = 50f * delta;
			if (iUp) {
				newY += moveBy;
			} else if (iDown) {
				newY -= moveBy;
			}
			if (iLeft) {
				newX -= moveBy;
			} else if (iRight) {
				newX += moveBy;
			}
		} else {
			// "Gravity" Mode

			playerVelocity += -getPlayerVelocityFactor * delta;
			newY += playerVelocity * delta;
		}

		player.setPosition(newX, newY);

		if(!cheat_noclip) {
			// NoClip Mode disables collision
			// collision detection
			for (Rectangle bbox : deathBoxes) {
				if (bbox.overlaps(player.getBoundingRectangle())) {
					System.out.println("Collision");
				}
			}
		}
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


	@Override
	public boolean keyDown(int keycode) {
		switch (keycode) {
			case com.badlogic.gdx.Input.Keys.W:
				iUp = true;
				break;
			case com.badlogic.gdx.Input.Keys.S:
				iDown = true;
				break;
			case com.badlogic.gdx.Input.Keys.A:
				iLeft = true;
				break;
			case com.badlogic.gdx.Input.Keys.D:
				iRight = true;
				break;
		}

		return true;
	}

	@Override
	public boolean keyUp(int keycode) {
		switch (keycode) {
			case com.badlogic.gdx.Input.Keys.W:
				iUp = false;
				break;
			case com.badlogic.gdx.Input.Keys.S:
				iDown = false;
				break;
			case com.badlogic.gdx.Input.Keys.A:
				iLeft = false;
				break;
			case com.badlogic.gdx.Input.Keys.D:
				iRight = false;
				break;
			case com.badlogic.gdx.Input.Keys.J:
				cheat_freemove = !cheat_freemove;
				break;
			case com.badlogic.gdx.Input.Keys.K:
				cheat_noclip = !cheat_noclip;
				break;
			case com.badlogic.gdx.Input.Keys.L:
				cheat_drawboxes = !cheat_drawboxes;
				break;
		}
		return true;
	}




	@Override
	public boolean keyTyped(char character) {
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		return false;
	}

	@Override
	public boolean scrolled(float amountX, float amountY) {
		return false;
	}

}
