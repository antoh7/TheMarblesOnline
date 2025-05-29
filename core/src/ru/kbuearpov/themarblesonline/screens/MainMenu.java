package ru.kbuearpov.themarblesonline.screens;


import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import ru.kbuearpov.themarblesonline.EntryPoint;
import ru.kbuearpov.themarblesonline.utils.constants.NetConstants;

import static com.badlogic.gdx.Gdx.*;
import static com.badlogic.gdx.Input.OnscreenKeyboardType.Password;
import static com.badlogic.gdx.Input.Peripheral.OnscreenKeyboard;
import static com.badlogic.gdx.utils.Align.center;
import static ru.kbuearpov.themarblesonline.utils.constants.DeviceConstants.*;

public class MainMenu implements Screen {
	private final EntryPoint entryPoint;
	private final Stage stage;

	private final Image background;
	private final TextButton joinButton, createButton;
	private final TextField serverAddress;
	private final TextButton confirm;

	private final Sound buttonPressedSound;

	public MainMenu(EntryPoint entryPoint) {
		this.entryPoint = entryPoint;

		stage = new Stage();

		background = new Image(new Texture(files.internal("textures/main_menu_background.jpg")));

		buttonPressedSound = audio.newSound(files.internal("sounds/button_pressed.mp3"));

		joinButton = new TextButton("ЗАЙТИ", new Skin(files.internal("buttons/connectbuttonassets/connectbuttonskin.json")));
		confirm = new TextButton("ОК", new Skin(files.internal("buttons/utilbuttonassets/utilbuttonskin.json")));
		createButton = new TextButton("СОЗДАТЬ", new Skin(files.internal("buttons/createbuttonassets/createbuttonskin.json")));
		serverAddress = new TextField("", new Skin(files.internal("widgets/inputfield/inputfieldskin.json")));

		initBackground();
		initAddressInput();
		initConfirmButton();
		initCreateButton();
		initJoinButton();

	}


	@Override
	public void show() {

		stage.addActor(background);
		stage.addActor(serverAddress);
		stage.addActor(confirm);
		stage.addActor(joinButton);
		stage.addActor(createButton);

		input.setInputProcessor(stage);

		String addr = app.getPreferences(NetConstants.PREFS_NAME).getString(NetConstants.PREFS_KEY);

		if (addr.isEmpty())
			serverAddress.setText("0.0.0.0:12345");
		else
			serverAddress.setText(addr);

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
	public void dispose() {
		stage.dispose();
		buttonPressedSound.dispose();
	}

	// ########################### инициализационные методы ############################

	private void initCreateButton(){
		createButton.setSize(WIDGET_PREFERRED_WIDTH, WIDGET_PREFERRED_HEIGHT);
		createButton.setPosition((float) WIDTH/2 + createButton.getWidth()/2,
				(float) HEIGHT/2 - createButton.getHeight()/2);

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
		joinButton.setSize(WIDGET_PREFERRED_WIDTH, WIDGET_PREFERRED_HEIGHT);
		joinButton.setPosition((float) WIDTH/2 - joinButton.getWidth() - joinButton.getWidth()/2,
				(float) HEIGHT/2 - joinButton.getHeight()/2);

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

	private void initAddressInput() {
		serverAddress.setSize(WIDGET_PREFERRED_WIDTH + 200, WIDGET_PREFERRED_HEIGHT - 20);
		serverAddress.setPosition((float) WIDTH/2 - serverAddress.getWidth()/2,
				(float) HEIGHT - serverAddress.getHeight() * 2);
		serverAddress.setAlignment(center);

		serverAddress.setTextFieldFilter((textField, c) -> String.valueOf(c).matches("^[0-9.:]{1,18}$"));

		serverAddress.setMaxLength(21);

		if(input.isPeripheralAvailable(OnscreenKeyboard))
			serverAddress.setOnscreenKeyboard(visible ->
					input.setOnscreenKeyboardVisible(true, Password));
	}

	private void initConfirmButton() {
		confirm.setSize(WIDGET_PREFERRED_WIDTH, WIDGET_PREFERRED_HEIGHT - 20);
		confirm.setPosition(serverAddress.getX() + serverAddress.getWidth()/2 - confirm.getWidth()/2,
				serverAddress.getY() - confirm.getHeight() - 10);

		confirm.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				Preferences prefs = app.getPreferences(NetConstants.PREFS_NAME);
				prefs.putString(NetConstants.PREFS_KEY, serverAddress.getText());
				prefs.flush();

				confirm.setText("СОХРАНЕНО");
			}
		});
	}

	private void initBackground(){
		background.setPosition(0, 0);
		background.setSize(WIDTH, HEIGHT);
	}

}
