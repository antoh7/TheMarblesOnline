package ru.kbuearpov.themarblesonline.utils;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;
import lombok.experimental.UtilityClass;

import java.util.concurrent.TimeUnit;

@UtilityClass
public class GameUtils {

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
        Array<Integer> integerArray = new Array<>();
        for (int i = start; i <= end; i++) {
            integerArray.add(i);
        }
        return integerArray;
    }

    public static boolean isEven(int value){
        return value % 2 == 0;
    }

    public static boolean isOdd(int value){
        return value % 2 != 0;
    }

}
