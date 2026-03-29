package net.hexuscraft.common.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class UtilCooldown
{

    private static final Map<Object, Set<Cooldown>> _cooldownMap = new HashMap<>();

    public static Cooldown getCooldown(Object rawParent, String name)
    {
        Object parent = rawParent == null ? UtilCooldown.class : rawParent;

        if (!_cooldownMap.containsKey(parent))
        {
            return null;
        }
        for (Cooldown cooldown : _cooldownMap.get(parent))
        {
            if (!cooldown._name().equals(name))
            {
                continue;
            }
            return cooldown;
        }
        return null;
    }

    private static void addCooldown(Object rawParent, Cooldown cooldown)
    {
        Object parent = rawParent == null ? UtilCooldown.class : rawParent;

        if (!_cooldownMap.containsKey(parent))
        {
            _cooldownMap.put(parent, new HashSet<>());
        }

        Set<Cooldown> parentList = _cooldownMap.get(parent);
        parentList.add(cooldown);
        new Thread(() ->
                   {
                       try
                       {
                           Thread.sleep(cooldown._delayMs);
                           parentList.remove(cooldown);
                       }
                       catch (InterruptedException ex)
                       {
                           ex.printStackTrace();
                       }
                   }).start();
    }

    public static boolean use(Object parent, String name, Long delayMs)
    {
        Cooldown cooldown = getCooldown(parent, name);
        if (cooldown != null)
        {
            return false;
        }

        addCooldown(parent, new Cooldown(name, System.currentTimeMillis(), delayMs));
        return true;
    }

    public record Cooldown(String _name, long _started, long _delayMs)
    {

    }

}
