package com.ninjaghost.flappy;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.*;

/**
 * Plan:
 * ✔️ we should get the infinitely scrolling floor first
 * ✔️ let's infinitely spawn some pipe pairs
 * ✔️ Background needs to infinitely scroll too
 * ✔️ Bird debug move around mode
 * ✔️ collision detection for death state
 * ✔️ let's then make the bird fall
 * ✔️ get some input in to jump the bird up
 * ✔️ Collision triggers death state
 * ✔️ Able to respawn after death
 * ✔️ "Start menu"
 * ✔️ "Game over"
 * ✔️ Respawnable
 * Death Sequence
 * GUI Bar
 * Counter
 * Make things smoother (rotate bird)
 * Make Exe
 */


public class FlappyBirdCloneGame extends ApplicationAdapter implements InputProcessor {
	enum GameState {
		NONE, MENU, GAME, DEATH
	}
	GameState gameState = GameState.NONE;

	enum Menu {
		NONE, START, DEATH
	}
	Menu activeMenu = Menu.NONE;


	enum Difficulty {
		EASY, MEDIUM, HARD
	}
	private record Settings(
			String name,
			float pipeGap,
			float pipeSpawnFreq,
			float pipeMoveFactor,
			float groundMoveFactor,
			float bgMoveFactor
	) {}
	Map<Difficulty, Settings> difficultySettingsMap = Map.of(
			Difficulty.EASY, new Settings("Easy",85f, 4f, 120f, 60f, 20f),
			Difficulty.MEDIUM, new Settings("Medium",75f, 3f, 180f, 80f, 40f),
			Difficulty.HARD, new Settings("Hard",65f, 1.5f, 240f, 120f, 80f)
	);

	Difficulty difficulty = Difficulty.EASY;

	OrthographicCamera camera;
	Viewport viewport;
	SpriteBatch batch;

	Sprite backgroundSprite;
	Sprite floorSprite;
	float backgroundMovementConst = difficultySettingsMap.get(difficulty).bgMoveFactor;
	float floorMovementConst = difficultySettingsMap.get(difficulty).groundMoveFactor;

	Sprite greenPipeHigh;
	Sprite greenPipeLow;

	Bird bird;

	ArrayList<Rectangle> deathBoxes = new ArrayList<>();
	ArrayList<Float> floorOffsets = new ArrayList<>();
	ArrayList<Float> bgOffsets = new ArrayList<>();
	ArrayList<Float[]> pipeOffsets = new ArrayList<>();
	float pipeSpawnTimer = 0;
	float pipeSpawnTimerMax = difficultySettingsMap.get(difficulty).pipeSpawnFreq;
	float pipeGapSize = difficultySettingsMap.get(difficulty).pipeGap;
	float pipeMovementConst = difficultySettingsMap.get(difficulty).pipeMoveFactor;

	Sprite player;
	float playerMovementConst = 75f;
	float playerVelocity = 0;
	float getPlayerVelocityFactor = 980;
	float playerJumpStrength = 250;
	float playerDeathTimer = 0;
	float playerDeathTimerMax = 4.0f;
	boolean playerDead = false;
	boolean playerCanRespawn = false;
	int playerScore = 0;
	int playerHighScore = 0;
	int playerLastScore = 0;


	boolean cheat_freemove = false; // disables gravity, enables WASD movement
	boolean cheat_noclip = false; // when enabled, disables collision
	boolean cheat_drawboxes = false; // when enabled draws the bounding boxes

	// input state
	boolean iUp = false;
	boolean iDown = false;
	boolean iLeft = false;
	boolean iRight = false;
	boolean iSpace = false;

	// UI stuff
	BitmapFont font;
	GlyphLayout layout;



	@Override
	public void create () {
		Gdx.input.setInputProcessor(this);
		batch = new SpriteBatch();
		layout = new GlyphLayout();
		font = new BitmapFont();
		camera = new OrthographicCamera();
		viewport = new FitViewport(400, 450, camera);

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

		startStartMenu();
//		startGame();
	}

	private void applySettings() {
		pipeSpawnTimerMax = difficultySettingsMap.get(difficulty).pipeSpawnFreq;
		pipeGapSize = difficultySettingsMap.get(difficulty).pipeGap;
		pipeMovementConst = difficultySettingsMap.get(difficulty).pipeMoveFactor;
		backgroundMovementConst = difficultySettingsMap.get(difficulty).bgMoveFactor;
		floorMovementConst = difficultySettingsMap.get(difficulty).groundMoveFactor;
	}

	private void startStartMenu() {
		gameState = GameState.MENU;
		activeMenu = Menu.START;
	}

