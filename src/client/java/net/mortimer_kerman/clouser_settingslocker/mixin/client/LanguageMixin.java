package net.mortimer_kerman.clouser_settingslocker.mixin.client;

import com.google.common.collect.ImmutableSet;
import net.minecraft.util.Language;
import net.mortimer_kerman.clouser_settingslocker.interfaces.LanguageMixinInterface;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.google.common.collect.ImmutableMap;

import java.util.function.BiConsumer;

@Mixin(Language.class)
public abstract class LanguageMixin implements LanguageMixinInterface {
    @Shadow private static void load(BiConsumer<String, String> entryConsumer, String path) {}

    @Override
    public ImmutableSet<String> clouserSettingsLocker$GrabKeys() {
        ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
        BiConsumer<String, String> biConsumer = builder::put;
        load(biConsumer, "/assets/minecraft/lang/en_us.json");
        return builder.build().keySet();
    }
}
