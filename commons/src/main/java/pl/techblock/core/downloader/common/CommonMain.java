package pl.techblock.core.downloader.common;

import java.io.File;
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

        dirty = false;
        for (DownloadableMod candidate : candidates) {
            if(candidate.downLoadOrReplaceToLocation(modDirectory)){
                dirty = true;
            }
        }

        if(dirty){
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
