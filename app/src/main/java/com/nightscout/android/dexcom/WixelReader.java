package com.nightscout.android.dexcom;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nightscout.core.dexcom.records.EGVRecord;

public class WixelReader {
    
    // todo, should not be static...
    public static boolean IsConfigured() {
        return true;
    }
    
    public static TransmitterRawData[] Read(String hostName,int port)
    {
        List<TransmitterRawData> trd_list = new ArrayList<TransmitterRawData>();
        TransmitterRawData []trd_array;
        try
        {
            
            Gson gson = new GsonBuilder().create();

            // An example of using gson.
            ComunicationHeader ch = new ComunicationHeader();
            ch.version = 1;
            ch.numberOfRecords = 300;
            String flat = gson.toJson(ch);
            ComunicationHeader ch2 = gson.fromJson(flat, ComunicationHeader.class);  
            System.out.println("Results code" + flat + ch2.version);


            // Real client code
            Socket MySocket = new Socket(hostName, port);

            System.out.println("After the new socket \n");
            MySocket.setSoTimeout(2000); 
                     
            System.out.println("client connected... " );
            
            PrintWriter out = new PrintWriter(MySocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(MySocket.getInputStream()));

            out.println(flat);
            
            while(true) {
                String data = in.readLine();
                if(data == null) {
                    System.out.println("recieved null exiting");
                    break;
                }
                if(data.equals("")) {
                    System.out.println("recieved \"\" exiting");
                    break;
                }

                System.out.println( "data size " +data.length() + " data = "+ data);
                TransmitterRawData trd = gson.fromJson(data, TransmitterRawData.class);
                trd_list.add(0,trd);
                System.out.println( trd.toTableString());
            }


            MySocket.close();
            trd_array = new TransmitterRawData[trd_list.size()];
            trd_list.toArray(trd_array);
            return trd_array;
        }catch(SocketTimeoutException s) {
            System.out.println("Socket timed out!...");
        }
        catch(IOException e) {
            e.printStackTrace();
            System.out.println("cought exception " + e.getMessage());
        }
        trd_array = new TransmitterRawData[trd_list.size()];
        trd_list.toArray(trd_array);
        return trd_array;
    }
    
    public static int ConvertTransmiterValues(int RawValue)
    {
        return (RawValue - 35000) / 667;
    }
    
    // last in the array, is first in time
    public static EGVRecord[] ConvertValues(TransmitterRawData[] RawData)
    {
        EGVRecord []EGVRecords = new EGVRecord[RawData.length];
        for(int i=0; i < RawData.length; i++) {
            EGVRecords[i] = new EGVRecord();
            EGVRecords[i].setBGValue(ConvertTransmiterValues(RawData[i].RawValue));
            EGVRecords[i].setDisplayTime(new Date(System.currentTimeMillis() - RawData[i].RelativeTime)) ;
        }
        return EGVRecords;
        
    }
    /*    
    private EGVRecord[] getRecentEGVsPages(int numOfRecentPages) {
        EGVRecord[] evgRecords;
        TransmitterRawData[] RawData = Read("192.168.1.25", 50005);
        
        return ConvertValues(RawData);
        
    }
    
    
    // last in the array, is first in time
    private EGVRecord[] getFakedRecentEGVsPages(int numOfRecentPages) {
        int data_size = numOfRecentPages * 20;
        EGVRecord[] evgRecord = new EGVRecord[data_size];
        for(int i=0; i < data_size; i++) {
            evgRecord[i] = new EGVRecord();
            evgRecord[i].setBGValue(40 + i%100);
            evgRecord[i].displayTime = new Date(System.currentTimeMillis() - 300000 *((data_size - i)%100));
        }
        return evgRecord;
    }
*/    
}
