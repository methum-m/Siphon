package com.methum.logstream.encoding;

import java.io.DataInput;

import java.io.IOException;

public class ReadVLE {

    public int decode(DataInput di) throws IOException {


        int intValue = 0;
        int shift = 0;

        while (true){

            byte value = di.readByte();

            intValue = intValue | (value & 0x7F ) << shift;
            shift += 7;

            if ((value & 0x80)==0){
                return intValue;
            }

        }




    }
}
