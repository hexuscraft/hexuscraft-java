package net.hexuscraft.common.database.messages;

public interface IMessage
{
    static BaseMessage parse(String jsonString)
    {
        return null;
    }

    String stringify();
}
