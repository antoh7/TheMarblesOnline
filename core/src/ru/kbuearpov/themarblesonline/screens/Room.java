package ru.kbuearpov.themarblesonline.screens;

import static com.badlogic.gdx.Gdx.app;
import static com.badlogic.gdx.Gdx.audio;
import static com.badlogic.gdx.Gdx.files;
import static com.badlogic.gdx.Gdx.input;
import static com.badlogic.gdx.utils.Align.center;
import static java.util.concurrent.TimeUnit.SECONDS;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import ru.kbuearpov.themarblesonline.EntryPoint;
import ru.kbuearpov.themarblesonline.Player;
import ru.kbuearpov.themarblesonline.myImpls.SelectBox;
import ru.kbuearpov.themarblesonline.myImpls.SerializableImage;
import ru.kbuearpov.themarblesonline.networking.DataPacket;
import ru.kbuearpov.themarblesonline.networking.Receiver;
import ru.kbuearpov.themarblesonline.utils.FontGenerator;
import ru.kbuearpov.themarblesonline.utils.GameUtils;
import ru.kbuearpov.themarblesonline.utils.ThreadFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import ru.kbuearpov.themarblesonline.constants.Constants;

/** Main game class working in multithreaded mode, all events occur here, contains sounds, textures, player instances.
 * @see Screen
 * @see SerializableImage
 * @see Player
 * @see Sound
 * @see Receiver
 * @see Stage
 * @see SelectBox
 * @see EntryPoint
 * @see Map
 * **/

public class Room implements Screen {

    private final EntryPoint entryPoint;
    private final Stage stage;
    private final Image background;
    private final SelectBox<Integer> betSelection;
    private final SelectBox<String> statementSelection;
    private final BitmapFont indicatorFont;
    private final ThreadFactory threadFactory;
    private final TextButton startButton;
    private final Sound betMadeSound;
    private final Label tokenArea;
    private final Label tokenLabel;
    private final GlyphLayout marblesAmountLayout, turnLayout;
    private final Player we, opponent;

    private Map<Integer, SerializableImage> opponentHandInstances;
    private Map<Integer, SerializableImage> ourHandInstances;
    private Map<Integer, Sound> marblesHittingSounds;
    private Map<Integer, Sound> givingMarblesAwaySounds;
    private String game_state;
    private Receiver receiver;
    private boolean ourTurn, endOfStage,weReady, opponentReady;

    public Room(EntryPoint entryPoint){
        this.entryPoint = entryPoint;

        stage = new Stage();

        game_state = Constants.WAITING_FOR_PLAYER_CONNECT;

        weReady = false;
        opponentReady = false;
        endOfStage = false;

        threadFactory = new ThreadFactory();

        threadFactory.createAndAdd(this::initAcceptingThread, "accepting_thread", true);
        threadFactory.createAndAdd(this::initNetUpdateListener, "net_update_listener_thread", true);
        threadFactory.createAndAdd(this::initEventUpdateManager, "event_update_manager_thread", true);

        indicatorFont = FontGenerator.generateFont(files.internal("fonts/indicatorFont.ttf"), 80, Color.ROYAL, Constants.CHARACTERS);

        marblesAmountLayout = new GlyphLayout();
        turnLayout = new GlyphLayout();

        startButton = new TextButton("НАЧАТЬ", new Skin(files.internal("buttons/startbuttonassets/startbuttonskin.json")));
        betSelection = new SelectBox<>(new Skin(files.internal("labels/selectlist/selectlistskin.json")));
        statementSelection = new SelectBox<>(new Skin(files.internal("labels/selectlist/selectlistskin.json")));

        tokenArea = new Label("", new Skin(files.internal("labels/tokenlabel/tokenlabelskin.json")));
        tokenLabel = new Label("", new Skin(files.internal("labels/tokenlabel/tokenlabelskin.json")));

        background = new Image(new Texture(files.internal("textures/game_background.jpg")));
        loadImages();

        betMadeSound = audio.newSound(files.internal("sounds/bet_made.mp3"));
        loadSounds();

        we = new Player(new SerializableImage((new Texture(files.internal("textures/ou_h_c.png")))),
                new SerializableImage(), Constants.WIDTH - Player.getDefaultHandWidth(), 0);
        opponent = new Player(new SerializableImage((new Texture(files.internal("textures/op_h_c.png")))),
                new SerializableImage(), 0, Constants.HEIGHT - Player.getDefaultHandHeight());

        initBackground();
        initTokenLabel();
        initStartButton();
        initBetSelectionWindow();
        initStatementSelectionWindow();
    }

