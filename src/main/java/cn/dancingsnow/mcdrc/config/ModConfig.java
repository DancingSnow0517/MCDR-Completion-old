package cn.dancingsnow.mcdrc.config;

import cn.dancingsnow.mcdrc.server.MCDRCommandServer;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;

@Config(name = MCDRCommandServer.MOD_ID)
public class ModConfig implements ConfigData {
    public String node_path = "config/node.json";
}
