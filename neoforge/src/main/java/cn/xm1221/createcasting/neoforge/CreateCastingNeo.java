package cn.xm1221.createcasting.neoforge;

import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.RegisterEvent;

import cn.xm1221.createcasting.CreateCastingMod;

@Mod(CreateCastingMod.MOD_ID)
public final class CreateCastingNeo {
    public  CreateCastingNeo(ModContainer modContainer) {
        // Run our common setup.
        var  modBus = modContainer.getEventBus();
        modBus.addListener((RegisterEvent event) -> {
            CreateCastingMod.init();
        });


    };
}
