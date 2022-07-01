package com.example.discord2test.Protocol;


public interface Field {

    byte[] toByteString();
    void fromByteForm(byte[] bytes);

}
