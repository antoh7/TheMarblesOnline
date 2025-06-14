package ru.kbuearpov.themarblesonline.utils;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import lombok.experimental.UtilityClass;
import ru.kbuearpov.themarblesonline.EntryPoint;
import ru.kbuearpov.themarblesonline.utils.constants.NetConstants;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.badlogic.gdx.Gdx.app;

@UtilityClass
public class GameUtils {

    // процесс игры
    public static void setActorVisible(Actor actor, boolean visible){
        actor.setVisible(visible);
    }

    public static int checkValue(int value){
        if (value < 0){
            value = 0;
        }
        return value;
    }

    public static void timedWaiting(TimeUnit unit, int time){
        // остановка потока на время
        try {
            unit.sleep(time);
        } catch (InterruptedException e) {
            System.exit(10);
        }
    }

    public static Array<Integer> computeBetsRange(int start, int end) {
        // вычисление диапазона возможных ставок
        Array<Integer> bets = new Array<>();
        for (int i = start; i <= end; i++) {
            bets.add(i);
        }
        return bets;
    }

    public static boolean isEven(int value){
        return value % 2 == 0;
    }

    public static boolean isOdd(int value){
        return value % 2 != 0;
    }

    // ##########################################################

    public static String generateRoomId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 7);
    }

    public static void initServerConnection(EntryPoint entryPoint) throws Exception {
        try {
            entryPoint.serverConnection = new WebSocketFactory()
                    .createSocket("ws://{}/connection/new".replace("{}",
                            app
                                    .getPreferences(NetConstants.PREFS_NAME)
                                    .getString(NetConstants.PREFS_KEY)), 7000);
        } catch (IOException | IllegalArgumentException exception) {
            throw exception;
        }

        entryPoint.serverConnection.addHeader("X-CSUM", NetConstants.CHECKSUM);
        try {
            entryPoint.serverConnection.connect();
        } catch (WebSocketException exception) {
            throw exception;
        }
    }

}