    @Override
    public void show() {

        initTokenArea();

        threadFactory.startThread("accepting_thread");

        stage.addActor(background);

        stage.addActor(we.getPlayerHandClosed());
        stage.addActor(opponent.getPlayerHandClosed());
        stage.addActor(we.getPlayerHandOpened());
        stage.addActor(opponent.getPlayerHandOpened());

        stage.addActor(betSelection);
        stage.addActor(statementSelection);

        if (entryPoint.deviceState.equals(Constants.SERVER)) {
            stage.addActor(tokenLabel);
            stage.addActor(tokenArea);
        }

        input.setInputProcessor(stage);

    }

    @Override
    public void render(float delta){

        stage.act(delta);
        stage.draw();

        entryPoint.batch.begin();

        //hand rendering
        if (game_state.equals(Constants.GAME_RUNNING)) {
            String text;

            synchronized (this) {
                text = ourTurn ? "Твой ход" : "Ход соперника";
            }

            marblesAmountLayout.setText(indicatorFont, "Шары: " + we.getMarblesAmount());
            turnLayout.setText(indicatorFont, text);

            indicatorFont.draw(entryPoint.batch, turnLayout, (float) Constants.WIDTH/6 - turnLayout.width/2, turnLayout.height + 10);
            indicatorFont.draw(entryPoint.batch, marblesAmountLayout, (float) Constants.WIDTH/6*5 - marblesAmountLayout.width/2, turnLayout.height + 10);

            threadFactory.startThread("event_update_manager_thread");

            if (weReady) {
                we.setHandVisible(we.getPlayerHandClosed(), true);
            }
            if (opponentReady) {
                opponent.setHandVisible(opponent.getPlayerHandClosed(), true);
            }
            if (endOfStage){
                we.setHandVisible(we.getPlayerHandClosed(), false);
                opponent.setHandVisible(opponent.getPlayerHandClosed(), false);
                we.setHandVisible(we.getPlayerHandOpened(), true);
                opponent.setHandVisible(opponent.getPlayerHandOpened(), true);

            }
        }
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
        stage.clear();
    }

    @Override
    public void dispose() {
        stage.dispose();
        betMadeSound.dispose();
        indicatorFont.dispose();
        receiver.disable();
        disposeSounds();
    }


    // ################################## threads init methods #################################

    private void initAcceptingThread(){
        try {
            entryPoint.client = entryPoint.server.accept();
            stage.addActor(startButton);
        } catch (NullPointerException ignore){
        } catch (IOException e) {
            System.exit(5);
        }
        receiver = new Receiver(entryPoint.client);
        game_state = Constants.WAITING_FOR_START;
        threadFactory.startThread("net_update_listener_thread");
    }

    private void initNetUpdateListener(){

        DataPacket currPacket;
        Player abstractPlayer;

        while (!game_state.equals(Constants.GAME_FINISHED)) {

            currPacket = receiver.getData();
            if (currPacket == null) continue;
            abstractPlayer = currPacket.getPlayerData();

            opponent.setBet(abstractPlayer.getBet());
            opponent.setMarblesAmount(abstractPlayer.getMarblesAmount());
            opponent.setStatement(abstractPlayer.getStatement());

            synchronized (this) {
                game_state = currPacket.getGameState();
                opponentReady = currPacket.getPlayerReady();
                ourTurn = !currPacket.getTurnOrder();
            }
        }
    }

