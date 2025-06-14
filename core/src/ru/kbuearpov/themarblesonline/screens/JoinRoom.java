package ru.kbuearpov.themarblesonline.screens;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.neovisionaries.ws.client.*;
import ru.kbuearpov.themarblesonline.EntryPoint;
import ru.kbuearpov.themarblesonline.networking.Message;
import ru.kbuearpov.themarblesonline.utils.FontUtils;
import ru.kbuearpov.themarblesonline.utils.GameUtils;
import ru.kbuearpov.themarblesonline.utils.constants.NetConstants;

import static com.badlogic.gdx.Gdx.*;
import static com.badlogic.gdx.Input.OnscreenKeyboardType.Password;
import static com.badlogic.gdx.Input.Peripheral.OnscreenKeyboard;
import static com.badlogic.gdx.utils.Align.center;
import static ru.kbuearpov.themarblesonline.utils.constants.DeviceConstants.*;

public class JoinRoom implements Screen {

    private final EntryPoint entryPoint;
    private final Stage stage;

    private final TextField roomIdInput;
    private final TextButton join, cancel, paste;
    private final Image background;

    private final Sound buttonPressedSound;

    private boolean showExceptionMessage;
    private final BitmapFont exceptionFont;
    private final GlyphLayout exceptionLayout;
    private final int indent;

    public JoinRoom(EntryPoint entryPoint) {
        this.entryPoint = entryPoint;

        stage = new Stage();

        background = new Image(new Texture(files.internal("textures/joinroom_menu_background.jpg")));

        buttonPressedSound = audio.newSound(files.internal("sounds/button_pressed.mp3"));

        join = new TextButton("ЗАЙТИ", new Skin(files.internal("buttons/connectbuttonassets/connectbuttonskin.json")));
        cancel = new TextButton("ОТМЕНА", new Skin(files.internal("buttons/cancelbuttonassets/cancelbuttonskin.json")));
        paste = new TextButton("ВСТАВИТЬ", new Skin(files.internal("buttons/utilbuttonassets/utilbuttonskin.json")));
        roomIdInput = new TextField("ID КОМНАТЫ:", new Skin(files.internal("widgets/inputfield/inputfieldskin.json")));

        exceptionFont = FontUtils.generateFont(files.internal("fonts/defeatFont.otf"), 50, Color.RED, CHARACTERS);
        exceptionLayout = new GlyphLayout(exceptionFont, "");

        indent = 20;

        initBackground();
        initCancelButton();
        initJoinButton();
        initRoomIdInput();
        initPasteButton();

    }

    @Override
    public void show() {

        stage.addActor(background);
        stage.addActor(join);
        stage.addActor(cancel);
        stage.addActor(roomIdInput);
        stage.addActor(paste);

        input.setInputProcessor(stage);

    }

    @Override
    public void render(float delta) {
        stage.act(delta);
        stage.draw();
        if (showExceptionMessage) {
            entryPoint.batch.begin();
            exceptionFont.draw(entryPoint.batch, exceptionLayout, indent, exceptionLayout.height + 5);
            entryPoint.batch.end();
        }
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
        showExceptionMessage = false;
        stage.clear();
        input.setOnscreenKeyboardVisible(false);
    }

    @Override
    public void dispose() {
        stage.dispose();
        buttonPressedSound.dispose();
        exceptionFont.dispose();
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

    private void initJoinButton() {
        join.setSize(WIDGET_PREFERRED_WIDTH, WIDGET_PREFERRED_HEIGHT);
        join.setPosition((float) WIDTH/2 + join.getWidth()/2, (float) HEIGHT/2 - 60);

        join.getLabel().setFontScale(MathUtils.floor(join.getWidth()/join.getMinWidth()),
                MathUtils.floor(join.getHeight()/join.getMinHeight()));

        join.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {

                try {
                    GameUtils.initServerConnection(entryPoint);
                } catch (Exception halt) {
                    String msg = FontUtils.computeBreakPosition(halt.getMessage(), exceptionFont, WIDTH, indent);
                    exceptionLayout.setText(exceptionFont, msg);
                    showExceptionMessage = true;
                    return;
                }

                buttonPressedSound.play();
                entryPoint.menuMusic.stop();

                entryPoint.currentRoomId = roomIdInput.getText();
                entryPoint.clientType = NetConstants.JOINER;

                Message message = Message.builder()
                                .roomId(entryPoint.currentRoomId)
                                .messageType(NetConstants.ROOM_JOIN)
                                .clientType(NetConstants.JOINER)
                                .build();

                WebSocket response = entryPoint.serverConnection.sendText(entryPoint.converter.toJson(message));
                response.addListener(new WebSocketAdapter() {
                    @Override
                    public void onTextMessage(WebSocket websocket, String text) {
                        Message responseMsg = entryPoint.converter.fromJson(text, Message.class);
                        if (!responseMsg.getMessageType().equals("ERROR_OCCURRED")) {
                            entryPoint.setScreen(entryPoint.room);
                        } else {
                            String msg = FontUtils.computeBreakPosition(responseMsg.getNotification(), exceptionFont, WIDTH, indent);
                            exceptionLayout.setText(exceptionFont, msg);
                            showExceptionMessage = true;
                        }
                    }
                });

            }
        });
    }

    private void initRoomIdInput() {
        roomIdInput.setSize(WIDGET_PREFERRED_WIDTH + 100, WIDGET_PREFERRED_HEIGHT - 20);
        roomIdInput.setPosition((float) WIDTH/2 - roomIdInput.getWidth()/2, (float) HEIGHT/2 + 100);
        roomIdInput.setAlignment(center);

        roomIdInput.setTextFieldFilter((textField, c) -> String.valueOf(c).matches("^[A-Za-z0-9]$"));

        roomIdInput.setMaxLength(7);

        if(input.isPeripheralAvailable(OnscreenKeyboard))
            roomIdInput.setOnscreenKeyboard(visible ->
                    input.setOnscreenKeyboardVisible(true, Password));

    }

    private void initBackground() {
        background.setPosition(0, 0);
        background.setSize(WIDTH, HEIGHT);
    }

    private void initPasteButton() {
        paste.setSize(WIDGET_PREFERRED_WIDTH, WIDGET_PREFERRED_HEIGHT - 20);
        paste.setPosition(roomIdInput.getX() + roomIdInput.getWidth()/2 - paste.getWidth()/2,
                roomIdInput.getY() + paste.getHeight() + 10);

        paste.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (app.getClipboard().hasContents())
                    roomIdInput.setText(app.getClipboard().getContents());
            }
        });

        paste.getLabel().setFontScale(MathUtils.floor(paste.getWidth()/paste.getMinWidth()),
                MathUtils.floor(paste.getHeight()/paste.getMinHeight()));

    }

}
