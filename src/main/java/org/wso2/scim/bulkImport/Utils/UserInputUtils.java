package org.wso2.scim.bulkImport.Utils;

import org.json.simple.JSONObject;
import org.wso2.scim.bulkImport.Exceptions.BulkImportException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

public class UserInputUtils {

    static Scanner sc= new Scanner(System.in); //System.in is a standard input stream.

    /**
     * Get the valid CSV file path from the user.
     *
     * @return {File} csvFile - CSV file with user details.
     * @throws BulkImportException - Exception to handle Bulk Import.
     */
    public static File getCsvFile() throws BulkImportException {

        File csvFile = null;
        boolean validFile = false;
        while (!validFile) {
            System.out.print("Please enter the path for CSV file: ");
            String csvDirectory = sc.nextLine(); //reads string.
            csvFile = new File(csvDirectory);
            if (csvFile.exists() && !csvFile.isDirectory()) {
                validFile = true;
            }
            if (!validFile) {
                System.out.println("The file does not exists. Please give a valid file path...");
            }
        }
        return csvFile;
    }

    public static HashMap<String, String> getAttributeMapping(
            JSONObject supportedAttributes, List<String> headersList
    ) {

        HashMap<String, String> map = new HashMap<>();

        Iterator<String> keys = (Iterator<String>) supportedAttributes.keySet().iterator();
        System.out.println("================================================================");
        System.out.println("Please map your file with the attributes supported by the tool.");
        System.out.println("================================================================");
        while(keys.hasNext()) {
            String key = keys.next();
            boolean promptNext = false;
            while (!promptNext) {
                System.out.print(key + " : ");
                String mapping = sc.nextLine(); //reads string.

                if (mapping.isEmpty()) {
                    if (!(boolean) supportedAttributes.get(key)) {
                        promptNext = true;
                    } else {
                        System.out.println(key  + "cannot be empty.\nPlease give a valid mapping!!!\n");
                    }
                }
                else {
                    if (headersList.contains(mapping)){
                        promptNext = true;
                        map.put(key, mapping);
                    } else {
                        System.out.println(mapping + "is not found in the provided CSV file.\nPlease try again!!!\n");
                    }
                }
            }
        }
        return map;
    }

}
