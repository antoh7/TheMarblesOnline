package ru.kbuearpov.themarblesonline.utils;

import static com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.*;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import lombok.experimental.UtilityClass;

@UtilityClass
public class FontGenerator {

    public static BitmapFont generateFont(FileHandle pathToFont, int size, Color color, String characters){

        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(pathToFont);
        FreeTypeFontParameter parameter = new FreeTypeFontParameter();

        parameter.size = size;
        parameter.color = color;
        parameter.characters = characters;

        return generator.generateFont(parameter);

    }

}
