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
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.suggestion.SuggestionProviders;
import net.minecraft.network.PacketByteBuf;
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

	public static final Identifier SET_KEYBIND = new Identifier(MOD_ID, "set_keybind");
	public static final Identifier LOCK_SETTINGS = new Identifier(MOD_ID, "lock_settings");
	public static final Identifier RECORD_BINDINGS = new Identifier(MOD_ID, "record_bindings");
	public static final Identifier RECORD_KEYS = new Identifier(MOD_ID, "record_keys");
	public static final Identifier RECORD_SETTINGS = new Identifier(MOD_ID, "record_settings");

	public static final SimpleCommandExceptionType BINDING_NOT_FOUND_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("argument.binding.notfound"));
	public static final SimpleCommandExceptionType KEY_NOT_FOUND_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("argument.key.notfound"));
	public static final SimpleCommandExceptionType SETTING_NOT_FOUND_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("argument.setting.notfound"));

	public static final HashSet<String> BINDINGS = new HashSet<>();
	public static final HashSet<String> KEYS = new HashSet<>();
	public static final HashSet<String> SETTINGS = new HashSet<>();

	@Override
	public void onInitialize()
	{
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> KeybindCommand(dispatcher));
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> LockSettingsCommand(dispatcher));

		ServerPlayNetworking.registerGlobalReceiver(RECORD_BINDINGS, ((server, player, handler, buf, responseSender) ->
		{
			String binding = buf.readString();
			server.execute(() -> BINDINGS.add(binding));
			server.execute(() -> SETTINGS.add(binding));
		}));

		ServerPlayNetworking.registerGlobalReceiver(RECORD_KEYS, ((server, player, handler, buf, responseSender) ->
		{
			String key = buf.readString();
			server.execute(() -> KEYS.add(key));
		}));

		ServerPlayNetworking.registerGlobalReceiver(RECORD_SETTINGS, ((server, player, handler, buf, responseSender) ->
		{
			String setting = buf.readString();
			server.execute(() -> SETTINGS.add(setting));
		}));
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
			PacketByteBuf buf = PacketByteBufs.create();
			buf.writeString(binding);
			buf.writeString(key);
			server.execute(() -> ServerPlayNetworking.send(player, SET_KEYBIND, buf));
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
			PacketByteBuf buf = PacketByteBufs.create();
			buf.writeString(option);
			buf.writeBoolean(unlocked);
			server.execute(() -> ServerPlayNetworking.send(player, LOCK_SETTINGS, buf));
		}

		if (unlocked) src.sendFeedback(() -> Text.translatable("commands.locksettings.success.unlock", Text.translatable(option)), true);
		else src.sendFeedback(() -> Text.translatable("commands.locksettings.success.lock", Text.translatable(option)), true);

		return targets.size();
	}
}