	private void startGame() {
		gameState = GameState.GAME;
		activeMenu = Menu.NONE;
		respawn();
	}

	/**
	 * Reset the player into a new playable state
	 */
	private void respawn() {
		player.setPosition(100f, (float) Gdx.graphics.getHeight() / 2);
		playerVelocity = 0;
		pipeSpawnTimer = 0;
		playerScore = 0;
		playerDeathTimer = 0;
		playerDead = false;
		playerCanRespawn = false;
		pipeOffsets.clear();
		gameState = GameState.GAME;
	}

	@Override
	public void render () {
		this.update(Gdx.graphics.getDeltaTime());

		ScreenUtils.clear(0, 0, 0.75f, 1);
		ShapeRenderer shapeRenderer = new ShapeRenderer();

		batch.begin();

		// Draw the background first
		for (float bgOffset : bgOffsets) {
			batch.draw(backgroundSprite, bgOffset, 0);
		}

		// Draw the ground on top
		for (float floorOffset : floorOffsets) {
			batch.draw(floorSprite, floorOffset, 0);
		}

		batch.end();


		// reset font scale
		font.getData().setScale(1.0f);

		if(gameState == GameState.GAME || gameState == GameState.DEATH) {
			batch.begin();
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
			// bird.Render(batch);
			batch.draw(player, player.getX(), player.getY());

			// Send it home
			batch.end();


			if(cheat_drawboxes) {
				// DrawBoxes mode, outline in red all the deathboxes in the scene.
				// Requires a special drawing mode so the previous SpriteBatch needs to be ended first

				shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
				shapeRenderer.setColor(Color.RED);
				for (Rectangle bbox : deathBoxes) {
					shapeRenderer.rect(bbox.x, bbox.y, bbox.width, bbox.height);
				}
				shapeRenderer.end();
			}


			// UI goes on top of everything else so it must be rendered last
			batch.begin();
			font.getData().setScale(1.2f);

			// Show settings
			font.draw(batch, "Mode: " + difficultySettingsMap.get(difficulty).name, Gdx.graphics.getWidth() - 100, Gdx.graphics.getHeight() - 20);

			// Show activated cheats
			List<String> cheats = new ArrayList<>();
			if(cheat_freemove) cheats.add(" FreeMove");
			if(cheat_noclip) cheats.add(" NoClip");
			if(cheat_drawboxes) cheats.add(" BBoxes");

			if(!cheats.isEmpty()) {
				font.draw(batch, "Cheats:\n" + String.join("\n", cheats), Gdx.graphics.getWidth() - 100, Gdx.graphics.getHeight() - 50);
			}
			batch.end();


			if(playerDead) {
				batch.begin();
				/// This may get moved out into the main loop
				// as the death message could show up the instant
				// the player hits something. Keep this in mind.
				String deadText = "You Dead.";
				layout.setText(font, deadText);
				float deadTextWidth = layout.width;
				float deadTextHeight = layout.height;
				float deadTextBottomOffset = 100f;

				font.draw(batch, deadText, ((float) Gdx.graphics.getWidth() / 2) - deadTextWidth / 2, ((float)Gdx.graphics.getHeight() / 2 + deadTextHeight / 2) + deadTextBottomOffset);

				if(playerCanRespawn) {
					String respawnText = "Press space to try again";
					layout.setText(font, respawnText);
					float respawnTextWidth = layout.width;
					float respawnTextHeight = layout.height;
					float respawnTextBottomOffset = 50f;

					font.draw(batch, respawnText, ((float) Gdx.graphics.getWidth() / 2) - respawnTextWidth / 2, ((float)Gdx.graphics.getHeight() / 2 + respawnTextHeight / 2) + respawnTextBottomOffset);

				}

				batch.end();
			}

		} else if (gameState == GameState.MENU) {
			batch.begin();
			if(activeMenu == Menu.START) {

				String titleText = "FlappyBird Clone \n               by NinjaGhost\n\n\n\nPress Space / Tap to Start";
				layout.setText(font, titleText);
				float titleTextWidth = layout.width;
				float titleTextHeight = layout.height;
				float titleTextBottomOffset = 100f;

				font.draw(batch, titleText, ((float) Gdx.graphics.getWidth() / 2) - titleTextWidth / 2, ((float)Gdx.graphics.getHeight() / 2 + titleTextHeight / 2) + titleTextBottomOffset);

			}
//			else if(activeMenu == Menu.DEATH) {
//
//
//			}
			batch.end();
		}

		// Render the UI bar atop the floor tiles
		shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
		shapeRenderer.setColor(new Color(100f / 255, 149f / 255, 237f / 255, 255f / 255));
		shapeRenderer.rect(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight() * 0.08f);
		shapeRenderer.end();

		batch.begin();
		font.getData().setScale(1.2f);

		String scoreText = "Score: " + playerScore;
		String lastScoreText = "Last: " + playerLastScore;
		String highScoreText = "High: " + playerHighScore;
//		layout.setText(font, scoreText);
//		float scoreTextWidth = layout.width;
		layout.setText(font, lastScoreText);
		float lastScoreTextWidth = layout.width;
		layout.setText(font, highScoreText);
		float highScoreTextWidth = layout.width;

		font.draw(batch, scoreText, 15, 25);
		font.draw(batch, lastScoreText, ((float)Gdx.graphics.getWidth() / 2) - (lastScoreTextWidth / 2), 25);
		font.draw(batch, highScoreText, Gdx.graphics.getWidth() - highScoreTextWidth - 15, 25);
		batch.end();

	}

