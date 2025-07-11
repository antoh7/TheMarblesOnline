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
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketFrame;
import ru.kbuearpov.themarblesonline.EntryPoint;
import ru.kbuearpov.themarblesonline.Player;
import ru.kbuearpov.themarblesonline.myImpls.SelectBox;
import ru.kbuearpov.themarblesonline.networking.Message;
import ru.kbuearpov.themarblesonline.utils.FontUtils;
import ru.kbuearpov.themarblesonline.utils.GameUtils;
import ru.kbuearpov.themarblesonline.utils.constants.NetConstants;

import java.util.HashMap;
import java.util.Map;

import static com.badlogic.gdx.Gdx.*;
import static com.badlogic.gdx.utils.Align.center;
import static java.util.concurrent.TimeUnit.SECONDS;
import static ru.kbuearpov.themarblesonline.utils.constants.DeviceConstants.*;
import static ru.kbuearpov.themarblesonline.utils.constants.GameConstants.*;

@SuppressWarnings("SynchronizeOnNonFinalField")
public class Room implements Screen {

    private final EntryPoint entryPoint;
    private final Stage stage;

    private final Image background;
    private final SelectBox<Integer> betSelection;
    private final SelectBox<String> statementSelection;
    private final TextButton startButton;
    private final Label tokenArea, tokenLabel;
    private Map<Integer, Image> opponentHandInstances, ourHandInstances;

    private final BitmapFont indicatorFont;
    private final GlyphLayout marblesAmountLayout, turnLayout;

    private final Sound betMadeSound;
    private Map<Integer, Sound> marblesHittingSounds, givingMarblesAwaySounds;

    private final Player current, opponent;
    private Thread gameEventThread;

    private String gameState;

    private boolean turnCurrent, endOfAct, currentReady, opponentReady;
    private volatile boolean showExceptionMessage;

    private final BitmapFont exceptionFont;
    private final GlyphLayout exceptionLayout;
    private final int indent;

    public Room(EntryPoint entryPoint){
        this.entryPoint = entryPoint;

        stage = new Stage();

        gameState = WAITING_FOR_PLAYER_CONNECT;

        indicatorFont = FontUtils.generateFont(files.internal("fonts/indicatorFont.ttf"), 80, Color.ROYAL, CHARACTERS);

        marblesAmountLayout = new GlyphLayout();
        turnLayout = new GlyphLayout();

        startButton = new TextButton("НАЧАТЬ", new Skin(files.internal("buttons/startbuttonassets/startbuttonskin.json")));
        betSelection = new SelectBox<>(new Skin(files.internal("widgets/selectlist/selectlistskin.json")));
        statementSelection = new SelectBox<>(new Skin(files.internal("widgets/selectlist/selectlistskin.json")));

        tokenArea = new Label("", new Skin(files.internal("widgets/tokenlabel/tokenlabelskin.json")));
        tokenLabel = new Label("", new Skin(files.internal("widgets/tokenlabel/tokenlabelskin.json")));

        background = new Image(new Texture(files.internal("textures/game_background.jpg")));
        loadImages();

        betMadeSound = audio.newSound(files.internal("sounds/bet_made.mp3"));
        loadSounds();

        current = new Player(new Image((new Texture(files.internal("textures/ou_h_c.png")))),
                new Image(), WIDTH - Player.getDefaultHandWidth(), 0);
        opponent = new Player(new Image((new Texture(files.internal("textures/op_h_c.png")))),
                new Image(), 0, HEIGHT - Player.getDefaultHandHeight());

        exceptionFont = FontUtils.generateFont(files.internal("fonts/defeatFont.otf"), 50, Color.RED, CHARACTERS);
        exceptionLayout = new GlyphLayout(exceptionFont, "");

        indent = 20;

        initBackground();
        initTokenLabel();
        initStartButton();
        initBetSelectionWindow();
        initStatementSelectionWindow();
    }

    @Override
    public void show() {

        gameEventThread = new Thread(this::initGameEventListener, "game_event_thread");
        gameEventThread.setDaemon(true);

        if (!entryPoint.canBeRestarted) {
            initTokenArea();

            initWebSocketListener(entryPoint.serverConnection);

            stage.addActor(background);

            stage.addActor(current.getPlayerHandClosed());
            stage.addActor(opponent.getPlayerHandClosed());
            stage.addActor(current.getPlayerHandOpened());
            stage.addActor(opponent.getPlayerHandOpened());

            stage.addActor(betSelection);
            stage.addActor(statementSelection);

            if (entryPoint.clientType.equals(NetConstants.INITIATOR)) {
                stage.addActor(tokenLabel);
                stage.addActor(tokenArea);
            }

        } else {
            if (entryPoint.clientType.equals(NetConstants.INITIATOR))
                GameUtils.setActorVisible(startButton, true);
        }

        input.setInputProcessor(stage);

    }

