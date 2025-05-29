package ru.kbuearpov.themarblesonline.myImpls;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Null;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SelectBox<T> extends com.badlogic.gdx.scenes.scene2d.ui.SelectBox<T> {

    private boolean forward;

    public SelectBox(Skin skin) {
        super(skin);
        forward = true;
    }

    @Override
    public void setItems(Array<T> newItems) {
        forward = false;
        super.setItems(newItems);
    }

    @Override
    public @Null T getSelected() {
        forward = false;
        return super.getSelected();
    }

}
