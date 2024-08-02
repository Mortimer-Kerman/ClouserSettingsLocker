package net.mortimer_kerman.clouser_settingslocker;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.MutableText;
import net.minecraft.text.TextContent;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.Language;

import net.mortimer_kerman.clouser_settingslocker.interfaces.LanguageMixinInterface;

import com.google.common.collect.ImmutableSet;

import java.io.*;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class LockData {
    public static final ImmutableSet<String> toggles = ImmutableSet.of(
            "options.realmsNotifications.toggle",
            "options.allowServerListing.toggle",
            "options.modelPart.cape.toggle",
            "options.modelPart.jacket.toggle",
            "options.modelPart.left_sleeve.toggle",
            "options.modelPart.right_sleeve.toggle",
            "options.modelPart.left_pants_leg.toggle",
            "options.modelPart.right_pants_leg.toggle",
            "options.modelPart.hat.toggle",
            "options.mainHand.toggle",
            "options.audioDevice.toggle",
            "options.showSubtitles.toggle",
            "options.directionalAudio.toggle",
            "options.graphics.toggle",
            "options.prioritizeChunkUpdates.toggle",
            "options.ao.toggle",
            "options.vsync.toggle",
            "options.viewBobbing.toggle",
            "options.guiScale.toggle",
            "options.attackIndicator.toggle",
            "options.renderClouds.toggle",
            "options.fullscreen.toggle",
            "options.particles.toggle",
            "options.entityShadows.toggle",
            "options.autosaveIndicator.toggle",
            "key.sneak.toggle",
            "key.sprint.toggle",
            "options.autoJump.toggle",
            "options.operatorItemsTab.toggle",
            "options.invertMouse.toggle",
            "options.discrete_mouse_scroll.toggle",
            "options.touchscreen.toggle",
            "options.rawMouseInput.toggle",
            "options.chat.visibility.toggle",
            "options.chat.color.toggle",
            "options.chat.links.toggle",
            "options.chat.links.prompt.toggle",
            "options.narrator.toggle",
            "options.autoSuggestCommands.toggle",
            "options.hideMatchedNames.toggle",
            "options.reducedDebugInfo.toggle",
            "options.onlyShowSecureChat.toggle",
            "options.accessibility.high_contrast.toggle",
            "options.accessibility.text_background.toggle",
            "options.hideLightningFlashes.toggle",
            "options.darkMojangStudiosBackgroundColor.toggle",
            "options.hideSplashTexts.toggle",
            "options.accessibility.narrator_hotkey.toggle");

    public static final ImmutableSet<String> blacklist = ImmutableSet.of(
            "options.ao",
            "options.ao.max",
            "options.ao.min",
            "options.ao.off",
            "options.attack.crosshair",
            "options.attack.hotbar",
            "options.audioDevice.default",
            "options.biomeBlendRadius.1",
            "options.biomeBlendRadius.3",
            "options.biomeBlendRadius.5",
            "options.biomeBlendRadius.7",
            "options.biomeBlendRadius.9",
            "options.biomeBlendRadius.11",
            "options.biomeBlendRadius.13",
            "options.biomeBlendRadius.15",
            "options.chat.visibility.full",
            "options.chat.visibility.hidden",
            "options.chat.visibility.system",
            "options.chunks",
            "options.clouds.fancy",
            "options.clouds.fast",
            "options.customizeTitle",
            "options.difficulty.easy",
            "options.difficulty.hard",
            "options.difficulty.hardcore",
            "options.difficulty.normal",
            "options.difficulty.peaceful",
            "options.fov.max",
            "options.fov.min",
            "options.framerate",
            "options.framerateLimit.max",
            "options.fullscreen.current",
            "options.fullscreen.entry",
            "options.fullscreen.unavailable",
            "options.gamma.default",
            "options.gamma.max",
            "options.gamma.min",
            "options.generic_value",
            "options.graphics.fabulous",
            "options.graphics.fancy",
            "options.graphics.fast",
            "options.guiScale.auto",
            "options.hidden",
            "options.key.hold",
            "options.key.toggle",
            "options.language.title",
            "options.languageAccuracyWarning",
            "options.languageWarning",
            "options.mainHand.left",
            "options.mainHand.right",
            "options.mouse_settings.title",
            "options.multiplier",
            "options.narrator.all",
            "options.narrator.chat",
            "options.narrator.notavailable",
            "options.narrator.off",
            "options.narrator.system",
            "options.off",
            "options.off.composed",
            "options.on",
            "options.on.composed",
            "options.online.title",
            "options.particles.all",
            "options.particles.decreased",
            "options.particles.minimal",
            "options.percent_add_value",
            "options.percent_value",
            "options.pixel_value",
            "options.prioritizeChunkUpdates.byPlayer",
            "options.prioritizeChunkUpdates.nearby",
            "options.prioritizeChunkUpdates.none",
            "options.sensitivity.max",
            "options.sensitivity.min",
            "options.skinCustomisation.title",
            "options.sounds.title",
            "options.videoTitle",
            "options.visible");

    public static TreeMap<String, Boolean> data = new TreeMap<>();

    public static TreeMap<String, Boolean> GetNewData()
    {
        TreeMap<String, Boolean> grabbedData = new TreeMap<>();

        ImmutableSet<String> keys = ((LanguageMixinInterface)Language.getInstance()).clouserSettingsLocker$GrabKeys();
        for(String key : keys) {
            if ((key.startsWith("options") || (key.startsWith("key") && !key.startsWith("key.keyboard") && !key.startsWith("key.mouse") && !key.startsWith("key.categories"))) && !key.endsWith(".tooltip") && !key.endsWith(".info") && !key.endsWith(".message") && !key.startsWith("options.graphics.warning") && !blacklist.contains(key)) grabbedData.put(key, true);
        }

        for (String key : toggles) {
            grabbedData.put(key, true);
            if (!key.startsWith("key")) grabbedData.remove(key.replace(".toggle", ""));
        }

        return grabbedData;
    }

    public static void SaveData()
    {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(GetFileDir())))
        {
            for (Map.Entry<String, Boolean> entry : data.entrySet())
            {
                String translation;
                if (entry.getKey().endsWith(".toggle")) translation = Language.getInstance().get(entry.getKey().replace(".toggle",""));
                else translation = Language.getInstance().get(entry.getKey());

                writer.write(entry.getKey() + " = " + entry.getValue() + " // " + translation);
                writer.newLine();
            }
            System.out.println("Locking data saved!");
        }
        catch (IOException e)
        {
            System.err.println("An error occured while saving the locking data : " + e.getMessage());
        }
    }

    public static void LoadData()
    {
        if (!(new File(GetFileDir()).exists()))
        {
            data = GetNewData();
            SaveData();
            return;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(GetFileDir())))
        {
            data = GetNewData();

            TreeMap<String, Boolean> grabbedData = new TreeMap<>();

            String line;
            while ((line = reader.readLine()) != null)
            {
                String[] parts = line.split(" = ");
                if (parts.length == 2)
                {
                    String key = parts[0].trim();
                    Boolean value = Boolean.parseBoolean(parts[1].split(" // ")[0].trim());
                    grabbedData.put(key, value);
                }
            }

            boolean dirty = !data.keySet().equals(grabbedData.keySet());

            data.putAll(grabbedData);

            if (dirty) SaveData();
        }
        catch (IOException e)
        {
            System.err.println("An error occured while loading the locking data : " + e.getMessage());
        }
    }

    private static String GetFileDir() { return MinecraftClient.getInstance().runDirectory.toString() + "/settingsLock.txt"; }

    public static boolean IsDisabled(String key)
    {
        if(key.equals("menu.options")) key = "options.title";

        return !data.getOrDefault(key,true);
    }

    public static String ExtractKey(TextContent text)
    {
        if (text instanceof TranslatableTextContent textContent)
        {
            if (textContent.getKey().equals("options.generic_value") && textContent.getArgs().length != 0)
            {
                if (textContent.getArg(0) instanceof MutableText innerText)
                {
                    return ExtractKey(innerText.getContent()) + ".toggle";
                }
            }
            return textContent.getKey();
        }
        return text.toString();
    }
}
