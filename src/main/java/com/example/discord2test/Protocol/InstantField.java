package com.example.discord2test.Protocol;


import com.example.discord2test.HelperFunctions;

import java.nio.ByteBuffer;
import java.time.Instant;

//we should be able to implement this the same way that Java does for the Serialisation interface - just send
// the long seconds and int nanoseconds.
public class InstantField implements Field {

    public static final byte FIELD_ID = 5;

    private Instant value;

    public InstantField(Instant value) {
        this.value = value;
    }

    public InstantField() {
    }

    @Override
    public byte[] toByteString() {
        byte[] toReturn = new byte[Byte.BYTES + Long.BYTES + Integer.BYTES];
        toReturn[0] = FIELD_ID;
        HelperFunctions.InsertLongIntoByteArray(toReturn, value.getEpochSecond(), Byte.BYTES);
        HelperFunctions.InsertIntegerIntoByteArray(toReturn, value.getNano(), Byte.BYTES + Long.BYTES);
        return toReturn;
    }

    @Override
    public void fromByteForm(byte[] bytes) {
        ByteBuffer bb = ByteBuffer.allocate(Long.BYTES);
        long seconds = bb.put(bytes, 1, Long.BYTES).getLong();
        bb.rewind();
        int nano = bb.put(bytes, 1 + Long.BYTES, Integer.BYTES).getInt();
        value = Instant.ofEpochSecond(seconds, nano);
    }
}
