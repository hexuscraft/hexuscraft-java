package net.hexuscraft.core.portal;

import com.google.common.io.ByteArrayDataInput;

public abstract class ByteArrayDataInputRunnable implements Runnable
{

    ByteArrayDataInput _in;

    public ByteArrayDataInput getIn()
    {
        return _in;
    }

    public void setIn(ByteArrayDataInput in)
    {
        _in = in;
    }

    @Override
    public void run()
    {
    }

}