    private void initEventUpdateManager(){

        while (!game_state.equals(Constants.GAME_FINISHED)) {

            synchronized (this) {
                if (ourTurn && !weReady) {
                    GameUtils.setActorVisualProps(betSelection, true);
                }

            }

            synchronized (this) {
                if (!ourTurn && opponentReady && !weReady) {
                    GameUtils.setActorVisualProps(betSelection, true);
                }
            }

            if (weReady && opponentReady) {

                GameUtils.timedWaiting(SECONDS, 2);

                weReady = false;
                opponentReady = false;
                endOfStage = true;

                final int ourMarblesAmount = we.getMarblesAmount();
                final int opponentMarblesAmount = opponent.getMarblesAmount();
                final String ourStmt = we.getStatement();
                final String opponentStmt = opponent.getStatement();

                int ourBet = we.getBet();
                int opponentBet = opponent.getBet();
                int opponentMarblesAmountOnHand = 0, ourMarblesAmountOnHand = 0;

                we.setPlayerHandOpened(ourHandInstances.get(ourBet));
                opponent.setPlayerHandOpened(opponentHandInstances.get(opponentBet));

                GameUtils.timedWaiting(SECONDS, 3);

                //we make bet,
                //opponent guesses it
                if (ourTurn){

                    if ((opponentStmt.equals(Constants.EVEN) && GameUtils.isEven(ourBet)) ||
                            (opponentStmt.equals(Constants.ODD) && GameUtils.isOdd(ourBet))){
                        if (ourBet <= opponentBet) {
                            opponent.setMarblesAmount(opponentMarblesAmount + ourBet);
                            we.setMarblesAmount(ourMarblesAmount - ourBet);

                            opponentMarblesAmountOnHand = opponentBet + ourBet;
                        } else {
                            opponent.setMarblesAmount(opponentMarblesAmount + opponentBet);
                            we.setMarblesAmount(ourMarblesAmount - opponentBet);

                            ourMarblesAmountOnHand = ourBet - opponentBet;
                            opponentMarblesAmountOnHand = opponentBet*2;
                        }

                    } else{

                        if (ourBet <= opponentBet) {
                            we.setMarblesAmount(ourMarblesAmount + ourBet);
                            opponent.setMarblesAmount(opponentMarblesAmount - ourBet);

                            ourMarblesAmountOnHand = ourBet*2;
                            opponentMarblesAmountOnHand = opponentBet - ourBet;
                        } else {
                            we.setMarblesAmount(ourMarblesAmount + opponentBet);
                            opponent.setMarblesAmount(opponentMarblesAmount - opponentBet);

                            ourMarblesAmountOnHand = ourBet + opponentBet;
                        }

                    }
                }

                //opponent makes bet,
                //we guess it
                if (!ourTurn){

                    if ((ourStmt.equals(Constants.EVEN) && GameUtils.isEven(opponentBet)) ||
                            (ourStmt.equals(Constants.ODD) && GameUtils.isOdd(opponentBet))){
                        if (ourBet <= opponentBet) {
                            we.setMarblesAmount(ourMarblesAmount + ourBet);
                            opponent.setMarblesAmount(opponentMarblesAmount - ourBet);

                            ourMarblesAmountOnHand = ourBet*2;
                            opponentMarblesAmountOnHand = opponentBet - ourBet;
                        } else {
                            we.setMarblesAmount(ourMarblesAmount + opponentBet);
                            opponent.setMarblesAmount(opponentMarblesAmount - opponentBet);

                            ourMarblesAmountOnHand = ourBet + opponentBet;
                            opponentMarblesAmountOnHand = 0;
                        }

                    } else {

                        if (ourBet <= opponentBet) {
                            we.setMarblesAmount(ourMarblesAmount - ourBet);
                            opponent.setMarblesAmount(opponentMarblesAmount + ourBet);

                            ourMarblesAmountOnHand = 0;
                            opponentMarblesAmountOnHand = opponentBet + ourBet;
                        } else {
                            we.setMarblesAmount(ourMarblesAmount - opponentBet);
                            opponent.setMarblesAmount(opponentMarblesAmount + opponentBet);

                            ourMarblesAmountOnHand = ourBet - opponentBet;
                            opponentMarblesAmountOnHand = opponentBet*2;
                        }

                    }
                }

                givingMarblesAwaySounds.get(MathUtils.random(0, givingMarblesAwaySounds.size() - 1)).play();

                we.setPlayerHandOpened(ourHandInstances.get(GameUtils.checkValue(ourMarblesAmountOnHand)));
                opponent.setPlayerHandOpened(opponentHandInstances.get(GameUtils.checkValue(opponentMarblesAmountOnHand)));

                GameUtils.timedWaiting(SECONDS, 3);

                reset();

                checkGameFinished();

            }
        }
    }

    // $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$ widgets init methods $$$$$$$$$$$$$$$$$$$$$$$$$$


    private void initStartButton(){
        startButton.setSize(Constants.WIDGET_PREFERRED_WIDTH + 100, Constants.WIDGET_PREFERRED_HEIGHT + 35);
        startButton.setPosition((float) Constants.WIDTH/2 - startButton.getWidth() / 2,
                (float) Constants.HEIGHT/2 - startButton.getHeight() / 2);

        startButton.getLabel().setFontScale(MathUtils.floor(startButton.getWidth()/startButton.getMinWidth()),
                MathUtils.floor(startButton.getHeight()/startButton.getMinHeight()));

        startButton.addListener(new ClickListener() {
            @Override
            public void clicked (InputEvent event, float x, float y) {
                if (game_state.equals(Constants.WAITING_FOR_START)) {
                    choosePlayerTurn();

                    GameUtils.setActorVisualProps(startButton, false);
                    GameUtils.setActorVisualProps(tokenArea, false);
                    GameUtils.setActorVisualProps(tokenLabel, false);
                }
            }
        });

    }

