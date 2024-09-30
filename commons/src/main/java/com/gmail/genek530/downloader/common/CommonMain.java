package com.gmail.genek530.downloader.common;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import org.apache.logging.log4j.Logger;

public class CommonMain {

    //mc 1.12.2 didn't like system.exit due to security manager it seems it got removed in 1.16.5 and later
    //so after 1.16.5 prefer system.exit and before choose to crash client
    public static void main(Logger logger, File configDirectory, File modDirectory, boolean crashRatherThanQuit) throws Exception {
        boolean certCheck = TechBlockAPI.connectionCertificateCheck();
        if(!certCheck){
            logger.info("Either no connection or problem with certificates");
            return;
        }
        logger.info("Successful certificate check");

        File configFile = new File(configDirectory.getPath() + "/TBD.config");

        int modPackID;

        if(configFile.exists()){
            Scanner scanner = new Scanner(configFile);

            if(scanner.hasNextInt()){
                modPackID = scanner.nextInt();
                logger.info("TBD.config MOD pack ID: " + modPackID);
            } else {
                scanner.close();
                throw new Exception("Invalid value in TBD.config");
            }
            scanner.close();
        } else {
            throw new Exception("Unable to find TBD.config");
        }

        boolean dirty = false;

        Map<String, String> onDisk = FileSystem.checkMD5ForDir(modDirectory);

        ModPackData targetModPack;
        try {
            targetModPack = TechBlockAPI.getModPackDataForPackId(modPackID);
        } catch (Exception e) {
            logger.info("Issue with techblock API");
            e.printStackTrace();
            return;
        }

        //probably should be a map
        List<DownloadableMod> candidates = targetModPack.getDownloadables();

        for (DownloadableMod candidate : candidates) {
            if(onDisk.containsKey(candidate.getName())){
                String onDiskMD5 = onDisk.get(candidate.getName());
                if(!onDiskMD5.equals(candidate.getMd5())){
                    dirty = true;
                }
            } else {
                dirty = true;
            }
        }

        //change in behaviour if dirty redownload all mods as i wont be keeping of which mod changed including version better to just slap everything
        if(dirty){
            File migrationFolder = new File(modDirectory.getParentFile() + "/" + "TBCoreMigrationTemporary");
            FileSystem.deleteNonEmptyDirectory(migrationFolder);
            migrationFolder.mkdir();

            for (File file : modDirectory.listFiles()) {
               if(file.getName().toLowerCase().contains("tbcore")){
                   Files.copy(file.toPath(), new File(migrationFolder + "/" + file.getName()).toPath());
               }
            }

            FileSystem.deleteNonEmptyDirectory(modDirectory);
            modDirectory.mkdir();

            for (File file : migrationFolder.listFiles()) {
                if(file.getName().toLowerCase().contains("tbcore")){
                    Files.copy(file.toPath(), new File(modDirectory + "/" + file.getName()).toPath());
                }
            }
            System.out.println("Remove migrationFolder");
            FileSystem.deleteNonEmptyDirectory(migrationFolder);

            for (DownloadableMod candidate : candidates) {
                candidate.downLoadOrReplaceToLocation(modDirectory);
            }

            //this should only be ran on client side so should be ok
            System.setProperty("java.awt.headless", "false");
            GUIThing guiThing = new GUIThing();

            while(true){
                //yes this blocks main thred, this is expected
                //couldn't do it other way due to security manager unsure how to bypass but this crashes client
                //which is either way what i want, and user needs to confirm
                Thread.sleep(1000);
                if(guiThing.didRaise()){
                    if(crashRatherThanQuit){
                        throw new Exception("Please restart your minecraft client");
                    }
                    System.exit(1);
                }
            }
        }
    }
}
