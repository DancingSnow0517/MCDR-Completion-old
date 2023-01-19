package cn.dancingsnow.mcdrc.config;

import cn.dancingsnow.mcdrc.server.MCDRCommandServer;
import com.google.gson.JsonParseException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;


public class ModConfig {

    private final Path path;

    public ModConfig(String path) {
        this(Path.of(path));
    }

    public ModConfig(Path path) {
        this.path = path;
    }
    private ConfigData data = new ConfigData();

    public boolean save() {
        if (!Files.exists(path)) {
            try {
                Files.createFile(path);
            } catch (IOException e) {
                e.printStackTrace();
                MCDRCommandServer.LOGGER.error("Save {} error: createFile fail.", path);
                return false;
            }
        }

        try (BufferedWriter bfw = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            bfw.write(MCDRCommandServer.GSON.toJson(getData()));
        } catch (IOException e) {
            e.printStackTrace();
            MCDRCommandServer.LOGGER.error("Save {} error", path);
            return false;
        }
        return true;
    }

    public boolean load() {
        if (!Files.exists(path)) {
            return save();
        }
        try (BufferedReader bfr = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            setData(MCDRCommandServer.GSON.fromJson(bfr, ConfigData.class));
        } catch (IOException e) {
            e.printStackTrace();
            MCDRCommandServer.LOGGER.error("Load {} error: newBufferedReader fail.", path);
            return false;
        } catch (JsonParseException e) {
            MCDRCommandServer.LOGGER.error("Json {} parser fail!!", path);
            return false;
        }
        return true;
    }

    public ConfigData getData() {
        return data;
    }

    public void setData(ConfigData data) {
        this.data = data;
    }

    public String getNodePath() {
        return data.node_path;
    }

    public void setNodePath(String node_path) {
        data.node_path = node_path;
        save();
    }

    public static class ConfigData {
        public String node_path = "config/node.json";
    }
}
