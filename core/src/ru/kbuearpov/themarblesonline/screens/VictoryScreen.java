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
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import ru.kbuearpov.themarblesonline.EntryPoint;
import ru.kbuearpov.themarblesonline.networking.Message;
import ru.kbuearpov.themarblesonline.utils.FontUtils;
import ru.kbuearpov.themarblesonline.utils.constants.NetConstants;

import static com.badlogic.gdx.Gdx.*;
import static ru.kbuearpov.themarblesonline.utils.constants.DeviceConstants.*;
import static ru.kbuearpov.themarblesonline.utils.constants.DeviceConstants.CHARACTERS;

public class VictoryScreen implements Screen {

    private final EntryPoint entryPoint;
    private final Stage stage;

    private final Image background;
    private final TextButton exit;
    private final TextButton restart;
    private final BitmapFont victoryFont;

    private final Sound victorySound;

    private final GlyphLayout victoryLayout;

    public VictoryScreen(EntryPoint entryPoint) {
        this.entryPoint = entryPoint;

        stage = new Stage();

        background = new Image(new Texture(files.internal("textures/victory.jpg")));

        exit = new TextButton("ВЫЙТИ", new Skin(files.internal("buttons/utilbuttonassets/utilbuttonskin.json")));
        restart = new TextButton("ЗАНОВО", new Skin(files.internal("buttons/utilbuttonassets/utilbuttonskin.json")));

        victoryFont = FontUtils.generateFont(files.internal("fonts/victoryFont.ttf"), 160, Color.CYAN, CHARACTERS);

        victoryLayout = new GlyphLayout(victoryFont, "ПОБЕДА!");

        victorySound = audio.newSound(files.internal("sounds/victory_sound.wav"));

        initExitButton();
        initRestartButton();
        initBackground();

    }

    @Override
    public void show() {

        stage.addActor(background);
        stage.addActor(restart);
        stage.addActor(exit);

        input.setInputProcessor(stage);

        victorySound.play(1);
    }

    @Override
    public void render(float delta) {

        stage.act(delta);

        stage.draw();

        entryPoint.batch.begin();

        victoryFont.draw(entryPoint.batch, victoryLayout, (float) WIDTH/2 - victoryLayout.width/2,
                (float) HEIGHT/2 + victoryLayout.height*2);

        entryPoint.batch.end();
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
    }

    @Override
    public void dispose() {
        stage.dispose();
        victoryFont.dispose();
        victorySound.dispose();
    }


    // ########################### инициализационные методы ############################

    private void initExitButton(){
        exit.setSize(WIDGET_PREFERRED_WIDTH + 20, WIDGET_PREFERRED_HEIGHT + 10);
        exit.setPosition((float) WIDTH/2 - exit.getWidth() / 2,
                (float) HEIGHT/2 - exit.getHeight() / 2);

        exit.getLabel().setFontScale(MathUtils.floor(exit.getWidth()/exit.getMinWidth()),
                MathUtils.floor(exit.getHeight()/exit.getMinHeight()));
        exit.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                app.exit();
            }
        });
    }

    private void initRestartButton(){
        restart.setSize(WIDGET_PREFERRED_WIDTH + 20, WIDGET_PREFERRED_HEIGHT + 10);
        restart.setPosition((float) WIDTH/2 - exit.getWidth() / 2,
                (float) HEIGHT/2 - restart.getHeight() * 1.5f - 20);

        restart.getLabel().setFontScale(MathUtils.floor(restart.getWidth()/restart.getMinWidth()),
                MathUtils.floor(restart.getHeight()/restart.getMinHeight()));
        restart.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                entryPoint.canBeRestarted = true;

                Message message = Message.builder()
                                .roomId(entryPoint.currentRoomId)
                                .clientType(entryPoint.clientType)
                                .messageType(NetConstants.GAME_IN_PROCESS)
                                .restartAvailable(true)
                                .build();

                entryPoint.serverConnection.sendText(entryPoint.converter.toJson(message));

                entryPoint.setScreen(entryPoint.room);
            }
        });
    }

    private void initBackground(){
        background.setSize(WIDTH, HEIGHT);
        background.setPosition(0, 0);
    }
}
