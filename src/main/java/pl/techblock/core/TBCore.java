package pl.techblock.core;

import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pl.tbcore.lib.CommonMain;
import java.io.File;

@Mod("tbcore")
public class TBCore {

    private static final Logger LOGGER = LogManager.getLogger();

    public TBCore(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::setup);
    }

    private void setup(final FMLCommonSetupEvent event) {
        File clientdir = new File(FMLPaths.GAMEDIR.get().toString());
        try {
            CommonMain.main(LOGGER, new File(clientdir + "/config"), new File(clientdir + "/mods"), false);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}