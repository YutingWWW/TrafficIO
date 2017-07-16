/**
 * Created by sun on 7/12/17.
 */

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.*;
import java.util.Arrays;

public class dataCollection {

    static JSONArray readJson(String path) {
        JSONParser parser = new JSONParser();
        JSONArray jsonArray = null;
        try {
            jsonArray = (JSONArray) parser.parse(new FileReader(path));
        } catch(Exception e) {
            e.printStackTrace();
        }
        return jsonArray;
    }

    static void processImg(String path, JSONArray mapping) {
        File folder = new File(path);
        File[] files = folder.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".png");
            }
        });

        for (File file : files) {
            if (file.isFile()) {
                JSONArray ret = new JSONArray();

                String filename = file.getName();
                System.out.println(filename);

                int len = mapping.size();
                System.out.println("MapDataStructure size: " + len);

                BufferedImage img = null;
                int[] rgb = null;

                try {
                    img = ImageIO.read(file);
                    int maxWidth = img.getWidth();
                    int maxHeight = img.getHeight();
                    Raster raster = img.getRaster();
                    for (int i = 0; i < len; i++) {
                        JSONObject intersec = (JSONObject) mapping.get(i);
                        JSONObject result = new JSONObject();

                        int index = Integer.parseInt(intersec.get("index").toString());
                        JSONArray incoming = (JSONArray) intersec.get("incoming");
                        JSONArray outgoing = (JSONArray) intersec.get("outgoing");
                        if (incoming.size() != outgoing.size()) {
                            System.out.println("Error, incoming | outgoing not matching! index: " + index);
                            System.out.println(incoming.toJSONString() + " | " + outgoing.toJSONString());
                            continue;
                        }

                        int size = incoming.size();
                        int[][] inArr = new int[size][2];
                        int[][] outArr = new int[size][2];

                        int[] incomingResult = new int[size];
                        int[] outgoingResult = new int[size];

                        for (int j = 0; j < size; j++) {
                            String inStr = incoming.get(j).toString();
                            String[] inStrArr = inStr.substring(1, inStr.length() - 1).split(",");

                            String outStr = outgoing.get(j).toString();
                            String[] outStrArr = outStr.substring(1, outStr.length() - 1).split(",");


                            inArr[j][0] = Integer.parseInt(inStrArr[0]);
                            inArr[j][1] = Integer.parseInt(inStrArr[1]);

                            outArr[j][0] = Integer.parseInt(outStrArr[0]);
                            outArr[j][1] = Integer.parseInt(outStrArr[1]);

                            int[] inPoint = inArr[j];
                            int[] outPoint = outArr[j];

                            if (inPoint[0] >= 0 && inPoint[0] <= maxWidth
                                    && inPoint[1] >= 0 && inPoint[1] <= maxHeight
                                    && outPoint[0] >= 0 && outPoint[0] <= maxWidth
                                    && outPoint[1] >= 0 && outPoint[1] <= maxHeight) {
                                rgb = raster.getPixel(inPoint[0], inPoint[1], new int[3]);
                                incomingResult[j] = convertRGB(inPoint, rgb);

                                rgb = raster.getPixel(outPoint[0], outPoint[1], new int[3]);
                                outgoingResult[j] = convertRGB(outPoint, rgb);
                            }
                        }

                        result.put("index", intersec.get("index"));
                        result.put("center", intersec.get("center"));
                        result.put("xWay", size);
                        result.put("incoming", Arrays.toString(incomingResult));
                        result.put("outgoing", Arrays.toString(outgoingResult));
                        result.put("time", filename.substring(0, filename.indexOf(".png")));
                        ret.add(result);
                    }
                    System.out.println("Output size: " + ret.size());
                    System.out.println(ret.toJSONString());
                    System.out.println();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
        //return ret;
    }

    static int convertRGB(int[] point, int[] rgb) {
        int[] green = new int[]{121, 207, 76};
        int[] orange = new int[]{247, 124, 0};
        int[] red = new int[]{223, 0, 0};
        int[] darkRed = new int[]{153, 20, 16};

        double min = Double.MAX_VALUE;
        int ret = -1;

        double tmp = calColor(rgb, green);
        if (tmp >= 0 && tmp < min) {
            min = tmp;
            ret = 1;
        }

        tmp = calColor(rgb, orange);
        if (tmp >= 0 && tmp < min) {
            min = tmp;
            ret = 2;
        }

        tmp = calColor(rgb, red);
        if (tmp >= 0 && tmp < min) {
            min = tmp;
            ret = 3;
        }

        tmp = calColor(rgb, darkRed);
        if (tmp >= 0 && tmp < min) {
            min = tmp;
            ret = 4;
        }

        return ret;
        
        /*
        System.out.println(Arrays.toString(point) + ": " + Arrays.toString(rgb));
        int r = rgb[0];
        int g = rgb[1];
        int b = rgb[2];

        if (g >= 200 && r < 200 && b < 200) return 1;
        if (r >= 200 && g < 200 && b < 100) return 2;
        if (r >= 200 && g < 100 && b < 100) return 3;
        if (r >= 200 && g < 50 && b < 50) return 4;
        return 0;
        */
    }

    static double calColor(int[] input, int[] target) {
        if (input.length != 3 || target.length != 3) return (-1.0);
        long dotProduct = input[0] * target[0] + input[1] * target[1] + input[2] * target[2];
        double mag = Math.sqrt(Math.pow(input[0], 2) + Math.pow(input[1], 2) + Math.pow(input[2], 2)) *
                Math.sqrt(Math.pow(target[0], 2) + Math.pow(target[1], 2) + Math.pow(target[2], 2));
        return Math.acos(dotProduct / mag);
    }

    public static void main(String[] args) {

        String jsonPath = "./mapping/MapDataStructure.json";
        System.out.println("Default json path: " + jsonPath);

        JSONArray jsonArr = readJson(jsonPath);
        if (jsonArr == null) return;

        int len = jsonArr.size();
        for (int i = 0; i < len; i++) {
            JSONObject intersec = (JSONObject) jsonArr.get(i);
        }

        String dataPath = "../imgData";
        System.out.println("Default png path: " + dataPath);
        processImg(dataPath, jsonArr);

    }
}
