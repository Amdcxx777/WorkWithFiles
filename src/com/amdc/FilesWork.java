package com.amdc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.*;

public class FilesWork {
    private static char index;
    private static int valPrice;
    private static int valSize;
    private static PrintWriter printDataBase;
    private static PrintWriter printOutFile;
    static String separator = File.separator;
    static Map<Integer, String[]> mapDB = new HashMap<>();
    static String[] valueData = new String[3];
    static int key;

    public static void main(String[] args) throws NullPointerException, IOException, SQLException {
        Date currentTime = new Date();
        File fileOut = new File("src" + separator + "datafiles" + separator + "output_data.txt"); // адрес исходящего файла
        if(!fileOut.exists()) fileOut.createNewFile();
        printOutFile = new PrintWriter(fileOut);
        File fileDB = new File("src" + separator + "datafiles" + separator + "data_base.txt");
        if(!fileDB.exists()) fileOut.createNewFile();
        readDataFromDataFile(fileDB);
        printDataBase = new PrintWriter(fileDB);
        readDataFromInputFile();
        writeFileDB();
        printOutFile.close();
        printDataBase.close();

        Date newTime = new Date(); //получаем новое текущее время
        long msDelay = newTime.getTime() - currentTime.getTime();
        System.out.println("Time distance is: " + msDelay + " ms");

    }
    public static void readDataFromDataFile(File file) throws NullPointerException, NoSuchElementException, FileNotFoundException {
        Scanner scanner = new Scanner(file); // путь к файлу
            while (scanner.hasNextLine()) { // обработка файла
                String line = scanner.nextLine(); // чтение строк из файла
                if (!line.isEmpty()) {
                    String[] array = line.split(","); // разделитель
                    mapDB.put(mapDB.size() + 1, new String[]{array[0] + "", array[1] + "", array[2] + ""});
                    //System.out.println(array[0] + array[1] + array[2]);
                }
            }
            scanner.close();
    }

    public static void writeFileDB() {
        for(Object value : mapDB.values()) {
            valueData = (String[]) value;
            printDataBase.println(valueData[0] + "," + valueData[1] + "," + valueData[2]);
        }
    }

    public static void readDataFromInputFile() throws NullPointerException, NoSuchElementException, FileNotFoundException, SQLException {
        Scanner scanner = new Scanner(new File("src" + separator + "datafiles" + separator + "input_data.txt")); // путь к файлу
        while (scanner.hasNextLine()) { // обработка файла
            String line = scanner.nextLine(); // чтение строк из файла
            String[] array = line.split(","); // разделитель
            if (array[0].equals("u") && array.length == 4)
                writeBD(Integer.parseInt(array[1]), Integer.parseInt(array[2]), array[3]);
            else if (array[0].equals("o") && array.length == 3) renewBD(array[1], Integer.parseInt(array[2]));
            else if (array[0].equals("q") && array.length == 2) outputBestRequest(array[1]);
            else if (array[0].equals("q") && array.length == 3) outputBestPrice(Integer.parseInt(array[2]));
            else break;
        }
        scanner.close();
    }

    public static void writeBD(int priceIn, int sizeIn, String indexIn) { //добавление в базу
        switch (indexIn) {
            case "bid":
                index = 'B'; // заявка на покупку
                break;
            case "ask":
                index = 'A'; //заявка на продажу
                break;
            case "spread":
                index = 'S'; //заявка spread
                break;
            default:
                return;
        }
        mapDB.put(mapDB.size() + 1, new String[] {priceIn + "", sizeIn + "", index + ""});
    }

    public static void renewBD(String delRequest, int sizeIn) throws SQLException, FileNotFoundException {
        if (delRequest.equals("sell")) { // удаляет <размер> акций из заявок на покупку с максимальной ценой.
            valPrice = 0;
            for (Map.Entry value : mapDB.entrySet()) {
                valueData = (String[]) value.getValue();
                if (valueData[2].contains("B") && Integer.parseInt(valueData[0]) > valPrice) {
                    valPrice = Integer.parseInt(valueData[0]);
                    key = (int) value.getKey();
                }
            }
            valueData = mapDB.get(key);
            if (Integer.parseInt(valueData[1]) < sizeIn) sizeIn = Integer.parseInt(valueData[1]);
            valueData[1] = (Integer.parseInt(valueData[1]) - sizeIn) + "";
            mapDB.put(key, valueData);

        } else if (delRequest.equals("buy")) { // удаляет <размер> акций из заявок на продажу с минимальной ценой.
            for (Map.Entry value : mapDB.entrySet()) {
                valueData = (String[]) value.getValue();
                if (valueData[2].contains("A")) {
                    valPrice = Integer.parseInt(valueData[0]);
                }
            }
            for (Map.Entry value : mapDB.entrySet()) {
                valueData = (String[]) value.getValue();
                if (valueData[2].contains("A") && Integer.parseInt(valueData[0]) < valPrice) {
                    valPrice = Integer.parseInt(valueData[0]);
                    key = (int) value.getKey();
                }
            }
            valueData = mapDB.get(key);
            if (Integer.parseInt(valueData[1]) < sizeIn) sizeIn = Integer.parseInt(valueData[1]);
            valueData[1] = (Integer.parseInt(valueData[1]) - sizeIn) + "";
            mapDB.put(key, valueData);
        }
    }

    public static void outputBestRequest(String request) {
        if (request.equals("best_bid")) {
            valPrice = 0;
            for (Map.Entry value : mapDB.entrySet()) {
                valueData = (String[]) value.getValue();
                if (valueData[2].contains("B") && Integer.parseInt(valueData[0]) > valPrice) {
                    valPrice = Integer.parseInt(valueData[0]);
                    valSize = Integer.parseInt(valueData[1]);
                }
            }
            printOutFile.println(valPrice + "," + valSize);
        } else if (request.equals("best_ask")) {
            for (Map.Entry value : mapDB.entrySet()) {
                valueData = (String[]) value.getValue();
                if (valueData[2].contains("A")) {
                    valPrice = Integer.parseInt(valueData[0]);
                }
            }
            for (Map.Entry value : mapDB.entrySet()) {
                valueData = (String[]) value.getValue();
                if (valueData[2].contains("A") && Integer.parseInt(valueData[0]) < valPrice) {
                    valPrice = Integer.parseInt(valueData[0]);
                    valSize = Integer.parseInt(valueData[1]);
                }
            }
            printOutFile.println(valPrice + "," + valSize);
        }
    }

    public static void outputBestPrice(int price) throws SQLException {
        for (Map.Entry value : mapDB.entrySet()) {
            valueData = (String[]) value.getValue();
            if (Integer.parseInt(valueData[0]) == price) {
                valSize = Integer.parseInt(valueData[1]);
                printOutFile.println(valSize);
            }
        }
    }
}

