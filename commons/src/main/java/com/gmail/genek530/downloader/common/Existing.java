package com.gmail.genek530.downloader.common;

import com.google.gson.internal.LinkedTreeMap;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.Map;

public class Existing {
    public static Map<String, String> checkMD5(File destination) throws Exception {
        if(!destination.isDirectory()) throw new Exception("Destination not a directory");

        LinkedTreeMap collector = new LinkedTreeMap();

        for (File file : destination.listFiles()) {
            if(file.isDirectory()) continue;

            try (InputStream inputStream = new FileInputStream(file)) {
                MessageDigest md = MessageDigest.getInstance("MD5");

                byte[] buffer = new byte[1024]; // buffer size
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    md.update(buffer, 0, bytesRead);
                }

                byte[] digest = md.digest();
                StringBuilder hexString = new StringBuilder();

                for (byte b : digest) {
                    hexString.append(String.format("%02x", b));
                }
                collector.put(file.getName(), hexString.toString());
            }
        }
        return collector;
    }
}