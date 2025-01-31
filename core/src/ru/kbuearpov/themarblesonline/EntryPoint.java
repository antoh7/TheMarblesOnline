package ru.kbuearpov.themarblesonline;

import static com.badlogic.gdx.Gdx.audio;
import static com.badlogic.gdx.Gdx.files;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Null;
import com.neovisionaries.ws.client.WebSocket;

import ru.kbuearpov.themarblesonline.screens.CreateRoom;
import ru.kbuearpov.themarblesonline.screens.DefeatScreen;
import ru.kbuearpov.themarblesonline.screens.JoinRoom;
import ru.kbuearpov.themarblesonline.screens.MainMenu;
import ru.kbuearpov.themarblesonline.screens.Room;
import ru.kbuearpov.themarblesonline.screens.VictoryScreen;

import java.net.ServerSocket;
import java.net.Socket;

/** Entrypoint of the game. When app starts, screens, backends, batches initialization occurs here.
 * @see Socket
 * @see ServerSocket
 * @see com.badlogic.gdx.Screen
 * @see com.badlogic.gdx.graphics.g2d.SpriteBatch
 */

 public class EntryPoint extends Game {

    public JoinRoom joinRoom;
    public CreateRoom createRoom;
    public MainMenu mainMenu;
    public Room room;
    public DefeatScreen defeatScreen;
    public VictoryScreen victoryScreen;
    public Music menuMusic;

    public SpriteBatch batch;

    // TODO удалить
    @Null
    public ServerSocket server;
    public Socket client;

    public WebSocket serverConnection;

    //invite token
    @Null
    public String inviteToken;

    //device state
    public String deviceState;


    @SuppressWarnings("NewApi")
    @Override
    public void create() {
        menuMusic = audio.newMusic(files.internal("sounds/menu_music.mp3"));
        menuMusic.setVolume(0.15f);
        menuMusic.setLooping(true);

        mainMenu = new MainMenu(this);
        joinRoom = new JoinRoom(this);
        createRoom = new CreateRoom(this);
        defeatScreen = new DefeatScreen(this);
        victoryScreen = new VictoryScreen(this);
        room = new Room(this);

        batch = new SpriteBatch();

        setScreen(mainMenu);

    }
}
