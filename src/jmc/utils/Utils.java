package jmc.utils;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;

import static jmc.utils.ColorFormatter.*;
import static tests.TestPrint.l;

/**
 * Created by Jiachen on 3/10/18.
 * Utils
 */
public class Utils {
    public static String read(String filePath) {
        final String[] acc = {""};
        InputStream inputStream;
        inputStream = Class.class.getResourceAsStream(filePath);
        InputStreamReader reader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(reader);
        bufferedReader.lines().forEach(line -> acc[0] += line + "\n");
        return acc[0];
    }

    public static void write(String filePath, String content) {
        try {
            URI uri = Class.class.getResource(filePath).toURI();
            PrintWriter pw = new PrintWriter(new File(uri));
            pw.write(content);
            pw.flush();
            pw.close();
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }


}
