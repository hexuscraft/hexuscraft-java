package net.hexuscraft.proxy.database;

public class MessagedRunnable implements Runnable {

    public String _message;

    protected MessagedRunnable(String message) {
        _message = message;
    }

    public final void setMessage(String message) {
        _message = message;
    }

    @SuppressWarnings("unused")
    public final String getMessage() {
        return _message;
    }

    @Override
    public void run() {
    }

}