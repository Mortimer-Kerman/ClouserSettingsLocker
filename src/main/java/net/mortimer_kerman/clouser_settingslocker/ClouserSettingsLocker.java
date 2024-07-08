package net.mortimer_kerman.clouser_settingslocker;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.suggestion.SuggestionProviders;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.*;

public class ClouserSettingsLocker implements ModInitializer
{

	public static final String MOD_ID = "clouser-settingslocker";

	public static final String SET_KEYBIND = "set_keybind";
	public static final String LOCK_SETTINGS = "lock_settings";
	public static final String RECORD_BINDINGS = "record_bindings";
	public static final String RECORD_KEYS = "record_keys";
	public static final String RECORD_SETTINGS = "record_settings";

	public static final SimpleCommandExceptionType BINDING_NOT_FOUND_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("argument.binding.notfound"));
	public static final SimpleCommandExceptionType KEY_NOT_FOUND_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("argument.key.notfound"));
	public static final SimpleCommandExceptionType SETTING_NOT_FOUND_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("argument.setting.notfound"));

	public static final HashSet<String> BINDINGS = new HashSet<>();
	public static final HashSet<String> KEYS = new HashSet<>();
	public static final HashSet<String> SETTINGS = new HashSet<>();

	@Override
	public void onInitialize()
	{
		Payloads.RegisterPayloads();

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> KeybindCommand(dispatcher));
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> LockSettingsCommand(dispatcher));


		ServerPlayNetworking.registerGlobalReceiver(Payloads.StringPayload.ID, (payload, context) -> {
			switch (payload.strId()) {
				case RECORD_BINDINGS -> context.server().execute(() -> {
					String[] bindings = payload.value().split(";");
					bindings = Arrays.copyOf(bindings, bindings.length - 1);
					BINDINGS.addAll(List.of(bindings));
				});
				case RECORD_KEYS -> context.server().execute(() -> {
					String[] keys = payload.value().split(";");
					keys = Arrays.copyOf(keys, keys.length - 1);
					KEYS.addAll(List.of(keys));
				});
				case RECORD_SETTINGS -> context.server().execute(() -> {
					String[] settings = payload.value().split(";");
					settings = Arrays.copyOf(settings, settings.length - 1);
					SETTINGS.addAll(List.of(settings));
				});
			}
		});
	}

	private static final SuggestionProvider<ServerCommandSource> BINDINGS_SUGGESTION_PROVIDER = SuggestionProviders
			.register(Identifier.of(MOD_ID, "keybinds"), (context, builder) -> CommandSource
					.suggestMatching(BINDINGS, builder));

	private static final SuggestionProvider<ServerCommandSource> KEYS_SUGGESTION_PROVIDER = SuggestionProviders
			.register(Identifier.of(MOD_ID, "keys"), (context, builder) -> CommandSource
					.suggestMatching(KEYS, builder));

	private static final SuggestionProvider<ServerCommandSource> SETTINGS_SUGGESTION_PROVIDER = SuggestionProviders
			.register(Identifier.of(MOD_ID, "settings"), (context, builder) -> CommandSource
					.suggestMatching(SETTINGS, builder));

	private static void KeybindCommand(CommandDispatcher<ServerCommandSource> dispatcher)
	{
		dispatcher.register(LiteralArgumentBuilder.<ServerCommandSource>literal("keybind")
				.requires(s -> s.hasPermissionLevel(2))
				.then(
						CommandManager.argument("binding", StringArgumentType.word()).suggests(BINDINGS_SUGGESTION_PROVIDER)
								.then(
										CommandManager.literal("set")
												.then(
														CommandManager.argument("key", StringArgumentType.word()).suggests(KEYS_SUGGESTION_PROVIDER)
																.executes(ctx -> setKeybind(ctx.getSource(), Collections.singleton((ctx.getSource()).getPlayerOrThrow()), StringArgumentType.getString(ctx, "binding"), StringArgumentType.getString(ctx, "key")))
												)
								)
								.then(
										CommandManager.literal("reset")
												.executes(ctx -> setKeybind(ctx.getSource(), Collections.singleton((ctx.getSource()).getPlayerOrThrow()), StringArgumentType.getString(ctx, "binding"), "RESET"))
								)
								.then(
										CommandManager.literal("unbind")
												.executes(ctx -> setKeybind(ctx.getSource(), Collections.singleton((ctx.getSource()).getPlayerOrThrow()), StringArgumentType.getString(ctx, "binding"), "NONE"))
								)
				)
				.then(
						CommandManager.argument("targets", EntityArgumentType.players())
								.then(
										CommandManager.argument("binding", StringArgumentType.word()).suggests(BINDINGS_SUGGESTION_PROVIDER)
												.then(
														CommandManager.literal("set")
																.then(
																		CommandManager.argument("key", StringArgumentType.word()).suggests(KEYS_SUGGESTION_PROVIDER)
																				.executes(ctx -> setKeybind(ctx.getSource(), EntityArgumentType.getPlayers(ctx, "targets"), StringArgumentType.getString(ctx, "binding"), StringArgumentType.getString(ctx, "key")))
																)
												)
												.then(
														CommandManager.literal("reset")
																.executes(ctx -> setKeybind(ctx.getSource(), EntityArgumentType.getPlayers(ctx, "targets"), StringArgumentType.getString(ctx, "binding"), "RESET"))
												)
												.then(
														CommandManager.literal("unbind")
																.executes(ctx -> setKeybind(ctx.getSource(), EntityArgumentType.getPlayers(ctx, "targets"), StringArgumentType.getString(ctx, "binding"), "NONE"))
												)
								)
				)
		);
	}

	private static int setKeybind(ServerCommandSource src, Collection<? extends ServerPlayerEntity> targets, String binding, String key) throws CommandSyntaxException
	{
		if (!BINDINGS.contains(binding)) throw BINDING_NOT_FOUND_EXCEPTION.create();
		if (!key.equals("NONE") && !key.equals("RESET") && !KEYS.contains(key)) throw KEY_NOT_FOUND_EXCEPTION.create();

		MinecraftServer server = src.getServer();
		for (ServerPlayerEntity player : targets)
		{
			server.execute(() -> ServerPlayNetworking.send(player, new Payloads.StringStringPayload(SET_KEYBIND, binding, key)));
		}

		switch (key)
		{
			case "NONE" -> src.sendFeedback(() -> Text.translatable("commands.keybind.success.unbind", Text.translatable(binding)), true);
			case "RESET" -> src.sendFeedback(() -> Text.translatable("commands.keybind.success.reset", Text.translatable(binding)), true);
			default -> {
				Text keyText = Text.translatable(key);
				if(keyText.getString().equals(key))
				{
					String[] elements = key.split("\\.");
					src.sendFeedback(() -> Text.translatable("commands.keybind.success", Text.translatable(binding), elements[elements.length-1].toUpperCase()), true);
				}
				else src.sendFeedback(() -> Text.translatable("commands.keybind.success", Text.translatable(binding), keyText), true);
			}
		}

		return 1;
	}

	private static void LockSettingsCommand(CommandDispatcher<ServerCommandSource> dispatcher)
	{
		dispatcher.register(LiteralArgumentBuilder.<ServerCommandSource>literal("lockSettings")
				.requires(s -> s.hasPermissionLevel(2))
				.then(
						CommandManager.argument("option", StringArgumentType.word()).suggests(SETTINGS_SUGGESTION_PROVIDER)
								.then(
										CommandManager.argument("unlocked", BoolArgumentType.bool())
												.executes(ctx -> setSettingsLock(ctx.getSource(), Collections.singleton((ctx.getSource()).getPlayerOrThrow()), StringArgumentType.getString(ctx, "option"), BoolArgumentType.getBool(ctx, "unlocked")))
								)
				)
				.then(
						CommandManager.argument("targets", EntityArgumentType.players())
								.then(
										CommandManager.argument("option", StringArgumentType.word()).suggests(SETTINGS_SUGGESTION_PROVIDER)
												.then(
														CommandManager.argument("unlocked", BoolArgumentType.bool())
																.executes(ctx -> setSettingsLock(ctx.getSource(), EntityArgumentType.getPlayers(ctx, "targets"), StringArgumentType.getString(ctx, "option"), BoolArgumentType.getBool(ctx, "unlocked")))
												)
								)
				)
		);
	}

	private static int setSettingsLock(ServerCommandSource src, Collection<? extends ServerPlayerEntity> targets, String option, boolean unlocked) throws CommandSyntaxException
	{
		if (!SETTINGS.contains(option)) throw SETTING_NOT_FOUND_EXCEPTION.create();

		MinecraftServer server = src.getServer();
		for (ServerPlayerEntity player : targets)
		{
			server.execute(() -> ServerPlayNetworking.send(player, new Payloads.StringBoolPayload(LOCK_SETTINGS, option, unlocked)));
		}

		if (unlocked) src.sendFeedback(() -> Text.translatable("commands.locksettings.success.unlock", Text.translatable(option)), true);
		else src.sendFeedback(() -> Text.translatable("commands.locksettings.success.lock", Text.translatable(option)), true);

		return targets.size();
	}
}