package ru.kbuearpov.themarblesonline.screens;

import static com.badlogic.gdx.Gdx.*;
import static ru.kbuearpov.themarblesonline.utils.constants.DeviceConstants.HEIGHT;
import static ru.kbuearpov.themarblesonline.utils.constants.DeviceConstants.WIDGET_PREFERRED_HEIGHT;
import static ru.kbuearpov.themarblesonline.utils.constants.DeviceConstants.WIDGET_PREFERRED_WIDTH;
import static ru.kbuearpov.themarblesonline.utils.constants.DeviceConstants.WIDTH;

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
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;

import java.io.IOException;

import ru.kbuearpov.themarblesonline.networking.constants.ClientType;
import ru.kbuearpov.themarblesonline.EntryPoint;
import ru.kbuearpov.themarblesonline.networking.Message;
import ru.kbuearpov.themarblesonline.networking.constants.MessageType;
import ru.kbuearpov.themarblesonline.utils.PreGameStartedUtils;
import ru.kbuearpov.themarblesonline.utils.constants.PrefsConstants;

public class CreateRoom implements Screen {

    private final EntryPoint entryPoint;
    private final Stage stage;

    private final Image background;
    private final TextButton create, cancel;

    private final Sound buttonPressedSound;

    public CreateRoom(EntryPoint entryPoint) {
        this.entryPoint = entryPoint;
        stage = new Stage();

        background = new Image(new Texture(files.internal("textures/createroom_menu_background.jpg")));

        buttonPressedSound = audio.newSound(files.internal("sounds/button_pressed.mp3"));

        create = new TextButton("СОЗДАТЬ", new Skin(files.internal("buttons/createbuttonassets/createbuttonskin.json")));
        cancel = new TextButton("ОТМЕНА",new Skin(files.internal("buttons/cancelbuttonassets/cancelbuttonskin.json")));

        initBackground();
        initCancelButton();
        initCreateButton();

    }

    @Override
    public void show() {

        stage.addActor(background);
        stage.addActor(create);
        stage.addActor(cancel);

        input.setInputProcessor(stage);
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
        input.setOnscreenKeyboardVisible(false);
    }

    @Override
    public void dispose() {
        stage.dispose();
        buttonPressedSound.dispose();
    }


    // ########################### инициализационные методы ############################

    private void initCancelButton(){
        cancel.setSize(WIDGET_PREFERRED_WIDTH, WIDGET_PREFERRED_HEIGHT);
        cancel.setPosition((float) WIDTH/2 - cancel.getWidth() - cancel.getWidth()/2,
                (float) HEIGHT/2 - 60);

        cancel.getLabel().setFontScale(MathUtils.floor(cancel.getWidth()/cancel.getMinWidth()),
                MathUtils.floor(cancel.getHeight()/cancel.getMinHeight()));

        cancel.addListener(new ClickListener() {
            @Override
            public void clicked (InputEvent event, float x, float y) {
                buttonPressedSound.play();
                entryPoint.setScreen(entryPoint.mainMenu);
            }
        });
    }

    private void initCreateButton(){
        create.setSize(WIDGET_PREFERRED_WIDTH, WIDGET_PREFERRED_HEIGHT);
        create.setPosition((float) WIDTH/2 + create.getWidth()/2, (float) HEIGHT/2 - 60);

        create.getLabel().setFontScale(MathUtils.floor(create.getWidth()/create.getMinWidth()),
                MathUtils.floor(create.getHeight()/create.getMinHeight()));

        create.addListener(new ClickListener() {
            @Override
            public void clicked (InputEvent event, float x, float y) {

                try {
                    entryPoint.serverConnection = new WebSocketFactory()
                            .createSocket("ws://{}/connection/new".replace(
                                    "{}", app.getPreferences(PrefsConstants.PREFS_NAME)
                                            .getString(PrefsConstants.PREFS_KEY)), 7000);
                } catch (IOException | IllegalArgumentException exception) {
                    return;
                }
                entryPoint.serverConnection.addHeader("User-Agent", "The-Marbles-Online-Client");

                try {
                    entryPoint.serverConnection.connect();
                } catch (WebSocketException webSocketException) {
                    return;
                }

                buttonPressedSound.play();
                entryPoint.menuMusic.stop();

                entryPoint.clientType = ClientType.INITIATOR;
                entryPoint.currentRoomId = PreGameStartedUtils.generateRoomId();

                Message createMessage = new Message();

                createMessage.setRoomId(entryPoint.currentRoomId);
                createMessage.setMessageType(MessageType.ROOM_INIT);
                createMessage.setClientType(ClientType.INITIATOR);

                entryPoint.serverConnection.sendText(entryPoint.converter.toJson(createMessage));

                entryPoint.setScreen(entryPoint.room);
            }
        });
    }

    private void initBackground(){
        background.setPosition(0, 0);
        background.setSize(WIDTH, HEIGHT);
    }

}
