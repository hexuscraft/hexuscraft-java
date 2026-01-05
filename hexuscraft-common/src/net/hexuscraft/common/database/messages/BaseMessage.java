package net.hexuscraft.common.database.messages;

import redis.clients.jedis.UnifiedJedis;

public abstract class BaseMessage implements IMessage {

    public static String CHANNEL_NAME;

    public void publish(final UnifiedJedis jedis) {
        jedis.publish(CHANNEL_NAME, stringify());
    }

}
