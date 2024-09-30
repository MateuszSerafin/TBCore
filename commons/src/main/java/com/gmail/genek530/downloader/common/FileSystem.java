package com.gmail.genek530.downloader.common;

import com.google.gson.internal.LinkedTreeMap;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.Map;

public class FileSystem {
    public static Map<String, String> checkMD5ForDir(File directory) throws Exception {
        if(!directory.isDirectory()) throw new Exception("Destination not a directory");

        LinkedTreeMap collector = new LinkedTreeMap();

        for (File file : directory.listFiles()) {
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

    public static void deleteNonEmptyDirectory(File directory) {
        if(directory.isDirectory()) {
            File[] files = directory.listFiles();
            if(files != null) {
                for(File file : files) {
                    deleteNonEmptyDirectory(file);
                }
            }
        }
        try {
            if(directory.delete()) {
                System.out.println(directory + " is deleted");
            }
            else {
                System.out.println("Directory not deleted");
            }
        } catch (Exception e){
            System.out.println(e);
        }
    }
}