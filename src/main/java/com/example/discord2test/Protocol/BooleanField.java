package com.example.discord2test.Protocol;


//a boolean field follows the old principle that zero == false, and non-zero == true. although true should only ever
//equal one. *shrugs*
public class BooleanField implements Field{

    public final static byte FIELD_ID = 2;

    private boolean value;

    public BooleanField() {
    }

    public BooleanField(boolean value) {
        this.value = value;
    }

    @Override
    public byte[] toByteString() {
        byte[] toReturn = new byte[2];
        toReturn[0] = FIELD_ID;

        if (value) toReturn[1] = 1;
        //otherwise, value is already zeroed by the instantiation.
        return toReturn;
    }

    @Override
    public void fromByteForm(byte[] bytes) {
        value = bytes[1] != 0;
    }

    public boolean getValue() {
        return value;
    }

    public void setValue(boolean value) {
        this.value = value;
    }
}
