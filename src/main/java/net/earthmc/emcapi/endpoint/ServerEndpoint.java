package net.earthmc.emcapi.endpoint;

import au.lupine.quarters.api.manager.QuarterManager;
import com.google.gson.JsonObject;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownySettings;
import net.earthmc.emcapi.object.endpoint.GetEndpoint;
import net.earthmc.emcapi.util.EndpointUtils;
import au.lupine.quarters.object.entity.Quarter;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.time.LocalTime;
import java.util.List;

public class ServerEndpoint extends GetEndpoint {

    @Override
    public String lookup() {
        return getJsonElement().toString();
    }

    @Override
    public JsonObject getJsonElement() {
        JsonObject serverObject = new JsonObject();

        TownyAPI townyAPI = TownyAPI.getInstance();
        QuarterManager quarterManager = QuarterManager.getInstance();
        World overworld = Bukkit.getWorlds().get(0);

        serverObject.addProperty("version", Bukkit.getMinecraftVersion());
        serverObject.addProperty("moonPhase", overworld.getMoonPhase().toString());

        JsonObject timestampsObject = new JsonObject();
        timestampsObject.addProperty("newDayTime", TownySettings.getNewDayTime());
        timestampsObject.addProperty("serverTimeOfDay", LocalTime.now().toSecondOfDay());
        serverObject.add("timestamps", timestampsObject);

        JsonObject statusObject = new JsonObject();
        statusObject.addProperty("hasStorm", overworld.hasStorm());
        statusObject.addProperty("isThundering", overworld.isThundering());
        serverObject.add("status", statusObject);

        JsonObject statsObject = new JsonObject();
        statsObject.addProperty("time", overworld.getTime());
        statsObject.addProperty("fullTime", overworld.getFullTime());
        statsObject.addProperty("maxPlayers", Bukkit.getMaxPlayers());
        statsObject.addProperty("numOnlinePlayers", Bukkit.getOnlinePlayers().size());
        statsObject.addProperty("numOnlineNomads", EndpointUtils.getNumOnlineNomads());
        statsObject.addProperty("numResidents", townyAPI.getResidents().size());
        statsObject.addProperty("numNomads", townyAPI.getResidentsWithoutTown().size());
        statsObject.addProperty("numTowns", townyAPI.getTowns().size());
        statsObject.addProperty("numTownBlocks", townyAPI.getTownBlocks().size());
        statsObject.addProperty("numNations", townyAPI.getNations().size());
        
        // Calculate total outpost spawns across all towns (looks like it works thanks copilot :)
        int totalOutpostSpawns = townyAPI.getTowns().stream()
            .filter(town -> town.hasOutpostSpawn())
            .mapToInt(town -> {
                try {
                    return town.getAllOutpostSpawns().size();
                } catch (Exception e) {
                    return 0; 
                }
            })
            .sum();
        statsObject.addProperty("numOutposts", totalOutpostSpawns);

        List<Quarter> quarters = quarterManager.getAllQuarters();
        statsObject.addProperty("numQuarters", quarters.size());
        statsObject.addProperty("numCuboids", quarters.parallelStream().mapToInt(q -> q.getCuboids().size()).sum());

        serverObject.add("stats", statsObject);
        return serverObject;
    }
}