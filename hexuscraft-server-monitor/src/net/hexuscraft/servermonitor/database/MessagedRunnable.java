package net.hexuscraft.servermonitor.database;

public final class MessagedRunnable implements Runnable {

    public String _message;

    private MessagedRunnable(String message) {
        _message = message;
    }

    public String getMessage() {
        return _message;
    }

    public void setMessage(String message) {
        _message = message;
    }

    @Override
    public void run() {
    }

}