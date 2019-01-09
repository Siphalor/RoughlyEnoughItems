package me.shedaniel.mixins;

import me.shedaniel.Core;
import me.shedaniel.listenerdefinitions.DoneLoading;
import net.minecraft.init.Bootstrap;
import org.dimdev.riftloader.RiftLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Created by James on 7/27/2018.
 */
@Mixin(Bootstrap.class)
public class MixinDoneLoading {
    
    @Inject(method = "register", at = @At("RETURN"))
    private static void onBootstrapRegister(CallbackInfo ci) {
        Core.LOGGER.info("REI: Done Loading");
        RiftLoader.instance.getListeners(DoneLoading.class).forEach(DoneLoading::onDoneLoading);
    }
    
}
