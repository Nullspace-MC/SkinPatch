package net.nullspace_mc.skinpatch.mixin;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.entity.living.player.ClientPlayerEntity;
import net.nullspace_mc.skinpatch.SkinPatch;
import net.nullspace_mc.skinpatch.exceptions.TextureNotFoundException;
import net.nullspace_mc.skinpatch.exceptions.UnknownPlayerException;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Objects;
import java.util.Optional;

@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin {
    private static boolean canFetch = true;
    private static final String mojangEndpointUsernameToUUID = "https://api.mojang.com/users/profiles/minecraft/";
    private static final String mojangEndpointUUIDToSkinCape = "https://sessionserver.mojang.com/session/minecraft/profile/";


    private static String getPlayerApiUuid(String playerName) {
        if (!canFetch) return "";

        String accountApiUuid = "";

        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(
                    mojangEndpointUsernameToUUID + playerName
            ).openConnection(MinecraftClient.getInstance().getNetworkProxy());
            connection.setDoInput(true);
            connection.setDoOutput(false);
            connection.connect();

            if (connection.getResponseCode() / 100 == 2) {
                JsonObject json = new JsonParser().parse(
                        new InputStreamReader(connection.getInputStream())
                ).getAsJsonObject();
                accountApiUuid = json.get("id").getAsString();
            }

        } catch (IOException ignored) {
            canFetch = false;
            SkinPatch.LOGGER.error("Couldn't open HTTP connection");
        } catch (IllegalStateException ignored) {
            SkinPatch.LOGGER.error(String.format("Couldn't fetch UUID for player %s", playerName));
        } finally {
            if (connection != null) connection.disconnect();
        }
        return accountApiUuid;
    }

    private static JsonObject getProfileTextures(String playerName) throws UnknownPlayerException, IOException, TextureNotFoundException {
        String uuid = getPlayerApiUuid(playerName);
        if (!canFetch) throw new IOException();
        if ("".equals(uuid)) throw new UnknownPlayerException();
        JsonObject json = null;

        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(
                    mojangEndpointUUIDToSkinCape + uuid
            ).openConnection(MinecraftClient.getInstance().getNetworkProxy());
            connection.setDoInput(true);
            connection.setDoOutput(false);
            connection.connect();

            if (connection.getResponseCode() / 100 == 2) {
                JsonObject profileJson = new JsonParser().parse(
                        new InputStreamReader(connection.getInputStream())
                ).getAsJsonObject();

                JsonArray propertiesJson = profileJson.get("properties").getAsJsonArray();

                ArrayList<JsonObject> ls = new ArrayList<>();
                propertiesJson.forEach(e -> ls.add(e.getAsJsonObject()));
                Optional<JsonObject> maybeJson = ls.stream().filter(
                        e -> Objects.equals(e.get("name").getAsString(), "textures")
                ).findFirst();
                if (!maybeJson.isPresent()) throw new TextureNotFoundException();
                JsonObject jsonTextures = maybeJson.get();

                json = new JsonParser().parse(
                        new String (Base64.getDecoder().decode(jsonTextures.get("value").getAsString()))
                ).getAsJsonObject();
            }
        } catch (IOException ignored) {
            canFetch = false;
            SkinPatch.LOGGER.error("Couldn't open HTTP connection");
        } catch (IllegalStateException ignored) {
            SkinPatch.LOGGER.error("Couldn't get profile");
            throw new UnknownPlayerException();
        } finally {
            if (connection != null) connection.disconnect();
        }
        return json;
    }

    /**
     * @author SRAZKVT
     * @reason If another mod mixins into this method, it is going to change the endpoint, giving another valid url, which wouldn't be compatible anyway.
     */
    @Overwrite
    public static String getSkinTextureUrl(String playerName) {
        if (!canFetch) return "";

        String skinTextureUrl = "";

        try {
            JsonObject json = getProfileTextures(playerName);
            JsonObject textures = json.get("textures").getAsJsonObject();

            if (textures.has("SKIN")) {
                skinTextureUrl = textures.get("SKIN").getAsJsonObject().get("url").getAsString();
            }
        } catch (UnknownPlayerException e) {
            SkinPatch.LOGGER.error(String.format("Player %s is unknown", playerName));
        } catch (IOException e) {
            SkinPatch.LOGGER.error("Couldn't fetch profile");
            canFetch = false;
        } catch (TextureNotFoundException e) {
            SkinPatch.LOGGER.error("Couldn't find textures");
        }

        return skinTextureUrl;
    }

    /**
     * @author SRAZKVT
     * @reason If another mod mixins into this method, it is going to change the endpoint, giving another valid url, which wouldn't be compatible anyway.
     */
    @Overwrite
    public static String getCapeTextureUrl(String playerName) {
        if (!canFetch) return "";

        String capeTextureUrl = "";

        try {
            JsonObject json = getProfileTextures(playerName);
            JsonObject textures = json.get("textures").getAsJsonObject();

            if (textures.has("CAPE")) {
                capeTextureUrl = textures.get("CAPE").getAsJsonObject().get("url").getAsString();
            }
        } catch (UnknownPlayerException e) {
            SkinPatch.LOGGER.error(String.format("Player %s is unknown", playerName));
        } catch (IOException e) {
            SkinPatch.LOGGER.error("Couldn't fetch profile");
            canFetch = false;
        } catch (TextureNotFoundException e) {
            SkinPatch.LOGGER.error("Couldn't find textures");
        }

        return capeTextureUrl;
    }
}
