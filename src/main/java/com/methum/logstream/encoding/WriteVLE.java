package com.methum.logstream.encoding;

public class WriteVLE {

    public byte[] write(int inputValue){

        int value  = inputValue;

        byte[] buffer = new byte[5];

        int position = 0;

        while(true){

            if ((value & ~0x7F)==0){
                buffer[position++] = (byte) value;
                break;
            }

            buffer[position++] = (byte) (value & 0x7F | 0x80);
            value >>>= 7;

        }

        byte[] result = new byte[position];
        System.arraycopy(buffer,0,result,0,position);
        return result;


    }

}