    private void initBackground(){
        background.setPosition(0, 0);
        background.setSize(Constants.WIDTH, Constants.HEIGHT);
    }

    private void initTokenArea() {
        String text = entryPoint.inviteToken;

        tokenArea.setSize((float) Constants.WIDTH/2, Constants.WIDGET_PREFERRED_HEIGHT - 20);
        tokenArea.setPosition((float) Constants.WIDTH/2 - tokenArea.getWidth()/2,
                (float) Constants.HEIGHT/2 - tokenArea.getHeight()*2 - 30);

        tokenArea.setAlignment(center);

        tokenArea.setText(text);

        tokenArea.setFontScale(MathUtils.ceil(tokenArea.getWidth()/ tokenArea.getMinWidth()),
                MathUtils.ceil(tokenArea.getHeight()/ tokenArea.getMinHeight()));

        tokenArea.addListener(new ClickListener() {
            @Override
            public void clicked (InputEvent event, float x, float y) {
                app.getClipboard().setContents(tokenArea.getText().toString());
                tokenLabel.setText("ТОКЕН СКОПИРОВАН");
            }
        });
    }

    private void initTokenLabel() {
        String text = "(КЛИК ЧТОБЫ СКОПИРОВАТЬ)";

        tokenLabel.setSize(Constants.WIDGET_PREFERRED_WIDTH + 100, Constants.WIDGET_PREFERRED_HEIGHT - 20);
        tokenLabel.setPosition((float) Constants.WIDTH/2 - tokenLabel.getWidth()/2,
                (float) Constants.HEIGHT/2 - tokenLabel.getHeight()*2 - 50 - tokenLabel.getHeight());

        tokenLabel.setAlignment(center);

        tokenLabel.setText(text);

        tokenLabel.setFontScale(MathUtils.ceil(tokenLabel.getWidth()/ tokenLabel.getMinWidth()),
                MathUtils.ceil(tokenLabel.getHeight()/ tokenLabel.getMinHeight()));

    }


