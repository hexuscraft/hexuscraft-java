package net.hexuscraft.common.database.messages;

public interface IMessage {
    static BaseMessage parse(final String jsonString) {
        return null;
    }

    String stringify();
}
