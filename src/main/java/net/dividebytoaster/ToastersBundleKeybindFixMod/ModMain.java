package net.dividebytoaster.ToastersBundleKeybindFixMod;

/// Imports - Java
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/// Imports - Logging
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/// Imports - JSON
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;

/// Imports - Fabric
import net.fabricmc.loader.api.FabricLoader;

/// Imports - Annotations
import org.spongepowered.asm.mixin.Unique;

/**********************************************************************************************************************\
> Properties
\**********************************************************************************************************************/

public class ModMain
implements net.fabricmc.api.ModInitializer
{
    /// Nothing to do (all mixins)!
    @Override
    public void onInitialize()
    {}

    /// Standard Mod Stuff
    public static final String MOD_ID = "toasters-bundle-keybind-fix-mod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    /// Mod Helpers
    public static final Map<String, Boolean> CONFIG = initConfig();

    /// Config Helper Variables
    public static final String  CONFIG_KEY_SWAP_KEY = "KEY_SWAP";
    public static final String  CONFIG_ON_EMPTY_KEY = "ON_EMPTY";
    private static      boolean CONFIG_ERROR;

    // Returns `true` if an error occurred while loading `CONFIG`
    public static boolean hadConfigError() { return CONFIG_ERROR; }

    // Returns `true` if the core keybind changes are enabled
    public static boolean key_swap()
    {
        if (CONFIG_ERROR || !CONFIG.containsKey(CONFIG_KEY_SWAP_KEY))
            return false;
        return CONFIG.get(CONFIG_KEY_SWAP_KEY);
    }

    // Assigns the `KEY_SWAP` flag and saves the config file
    public static void key_swap(boolean value)
    {
        CONFIG.put(CONFIG_KEY_SWAP_KEY, value);
        updateConfigFile();
    }

    // Returns `true` if the optional keybind changes are enabled
    public static boolean on_empty()
    {
        if (CONFIG_ERROR || !CONFIG.containsKey(CONFIG_ON_EMPTY_KEY))
            return false;
        return CONFIG.get(CONFIG_ON_EMPTY_KEY);
    }

    // Assigns the `ON_EMPTY` flag and saves the config file
    public static void on_empty(boolean value)
    {
        CONFIG.put(CONFIG_ON_EMPTY_KEY, value);
        updateConfigFile();
    }

/**********************************************************************************************************************\
> Setup Methods
\**********************************************************************************************************************/

    @Unique
    private static File getConfigFile()
    {
        Path config_dir = FabricLoader.getInstance().getConfigDir().resolve("dividebytoaster");
        if (config_dir.toFile().mkdirs())
        {
            LOGGER.debug("Created config directory: {}", config_dir);
        }

        // Open the config file, creating one if needed
        return config_dir.resolve("bundle.json").toFile();
    }

    @Unique
    private static HashMap<String, Boolean> initConfig()
    {
        HashMap<String, Boolean> config = new HashMap<>();

        // Open the config file, creating one if needed
        File    configFile = getConfigFile();
        Boolean created    = null;
        try
        {
            created = configFile.createNewFile();

            ObjectMapper mapper = new ObjectMapper();
            if (created)
            {
                config.put(CONFIG_KEY_SWAP_KEY, true );
                config.put(CONFIG_ON_EMPTY_KEY, false);
                mapper.writeValue(configFile, config);
            }
            else
            {
                MapType type = TypeFactory.defaultInstance().constructMapType(HashMap.class, String.class, Boolean.class);
                config = mapper.readValue(configFile, type);

                boolean invalid = false;
                if (!config.containsKey(CONFIG_KEY_SWAP_KEY))
                {
                    invalid = true;
                    config.put(CONFIG_KEY_SWAP_KEY, true);
                }
                if (!config.containsKey(CONFIG_ON_EMPTY_KEY))
                {
                    invalid = true;
                    config.put(CONFIG_ON_EMPTY_KEY, false);
                }
                if (invalid)
                {
                    mapper.writeValue(configFile, config);
                }
            }
            CONFIG_ERROR = false;
        }
        catch (IOException e)
        {
            if (created == null)
            {
                LOGGER.error("Failed to create config file: {}", configFile);
            }
            else if (created)
            {
                LOGGER.error("Failed to initialize config file: {}", configFile);
            }
            else
            {
                LOGGER.error("Failed to parse JSON object from config file: {}", configFile);
            }
            CONFIG_ERROR = true;
        }

        // Return the map
        LOGGER.debug("Imported Config: {}", config);
        return config;
    }

    @Unique
    public static void updateConfigFile()
    {
        File configFile = getConfigFile();
        try
        {
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(configFile, CONFIG);
        }
        catch (IOException e)
        {
            LOGGER.error("Failed to update config file: {}", configFile);
        }
    }
}