    private void initBetSelectionWindow(){
        betSelection.setAlignment(center);
        betSelection.setSize(Constants.WIDGET_PREFERRED_WIDTH + 100, Constants.WIDGET_PREFERRED_HEIGHT + 35);
        betSelection.setPosition((float) Constants.WIDTH / 2 - betSelection.getWidth() / 2,
                (float) Constants.HEIGHT / 2);

        betSelection.getScrollPane().getList().setAlignment(center);

        betSelection.getStyle().listStyle.selection.setBottomHeight(MathUtils.floor((float) (Constants.HEIGHT/4) / 9));

        betSelection.setItems(GameUtils.computeBetsRange(1, 5));

        betSelection.addListener(new ClickListener() {
            @Override
            public void clicked (InputEvent event, float x, float y) {
                betSelection.setCanBeExecuted(true);
            }
        });
        betSelection.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {

                if (!betSelection.getCanBeExecuted()) return;

                int ma = betSelection.getSelected();

                we.setBet(ma);
                we.setPlayerHandOpened(ourHandInstances.get(ma));

                if (ourTurn) {

                    marblesHittingSounds.get(MathUtils.random(0, marblesHittingSounds.size() - 1)).play();

                    weReady = true;
                    GameUtils.setActorVisualProps(betSelection, false);

                    receiver.sendData(new DataPacket(game_state, ourTurn, weReady, we));

                    betMadeSound.play();
                    return;
                }

                marblesHittingSounds.get(MathUtils.random(0, marblesHittingSounds.size() - 1)).play();

                GameUtils.setActorVisualProps(statementSelection, true);

            }
        });

        GameUtils.setActorVisualProps(betSelection, false);
    }

    private void initStatementSelectionWindow() {
        statementSelection.setAlignment(center);
        statementSelection.setSize(Constants.WIDGET_PREFERRED_WIDTH + 100, Constants.WIDGET_PREFERRED_HEIGHT + 35);
        statementSelection.setPosition((float) Constants.WIDTH / 2 - statementSelection.getWidth() / 2,
                (float) Constants.HEIGHT / 2);

        statementSelection.getScrollPane().getList().setAlignment(center);
        statementSelection.getStyle().listStyle.selection.setBottomHeight(0);

        statementSelection.getStyle().listStyle.selection.setBottomHeight(MathUtils.floor((float) (Constants.HEIGHT/4) / 2));

        statementSelection.setItems(Array.with(Constants.ODD, Constants.EVEN));
        statementSelection.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {

                we.setStatement(statementSelection.getSelected());
                weReady = true;

                GameUtils.setActorVisualProps(betSelection, false);
                GameUtils.setActorVisualProps(statementSelection, false);

                betMadeSound.play();

                receiver.sendData(new DataPacket(game_state, ourTurn, weReady, we));

            }
        });

        GameUtils.setActorVisualProps(statementSelection, false);
    }


    // &&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&& load methods &&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&

    private void loadImages(){
        // loads hand textures
        opponentHandInstances = new HashMap<>();
        ourHandInstances = new HashMap<>();
        for(int i = 0; i < 11; i++){
            opponentHandInstances.put(i, new SerializableImage(new Texture(files.internal("textures/op_h_" + i + "_o.png"))));
            ourHandInstances.put(i, new SerializableImage(new Texture(files.internal("textures/ou_h_" + i + "_o.png"))));
        }
    }

    private void loadSounds(){
        marblesHittingSounds = new HashMap<>();
        givingMarblesAwaySounds = new HashMap<>();

        for(int i = 0; i < 3; i++){
            marblesHittingSounds.put(i, audio.newSound(files.internal("sounds/marbles_hitting_" + i + ".mp3")));
            givingMarblesAwaySounds.put(i, audio.newSound(files.internal("sounds/giving_marbles_away_" + i + ".mp3")));

        }
    }

    private void disposeSounds(){
        for(int i = 0; i < 3; i++){
            marblesHittingSounds.get(i).dispose();
            givingMarblesAwaySounds.get(i).dispose();
        }
    }


    // ********************************* in-game methods ********************************

    private void checkGameFinished(){
        // checks if a game must be finished
        if (we.getMarblesAmount() == 0){
            finishGame();
            entryPoint.setScreen(entryPoint.defeatScreen);
        }
        if (opponent.getMarblesAmount() == 0){
            finishGame();
            entryPoint.setScreen(entryPoint.victoryScreen);
        }
    }

    private void choosePlayerTurn(){
        // at start, once selects whose turn to make bet
        game_state = Constants.RANDOMIZING_TURN;
        boolean[] turnVariants = new boolean[]{true, false};

        ourTurn = turnVariants[MathUtils.random(0, turnVariants.length - 1)];

        game_state = Constants.GAME_RUNNING;

        receiver.sendData(new DataPacket(game_state, ourTurn, weReady, we));
    }

    private void reset(){
        // resets current stage, inverts turn order
        endOfStage = false;
        weReady = false;
        opponentReady = false;

        we.setHandVisible(we.getPlayerHandOpened(), false);
        opponent.setHandVisible(opponent.getPlayerHandOpened(), false);
        betSelection.setItems(GameUtils.computeBetsRange(1, we.getMarblesAmount()));

        if (entryPoint.deviceState.equals(Constants.SERVER)){
            if (we.getMarblesAmount() == 1) ourTurn = false;
            else if (opponent.getMarblesAmount() == 1) ourTurn = true;
            else ourTurn = !ourTurn;

            receiver.sendData(new DataPacket(game_state, ourTurn, weReady, we));
        } else {
            // waiting for net update listener thread
            // receive a packet with inverted turn order
            // to avoid visual glitches
            GameUtils.timedWaiting(SECONDS, 1);
        }
    }

    private void finishGame(){

        game_state = Constants.GAME_FINISHED;

        ourTurn = false;
        weReady = false;
        opponentReady = false;
        endOfStage = false;

        we.setPlayerHandOpened(new SerializableImage());
        we.setMarblesAmount(5);
        we.setBet(0);
        we.setStatement(null);
        opponent.setPlayerHandOpened(new SerializableImage());
        opponent.setMarblesAmount(5);
        opponent.setBet(0);
        opponent.setStatement(null);

        receiver.disable();

        try {
            entryPoint.server.close();
        } catch (NullPointerException ignore) {
        } catch (IOException e) {
            System.exit(5);
        }

        try {
            entryPoint.client.close();
        } catch (IOException e) {
            System.exit(5);
        }

    }

}