	public void update (float delta) {
		deathBoxes.clear(); // empty the death boxes

		if(gameState == GameState.DEATH) {

			if(!playerCanRespawn) {
				playerDeathTimer += delta;
				if(playerDeathTimer > playerDeathTimerMax) {
					playerCanRespawn = true;
				}
			}

			// don't do anything until space is hit to respawn but only when the player CAN respawn
			if(iSpace && playerCanRespawn) {
				respawn();
			}
		}
		else if(gameState == GameState.GAME) {

			// Main Gameplay Loop

			pipeSpawnTimer += delta;

			if(pipeSpawnTimer > pipeSpawnTimerMax) {
				spawnPipe();
				pipeSpawnTimer = 0;
			}

			// update floor position
			floorOffsets.replaceAll(aFloat -> aFloat - floorMovementConst * delta);
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
			bgOffsets.replaceAll(aFloat -> aFloat - backgroundMovementConst * delta);
			if(bgOffsets.get(0) <= -backgroundSprite.getWidth()) {
				for (int i = 0; i < 4; i++) {
					bgOffsets.set(i, backgroundSprite.getWidth() * i);
				}
			}

			// Update pipe positions
			for (int i = 0; i < pipeOffsets.size(); i++) {
				Float[] val = pipeOffsets.get(i);
				val[0] -= pipeMovementConst * delta;

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
				float moveBy = playerMovementConst * delta;
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
				if(iSpace) {
					playerVelocity = playerJumpStrength;
					iSpace = false;
				}
				newY += playerVelocity * delta;
			}

			player.setPosition(newX, newY);

			if(!cheat_noclip) {
				// NoClip Mode disables collision
				// collision detection
				for (Rectangle bbox : deathBoxes) {
					if (bbox.overlaps(player.getBoundingRectangle())) {
						System.out.println("Collision");
						triggerDeath();
					}
				}
			}
		} else if(gameState == GameState.MENU) {

			if(activeMenu == Menu.START) {
				// Start Menu Controls

				if(iSpace) {
					startGame();
				}
			} else if(activeMenu == Menu.DEATH) {
				// Death menu Controls

				if(iSpace) {
					startGame();
				}
			}
		}

		// reset keystate
		iSpace = false;
		iLeft = false;
		iDown = false;
		iRight = false;
		iUp = false;

	}

	private void triggerDeath() {
		// let the player bird react
		playerDead = true;
		playerCanRespawn = false;
		gameState = GameState.DEATH;
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
			case com.badlogic.gdx.Input.Keys.SPACE:
				iSpace = true;
				break;
		}

		return true;
	}

	@Override
	public boolean keyUp(int keycode) {
		switch (keycode) {
			case Input.Keys.W:
				iUp = false;
				break;
			case Input.Keys.S:
				iDown = false;
				break;
			case Input.Keys.A:
				iLeft = false;
				break;
			case Input.Keys.D:
				iRight = false;
				break;
			case Input.Keys.J:
				cheat_freemove = !cheat_freemove;
				break;
			case Input.Keys.K:
				cheat_noclip = !cheat_noclip;
				break;
			case Input.Keys.L:
				cheat_drawboxes = !cheat_drawboxes;
				break;
			case Input.Keys.UP:
				switch (difficulty) {
					case EASY -> difficulty = Difficulty.MEDIUM;
					case MEDIUM -> difficulty = Difficulty.HARD;
                }
				applySettings();
				break;
			case Input.Keys.DOWN:
				switch (difficulty) {
					case HARD -> difficulty = Difficulty.MEDIUM;
					case MEDIUM -> difficulty = Difficulty.EASY;
				}
				applySettings();
				break;

		}
		return true;
	}


	@Override
	public void resize(int width, int height) {
//		viewport.update(width, height, true);
		super.resize(width, height);
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
