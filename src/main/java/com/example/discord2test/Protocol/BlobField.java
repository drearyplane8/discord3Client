package com.example.discord2test.Protocol;

//this Field is for binary data, which ought to never exceed 4*2^20 -1 = 2^22 - 1 = 4,194,303
//clearly too long for a 16 bit datatype
//the easy option would be to use an integer to store the length of the blob but that would waste a *whole* byte (!)
//but it would make things a lot easier...

public class BlobField implements Field {

    public static final byte FIELD_ID = 4;
    public static final int MAX_SIZE = (1 << Byte.SIZE * 3) - 1;

    private byte[] value;
    private int length;

    public BlobField(byte[] value) {
        if(value.length > MAX_SIZE ) {
            System.err.println("Too long for blob field");
            return;
        }
        this.value = value;
        this.length = value.length;
    }

    public BlobField() {
    }

    @Override
    public byte[] toByteString() {
        byte[] toReturn = new byte[1 + 3 + length];
        toReturn[0] = FIELD_ID;

        toReturn[1] = (byte) (length >> Byte.SIZE * 2);
        toReturn[2] = (byte) (length >> Byte.SIZE);
        toReturn[3] = (byte) (length);

        System.arraycopy(value, 0, toReturn, 4, length);
        return toReturn;
    }

    @Override
    public void fromByteForm(byte[] bytes) {
        length = bytes[3] +
                 bytes[2] << Byte.SIZE +
                 bytes[3] << (Byte.SIZE * 2);
        System.arraycopy(bytes, 4, value, 0, length);
    }
}
