package net.hexuscraft.core.portal;

import com.google.common.io.ByteArrayDataInput;

public final class ByteArrayDataInputRunnable implements Runnable {

    ByteArrayDataInput in;

    public void setIn(ByteArrayDataInput in) {
        this.in = in;
    }

    @Override
    public void run() {}

}
