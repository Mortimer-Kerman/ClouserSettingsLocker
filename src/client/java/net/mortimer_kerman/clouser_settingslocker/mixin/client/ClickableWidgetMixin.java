package net.mortimer_kerman.clouser_settingslocker.mixin.client;

import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.*;

import net.mortimer_kerman.clouser_settingslocker.LockData;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClickableWidget.class)
public abstract class ClickableWidgetMixin
{
    @Shadow private Text message;

    @Shadow public boolean active;

    @Inject(method = "<init>", at=@At("RETURN"))
    private void onCreate(int x, int y, int width, int height, Text message, CallbackInfo ci)
    {
        String key = LockData.ExtractKey(message.getContent());
        if (key.contains("toggle")) System.out.println(key);
        if (LockData.IsDisabled(key)) this.active = false;
    }
}
