package ru.kbuearpov.themarblesonline.screens;

import static com.badlogic.gdx.Gdx.*;
import static ru.kbuearpov.themarblesonline.utils.constants.DeviceConstants.*;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import ru.kbuearpov.themarblesonline.EntryPoint;
import ru.kbuearpov.themarblesonline.networking.Message;
import ru.kbuearpov.themarblesonline.utils.FontUtils;
import ru.kbuearpov.themarblesonline.utils.GameUtils;
import ru.kbuearpov.themarblesonline.utils.constants.NetConstants;

public class CreateRoom implements Screen {

    private final EntryPoint entryPoint;
    private final Stage stage;

    private final Image background;
    private final TextButton create, cancel;

    private final Sound buttonPressedSound;

    private boolean showExceptionMessage;
    private final BitmapFont exceptionFont;
    private final GlyphLayout exceptionLayout;
    private final int indent;

    public CreateRoom(EntryPoint entryPoint) {
        this.entryPoint = entryPoint;
        stage = new Stage();

        background = new Image(new Texture(files.internal("textures/createroom_menu_background.jpg")));

        buttonPressedSound = audio.newSound(files.internal("sounds/button_pressed.mp3"));

        create = new TextButton("СОЗДАТЬ", new Skin(files.internal("buttons/createbuttonassets/createbuttonskin.json")));
        cancel = new TextButton("ОТМЕНА",new Skin(files.internal("buttons/cancelbuttonassets/cancelbuttonskin.json")));

        exceptionFont = FontUtils.generateFont(files.internal("fonts/defeatFont.otf"), 50, Color.RED, CHARACTERS);
        exceptionLayout = new GlyphLayout(exceptionFont, "");
        indent = 20;

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

    private void initCreateButton() {
        create.setSize(WIDGET_PREFERRED_WIDTH, WIDGET_PREFERRED_HEIGHT);
        create.setPosition((float) WIDTH/2 + create.getWidth()/2, (float) HEIGHT/2 - 60);

        create.getLabel().setFontScale(MathUtils.floor(create.getWidth()/create.getMinWidth()),
                MathUtils.floor(create.getHeight()/create.getMinHeight()));

        create.addListener(new ClickListener() {
            @Override
            public void clicked (InputEvent event, float x, float y) {

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

                entryPoint.clientType = NetConstants.INITIATOR;
                entryPoint.currentRoomId = GameUtils.generateRoomId();

                Message message = Message.builder()
                        .roomId(entryPoint.currentRoomId)
                        .messageType(NetConstants.ROOM_INIT)
                        .clientType(NetConstants.INITIATOR)
                        .build();

                entryPoint.serverConnection.sendText(entryPoint.converter.toJson(message));

                entryPoint.setScreen(entryPoint.room);
            }
        });
    }

    private void initBackground(){
        background.setPosition(0, 0);
        background.setSize(WIDTH, HEIGHT);
    }

}
