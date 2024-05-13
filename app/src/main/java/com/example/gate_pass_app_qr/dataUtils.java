package com.example.gate_pass_app_qr;

import android.util.Base64;

public class dataUtils {

    public static String combineAndEncode(String data1, String data2) {
        // Combine data (modify as needed)
        String combinedData = data1 + "#" + data2;

        // Encode the combined data using Base64
        byte[] dataBytes = combinedData.getBytes();
        return Base64.encodeToString(dataBytes, Base64.DEFAULT);
    }

    // ... combineAndEncode method here ...

    public static String[] decodeAndSeparate(String encodedData) {
        // Decode the encoded data using Base64
        byte[] decodedBytes = Base64.decode(encodedData, Base64.DEFAULT);
        String decodedString = new String(decodedBytes);

        // Separate the data based on the separator used earlier (",")
        String[] separatedData = decodedString.split("#");
        return separatedData;
    }

//    String data1 = "This is data 1";
//    String data2 = "This is data 2";
//
//    String encodedData = DataUtils.combineAndEncode(data1, data2);
//      Log.d("Encoded Data", encodedData);
//
//    String[] decodedData = DataUtils.decodeAndSeparate(encodedData);
//      Log.d("Decoded Data 1", decodedData[0]);  // Output: This is data 1
//      Log.d("Decoded Data 2", decodedData[1]);  // Output: This is data 2

}
