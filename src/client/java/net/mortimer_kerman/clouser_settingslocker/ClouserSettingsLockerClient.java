package net.mortimer_kerman.clouser_settingslocker;

import it.unimi.dsi.fastutil.objects.ObjectCollection;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.BuiltinRegistries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.mortimer_kerman.clouser_settingslocker.mixin.client.TypeMixin;

import java.util.HashSet;

public class ClouserSettingsLockerClient implements ClientModInitializer
{
	private static final HashSet<InputUtil.Key> KEYS = new HashSet<>();

	@Override
	public void onInitializeClient()
	{
		KEYS.addAll(((TypeMixin)(Object)InputUtil.Type.KEYSYM).getMap().values());
		KEYS.addAll(((TypeMixin)(Object)InputUtil.Type.MOUSE).getMap().values());

		ClientPlayNetworking.registerGlobalReceiver(ClouserSettingsLocker.SET_KEYBIND, ((client, handler, buf, responseSender) ->
		{
			String strBinding = buf.readString();
			String strKey = buf.readString();

			client.execute(() ->
			{
				GameOptions options = client.options;

				KeyBinding binding = null;
				for (KeyBinding bind : options.allKeys)
				{
					if (bind.getTranslationKey().equals(strBinding)) binding = bind;
				}

				if (binding == null) return;

				if (strKey.equals("RESET"))
				{
					options.setKeyCode(binding, binding.getDefaultKey());
					KeyBinding.updateKeysByCode();
					return;
				}

				if (strKey.equals("NONE"))
				{
					options.setKeyCode(binding, InputUtil.UNKNOWN_KEY);
					KeyBinding.updateKeysByCode();
					return;
				}

				InputUtil.Key key = null;
				for (InputUtil.Key input : KEYS)
				{
					if (input.getTranslationKey().endsWith(strKey)) key = input;
				}

				if (key != null) {
					options.setKeyCode(binding, key);
					KeyBinding.updateKeysByCode();
				}
			});
		}));

		ClientPlayNetworking.registerGlobalReceiver(ClouserSettingsLocker.LOCK_SETTINGS, ((client, handler, buf, responseSender) ->
		{
			String option = buf.readString();
			boolean unlocked = buf.readBoolean();

			client.execute(() ->
			{
				if(LockData.data.containsKey(option)) {
					LockData.data.replace(option, unlocked);
					LockData.SaveData();
				}
			});
		}));

		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) ->
		{
			for (KeyBinding bind : client.options.allKeys)
			{
				String binding = bind.getTranslationKey();
				PacketByteBuf recordBindings = PacketByteBufs.create();
				recordBindings.writeString(binding);
				client.execute(() -> ClientPlayNetworking.send(ClouserSettingsLocker.RECORD_BINDINGS, recordBindings));
				ClouserSettingsLocker.BINDINGS.add(binding);
				ClouserSettingsLocker.SETTINGS.add(binding);
			}

			for (InputUtil.Key input : ((TypeMixin)(Object)InputUtil.Type.KEYSYM).getMap().values())
			{
				String key = input.getTranslationKey();
				PacketByteBuf recordKeys = PacketByteBufs.create();
				recordKeys.writeString(key);
				client.execute(() -> ClientPlayNetworking.send(ClouserSettingsLocker.RECORD_KEYS, recordKeys));
				ClouserSettingsLocker.KEYS.add(key);
			}

			for (String setting : LockData.data.keySet())
			{
				PacketByteBuf recordSettings = PacketByteBufs.create();
				recordSettings.writeString(setting);
				client.execute(() -> ClientPlayNetworking.send(ClouserSettingsLocker.RECORD_SETTINGS, recordSettings));
				ClouserSettingsLocker.SETTINGS.add(setting);
			}
		});
	}
}