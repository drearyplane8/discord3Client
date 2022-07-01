package com.example.discord2test.Protocol;

import com.example.discord2test.HelperFunctions;

import java.nio.ByteBuffer;

public class IntField implements Field{

    private static final int INT_LENGTH = Integer.SIZE / Byte.SIZE;
    private static final byte FIELD_ID = 1;

    private int value;

    public IntField() {}

    public IntField(int value) {
        this.value = value;
    }

    @Override
    public byte[] toByteString() {
        byte[] toReturn = new byte[5];
        toReturn[FIELD_ID] = 1;
        HelperFunctions.InsertIntegerIntoByteArray(toReturn, value, 1);
        return toReturn;
    }

    @Override
    public void fromByteForm(byte[] bytes) {
        //ignore the first value
        value = ByteBuffer.allocate(INT_LENGTH).put(bytes, 1, INT_LENGTH).getInt();
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
