package pl.techblock.core;

import pl.tbcore.lib.CommonMain;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.File;

@Mod("tbcore")
public class TechMain {

    private static final Logger LOGGER = LogManager.getLogger();

    public TechMain() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event) {
        File serverDir = new File(FMLPaths.GAMEDIR.get().toString());
        try {
            CommonMain.main(LOGGER, new File(serverDir + "/config"), new File(serverDir + "/mods"), false);
        } catch (Exception e) {
            //yes force throwing exceptions there.
            throw new RuntimeException(e);
        }
    }
}