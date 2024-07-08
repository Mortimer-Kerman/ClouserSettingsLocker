package net.mortimer_kerman.clouser_settingslocker;

import it.unimi.dsi.fastutil.objects.ObjectCollection;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
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

		ClientPlayNetworking.registerGlobalReceiver(Payloads.StringStringPayload.ID, (payload, context) -> {
			switch (payload.strId()) {
				case ClouserSettingsLocker.SET_KEYBIND -> context.client().execute(() -> {

					GameOptions options = context.client().options;

					String strBinding = payload.value1();
					String strKey = payload.value2();

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
			}
		});

		ClientPlayNetworking.registerGlobalReceiver(Payloads.StringBoolPayload.ID, (payload, context) -> {
			switch (payload.strId()) {
				case ClouserSettingsLocker.LOCK_SETTINGS -> context.client().execute(() -> {

					String option = payload.valueS();
					boolean unlocked = payload.valueB();

					if(LockData.data.containsKey(option)) {
						LockData.data.replace(option, unlocked);
						LockData.SaveData();
					}
				});
			}
		});

		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {

			StringBuilder bindings = new StringBuilder();
			for (KeyBinding bind : client.options.allKeys) {
				bindings.append(bind.getTranslationKey()).append(";");
			}
			String finalBindings = bindings.toString();
			MinecraftClient.getInstance().execute(() -> ClientPlayNetworking.send(new Payloads.StringPayload(ClouserSettingsLocker.RECORD_BINDINGS, finalBindings)));


			StringBuilder keys = new StringBuilder();
			for (InputUtil.Key input : ((TypeMixin)(Object)InputUtil.Type.KEYSYM).getMap().values()) {
				keys.append(input.getTranslationKey()).append(";");
			}
			String finalKeys = keys.toString();
			MinecraftClient.getInstance().execute(() -> ClientPlayNetworking.send(new Payloads.StringPayload(ClouserSettingsLocker.RECORD_KEYS, finalKeys)));


			StringBuilder settings = new StringBuilder();
			for (String setting : LockData.data.keySet()) {
				settings.append(setting).append(";");
			}
			String finalSettings = settings.toString();
			MinecraftClient.getInstance().execute(() -> ClientPlayNetworking.send(new Payloads.StringPayload(ClouserSettingsLocker.RECORD_SETTINGS, finalSettings)));
		});
	}
}