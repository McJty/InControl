package mcjty.incontrol.rules;

import com.google.gson.*;
import mcjty.incontrol.InControl;
import org.apache.logging.log4j.Level;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class RulesManager {

    private static String path;
    public static List<SpawnRule> rules = new ArrayList<>();
    public static List<SummonAidRule> summonAidRules = new ArrayList<>();
    public static List<PotentialSpawnRule> potentialSpawnRules = new ArrayList<>();
    public static List<LootRule> lootRules = new ArrayList<>();

    public static void reloadRules() {
        rules.clear();
        summonAidRules.clear();
        potentialSpawnRules.clear();
        lootRules.clear();
        readAllRules();
    }

    public static void readRules(File directory) {
        path = directory.getPath();
        readAllRules();
    }

    private static void readAllRules() {
        readRules("spawn.json", SpawnRule::parse, rules);
        readRules("summonaid.json", SummonAidRule::parse, summonAidRules);
        readRules("potentialspawn.json", PotentialSpawnRule::parse, potentialSpawnRules);
        readRules("loot.json", LootRule::parse, lootRules);
    }

    private static <T> void readRules(String filename, Function<JsonElement, T> parser, List<T> rules) {
        JsonElement element = getRootElement(filename);
        if (element == null) {
            return;
        }
        int i = 0;
        for (JsonElement entry : element.getAsJsonArray()) {
            T rule = parser.apply(entry);
            if (rule != null) {
                rules.add(rule);
            } else {
                InControl.logger.log(Level.ERROR, "Rule " + i + " in " + filename + " is invalid, skipping!");
            }
            i++;
        }
    }


    private static JsonElement getRootElement(String filename) {
        File file = new File(path + File.separator + "incontrol", filename);
        if (!file.exists()) {
            // Create an empty rule file
            makeEmptyRuleFile(file);
            return null;
        }

        InControl.logger.log(Level.INFO, "Reading spawn rules from " + filename);
        InputStream inputstream = null;
        try {
            inputstream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            InControl.logger.log(Level.ERROR, "Error reading " + filename + "!");
            return null;
        }

        BufferedReader br;
        try {
            br = new BufferedReader(new InputStreamReader(inputstream, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            InControl.logger.log(Level.ERROR, "Error reading " + filename + "!");
            return null;
        }

        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(br);

        return element;
    }

    private static void makeEmptyRuleFile(File file) {
        PrintWriter writer;
        try {
            writer = new PrintWriter(file);
        } catch (FileNotFoundException e) {
            InControl.logger.log(Level.ERROR, "Error writing " + file.getName() + "!");
            return;
        }
        JsonArray array = new JsonArray();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        writer.print(gson.toJson(array));
        writer.close();
    }


}
