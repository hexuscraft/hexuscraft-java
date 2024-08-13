package net.hexuscraft.core.portal;

import com.google.common.io.ByteArrayDataInput;

public abstract class ByteArrayDataInputRunnable implements Runnable {

    ByteArrayDataInput in;

    public void setIn(final ByteArrayDataInput in) {
        this.in = in;
    }

    public ByteArrayDataInput getIn() {
        return this.in;
    }

    @Override
    public void run() {}

}
