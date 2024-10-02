package pl.techblock.core;

import pl.techblock.core.downloader.common.CommonMain;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;
import java.io.File;

@Mod(modid = TechMain.MODID, name = TechMain.NAME, version = TechMain.VERSION)
public class TechMain {
    public static final String MODID = "tbcore";
    public static final String NAME = "TBCore";
    public static final String VERSION = "1.0.1";
    private static Logger logger;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) throws Exception {
        logger = event.getModLog();
        CommonMain.main(logger, new File(event.getModConfigurationDirectory().getParent().toString() + "/config"), new File(event.getModConfigurationDirectory().getParent().toString() + "/mods"), true);
    }

}