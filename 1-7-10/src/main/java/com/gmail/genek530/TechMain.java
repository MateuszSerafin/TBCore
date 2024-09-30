package com.gmail.genek530;

import com.gmail.genek530.downloader.common.CommonMain;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;
import java.io.File;

@Mod(modid = TechMain.MODID, name = TechMain.NAME, version = TechMain.VERSION)
public class TechMain {
    public static final String MODID = "tbdownloader";
    public static final String NAME = "TB-Downloader";
    public static final String VERSION = "1.0";

    private static Logger logger;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) throws Exception {
        logger = event.getModLog();
        CommonMain.main(logger, new File(event.getModConfigurationDirectory().getParent().toString() + "/config"), new File(event.getModConfigurationDirectory().getParent().toString() + "/mods"), true);
    }

}