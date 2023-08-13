package net.hexuscraft.core.disguise;

public class DisguiseUnsupportedEntityException extends Exception {

    final String _message;

    @Override
    public String getMessage() {
        return _message;
    }

    public DisguiseUnsupportedEntityException(String message) {
        _message = message;
    }


}
