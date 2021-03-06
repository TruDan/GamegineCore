package com.stealthyone.mcb.gameginecore.backend.players;

import com.stealthyone.mcb.gameginecore.Gamegine;
import com.stealthyone.mcb.gameginecore.backend.games.Game;
import com.stealthyone.mcb.gameginecore.backend.games.GameInstance;
import com.stealthyone.mcb.gameginecore.config.ConfigHelper;
import com.stealthyone.mcb.stbukkitlib.lib.plugin.LogHelper;
import com.stealthyone.mcb.stbukkitlib.lib.storage.YamlFileManager;
import com.stealthyone.mcb.stbukkitlib.lib.utils.TimeUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PlayerManager {

    private Gamegine plugin;
    private File playerDir;

    private Map<String, GgPlayerFile> loadedFiles = new HashMap<>(); //id, file
    private Map<String, GgPlayer> playerCasts = new HashMap<>(); //player id, file
    private Map<String, String> playerGames = new HashMap<>(); //player ID, Arena ID

    public PlayerManager(Gamegine plugin) {
        Logger log = Bukkit.getLogger();

        this.plugin = plugin;
        LogHelper.DEBUG(plugin, "Creating player manager");

        playerDir = new File(plugin.getDataFolder() + File.separator + "players");
        if (playerDir.mkdir()) {
            LogHelper.DEBUG(plugin, "Created player directory");
        }
        purgeEmptyFiles();

        /* Check config values */
        log.log(Level.INFO, "");
        log.log(Level.INFO, "-----Gamegine Configuration: Players-----");

        //How often to check for inactive files
        int inactiveCheck = ConfigHelper.PLAYERS_FILES_INACTIVE_CHECK.get();
        log.log(Level.INFO, "Checking for inactive files " + (inactiveCheck <= 0 ? "DISABLED." : "after " + TimeUtils.translateSeconds(inactiveCheck) + "."));
        if (inactiveCheck > 0) {
            Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
                @Override
                public void run() {
                    purgeInactiveFiles();
                }
            }, 20 * inactiveCheck, 20 * inactiveCheck);
        }

        //How long a file should be inactive in order for it to be unloaded
        int inactiveTime = inactiveCheck <= 0 ? 0 : ConfigHelper.PLAYERS_FILES_INACTIVE_TIME.get();
        log.log(Level.INFO, "Unloading inactive files " + (inactiveTime <= 0 ? "DISABLED." : "after " + TimeUtils.translateSeconds(inactiveTime) + " of no use."));
    }

    public void save() {
        for (GgPlayerFile file : loadedFiles.values()) {
            file.saveFile();
        }
    }

    private void purgeEmptyFiles() {
        int purgeCount = 0;
        for (File file : playerDir.listFiles()) {
            if (file.getName().matches("[a-z0-9*].yml")) {
                YamlFileManager cast = new YamlFileManager(file);
                if (cast.isEmpty()) {
                    if (file.delete()) {
                        LogHelper.DEBUG(plugin, "Purged player file for UUID: " + file.getName().replace(".yml", ""));
                    }
                    purgeCount++;
                }
            }
        }
        LogHelper.INFO(plugin, "Purged " + purgeCount + " empty player files.");
    }

    public void purgeInactiveFiles() {
        int inactiveTime = ConfigHelper.PLAYERS_FILES_INACTIVE_TIME.get();
        if (inactiveTime <= 0) {
            return;
        }

        int purgeCount = 0;
        long curTime = System.currentTimeMillis();
        for (Entry<String, GgPlayerFile> entry : loadedFiles.entrySet()) {
            GgPlayerFile file = entry.getValue();
            if (curTime - file.getLastAccessed() / 1000 >= inactiveTime) {
                if (unloadFile(file.getUuid())) {
                    LogHelper.DEBUG(plugin, "Unloaded inactive file for UUID: " + file.getUuid());
                    purgeCount++;
                }
            }
        }
        LogHelper.INFO(plugin, "Unloaded " + purgeCount + " inactive player files.");
    }

    public GgPlayerFile getFile(String uuid, boolean createIfNotExists) {
        if (!loadedFiles.containsKey(uuid)) {
            File rawFile = new File(playerDir + File.separator + uuid + ".yml");
            if (rawFile.exists()) {
                loadedFiles.put(uuid, new GgPlayerFile(rawFile));
                plugin.getCooldownManager().loadCooldowns(uuid);
                plugin.getSelectionManager().loadSelection(uuid);
            } else if (createIfNotExists) {
                LogHelper.DEBUG(plugin, "Unable to find player file for UUID: " + uuid + ", creating now.");
                loadedFiles.put(uuid, new GgPlayerFile(rawFile));
            } else {
                LogHelper.DEBUG(plugin, "Unable to find player file for UUID: " + uuid);
            }
        }
        return loadedFiles.get(uuid);
    }

    public boolean unloadFile(String uuid) {
        if (loadedFiles.containsKey(uuid)) {
            loadedFiles.remove(uuid).saveFile();
            return true;
        }
        return false;
    }

    public GgPlayer castPlayer(Player player) {
        Validate.notNull(player, "Player cannot be null.");

        String id = player.getUniqueId().toString();
        if (!playerCasts.containsKey(id)) {
            getFile(id, true);
            playerCasts.put(id, new GgPlayer(id));
        }
        return playerCasts.get(id);
    }

    public boolean isPlayerInGame(GgPlayer player) {
        return playerGames.containsKey(player.getUuid());
    }

    public GameInstance getPlayerGame(GgPlayer player) {
        String uuid = player.getUuid();
        if (!playerGames.containsKey(uuid)) {
            return null;
        } else {
            try {
                return Gamegine.getInstance().getArenaManager().getArena(playerGames.get(uuid)).getGameInstance();
            } catch (NullPointerException ex) {
                return null;
            }
        }
    }

    public boolean addPlayerToGame(GgPlayer player, Game game) {

    }

    public boolean removePlayerFromGame(GgPlayer player) {
        String uuid = player.getUuid();
        if (!playerGames.containsKey(uuid)) {
            return false;
        } else {
            getGame
            return true;
        }
    }

}