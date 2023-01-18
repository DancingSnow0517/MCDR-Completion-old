package cn.dancingsnow.mcdr_command;

import cn.dancingsnow.mcdr_command.command.Node;
import cn.dancingsnow.mcdr_command.command.NodeData;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.FileReader;

public class test {
    public static void main(String[] args) {
        Gson GSON = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
        NodeData data;
        try {
            BufferedReader br = new BufferedReader(new FileReader("node.json"));
            data = GSON.fromJson(br, NodeData.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        for (Node node : data.data) {
            System.out.println(node.name);
            for (Node node1 : node.children) {
                System.out.println(node1.name);
            }
        }

    }
}
