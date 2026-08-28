package com.soarclient.management.cape;

import com.soarclient.skia.Skia;
import com.soarclient.skia.image.ImageHelper;
import java.io.Closeable;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;

public class CapeManager implements Closeable {
    private static CapeManager instance;

    private final Map<String, Identifier> loadedCapes = Collections.synchronizedMap(new HashMap<>());
    private final Map<Identifier, NativeImageBackedTexture> loadedCapeTextures = Collections.synchronizedMap(new HashMap<>());

    private String selectedCapeId = null;
    private volatile boolean closed;

    private final String namespace = "soar-capes";
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    public CapeManager() {
        instance = this;
    }

    public static CapeManager getInstance() {
        return instance;
    }

    public void selectCape(String capeId) {
        this.selectedCapeId = capeId;
    }

    public String getSelectedCapeId() {
        return selectedCapeId;
    }

    public Identifier getSelectedCapeTexture() {
        if (selectedCapeId == null) return null;
        return getLoadedCape(selectedCapeId);
    }

    public void clearSelectedCape() {
        this.selectedCapeId = null;
    }

    public void loadCape(String id, byte[] textureData) {
        if (closed || id == null || textureData == null) return;

        executorService.submit(() -> {
            NativeImage pixels;
            try {
                pixels = NativeImage.read(textureData);
            } catch (IOException exception) {
                throw new RuntimeException(exception);
            }

            if (closed) {
                pixels.close();
                return;
            }

            MinecraftClient.getInstance().execute(() -> {
                if (closed) {
                    pixels.close();
                    return;
                }
                NativeImageBackedTexture nativeImage = new NativeImageBackedTexture(() -> "Soar cape " + id, pixels);
                Identifier identifier = Identifier.of("soar", namespace + "/" + id);
                Skia.getImageHelper().put(identifier, ImageHelper.nativeImageToSkijaImage(pixels));
                MinecraftClient.getInstance().getTextureManager().registerTexture(identifier, nativeImage);
                loadedCapes.put(id, identifier);
                loadedCapeTextures.put(identifier, nativeImage);
            });
        });
    }

    public void unloadCape(String id) {
        if (id == null) return;

        if (id.equals(selectedCapeId)) {
            selectedCapeId = null;
        }

        Identifier cape = loadedCapes.remove(id);
        if (cape != null) {
            loadedCapeTextures.remove(cape);
            Skia.getImageHelper().remove(cape);
            MinecraftClient.getInstance().getTextureManager().destroyTexture(cape);
        }
    }

    public Identifier getLoadedCape(String id) {
        return id != null ? loadedCapes.get(id) : null;
    }

    public Set<String> getLoadedCapeIds() {
        return new HashSet<>(loadedCapes.keySet());
    }

    @Override
    public void close() {
        closed = true;
        selectedCapeId = null;
        new HashMap<>(loadedCapes).keySet().forEach(this::unloadCape);
        executorService.shutdown();
    }
}
