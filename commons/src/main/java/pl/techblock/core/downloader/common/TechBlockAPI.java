package pl.techblock.core.downloader.common;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.*;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.*;

public class TechBlockAPI {
    //techblock.pl is under cloudflare and certificates change every X amount of time
    //mdp.techblock.pl is still under cloudflare but is proxied and certificate does not change
    //no idea i did not set it up
    //it should be working properly now
    private static String api = "https://mdp.techblock.pl/launcher/";

    private static boolean checked = false;

    private static HttpsURLConnection openConnectionWithMyCert(URL url) throws Exception {
        TrustManager[] trustManagers = new TrustManager[] {
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(X509Certificate[] chain, String authType){
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                        X509Certificate serverCert = chain[0];

                        PublicKey knownServerCert = null;

                        try {
                            knownServerCert = getTechPublic();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }

                        if(!serverCert.getPublicKey().equals(knownServerCert)){
                            throw new CertificateException("Certificate does not match");
                        }
                    }

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }
                }
        };

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustManagers, new SecureRandom());
        HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
        con.setSSLSocketFactory(sslContext.getSocketFactory());
        return con;
    }


    protected static boolean connectionCertificateCheck(){
        try {
            URL url = new URL(api);
            HttpsURLConnection con = openConnectionWithMyCert(url);
            con.connect();

            PublicKey serverPublicKey = con.getServerCertificates()[0].getPublicKey();
            PublicKey knownTechBlockKey = getTechPublic();

            boolean ret =  serverPublicKey.equals(knownTechBlockKey);
            checked = ret;

            con.disconnect();
            return ret;
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    private static PublicKey getTechPublic() throws Exception {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        InputStream inputStream = classloader.getResourceAsStream("techblock-pl.pem");

        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate certificate = (X509Certificate) cf.generateCertificate(inputStream);
        inputStream.close();

        return certificate.getPublicKey();
    }

    protected static String sendGet(String appendTo) throws Exception {
        if(!checked){
            throw new Exception("Did not check for certificates");
        }
        URL url = new URL(new URI(null, null, api + appendTo, null).toString());

        HttpsURLConnection c = openConnectionWithMyCert(url);
        c.setRequestMethod("GET");
        c.setRequestProperty("User-Agent", "Mozilla/5.0 (compatible; Java)");

        int bufferSize = 1024;
        char[] buffer = new char[bufferSize];
        StringBuilder out = new StringBuilder();
        Reader in = new InputStreamReader(c.getInputStream(), StandardCharsets.UTF_8);
        for (int numRead; (numRead = in.read(buffer, 0, buffer.length)) > 0; ) {
            out.append(buffer, 0, numRead);
        }
        in.close();
        return out.toString();
    }

    protected static InputStream getRequestToInputStream(String appendTo) throws Exception {
        if(!checked){
            throw new Exception("Did not check for certificates");
        }

        URL url = new URL(new URI(null, null, api + appendTo, null).toString());

        HttpsURLConnection c = openConnectionWithMyCert(url);
        c.setRequestMethod("GET");
        c.setRequestProperty("User-Agent", "Mozilla/5.0 (compatible; Java)");

        return c.getInputStream();
    }

    private static Map<Integer, ModPackData> listModPacks() throws Exception {
        Gson gson = new Gson();

        Type type = new TypeToken<Map<String, List<Map<String,Object>>>>(){}.getType();
        String rawData = sendGet("api/launcherData.php");

        Map<String, List<Map<String,Object>>> data = gson.fromJson(rawData, type);

        LinkedHashMap<Integer, ModPackData> accumulator = new LinkedHashMap<>();


        for (Map<String, Object> categories : data.get("categories")) {
            String categoryID = (String) categories.get("categoryID");
            String user = (String) categories.get("user");
            String categoryName = (String) categories.get("categoryName");

            List<Map<String, Object>> packs = (List<Map<String, Object>>) categories.get("packs");

            for (Map<String, Object> pack : packs) {
                List<Map<String, String>> versions = (List<Map<String, String>>) pack.get("versions");
                accumulator.put(Integer.parseInt((String)pack.get("packid")), new ModPackData((String) pack.get("author"), Integer.parseInt((String)pack.get("packid")), versions));
            }
        }
        return accumulator;
    }

    public static ModPackData getModPackDataForPackId(int packID) throws Exception {
        //no longer call certificate there bcs its separated now
        //connectionCertificateCheck();
        Map<Integer, ModPackData> modPackDataMap = listModPacks();
        return modPackDataMap.get(packID);
    }

    //leave for testing
    public static void main(String[] args) throws Exception {
    }
}

class DownloadableMod {

    private ModPackData modPackData;
    private String version;
    private String name;
    private String md5;


    public DownloadableMod(ModPackData modPackData, String version, String name, String md5){
        this.modPackData = modPackData;
        this.version = version;
        this.name = name;
        this.md5 = md5;
    }

    public String getName() {
        return name;
    }

    public String getMd5() {
        return md5;
    }


    //true if dirty
    public boolean downLoadOrReplaceToLocation(File directory) throws Exception {
        if(!directory.exists()) throw new Exception("Directory should exist");
        if(!directory.isDirectory()) throw new Exception("Destination is not a directory");

        //i didn't write our api
        File destinationFile = null;

        if(this.getName().contains("|")){
            String[] splited = this.getName().split("\\|");

            StringBuilder fullPath = new StringBuilder();
            fullPath.append(directory.toPath());
            for (int i = 0; i < splited.length - 1; i++) {
                //create parent dir if doesn't exist
                String toAdd = splited[i];
                fullPath.append("/" + toAdd);
                new File(fullPath.toString()).mkdir();
            }
            fullPath.append("/" + splited[splited.length - 1]);
            destinationFile = new File(fullPath.toString());
        } else {
            destinationFile = new File(directory.getPath() + "/" + this.name);
        }

        if(destinationFile.exists()) return false;

        InputStream inputStream = TechBlockAPI.getRequestToInputStream("repository/" + modPackData.getAuthor() + "/packs/" + modPackData.getPackID() + "/" + this.version +  "/mods/" + this.name.replace("|", "/"));
        FileOutputStream writer = new FileOutputStream(destinationFile);
        byte[] buffer = new byte[8 * 1024];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            writer.write(buffer, 0, bytesRead);
        }
        inputStream.close();
        writer.close();
        return true;
    }
}

