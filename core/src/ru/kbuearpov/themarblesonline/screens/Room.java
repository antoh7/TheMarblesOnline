package ru.kbuearpov.themarblesonline.screens;

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
import com.google.gson.Gson;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import ru.kbuearpov.themarblesonline.*;
import ru.kbuearpov.themarblesonline.constants.Constants;
import ru.kbuearpov.themarblesonline.myImpls.SelectBox;
import ru.kbuearpov.themarblesonline.myImpls.SerializableImage;
import ru.kbuearpov.themarblesonline.networking.Receiver;
import ru.kbuearpov.themarblesonline.utils.FontGenerator;
import ru.kbuearpov.themarblesonline.utils.GameUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.badlogic.gdx.Gdx.*;
import static com.badlogic.gdx.utils.Align.center;
import static java.util.concurrent.TimeUnit.SECONDS;

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
   // private final ThreadFactory threadFactory;
    private final TextButton startButton;
    private final Sound betMadeSound;
    private final Label tokenArea;
    private final Label tokenLabel;
    private final GlyphLayout marblesAmountLayout, turnLayout;
    private final Player current, opponent;
    private final Gson gson;
    private final Thread gameEventThread;

    private Map<Integer, SerializableImage> opponentHandInstances;
    private Map<Integer, SerializableImage> ourHandInstances;
    private Map<Integer, Sound> marblesHittingSounds;
    private Map<Integer, Sound> givingMarblesAwaySounds;
    private String gameState;
    private boolean turnCurrent, endOfAct, currentReady, opponentReady;

    public Room(EntryPoint entryPoint){
        this.entryPoint = entryPoint;

        stage = new Stage();

        gameState = Constants.WAITING_FOR_PLAYER_CONNECT;

        currentReady = false;
        opponentReady = false;
        endOfAct = false;

        //threadFactory = new ThreadFactory();

        //threadFactory.createAndAdd(this::initAcceptingThread, "accepting_thread", true);
        //threadFactory.createAndAdd(this::initNetUpdateListener, "net_update_listener_thread", true);
        //threadFactory.createAndAdd(this::initGameEventListener, "event_update_manager_thread", true);
        gameEventThread = new Thread(this::initGameEventListener, "event_update_manager_thread");
        gameEventThread.setDaemon(true);

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

        current = new Player(new SerializableImage((new Texture(files.internal("textures/ou_h_c.png")))),
                new SerializableImage(), Constants.WIDTH - Player.getDefaultHandWidth(), 0);
        opponent = new Player(new SerializableImage((new Texture(files.internal("textures/op_h_c.png")))),
                new SerializableImage(), 0, Constants.HEIGHT - Player.getDefaultHandHeight());

        gson = new Gson();

        initBackground();
        initTokenLabel();
        initStartButton();
        initBetSelectionWindow();
        initStatementSelectionWindow();
    }

    @Override
    public void show() {

        initTokenArea();

        initWebSocketListener(entryPoint.serverConnection);

        //threadFactory.startThread("accepting_thread");

        stage.addActor(background);

        stage.addActor(current.getPlayerHandClosed());
        stage.addActor(opponent.getPlayerHandClosed());
        stage.addActor(current.getPlayerHandOpened());
        stage.addActor(opponent.getPlayerHandOpened());

        stage.addActor(betSelection);
        stage.addActor(statementSelection);

        if (entryPoint.clientType.equals(ClientType.INITIATOR)) {
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
        if (gameState.equals(Constants.GAME_RUNNING)) {
            String text;

            synchronized (this) {
                text = turnCurrent ? "Твой ход" : "Ход соперника";
            }

            marblesAmountLayout.setText(indicatorFont, "Шары: " + current.getMarblesAmount());
            turnLayout.setText(indicatorFont, text);

            indicatorFont.draw(entryPoint.batch, turnLayout, (float) Constants.WIDTH/6 - turnLayout.width/2, turnLayout.height + 10);
            indicatorFont.draw(entryPoint.batch, marblesAmountLayout, (float) Constants.WIDTH/6*5 - marblesAmountLayout.width/2, turnLayout.height + 10);

            //threadFactory.startThread("event_update_manager_thread");
            if (!gameEventThread.isAlive())
                gameEventThread.start();

            if (currentReady) {
                current.setHandVisible(current.getPlayerHandClosed(), true);
            }
            if (opponentReady) {
                opponent.setHandVisible(opponent.getPlayerHandClosed(), true);
            }
            if (endOfAct){
                current.setHandVisible(current.getPlayerHandClosed(), false);
                opponent.setHandVisible(opponent.getPlayerHandClosed(), false);
                current.setHandVisible(current.getPlayerHandOpened(), true);
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
        disposeSounds();
    }


    // ################################## threads init methods #################################

    //private void initAcceptingThread(){
    //    try {
    //        entryPoint.client = entryPoint.server.accept();
//
    //    } catch (NullPointerException ignore){
    //    } catch (IOException e) {
    //        System.exit(5);
    //    }
    //    receiver = new Receiver(entryPoint.client);
    //    gameState = Constants.WAITING_FOR_START;
    //    threadFactory.startThread("net_update_listener_thread");
    //}

    private void initWebSocketListener(final WebSocket client) {
        client.addListener(new WebSocketAdapter() {
            @Override
            public void onTextMessage(WebSocket websocket, String text) throws Exception {
                Message message = gson.fromJson(text, Message.class);
                String messageType = message.getMessageType();

                switch (messageType) {

                    // подсоединение к комнате
                    case MessageType.ROOM_JOIN -> {
                        stage.addActor(startButton);
                        gameState = Constants.WAITING_FOR_START;
                    }

                    // игра в процессе
                    case MessageType.GAME_IN_PROCESS -> {
                        opponent.setBet(message.getBet());
                        opponent.setMarblesAmount(message.getMarblesAmount());
                        opponent.setStatement(message.getStatement());

                        gameState = message.getGameState();
                        turnCurrent = !message.getTurnOrder();

                        opponentReady = message.isPlayerReady();

                        if (entryPoint.clientType.equals(ClientType.JOINER)
                                && gameEventThread.getState().equals(Thread.State.WAITING)) {
                            synchronized (gameEventThread) {
                                gameEventThread.notify();
                            }
                        }
                    }

                }
            }
        });
    }

   //private void initNetUpdateListener(){

   //    DataPacket currPacket;
   //    Player abstractPlayer;

   //    while (!gameState.equals(Constants.GAME_FINISHED)) {

   //        currPacket = receiver.getData();
   //        if (currPacket == null) continue;
   //        abstractPlayer = currPacket.getPlayerData();


   //        synchronized (this) {
   //            gameState = currPacket.getGameState();
   //            opponentReady = currPacket.getPlayerReady();
   //            turnCurrent = !currPacket.getTurnOrder();
   //        }
   //    }
   //}

    private void initGameEventListener() {

        while (!gameState.equals(Constants.GAME_FINISHED)) {

            synchronized (this) {
                if (turnCurrent && !currentReady)
                    GameUtils.setActorVisible(betSelection, true);
            }

            synchronized (this) {
                if (!turnCurrent && opponentReady && !currentReady)
                    GameUtils.setActorVisible(betSelection, true);
            }

            if (currentReady && opponentReady) {

                // задержка для отображения текстуры закрытой руки
                GameUtils.timedWaiting(SECONDS, 2);

                currentReady = false;
                opponentReady = false;
                endOfAct = true;

                final int currentMarblesAmount = current.getMarblesAmount();
                final int opponentMarblesAmount = opponent.getMarblesAmount();
                final String currentStatement = current.getStatement();
                final String opponentStatement = opponent.getStatement();

                int currentBet = current.getBet();
                int opponentBet = opponent.getBet();
                int opponentMarblesAmountOnHand = 0, currentMarblesAmountOnHand = 0;

                current.setPlayerHandOpened(ourHandInstances.get(currentBet));
                opponent.setPlayerHandOpened(opponentHandInstances.get(opponentBet));

                // задержка для отображения текстуры открытой руки перед изменением
                GameUtils.timedWaiting(SECONDS, 3);

                // текущий игрок делает ставку,
                // оппонент угадывает
                if (turnCurrent){

                    if ((opponentStatement.equals(Constants.EVEN) && GameUtils.isEven(currentBet)) ||
                            (opponentStatement.equals(Constants.ODD) && GameUtils.isOdd(currentBet))){
                        if (currentBet <= opponentBet) {
                            opponent.setMarblesAmount(opponentMarblesAmount + currentBet);
                            current.setMarblesAmount(currentMarblesAmount - currentBet);

                            opponentMarblesAmountOnHand = opponentBet + currentBet;
                        } else {
                            opponent.setMarblesAmount(opponentMarblesAmount + opponentBet);
                            current.setMarblesAmount(currentMarblesAmount - opponentBet);

                            currentMarblesAmountOnHand = currentBet - opponentBet;
                            opponentMarblesAmountOnHand = opponentBet*2;
                        }

                    } else{

                        if (currentBet <= opponentBet) {
                            current.setMarblesAmount(currentMarblesAmount + currentBet);
                            opponent.setMarblesAmount(opponentMarblesAmount - currentBet);

                            currentMarblesAmountOnHand = currentBet*2;
                            opponentMarblesAmountOnHand = opponentBet - currentBet;
                        } else {
                            current.setMarblesAmount(currentMarblesAmount + opponentBet);
                            opponent.setMarblesAmount(opponentMarblesAmount - opponentBet);

                            currentMarblesAmountOnHand = currentBet + opponentBet;
                        }

                    }
                }

                // оппонент делает ставку,
                // текущий игрок угадывает
                if (!turnCurrent){

                    if ((currentStatement.equals(Constants.EVEN) && GameUtils.isEven(opponentBet)) ||
                            (currentStatement.equals(Constants.ODD) && GameUtils.isOdd(opponentBet))){
                        if (currentBet <= opponentBet) {
                            current.setMarblesAmount(currentMarblesAmount + currentBet);
                            opponent.setMarblesAmount(opponentMarblesAmount - currentBet);

                            currentMarblesAmountOnHand = currentBet*2;
                            opponentMarblesAmountOnHand = opponentBet - currentBet;
                        } else {
                            current.setMarblesAmount(currentMarblesAmount + opponentBet);
                            opponent.setMarblesAmount(opponentMarblesAmount - opponentBet);

                            currentMarblesAmountOnHand = currentBet + opponentBet;
                            opponentMarblesAmountOnHand = 0;
                        }

                    } else {

                        if (currentBet <= opponentBet) {
                            current.setMarblesAmount(currentMarblesAmount - currentBet);
                            opponent.setMarblesAmount(opponentMarblesAmount + currentBet);

                            currentMarblesAmountOnHand = 0;
                            opponentMarblesAmountOnHand = opponentBet + currentBet;
                        } else {
                            current.setMarblesAmount(currentMarblesAmount - opponentBet);
                            opponent.setMarblesAmount(opponentMarblesAmount + opponentBet);

                            currentMarblesAmountOnHand = currentBet - opponentBet;
                            opponentMarblesAmountOnHand = opponentBet * 2;
                        }

                    }
                }

                givingMarblesAwaySounds.get(MathUtils.random(0, givingMarblesAwaySounds.size() - 1)).play();

                current.setPlayerHandOpened(ourHandInstances.get(GameUtils.checkValue(currentMarblesAmountOnHand)));
                opponent.setPlayerHandOpened(opponentHandInstances.get(GameUtils.checkValue(opponentMarblesAmountOnHand)));

                // задержка перед обновлением сцены
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
                if (gameState.equals(Constants.WAITING_FOR_START)) {
                    choosePlayerTurn();

                    GameUtils.setActorVisible(startButton, false);
                    GameUtils.setActorVisible(tokenArea, false);
                    GameUtils.setActorVisible(tokenLabel, false);
                }
            }
        });

    }

    private void initBackground(){
        background.setPosition(0, 0);
        background.setSize(Constants.WIDTH, Constants.HEIGHT);
    }

    private void initTokenArea() {
        String text = entryPoint.currentRoomId;

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

                int bet = betSelection.getSelected();

                current.setBet(bet);
                current.setPlayerHandOpened(ourHandInstances.get(bet));

                if (turnCurrent) {

                    marblesHittingSounds.get(MathUtils.random(0, marblesHittingSounds.size() - 1)).play();

                    currentReady = true;

                    GameUtils.setActorVisible(betSelection, false);

                    Message message = new Message();

                    message.setRoomId(entryPoint.currentRoomId);
                    message.setMessageType(MessageType.GAME_IN_PROCESS);
                    message.setClientType(entryPoint.clientType);

                    message.setGameState(gameState);
                    message.setTurnOrder(turnCurrent);
                    message.setPlayerReady(currentReady);

                    message.setBet(current.getBet());
                    message.setMarblesAmount(current.getMarblesAmount());

                    entryPoint.serverConnection.sendText(gson.toJson(message));

                    //receiver.sendData(new DataPacket(gameState, turnCurrent, currentReady, current));

                    betMadeSound.play();

                    return;
                }

                marblesHittingSounds.get(MathUtils.random(0, marblesHittingSounds.size() - 1)).play();

                GameUtils.setActorVisible(statementSelection, true);

            }
        });

        GameUtils.setActorVisible(betSelection, false);
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

                current.setStatement(statementSelection.getSelected());
                currentReady = true;

                GameUtils.setActorVisible(betSelection, false);
                GameUtils.setActorVisible(statementSelection, false);

                betMadeSound.play();

                Message message = new Message();

                message.setRoomId(entryPoint.currentRoomId);
                message.setMessageType(MessageType.GAME_IN_PROCESS);
                message.setClientType(entryPoint.clientType);

                message.setGameState(gameState);
                message.setTurnOrder(turnCurrent);
                message.setPlayerReady(currentReady);

                message.setBet(current.getBet());
                message.setMarblesAmount(current.getMarblesAmount());
                message.setStatement(current.getStatement());

                entryPoint.serverConnection.sendText(gson.toJson(message));

                //receiver.sendData(new DataPacket(gameState, turnCurrent, currentReady, current));

            }
        });

        GameUtils.setActorVisible(statementSelection, false);
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
        if (current.getMarblesAmount() == 0){
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
        gameState = Constants.RANDOMIZING_TURN;
        boolean[] turnVariants = new boolean[]{true, false};

        turnCurrent = turnVariants[MathUtils.random(0, turnVariants.length - 1)];

        gameState = Constants.GAME_RUNNING;

        Message message = new Message();

        message.setRoomId(entryPoint.currentRoomId);
        message.setMessageType(MessageType.GAME_IN_PROCESS);
        message.setClientType(entryPoint.clientType);

        message.setGameState(gameState);
        message.setTurnOrder(turnCurrent);
        message.setPlayerReady(currentReady);

        //message.setBet(current.getBet());
        //message.setMarblesAmount(current.getMarblesAmount());

        entryPoint.serverConnection.sendText(gson.toJson(message));

        //receiver.sendData(new DataPacket(gameState, turnCurrent, currentReady, current));
    }

    private void reset(){
        endOfAct = false;
        currentReady = false;
        opponentReady = false;

        current.setHandVisible(current.getPlayerHandOpened(), false);
        opponent.setHandVisible(opponent.getPlayerHandOpened(), false);
        betSelection.setItems(GameUtils.computeBetsRange(1, current.getMarblesAmount()));

        if (entryPoint.clientType.equals(ClientType.INITIATOR)) {
            // отправка данных клиенту с типом JOINER
            if (current.getMarblesAmount() == 1) turnCurrent = false;
            else if (opponent.getMarblesAmount() == 1) turnCurrent = true;
            else turnCurrent = !turnCurrent;

            Message message = new Message();

            message.setRoomId(entryPoint.currentRoomId);
            message.setMessageType(MessageType.GAME_IN_PROCESS);
            message.setClientType(entryPoint.clientType);

            message.setGameState(gameState);
            message.setTurnOrder(turnCurrent);
            message.setPlayerReady(currentReady);

            //message.setBet(current.getBet());
            message.setMarblesAmount(current.getMarblesAmount());

            entryPoint.serverConnection.sendText(gson.toJson(message));

            //receiver.sendData(new DataPacket(gameState, turnCurrent, currentReady, current));
        } else {
            // если клиент - JOINER, то он будет ждать получения сообщения от INITIATOR
            // TODO блокировать поток до получения сообщени
            try {
                synchronized (gameEventThread) {
                    gameEventThread.wait();
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            //GameUtils.timedWaiting(SECONDS, 1);
        }
    }

    private void finishGame(){

        gameState = Constants.GAME_FINISHED;

        turnCurrent = false;
        currentReady = false;
        opponentReady = false;
        endOfAct = false;

        current.setPlayerHandOpened(new SerializableImage());
        current.setMarblesAmount(5);
        current.setBet(0);
        current.setStatement(null);
        opponent.setPlayerHandOpened(new SerializableImage());
        opponent.setMarblesAmount(5);
        opponent.setBet(0);
        opponent.setStatement(null);

    }

}
