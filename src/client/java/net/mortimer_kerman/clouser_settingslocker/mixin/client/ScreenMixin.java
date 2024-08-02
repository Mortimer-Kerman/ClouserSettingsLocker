package net.mortimer_kerman.clouser_settingslocker.mixin.client;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.mortimer_kerman.clouser_settingslocker.LockData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public abstract class ScreenMixin
{
    @Inject(method = "<init>", at=@At(value = "RETURN"))
    private void onCreate(Text title, CallbackInfo ci)
    {
        LockData.LoadData();
    }

    @Inject(method = "close", at=@At(value = "RETURN"))
    private void onClose(CallbackInfo ci)
    {
        LockData.LoadData();
    }
}
