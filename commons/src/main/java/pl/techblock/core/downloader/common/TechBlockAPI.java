package pl.techblock.core.downloader.common;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.*;

public class TechBlockAPI {
    private static String api = "https://service.techblock.pl/launcher/";

    private static boolean checked = false;

    protected static boolean connectionCertificateCheck(){
        URL url;
        try {
            url = new URL(api);
            HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
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

        return certificate.getPublicKey();
    }

    protected static String sendGet(String appendTo) throws Exception {
        if(!checked){
            throw new Exception("Did not check for certificates");
        }
        URL url = new URL(new URI(null, null, api + appendTo, null).toString());

        HttpsURLConnection c = (HttpsURLConnection) url.openConnection();
        c.setRequestMethod("GET");
        c.setRequestProperty("User-Agent", "Mozilla/5.0 (compatible; Java)");

        int bufferSize = 1024;
        char[] buffer = new char[bufferSize];
        StringBuilder out = new StringBuilder();
        Reader in = new InputStreamReader(c.getInputStream(), StandardCharsets.UTF_8);
        for (int numRead; (numRead = in.read(buffer, 0, buffer.length)) > 0; ) {
            out.append(buffer, 0, numRead);
        }
        return out.toString();
    }

    protected static InputStream getRequestToInputStream(String appendTo) throws Exception {
        if(!checked){
            throw new Exception("Did not check for certificates");
        }

        URL url = new URL(new URI(null, null, api + appendTo, null).toString());

        HttpsURLConnection c = (HttpsURLConnection) url.openConnection();
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

    //@Nullable
    public static ModPackData getModPackDataForPackId(int packID) throws Exception {
        //no longer call certificate there bcs its separated now
        //connectionCertificateCheck();
        Map<Integer, ModPackData> modPackDataMap = listModPacks();
        return modPackDataMap.get(packID);
    }

    //leave for testing
    public static void main(String[] args) throws Exception {
        connectionCertificateCheck();
        ModPackData modPackData = getModPackDataForPackId(24);
        for (DownloadableMod downloadable : modPackData.getDownloadables()) {
            System.out.println(downloadable);
        }
    }
}

class DownloadableMod {

    private ModPackData modPackData;
    private String name;
    private String md5;


    public DownloadableMod(ModPackData modPackData, String name, String md5){
        this.modPackData = modPackData;
        this.name = name;
        this.md5 = md5;
    }

    public String getName() {
        return name;
    }

    public String getMd5() {
        return md5;
    }

    public void downLoadOrReplaceToLocation(File directory) throws Exception {
        if(!directory.isDirectory()) throw new Exception("Destination is not a directory");
        File destinationFile = new File(directory.getPath() + "/" + this.name);
        if(destinationFile.exists()) destinationFile.delete();

        InputStream inputStream = TechBlockAPI.getRequestToInputStream("repository/" + modPackData.getAuthor() + "/packs/" + modPackData.getPackID() + "/" + modPackData.getLatestVersion() +  "/mods/" + this.name);
        FileOutputStream writer = new FileOutputStream(destinationFile);
        byte[] buffer = new byte[8 * 1024];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            writer.write(buffer, 0, bytesRead);
        }
        inputStream.close();
        writer.close();
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

    public List<Map<String, String>> getVersions() {
        return versions;
    }

    public String getLatestVersion(){
        Map<String, String> latest = this.getVersions().get(this.getVersions().size() - 1);
        return latest.get("v");
    }

    public List<DownloadableMod> getDownloadables() throws Exception {
        String rawData = TechBlockAPI.sendGet("repository/" + this.getAuthor() + "/packs/" + packID + "/" + this.getLatestVersion() + "/data.json");

        Gson gson = new Gson();

        Type type = new TypeToken<Map<String, Object>>(){}.getType();
        Map<String, Object> firstPass = gson.fromJson(rawData, type);

        List<Map<String, String>> modsList = (List<Map<String, String>>) firstPass.get("mods");

        List<DownloadableMod> mods = new ArrayList<>();

        for (Map<String, String> stringStringMap : modsList) {
            mods.add(new DownloadableMod(this, stringStringMap.get("name"), stringStringMap.get("md5").toLowerCase()));
        }
        return mods;
    }
}
