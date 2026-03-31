package zyo.narutomod.jutsu;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.HashMap;
import java.util.Map;

public class JutsuManager extends SimpleJsonResourceReloadListener {

    private static final Gson GSON = new GsonBuilder().create();
    public static final Map<ResourceLocation, JutsuData> LOADED_JUTSUS = new HashMap<>();

    public JutsuManager() {
        super(GSON, "jutsus");
    }

    public static Map<ResourceLocation, JutsuData> getLoadedJutsus() {
        return LOADED_JUTSUS;
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> jsonMap, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        LOADED_JUTSUS.clear();

        for (Map.Entry<ResourceLocation, JsonElement> entry : jsonMap.entrySet()) {
            ResourceLocation fileLocation = entry.getKey();
            JsonElement jsonContent = entry.getValue();

            try {
                JutsuData jutsu = GSON.fromJson(jsonContent, JutsuData.class);
                LOADED_JUTSUS.put(fileLocation, jutsu);

                System.out.println("Loaded Jutsu: " + jutsu.name + " | Cost: " + jutsu.chakra_cost);

            } catch (Exception e) {
                System.err.println("Failed to load jutsu JSON: " + fileLocation);
                e.printStackTrace();
            }
        }
    }
}