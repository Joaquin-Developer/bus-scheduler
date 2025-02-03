package com.miapp;

import redis.clients.jedis.Jedis;
import com.google.gson.Gson;

import java.io.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class App {
    private static final String CSV_PATH = "/Users/jparrilla/Downloads/horarios.csv";
    private static final String REDIS_QUEUE = "bus_queue";
    private static final int SLEEP_TIME = 1000 * 60;

    private static List<Map<String, String>> getEventsData() {
        List<Map<String, String>> events = new ArrayList<>();
        LocalTime now = LocalTime.now();

        try (BufferedReader br = new BufferedReader(new FileReader(CSV_PATH))) {
            String line;
            br.readLine();  // skip first line

            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                String turno = values[0].trim();
                String linea = values[1].trim().toUpperCase();
                String destino = values[2].trim().toUpperCase();

                if (turno.length() == 4) {
                    turno = "0" + turno;
                }

                LocalTime turnoTime = LocalTime.parse(turno);

                if (turnoTime.equals(now)) {
                    Map<String, String> busEvent = new HashMap<>();
                    busEvent.put("turno", turno);
                    busEvent.put("linea", linea);
                    busEvent.put("destino", destino);
                    events.add(busEvent);
                }
            }
        } catch (Exception e) {
            System.err.println("Error al leer el archivo CSV: " + e.getMessage());
        }
        return events;
    }

    private static void publishInRedis(List<Map<String, String>> events) {
        try (Jedis jedis = new Jedis("localhost", 6379)) {
            Gson gson = new Gson();

            for (Map<String, String> event : events) {
                String jsonEvent = gson.toJson(event);
                jedis.lpush(REDIS_QUEUE, jsonEvent);
                System.out.println("- Evento agregado a Redis: " + jsonEvent);
            }

            System.out.println("Todos los eventos han sido enviados a Redis.");
        } catch (Exception e) {
            System.err.println("Error al conectar con Redis: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        try {
            while (true) {
                List<Map<String, String>> events = getEventsData();
                publishInRedis(events);
                Thread.sleep(SLEEP_TIME);
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

    }
}