    @Override
    public void render(float delta){

        stage.act(delta);
        stage.draw();

        entryPoint.batch.begin();

        // отрисовка рук
        if (gameState.equals(GAME_RUNNING)) {

            String text;
            synchronized (this) {
                text = turnCurrent ? "Твой ход" : "Ход соперника";
            }

            marblesAmountLayout.setText(indicatorFont, "Шары: " + current.getMarblesAmount());
            turnLayout.setText(indicatorFont, text);

            indicatorFont.draw(entryPoint.batch, turnLayout, (float) WIDTH/6 - turnLayout.width/2, turnLayout.height + 10);
            indicatorFont.draw(entryPoint.batch, marblesAmountLayout, (float) WIDTH/6*5 - marblesAmountLayout.width/2, turnLayout.height + 10);
            if (showExceptionMessage)
                exceptionFont.draw(entryPoint.batch, exceptionLayout, indent, HEIGHT - 35);

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
        showExceptionMessage = false;
    }

    @Override
    public void dispose() {
        stage.dispose();
        betMadeSound.dispose();
        indicatorFont.dispose();
        exceptionFont.dispose();

        disposeSounds();
    }


    // ################################## слушатель сети #################################

    private void initWebSocketListener(WebSocket client) {
        client.clearListeners();
        client.addListener(new WebSocketAdapter() {
            @Override
            public void onTextMessage(WebSocket websocket, String text) {
                Message message = entryPoint.converter.fromJson(text, Message.class);
                String messageType = message.getMessageType();

                switch (messageType) {

                    // подсоединение к комнате
                    case NetConstants.ROOM_JOIN -> {
                        stage.addActor(startButton);
                        gameState = WAITING_FOR_START;
                    }

                    // игра в процессе
                    case NetConstants.GAME_IN_PROCESS -> {
                        opponent.setBet(message.getBet());
                        opponent.setMarblesAmount(message.getMarblesAmount());
                        opponent.setStatement(message.getStatement());

                        gameState = message.getGameState();
                        // инверсия boolean значения для получения актуального значения очереди хода
                        turnCurrent = !message.isTurnOrder();
                        opponentReady = message.isPlayerReady();

                        if (entryPoint.clientType.equals(NetConstants.JOINER)
                                && gameEventThread.getState().equals(Thread.State.WAITING)) {
                            synchronized (gameEventThread) {
                                gameEventThread.notify();
                            }
                        }

                        if (message.isRestartAvailable()) {
                            entryPoint.canBeRestarted = true;
                            gameState = WAITING_FOR_START;
                        }
                    }

                }
            }

            @Override
            public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame,
                                       WebSocketFrame clientCloseFrame, boolean closedByServer) {
                exceptionLayout.setText(exceptionFont, "Соединение с сервером было закрыто");
                showExceptionMessage = true;
            }
        });
    }

    // ################################## слушатель игровых событий #################################

