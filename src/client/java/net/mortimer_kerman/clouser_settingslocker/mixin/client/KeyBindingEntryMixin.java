package net.mortimer_kerman.clouser_settingslocker.mixin.client;

import net.minecraft.client.gui.screen.option.ControlsListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.Text;

import net.mortimer_kerman.clouser_settingslocker.LockData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ControlsListWidget.KeyBindingEntry.class)
public class KeyBindingEntryMixin
{
    @Shadow @Final private ButtonWidget editButton;

    @Shadow @Final private ButtonWidget resetButton;

    @Shadow @Final private Text bindingName;

    @Inject(method = "<init>", at = @At(value = "RETURN"))
    private void onCreate(ControlsListWidget controlsListWidget, KeyBinding binding, Text bindingName, CallbackInfo ci)
    {
        String key = LockData.ExtractKey(bindingName.getContent());
        if (LockData.IsDisabled(key)) this.editButton.active = false;
    }

    @Inject(method = "update", at=@At(value = "TAIL"))
    private void onUpdate(CallbackInfo ci)
    {
        String key = LockData.ExtractKey(this.bindingName.getContent());
        if (LockData.IsDisabled(key)) this.resetButton.active = false;
    }
}
