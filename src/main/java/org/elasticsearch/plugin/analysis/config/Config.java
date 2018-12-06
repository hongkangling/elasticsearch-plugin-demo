package org.elasticsearch.plugin.analysis.config;

import com.homedo.bigdata.analysis.analyzer.dict.Dictionary;
import com.homedo.bigdata.analysis.elasticsearch.plugin.HmAnalysisPlugin;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.common.logging.ESLoggerFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;

public class Config {
    private static final Logger logger = ESLoggerFactory.getLogger(Config.class);

    private Properties properties;
    private final static String CONFIG_FILE_NAME = "config.properties";

    private volatile static Config instance;

    private Path configDir;

    public static void initialize(EnvInfo envInfo) {
        if(instance == null) {
            synchronized (Config.class) {
                if(instance == null) {
                    instance = new Config(envInfo);
                }
            }
        }
        Dictionary.getInstance();
    }

    private Config(EnvInfo envInfo) {
        this.configDir = envInfo.getEnvironment().configFile().resolve(HmAnalysisPlugin.PLUGIN_NAME);
        logger.info("config dir: {}", this.configDir);
        Path configFile = this.configDir.resolve(CONFIG_FILE_NAME);
        InputStream input = null;
        try {
            logger.info("try load config from {}", configFile);
            input = new FileInputStream(configFile.toFile());
        } catch (FileNotFoundException e) {
            configFile = this.configDir.resolve(CONFIG_FILE_NAME);
            try {
                logger.info("try load config from {}", configFile);
                input = new FileInputStream(configFile.toFile());
            } catch (FileNotFoundException ex) {
                logger.error("analysis-hm", e);
            }
        }
        if (input != null) {
            try {
                this.properties = new Properties();
                properties.load(input);
            } catch (InvalidPropertiesFormatException e) {
                logger.error("analysis-hm", e);
            } catch (IOException e) {
                logger.error("analysis-hm", e);
            }
        }
    }

    public static Config getInstance() {
        return instance;
    }

    public String get(String key) {
        if(this.properties != null) {
            return this.properties.getProperty(key);
        }
        return null;
    }

    public String get(String key, String defaultValue) {
        String value = this.get(key);
        if (value == null) {
            return defaultValue;
        }
        return value.trim();
    }

    public String getNoTrim(String key, String defaultValue) {
        String value = this.get(key);
        if (value == null) {
            return defaultValue;
        }
        return value;
    }

    public int getInt(String key, int defaultValue) {
        return NumberUtils.toInt(this.get(key), defaultValue);
    }

    public float getFloat(String key, float defaultValue) {
        return NumberUtils.toFloat(this.get(key), defaultValue);
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        String value = this.get(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Boolean.parseBoolean(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public Path getConfigFile(String key) {
        String fileName = this.get(key);
        if(StringUtils.isBlank(fileName)) {
            return null;
        }
        return this.configDir.resolve(fileName);
    }

    public static void main(String[] args) {
        Config.initialize(null);
    }
}
