package io.github.cardsandhuskers.tgttos.objects;

import io.github.cardsandhuskers.tgttos.TGTTOS;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.bukkit.Bukkit;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class StatCalculator {
    private TGTTOS plugin;
    private ArrayList<PlayerStatsHolder> playerStatsHolders;

    public StatCalculator(TGTTOS plugin) {
        this.plugin = plugin;
    }

    public void calculateStats() throws IOException {
        int initialEvent = 2;
        int eventNum;
        try {eventNum = Bukkit.getPluginManager().getPlugin("LobbyPlugin").getConfig().getInt("eventNum");}
        catch (Exception e) {eventNum = initialEvent;}

        HashMap<String, PlayerStatsHolder> playerStatsMap = new HashMap<>();
        FileReader reader;
        for(int i = initialEvent; i <= eventNum; i++) {
            try {
                reader = new FileReader(plugin.getDataFolder() + "/tgttosStats" + i + ".csv");
            } catch (IOException e) {
                plugin.getLogger().warning("Stats file not found!");
                continue;
            }
            String[] headers = {"Round", "Player", "Team", "Place", "Points"};

            CSVFormat.Builder builder = CSVFormat.Builder.create();
            builder.setHeader(headers);
            CSVFormat format = builder.build();

            CSVParser parser;
            parser = new CSVParser(reader, format);

            List<CSVRecord> recordList = parser.getRecords();
            reader.close();


            for(CSVRecord r:recordList) {
                if (r.getRecordNumber() == 1) continue;

                String name = r.get(1);
                if(playerStatsMap.containsKey(name)) {
                    playerStatsMap.get(name).addRecord(Integer.parseInt(r.get(3)));
                }
                else {
                    PlayerStatsHolder holder = new PlayerStatsHolder(name);
                    holder.addRecord(Integer.parseInt(r.get(3)));
                    playerStatsMap.put(name, holder);
                }
            }
        }

        playerStatsHolders = new ArrayList<>(playerStatsMap.values());
        playerStatsHolders.sort(new PlayerStatsComparator());

    }

    public ArrayList<PlayerStatsHolder> getPlayerStatsHolders() {
        return new ArrayList<>(playerStatsHolders);
    }

    public class PlayerStatsHolder {
        String name;
        ArrayList<Integer> placements;
        int wins;
        public PlayerStatsHolder(String name) {
            this.name = name;
            wins = 0;
            placements = new ArrayList<>();
        }

        public void addRecord(int placement) {
            placements.add(placement);
            if(placement == 1) wins++;
        }
        public double getAveragePlacement() {
            double sum = 0;
            for(Integer x: placements) {
                sum += x;
            }
            sum = sum / (double)placements.size();
            return sum;
        }
    }

    public class PlayerStatsComparator implements Comparator<PlayerStatsHolder> {
        public int compare(PlayerStatsHolder h1, PlayerStatsHolder h2) {
            int compare = Double.compare(h1.getAveragePlacement(), h2.getAveragePlacement());
            if(compare == 0) compare = h1.name.compareTo(h2.name);
            return compare;
        }
    }

}