class ModPackData {
    private String author;
    private int packID;

    private List<Map<String, String>> versions;
    public ModPackData(String author, int packID, List<Map<String, String>> versions){
        this.author = author;
        this.packID = packID;
        this.versions = versions;
    }

    public String getAuthor() {
        return author;
    }

    public int getPackID() {
        return packID;
    }

    public List<String> getVersions() {
        ArrayList<String> toReturn = new ArrayList<>();

        for (Map<String, String> version : versions) {
            toReturn.add(version.get("v"));
        }
        return toReturn;
    }

    public List<DownloadableMod> getDownloadables(String version) throws Exception {
        String rawData = TechBlockAPI.sendGet("repository/" + this.getAuthor() + "/packs/" + packID + "/" + version + "/data.json");

        Gson gson = new Gson();

        Type type = new TypeToken<Map<String, Object>>(){}.getType();
        Map<String, Object> firstPass = gson.fromJson(rawData, type);

        List<Map<String, String>> modsList = (List<Map<String, String>>) firstPass.get("mods");

        List<DownloadableMod> mods = new ArrayList<>();

        for (Map<String, String> stringStringMap : modsList) {
            mods.add(new DownloadableMod(this, version, stringStringMap.get("name"), stringStringMap.get("md5").toLowerCase()));
        }
        return mods;
    }
}