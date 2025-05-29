package ru.kbuearpov.themarblesonline;

import com.badlogic.gdx.scenes.scene2d.ui.Image;
import lombok.Getter;
import lombok.Setter;

import static ru.kbuearpov.themarblesonline.utils.constants.DeviceConstants.HEIGHT;
import static ru.kbuearpov.themarblesonline.utils.constants.DeviceConstants.WIDTH;

// сущность игрока
@Setter
@Getter
public class Player {

    private final Image playerHandClosed, playerHandOpened;

    private int marblesAmount, bet;
    private String statement;

    // параметры руки
    private final float defaultX, defaultY;
    private static final float defaultWidth = (float) WIDTH/3, defaultHeight = HEIGHT - (float) HEIGHT/3;

    public Player(final Image playerHandClosed, final Image playerHandOpened, final float handX, final float handY){

        this.marblesAmount = 5;

        this.playerHandClosed = playerHandClosed;
        this.playerHandOpened = playerHandOpened;
        this.defaultX = handX;
        this.defaultY = handY;

        initHand(this.playerHandClosed, defaultX, defaultY);
        initHand(this.playerHandOpened, defaultX, defaultY);

        setHandVisible(this.playerHandClosed, false);
        setHandVisible(this.playerHandOpened, false);
    }

    public void setPlayerHandOpened(final Image playerHandOpened){
        this.playerHandOpened.setDrawable(playerHandOpened.getDrawable());
        initHand(this.playerHandOpened, defaultX, defaultY);
    }

    public void setHandVisible(final Image hand, final boolean state) {
        hand.setVisible(state);
    }

    private void initHand(final Image hand, final float x, final float y) {
        hand.setPosition(x, y);
        hand.setSize(defaultWidth, defaultHeight);
    }

    public static float getDefaultHandWidth(){
        return defaultWidth;
    }

    public static float getDefaultHandHeight(){
        return defaultHeight;
    }

}
