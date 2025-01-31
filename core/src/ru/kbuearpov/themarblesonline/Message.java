package ru.kbuearpov.themarblesonline;

public class Message {

    public Message() {}

    // конструктор для служебного сообщения
    public Message(final String messageType, final String notification) {
        this.messageType = messageType;
        this.notification = notification;
    }

    // ********** обязательные параметры **********
    private String roomId;
    private String clientType;
    private String messageType;

    // ********** процесс игры **********
    private String gameState;
    private boolean turnOrder; // true - очередь отправителя, false - очередь получателя
    private boolean playerReady;

    // окончание игры
    private boolean finished;

    // игрок
    private int marblesAmount;
    private int bet;
    private String statement;

    // ********** служебное сообщение **********
    private String notification;

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

    public boolean isTurnOrder() {
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

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
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

    public String getNotification() {
        return notification;
    }

    public void setNotification(String notification) {
        this.notification = notification;
    }
}
