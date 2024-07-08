package net.mortimer_kerman.clouser_settingslocker.mixin.client;

import net.minecraft.client.option.SimpleOption;
import net.minecraft.text.Text;
import net.mortimer_kerman.clouser_settingslocker.interfaces.SimpleOptionMixinInterface;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(SimpleOption.class)
public class SimpleOptionMixin implements SimpleOptionMixinInterface
{
    @Shadow @Final Text text;

    public Text clouserSettingsLocker$getText() {
        return this.text;
    }
}
