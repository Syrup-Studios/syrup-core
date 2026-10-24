package net.syrupstudios.syrupcore.loaders.fabric;

//? if fabric {
import net.fabricmc.api.ModInitializer;
import net.syrupstudios.syrupcore.SyrupCore;

public final class FabricEntrypoint implements ModInitializer {
    @Override
    public void onInitialize() {
        SyrupCore.initialize();
    }
}
//?}
