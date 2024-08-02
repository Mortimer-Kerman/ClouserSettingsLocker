package net.mortimer_kerman.clouser_settingslocker.mixin.client;

import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.OptionSliderWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.text.Text;

import net.mortimer_kerman.clouser_settingslocker.LockData;

import net.mortimer_kerman.clouser_settingslocker.interfaces.SimpleOptionMixinInterface;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(SimpleOption.OptionSliderWidgetImpl.class)
public abstract class SliderWidgetMixin extends ClickableWidgetMixin
{
    @Shadow @Final private SimpleOption<?> option;

    @Inject(method = "<init>", at=@At("RETURN"))
    private void onCreate(GameOptions options, int x, int y, int width, int height, SimpleOption<?> option, @Coerce Object callbacks, SimpleOption.TooltipFactory<?> tooltipFactory, Consumer<?> changeCallback, boolean shouldApplyImmediately, CallbackInfo ci)
    {
        String key = LockData.ExtractKey(((SimpleOptionMixinInterface)(Object)this.option).clouserSettingsLocker$getText().getContent());
        if (LockData.IsDisabled(key)) this.active = false;
    }
}
