package ru.kbuearpov.themarblesonline.networking;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Message {

    // ********** обязательные параметры **********
    private String roomId;
    private String clientType;
    private String messageType;

    // ********** процесс игры **********
    private String gameState;
    private boolean turnOrder; // true - очередь отправителя, false - очередь получателя
    private boolean playerReady;

    // флаг, разрешающий начать игру заново
    private boolean restartAvailable;

    // игрок
    private int marblesAmount;
    private int bet;
    private String statement;

}
