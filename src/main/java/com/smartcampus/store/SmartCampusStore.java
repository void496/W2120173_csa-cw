package com.smartcampus.store;

import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SmartCampusStore {

    private static final Map<String, Room> ROOMS = new HashMap<>();
    private static final Map<String, Sensor> SENSORS = new HashMap<>();
    private static final Map<String, List<SensorReading>> READINGS_BY_SENSOR_ID = new HashMap<>();

    static {
        loadStarterData();
    }

    private SmartCampusStore() {
    }

    private static void loadStarterData() {
        addRoom(new Room("R001", "Lab 1", 40, new ArrayList<>()));
        addRoom(new Room("R002", "Lecture Hall A", 120, new ArrayList<>()));
        addRoom(new Room("R003", "Server Room", 10, new ArrayList<>()));

        addSensor(new Sensor("S001", "TEMPERATURE", "ACTIVE", 24.5, "R001"));
        addSensor(new Sensor("S002", "HUMIDITY", "MAINTENANCE", 0.0, "R002"));
        addSensor(new Sensor("S003", "MOTION", "ACTIVE", 1.0, "R003"));

        addReading("S001", new SensorReading("SR001", System.currentTimeMillis() - 300000, 23.8));
        addReading("S001", new SensorReading("SR002", System.currentTimeMillis() - 120000, 24.1));
        addReading("S001", new SensorReading("SR003", System.currentTimeMillis(), 24.5));
    }

    public static List<Room> getAllRooms() {
        return new ArrayList<>(ROOMS.values());
    }

    public static Room getRoom(String roomId) {
        return ROOMS.get(roomId);
    }

    public static boolean roomExists(String roomId) {
        return ROOMS.containsKey(roomId);
    }

    public static void addRoom(Room room) {
        ROOMS.put(room.getId(), room);
    }

    public static void removeRoom(String roomId) {
        ROOMS.remove(roomId);
    }

    public static List<Sensor> getAllSensors() {
        return new ArrayList<>(SENSORS.values());
    }

    public static Sensor getSensor(String sensorId) {
        return SENSORS.get(sensorId);
    }

    public static boolean sensorExists(String sensorId) {
        return SENSORS.containsKey(sensorId);
    }

    public static void addSensor(Sensor sensor) {
        SENSORS.put(sensor.getId(), sensor);

        Room room = ROOMS.get(sensor.getRoomId());
        if (room != null && !room.getSensorIds().contains(sensor.getId())) {
            room.getSensorIds().add(sensor.getId());
        }

        READINGS_BY_SENSOR_ID.put(sensor.getId(), new ArrayList<>());
    }

    public static List<SensorReading> getReadings(String sensorId) {
        List<SensorReading> readings = READINGS_BY_SENSOR_ID.get(sensorId);
        if (readings == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(readings);
    }

    public static void addReading(String sensorId, SensorReading reading) {
        List<SensorReading> readings = READINGS_BY_SENSOR_ID.get(sensorId);

        if (readings == null) {
            readings = new ArrayList<>();
            READINGS_BY_SENSOR_ID.put(sensorId, readings);
        }

        readings.add(reading);
    }

    public static String generateRoomId() {
        int nextNumber = ROOMS.size() + 1;
        String roomId = "R" + String.format("%03d", nextNumber);

        while (ROOMS.containsKey(roomId)) {
            nextNumber++;
            roomId = "R" + String.format("%03d", nextNumber);
        }

        return roomId;
    }

    public static String generateSensorId() {
        int nextNumber = SENSORS.size() + 1;
        String sensorId = "S" + String.format("%03d", nextNumber);

        while (SENSORS.containsKey(sensorId)) {
            nextNumber++;
            sensorId = "S" + String.format("%03d", nextNumber);
        }

        return sensorId;
    }

    public static String generateReadingId() {
        int nextNumber = getReadingCount() + 1;
        String readingId = "SR" + String.format("%03d", nextNumber);

        while (readingIdExists(readingId)) {
            nextNumber++;
            readingId = "SR" + String.format("%03d", nextNumber);
        }

        return readingId;
    }

    private static int getReadingCount() {
        int count = 0;

        for (List<SensorReading> readings : READINGS_BY_SENSOR_ID.values()) {
            count += readings.size();
        }

        return count;
    }

    private static boolean readingIdExists(String readingId) {
        for (List<SensorReading> readings : READINGS_BY_SENSOR_ID.values()) {
            for (SensorReading reading : readings) {
                if (readingId.equals(reading.getId())) {
                    return true;
                }
            }
        }

        return false;
    }
}
