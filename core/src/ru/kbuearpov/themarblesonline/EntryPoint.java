package ru.kbuearpov.themarblesonline;

import static com.badlogic.gdx.Gdx.audio;
import static com.badlogic.gdx.Gdx.files;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.google.gson.Gson;
import com.neovisionaries.ws.client.WebSocket;

import ru.kbuearpov.themarblesonline.screens.CreateRoom;
import ru.kbuearpov.themarblesonline.screens.DefeatScreen;
import ru.kbuearpov.themarblesonline.screens.JoinRoom;
import ru.kbuearpov.themarblesonline.screens.MainMenu;
import ru.kbuearpov.themarblesonline.screens.Room;
import ru.kbuearpov.themarblesonline.screens.VictoryScreen;

public class EntryPoint extends Game {

    // экраны игры
    public JoinRoom joinRoom;
    public CreateRoom createRoom;
    public MainMenu mainMenu;
    public Room room;
    public DefeatScreen defeatScreen;
    public VictoryScreen victoryScreen;

    // музыка
    public Music menuMusic;

    // отрисовка
    public SpriteBatch batch;

    // соединение с игровым сервером
    public WebSocket serverConnection;

    // информация о комнате и игроке
    public String currentRoomId;
    public String clientType;
    public boolean mightBeRestarted;

    // конвертер сообщений
    public Gson converter;

    @Override
    public void create() {
        menuMusic = audio.newMusic(files.internal("sounds/menu_music.mp3"));
        menuMusic.setVolume(0.15f);
        menuMusic.setLooping(true);

        joinRoom = new JoinRoom(this);
        createRoom = new CreateRoom(this);
        mainMenu = new MainMenu(this);
        room = new Room(this);
        defeatScreen = new DefeatScreen(this);
        victoryScreen = new VictoryScreen(this);

        batch = new SpriteBatch();

        converter = new Gson();

        setScreen(mainMenu);
    }

}
