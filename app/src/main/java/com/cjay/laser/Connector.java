package com.cjay.laser;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Enumeration;

/**
 * Created by Christopher on 09.01.2018.
 */

public class Connector extends Thread implements Runnable{

    @Override
    public void run() {

        Log.i("Server","try startServer");
        try{
            InetAddress addr = InetAddress.getByName(getLocalIpAddress());
            Log.i("Server",getLocalIpAddress());
            ServerSocket server = new ServerSocket(8080, 0, addr);
            Log.i("Server","started");
            while (true){
                Socket socket = server.accept();
                Log.i("Server","client connected");
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

                System.out.println(reader);
                String line = "";
                while((line = reader.readLine()) != null){
                    writer.write(line);
                    writer.newLine();
                    writer.flush();
                    Log.i("Server",line);
                }

                reader.close();
                writer.close();
                socket.close();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private String getLocalIpAddress() throws Exception {
        String resultIpv6 = "";
        String resultIpv4 = "";

        for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
             en.hasMoreElements();) {

            NetworkInterface intf = en.nextElement();
            for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses();
                 enumIpAddr.hasMoreElements();) {

                InetAddress inetAddress = enumIpAddr.nextElement();
                if(!inetAddress.isLoopbackAddress()){
                    if (inetAddress instanceof Inet4Address) {
                        resultIpv4 = inetAddress.getHostAddress().toString();
                    } else if (inetAddress instanceof Inet6Address) {
                        resultIpv6 = inetAddress.getHostAddress().toString();
                    }
                }
            }
        }
        return ((resultIpv4.length() > 0) ? resultIpv4 : resultIpv6);
    }
}
