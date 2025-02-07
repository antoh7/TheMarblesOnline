package ru.kbuearpov.themarblesonline.utils.constants;

import static com.badlogic.gdx.Gdx.graphics;
import static com.badlogic.gdx.math.MathUtils.ceil;

public class DeviceConstants {

    private static final float widthProportion = 0.2f;
    private static final float heightProportion = 0.1f;

    //widgets
    public static final int WIDTH = graphics.getWidth();
    public static final int HEIGHT = graphics.getHeight();
    public static final int WIDGET_PREFERRED_WIDTH = ceil((float) WIDTH * widthProportion);
    public static final int WIDGET_PREFERRED_HEIGHT = ceil((float) HEIGHT * heightProportion);

    // font
    public static String CHARACTERS = "абвгдеёжзийклмнопрстуфхцчшщъыьэюяАБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯabcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ!:0123456789";
}
