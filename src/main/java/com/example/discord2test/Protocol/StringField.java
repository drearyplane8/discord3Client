package com.example.discord2test.Protocol;

//StringField is our first variable length Field.
//we will use two bytes to carry the string length, hence theoretical max length
// is unsigned 16-bit integer = 65,535 BYTES.
//although nothing in the program should need this many characters, we will set the limit here, since it will be used
//to represent multiple things in the actual program, so validation for fields will be done elsewhere.

import com.example.discord2test.HelperFunctions;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

//java uses UTF-16: each character takes two bytes.
public class StringField implements Field {

    private String value;
    private char StringLengthInBytes;

    final private static byte FIELD_ID = 3;

    //results in 2^15 - 1 when characters are 2 bytes (16 bits)
    static final private int MAX_CHARACTER_COUNT = (1 << Character.SIZE - 1) - 1;

    public StringField() {
    }

    public StringField(String value) {
        if(value.length() > MAX_CHARACTER_COUNT) {
            System.err.println("Too long for StringField");
            return;
        }
        this.value = value;
        this.StringLengthInBytes = (char) (value.length() * 2);
    }

    @Override
    public byte[] toByteString() {
        //1 for type id, 2 for length, plus byte length for actual string
        byte[] toReturn = new byte[1 + 2 + StringLengthInBytes];
        toReturn[0] = FIELD_ID;
        HelperFunctions.InsertShortIntoByteArray(toReturn, StringLengthInBytes, 1);
        byte[] stringBytes = value.getBytes(StandardCharsets.UTF_16);
        System.arraycopy(stringBytes, 0, toReturn, 3, stringBytes.length);
        return toReturn;
    }

    @Override
    public void fromByteForm(byte[] bytes) {
        char length = ByteBuffer.allocate(2).put(bytes[0]).put(bytes[1]).getChar();
        char[] output = new char[length / 2];
        for(char i = 2; i < length; i += 2) {
            output[i / 2] = ReconstituteCharacter(bytes[i], bytes[i + 1]);
        }
        value = new String(output);
    }

    private char ReconstituteCharacter(byte upper, byte lower) {
        return (char) ((upper << Byte.SIZE) + lower);
    }

}
