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
import java.util.Random;

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

    static JSONArray processImg(String path, JSONArray mapping) {
        File folder = new File(path);
        File[] files = folder.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".png");
            }
        });

        JSONArray ret = new JSONArray();
        int len = mapping.size();
        System.out.println("Mapping size: " + len);

        BufferedImage img = null;
        int[] rgb = null;

        for (File file : files) {
            if (file.isFile()) {
                String filename = file.getName();
                System.out.println(filename);
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

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println("Total: " + ret.size());
        System.out.println(ret.toJSONString());
        return ret;
    }

    static int convertRGB(int[] point, int[] rgb) {
        if (rgb.length != 3) return -2;
        System.out.println(Arrays.toString(point) + ": " + Arrays.toString(rgb));
        int r = rgb[0];
        int g = rgb[1];
        int b = rgb[2];

        if (Math.abs(r - g) < 50 && Math.abs(r - b) < 50 && Math.abs(g - b) < 50) {
            return 0;
        }
        if (r > g && Math.max(r, b) == r) {
            return 4;
        }


        Random rn = new Random();
        return rn.nextInt(5) - 1;
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
