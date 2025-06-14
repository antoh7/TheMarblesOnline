package ru.kbuearpov.themarblesonline.utils;

import static com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.*;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import lombok.experimental.UtilityClass;

@UtilityClass
public class FontUtils {

    public static BitmapFont generateFont(FileHandle pathToFont, int size, Color color, String characters){
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(pathToFont);
        FreeTypeFontParameter parameter = new FreeTypeFontParameter();

        parameter.size = size;
        parameter.color = color;
        parameter.characters = characters;

        return generator.generateFont(parameter);
    }

    public static String computeBreakPosition(String text, BitmapFont font, int screenWidth, int indent) {
        GlyphLayout layout = new GlyphLayout(font, text);
        StringBuilder builder = new StringBuilder(text);

        int visiblePart = screenWidth - indent;
        float glyphWidth = layout.width / layout.glyphCount;
        int insertIndex = (int) (visiblePart / glyphWidth);
        int iter = 0;

        while (indent + layout.width > screenWidth) {
            iter++;
            layout.setText(font, builder.substring(insertIndex, text.length() - 1));
            int insertTo = insertIndex*iter;
            if (!(insertTo > builder.length()))
                builder.insert(insertTo, "\n");
        }
        return builder.toString();
    }

}
