package sample;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

public class DownloadImg {

    private void Download(String URL, String Name) throws Exception {

        try{
            String fileName = Name;
            String website = URL;

            System.out.println("Downloading File From: " + website);

            URL url = new URL(website);
            InputStream inputStream = url.openStream();
            OutputStream outputStream = new FileOutputStream(fileName);
            byte[] buffer = new byte[2048];

            int length = 0;

            while ((length = inputStream.read(buffer)) != -1) {
                System.out.println("Buffer Read of length: " + length);
                outputStream.write(buffer, 0, length);
            }

            inputStream.close();
            outputStream.close();

        } catch(Exception e) {
            System.out.println("Exception: " + e.getMessage());
        }

    }

    public static void main(String[] args) throws Exception {

    }
}