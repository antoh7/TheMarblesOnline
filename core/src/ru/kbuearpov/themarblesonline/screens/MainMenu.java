package ru.kbuearpov.themarblesonline.screens;


import static com.badlogic.gdx.Gdx.audio;
import static com.badlogic.gdx.Gdx.files;
import static com.badlogic.gdx.Gdx.input;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import ru.kbuearpov.themarblesonline.EntryPoint;
import ru.kbuearpov.themarblesonline.constants.Constants;

public class MainMenu implements Screen {
	private final EntryPoint entryPoint;
	private final Stage stage;

	private final Image background;
	private final TextButton joinButton, createButton;

	private final Sound buttonPressedSound;

	public MainMenu(EntryPoint entryPoint) {
		this.entryPoint = entryPoint;

		stage = new Stage();

		background = new Image(new Texture(files.internal("textures/main_menu_background.jpg")));

		buttonPressedSound = audio.newSound(files.internal("sounds/button_pressed.mp3"));

		joinButton = new TextButton("ЗАЙТИ", new Skin(files.internal("buttons/connectbuttonassets/connectbuttonskin.json")));
		createButton = new TextButton("СОЗДАТЬ",new Skin(files.internal("buttons/createbuttonassets/createbuttonskin.json")));

		initBackground();
		initCreateButton();
		initJoinButton();

	}


	@Override
	public void show() {

		stage.addActor(background);
		stage.addActor(joinButton);
		stage.addActor(createButton);

		input.setInputProcessor(stage);

		entryPoint.menuMusic.play();

	}

	@Override
	public void render(float delta) {

		stage.act(delta);
		stage.draw();
	}

	@Override
	public void resize(int width, int height) {
	}

	@Override
	public void pause() {

	}

	@Override
	public void resume() {

	}

	@Override
	public void hide() {
		stage.clear();
	}

	@Override
	public void dispose () {
		stage.dispose();
		buttonPressedSound.dispose();
	}

	// ########################### инициализационные методы ############################

	private void initCreateButton(){
		createButton.setSize(Constants.WIDGET_PREFERRED_WIDTH, Constants.WIDGET_PREFERRED_HEIGHT);
		createButton.setPosition((float) Constants.WIDTH/2 + createButton.getWidth()/2,
				(float) Constants.HEIGHT/2 - createButton.getHeight()/2);

		createButton.getLabel().setFontScale(MathUtils.floor(createButton.getWidth()/createButton.getMinWidth()),
				MathUtils.floor(createButton.getHeight()/createButton.getMinHeight()));

		createButton.addListener(new ClickListener() {
			@Override
			public void clicked (InputEvent event, float x, float y) {
				buttonPressedSound.play();
				entryPoint.setScreen(entryPoint.createRoom);
			}
		});
	}

	private void initJoinButton(){
		joinButton.setSize(Constants.WIDGET_PREFERRED_WIDTH, Constants.WIDGET_PREFERRED_HEIGHT);
		joinButton.setPosition((float) Constants.WIDTH/2 - joinButton.getWidth() - joinButton.getWidth()/2,
				(float) Constants.HEIGHT/2 - joinButton.getHeight()/2);

		joinButton.getLabel().setFontScale(MathUtils.floor(joinButton.getWidth()/joinButton.getMinWidth()),
				MathUtils.floor(joinButton.getHeight()/joinButton.getMinHeight()));

		joinButton.addListener(new ClickListener() {
			@Override
			public void clicked (InputEvent event, float x, float y) {
				buttonPressedSound.play();
				entryPoint.setScreen(entryPoint.joinRoom);
			}
		});
	}

	private void initBackground(){
		background.setPosition(0, 0);
		background.setSize(Constants.WIDTH, Constants.HEIGHT);
	}

}
