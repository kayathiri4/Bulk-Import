package org.wso2.scim.bulkImport.Utils;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class Summary {

    /**
     * Display the import summary.
     *
     * @param response - Response from the API call.
     */
    public static void displayUserImportSummary(JSONObject response, JSONArray Operations) {
        // Display response details
        JSONParser parser = new JSONParser();
        int totalCount = 0;
        int errorCount = 0;
        JSONArray results = (JSONArray) response.get("Operations");
        if (results.size() > 0) {
            totalCount = results.size();
            for (Object object : results) {
                JSONObject result = (JSONObject) object;
                if (result.get("response") != null) {
                    errorCount += 1;
                    String responses = (String) result.get("response");
                    JSONObject responseObject = null;
                    try {
                        responseObject = (JSONObject) parser.parse(responses);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    if (responseObject != null) {
                        String detail = (String) responseObject.get("detail");
                        System.out.println(detail);
                    }
                }
            }
        }
        System.out.println("===========================================");
        System.out.println(totalCount - errorCount + " out of " + totalCount + " lines are onboarded successfully.");
        System.out.println("===========================================");
    }

    public static void summarizeWorkerImport() {

    }
}
