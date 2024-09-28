package com.gmail.genek530;

import com.gmail.genek530.downloader.common.CommonMain;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.File;

@Mod("tbdownloader")
public class TechMain {

    private static final Logger LOGGER = LogManager.getLogger();

    public TechMain() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event) {
        FMLPaths.GAMEDIR.get();
        File serverDir = new File(FMLPaths.GAMEDIR.get().toString());
        try {
            CommonMain.main(LOGGER, new File(serverDir + "/config"), new File(serverDir + "/mods"), false);
        } catch (Exception e) {
            //yes force throwing exceptions there.
            throw new RuntimeException(e);
        }
    }
}