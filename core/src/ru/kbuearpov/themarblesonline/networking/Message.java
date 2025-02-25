package ru.kbuearpov.themarblesonline.networking;

public class Message {

    public Message() {}

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


    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getClientType() {
        return clientType;
    }

    public void setClientType(String clientType) {
        this.clientType = clientType;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getGameState() {
        return gameState;
    }

    public void setGameState(String gameState) {
        this.gameState = gameState;
    }

    public boolean getTurnOrder() {
        return turnOrder;
    }

    public void setTurnOrder(boolean turnOrder) {
        this.turnOrder = turnOrder;
    }

    public boolean isPlayerReady() {
        return playerReady;
    }

    public void setPlayerReady(boolean playerReady) {
        this.playerReady = playerReady;
    }

    public boolean getRestartAvailable() {
        return restartAvailable;
    }

    public void setRestartAvailable(boolean restartAvailable) {
        this.restartAvailable = restartAvailable;
    }

    public int getMarblesAmount() {
        return marblesAmount;
    }

    public void setMarblesAmount(int marblesAmount) {
        this.marblesAmount = marblesAmount;
    }

    public int getBet() {
        return bet;
    }

    public void setBet(int bet) {
        this.bet = bet;
    }

    public String getStatement() {
        return statement;
    }

    public void setStatement(String statement) {
        this.statement = statement;
    }
}
