/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.scim.bulkImport.Utils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.scim.bulkImport.Exceptions.BulkImportException;
import org.wso2.scim.bulkImport.Handlers.BulkImportHandler;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class JSONCreator {

    private final Logger logger = LoggerFactory.getLogger(JSONCreator.class.getName());

    private static final String CUSTOMER_DEFAULT = "CUSTOMER-DEFAULT";
    private static final String WORK_DEFAULT = "WORK-DEFAULT";

    public HashMap<String, String> getAttributeMapping(String[] headers) throws BulkImportException {

//        String directory = System.getProperty("user.dir");
//        String directory = "src/main/resources";
//        File csvFile = new File(directory + "/" + fileName);

        if (headers == null) {
            throw new BulkImportException("Headers are not found in the file");
        }
        JSONObject supportedAttributes = getSupportedAttributes();


        List<String> headersList = Arrays.asList(headers);

        // Map the CSV headers with SCIM attributes.

        final HashMap<String, String> attributeMapping = UserInputUtils.getAttributeMapping(supportedAttributes, headersList);
        return attributeMapping;
    }

    /**
     * Extract the Json body from the input file.
     *
     * @param csvFile - File contains user details.
     * @param mapping - Mapping between attributes and input file headers.
     *
     * @return {JSONArray} operations - Json array with bulk user operations.
     * @throws BulkImportException - Exception to handle bulk user import.
     */
    public JSONArray getJsonBody(File csvFile, HashMap<String, String > mapping, String userStore, String bearerToken)
            throws BulkImportException {

        JSONArray operations = new JSONArray();
        JSONObject data = new JSONObject();
        data.put("method","POST");
        data.put("path", "/Users");

        // It will receive the file lines
        String line;

        // Iterator that will read the file line by line
        LineIterator it = null;
        try {
            it = FileUtils.lineIterator(csvFile, "UTF-8");
        } catch (IOException e) {
            logger.error("Error in reading the file.", e);
            e.printStackTrace();
        }

        try {
            it = FileUtils.lineIterator(csvFile, "UTF-8");
            String[] arrHeader = null;
            if (it.hasNext()) {
                // Taking the useless first line
                String header = it.nextLine();
                arrHeader = header.split(",");
            }
            int index = 0;

            // Looping through the file lines
            while (it.hasNext()) {

                line = it.nextLine();
                String[] arrLine = line.split(",");

                // JSON object to store a user
                JSONObject user = new JSONObject();


                // Map to store user data
                Map<String, String> userMap = getUserAttributeValuesMap(arrHeader, arrLine);

                // Validate user details
                String username = getValue(userMap, mapping.get("Username"));
                boolean validUserName = Validation.validateEmail(username);
                if (!validUserName) {
                    System.out.println(username + " is not a valid username.\nHint: Your email must contain 3 to 50 " +
                            "characters. You can use alphanumeric characters, unicode characters, underscores (_), " +
                            "dashes (-), plus signs (+), periods (.), and an at sign (@).");
                    return null;
                }

                // Add unique bulkId
                data.remove("bulkId");
                data.put("bulkId", "qwerty" + index);
                index += 1;

                // Add user schema.
                JSONArray schemas = new JSONArray();
                schemas.add("urn:ietf:params:scim:schemas:core:2.0:User");
                user.put("schemas", schemas);

                if (userStore.equals(CUSTOMER_DEFAULT)) {
                    // Add askPassword claim.
                    JSONObject askPassword = new JSONObject();
                    askPassword.put("askPassword", "true");
                    user.put("urn:scim:wso2:schema", askPassword);

                    // Add emails
                    JSONArray emails = new JSONArray();
                    JSONObject email = new JSONObject();
                    email.put("primary", true);
                    email.put("value", username);
                    emails.add(email);
                    user.put("emails", emails);
                } else if (userStore.equals(WORK_DEFAULT)) {
                    String temporaryPassword = BulkImportHandler.getTemporaryPassword(bearerToken);
                    user.put("password", temporaryPassword);
                }

                // Add username
                user.put("userName", userStore + "/" + username);

                // Add Name
                JSONObject name = new JSONObject();
                name.put("givenName", getValue(userMap, mapping.get("First Name")));
                name.put("familyName", getValue(userMap, mapping.get("Last Name")));
                user.put("name", name);

                data.remove("data");
                data.put("data", user);

                operations.add(data.clone());
            }
        } catch (IOException | URISyntaxException e) {
            throw new BulkImportException("Error in processing the input file");
        } finally {
            LineIterator.closeQuietly(it);
        }
        return operations;
    }

    private JSONObject getSupportedAttributes() throws BulkImportException {
        //JSON parser object to parse read file
        JSONParser jsonParser = new JSONParser();
        JSONObject supportedAttributes;
        String currentDirectory = System.getProperty("user.dir");
        try (FileReader reader = new FileReader("src/main/java/org/wso2/scim/bulkImport/Configs/Attributes.json")) {
//        try (FileReader reader = new FileReader(currentDirectory + "/Attributes.json")) {
                //Read JSON file
                Object obj = jsonParser.parse(reader);
                supportedAttributes = (JSONObject) obj;
        } catch (IOException | ParseException e) {
            throw new BulkImportException("Error in getting supported attributes.");
        }
        return supportedAttributes;
    }

    public String[] getHeaders(File file) {
        // Iterator that will read the file line by line
        try {
            LineIterator it = FileUtils.lineIterator(file, "UTF-8");
            String header = it.nextLine();
            return header.split(",");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     *
     * @param headers - List of attribute names.
     * @param values - List of attribute values.
     * @return Map<String, String>
     */
    private Map<String, String> getUserAttributeValuesMap(String[] headers, String[] values) {
        List<String> list1 = Arrays.asList(headers);
        List<String> list2 = Arrays.asList(values);
        return IntStream.range(0, list1.size())
                .boxed()
                .collect(Collectors.toMap(list1::get, list2::get));
    }

    private String getValue(Map<String, String> userMap, String key) {
        String value = null;
        if (userMap.get(key) != null) {
            value = userMap.get(key).trim();
        }
        return value;
    }
}
