package org.wso2.scim.bulkImport.Utils;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileWriter;
import com.opencsv.CSVWriter;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileHandler {

    public static void createOutputFile(JSONArray operations, JSONObject response) {

        try {
            JSONParser parser = new JSONParser();
            JSONArray results = (JSONArray) response.get("Operations");
            File file = new File("src/main/resources/summary.csv");
            file.createNewFile();

            CSVWriter csvWriter = new CSVWriter(new FileWriter(file));
            csvWriter.writeNext(new String[]{"Username", "Status"});

            List<String[]> output = new ArrayList<>();
            int i = 0;
            if (results.size() > 0) {
                for (Object object : results) {
                    JSONObject result = (JSONObject) object;
                    JSONObject status = (JSONObject) result.get("status");
                    long code = (long) status.get("code");
                    JSONObject data = (JSONObject) ((JSONObject) operations.get(i)).get("data");
                    String userName = (String) data.get("userName");
                    if (code == 201) {
                        output.add(new String[]{userName,
                                "Successfully uploaded"});
                    } else if (code == 409) {
                        output.add(new String[]{userName,
                                "Username already exists"});
                    } else {
                        String responses = (String) result.get("response");
                        JSONObject responseObject = null;
                        try {
                            responseObject = (JSONObject) parser.parse(responses);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        if (responseObject != null) {
                            String detail = (String) responseObject.get("detail");
                            output.add(new String[]{userName, detail});
                        } else {
                            output.add(new String[]{userName, "Error in uploading user"});
                        }
                    }
                    i += 1;
                }
            }
            csvWriter.writeAll(output);
            csvWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