    private void initGameEventListener() {

        // event loop
        while (true) {

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

                int currentMarblesAmount = current.getMarblesAmount();
                int opponentMarblesAmount = opponent.getMarblesAmount();
                String currentStatement = current.getStatement();
                String opponentStatement = opponent.getStatement();

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

                    if ((opponentStatement.equals(EVEN) && GameUtils.isEven(currentBet)) ||
                            (opponentStatement.equals(ODD) && GameUtils.isOdd(currentBet))){
                        if (currentBet <= opponentBet) {
                            opponent.setMarblesAmount(opponentMarblesAmount + currentBet);
                            current.setMarblesAmount(currentMarblesAmount - currentBet);

                            opponentMarblesAmountOnHand = opponentBet + currentBet;
                        } else {
                            opponent.setMarblesAmount(opponentMarblesAmount + opponentBet);
                            current.setMarblesAmount(currentMarblesAmount - opponentBet);

                            currentMarblesAmountOnHand = currentBet - opponentBet;
                            opponentMarblesAmountOnHand = opponentBet * 2;
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

                    if ((currentStatement.equals(EVEN) && GameUtils.isEven(opponentBet)) ||
                            (currentStatement.equals(ODD) && GameUtils.isOdd(opponentBet))){
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

                if(checkGameFinished()) {
                    break;
                }

                reset();

            }
        }
    }

    // $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$ инициализация виджетов $$$$$$$$$$$$$$$$$$$$$$$$$$

    private void initStartButton(){
        startButton.setSize(WIDGET_PREFERRED_WIDTH + 100, WIDGET_PREFERRED_HEIGHT + 35);
        startButton.setPosition((float) WIDTH/2 - startButton.getWidth() / 2,
                (float) HEIGHT/2 - startButton.getHeight() / 2);

        startButton.getLabel().setFontScale(MathUtils.floor(startButton.getWidth()/startButton.getMinWidth()),
                MathUtils.floor(startButton.getHeight()/startButton.getMinHeight()));

        startButton.addListener(new ClickListener() {
            @Override
            public void clicked (InputEvent event, float x, float y) {
                if (gameState.equals(WAITING_FOR_START)) {
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
        background.setSize(WIDTH, HEIGHT);
    }

    private void initTokenArea() {
        String text = entryPoint.currentRoomId;

        tokenArea.setSize((float) WIDTH/2, WIDGET_PREFERRED_HEIGHT - 20);
        tokenArea.setPosition((float) WIDTH/2 - tokenArea.getWidth()/2,
                (float) HEIGHT/2 - tokenArea.getHeight()*2 - 30);

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
        tokenLabel.setSize(WIDGET_PREFERRED_WIDTH + 100, WIDGET_PREFERRED_HEIGHT - 20);
        tokenLabel.setPosition((float) WIDTH/2 - tokenLabel.getWidth()/2,
                (float) HEIGHT/2 - tokenLabel.getHeight()*2 - 50 - tokenLabel.getHeight());

        tokenLabel.setAlignment(center);

        tokenLabel.setText("(КЛИК ЧТОБЫ СКОПИРОВАТЬ)");

        tokenLabel.setFontScale(MathUtils.ceil(tokenLabel.getWidth()/ tokenLabel.getMinWidth()),
                MathUtils.ceil(tokenLabel.getHeight()/ tokenLabel.getMinHeight()));

    }


    private void initBetSelectionWindow(){
        betSelection.setAlignment(center);
        betSelection.setSize(WIDGET_PREFERRED_WIDTH + 100, WIDGET_PREFERRED_HEIGHT + 35);
        betSelection.setPosition((float) WIDTH / 2 - betSelection.getWidth() / 2,
                (float) HEIGHT / 2);

        betSelection.getScrollPane().getList().setAlignment(center);

        betSelection.getStyle().listStyle.selection.setBottomHeight(MathUtils.floor((float) (HEIGHT/4) / 9));

        betSelection.setItems(GameUtils.computeBetsRange(1, 5));

        betSelection.addListener(new ClickListener() {
            @Override
            public void clicked (InputEvent event, float x, float y) {
                betSelection.setForward(true);
            }
        });
        betSelection.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {

                if (!betSelection.isForward()) return;

                int bet = betSelection.getSelected();

                current.setBet(bet);
                current.setPlayerHandOpened(ourHandInstances.get(bet));

                if (turnCurrent) {

                    marblesHittingSounds.get(MathUtils.random(0, marblesHittingSounds.size() - 1)).play();

                    currentReady = true;

                    GameUtils.setActorVisible(betSelection, false);

                    Message message = Message.builder()
                                    .roomId(entryPoint.currentRoomId)
                                    .messageType(NetConstants.GAME_IN_PROCESS)
                                    .clientType(entryPoint.clientType)
                                    .gameState(gameState)
                                    .turnOrder(turnCurrent)
                                    .playerReady(currentReady)
                                    .bet(current.getBet())
                                    .marblesAmount(current.getMarblesAmount())
                                    .build();

                    entryPoint.serverConnection.sendText(entryPoint.converter.toJson(message));

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
        statementSelection.setSize(WIDGET_PREFERRED_WIDTH + 100, WIDGET_PREFERRED_HEIGHT + 35);
        statementSelection.setPosition((float) WIDTH / 2 - statementSelection.getWidth() / 2,
                (float) HEIGHT / 2);

        statementSelection.getScrollPane().getList().setAlignment(center);
        statementSelection.getStyle().listStyle.selection.setBottomHeight(0);

        statementSelection.getStyle().listStyle.selection.setBottomHeight(MathUtils.floor((float) (HEIGHT/4) / 2));

        statementSelection.setItems(Array.with(ODD, EVEN));
        statementSelection.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {

                current.setStatement(statementSelection.getSelected());
                currentReady = true;

                GameUtils.setActorVisible(betSelection, false);
                GameUtils.setActorVisible(statementSelection, false);

                betMadeSound.play();

                Message message = Message.builder()
                        .roomId(entryPoint.currentRoomId)
                        .messageType(NetConstants.GAME_IN_PROCESS)
                        .clientType(entryPoint.clientType)
                        .gameState(gameState)
                        .turnOrder(turnCurrent)
                        .playerReady(currentReady)
                        .bet(current.getBet())
                        .marblesAmount(current.getMarblesAmount())
                        .statement(current.getStatement())
                        .build();

                entryPoint.serverConnection.sendText(entryPoint.converter.toJson(message));

            }
        });

        GameUtils.setActorVisible(statementSelection, false);
    }


    // &&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&& загрузчики &&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&

    private void loadImages(){
        // загрузка текстур рук
        opponentHandInstances = new HashMap<>();
        ourHandInstances = new HashMap<>();

        for(int i = 0; i < 11; i++) {
            opponentHandInstances.put(i, new Image(new Texture(files.internal("textures/op_h_" + i + "_o.png"))));
            ourHandInstances.put(i, new Image(new Texture(files.internal("textures/ou_h_" + i + "_o.png"))));
        }
    }

    private void loadSounds(){
        marblesHittingSounds = new HashMap<>();
        givingMarblesAwaySounds = new HashMap<>();

        for(int i = 0; i <= 5; i++) {
            marblesHittingSounds.put(i, audio.newSound(files.internal("sounds/marbles_hitting_" + i + ".mp3")));
            givingMarblesAwaySounds.put(i, audio.newSound(files.internal("sounds/giving_marbles_away_" + i + ".mp3")));
        }

    }

    private void disposeSounds() {

        for(int i = 0; i < 3; i++) {
            marblesHittingSounds.get(i).dispose();
            givingMarblesAwaySounds.get(i).dispose();
        }

    }


    // ********************************* методы в процессе игры ********************************

    private boolean checkGameFinished() {
        // проверка игры на окончание
        if (current.getMarblesAmount() == 0) {
            finishGame();
            entryPoint.setScreen(entryPoint.defeatScreen);
            return true;
        }
        if (opponent.getMarblesAmount() == 0) {
            finishGame();
            entryPoint.setScreen(entryPoint.victoryScreen);
            return true;
        }
        return false;
    }

    private void choosePlayerTurn(){
        // выбор очередности хода в начале игры
        boolean[] turnVariants = new boolean[]{true, false};

        turnCurrent = turnVariants[MathUtils.random(0, turnVariants.length - 1)];

        gameState = GAME_RUNNING;

        Message message = Message.builder()
                .roomId(entryPoint.currentRoomId)
                .messageType(NetConstants.GAME_IN_PROCESS)
                .clientType(entryPoint.clientType)
                .gameState(gameState)
                .turnOrder(turnCurrent)
                .playerReady(currentReady)
                .build();

        entryPoint.serverConnection.sendText(entryPoint.converter.toJson(message));

    }

    private void reset(){
        endOfAct = false;
        currentReady = false;
        opponentReady = false;

        current.setHandVisible(current.getPlayerHandOpened(), false);
        opponent.setHandVisible(opponent.getPlayerHandOpened(), false);
        betSelection.setItems(GameUtils.computeBetsRange(1, current.getMarblesAmount()));

        if (entryPoint.clientType.equals(NetConstants.INITIATOR)) {
            // отправка данных клиенту с типом JOINER
            if (current.getMarblesAmount() == 1) turnCurrent = false;
            else if (opponent.getMarblesAmount() == 1) turnCurrent = true;
            else turnCurrent = !turnCurrent;

            Message message = Message.builder()
                    .roomId(entryPoint.currentRoomId)
                    .messageType(NetConstants.GAME_IN_PROCESS)
                    .clientType(entryPoint.clientType)
                    .gameState(gameState)
                    .turnOrder(turnCurrent)
                    .playerReady(currentReady)
                    .marblesAmount(current.getMarblesAmount())
                    .build();

            entryPoint.serverConnection.sendText(entryPoint.converter.toJson(message));

        } else {
            // если клиент - JOINER, то он будет ждать получения сообщения от INITIATOR
            try {
                synchronized (gameEventThread) {
                    gameEventThread.wait();
                }
            } catch (InterruptedException e) {
                exceptionLayout.setText(exceptionFont, "Игровой обработчик событий был прерван");
                showExceptionMessage = true;
            }
        }
    }

    private void finishGame(){

        gameState = GAME_FINISHED;

        turnCurrent = false;
        currentReady = false;
        opponentReady = false;
        endOfAct = false;

        finalizePlayer(opponent);
        finalizePlayer(current);

        betSelection.setItems(GameUtils.computeBetsRange(1, 5));

    }

    private void finalizePlayer(Player player) {
        GameUtils.setActorVisible(player.getPlayerHandOpened(), false);
        player.setMarblesAmount(5);
        player.setBet(0);
        player.setStatement(null);
    }

}
