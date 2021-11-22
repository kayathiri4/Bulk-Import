package org.wso2.scim.bulkImport;

import org.apache.http.client.utils.URIBuilder;

import java.io.File;
import java.net.URISyntaxException;
import java.util.HashMap;

import org.json.simple.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.scim.bulkImport.Exceptions.AuthenticationException;
import org.wso2.scim.bulkImport.Exceptions.BulkImportException;
import org.wso2.scim.bulkImport.Handlers.BulkImportHandler;
import org.wso2.scim.bulkImport.Handlers.AuthenticationHandler;
import org.wso2.scim.bulkImport.Utils.JSONCreator;
import org.wso2.scim.bulkImport.Utils.UserInputUtils;

public class BulkImport {

    private static final String SCIM_BULK_ENDPOINT = "scim2/Bulk";
    private static final String TOKEN_ENDPOINT = "oauth2/token";
    private static final String PATH_SEPARATOR = "/";

    private static final Logger logger = LoggerFactory.getLogger(JSONCreator.class.getName());

    public static void bulkImport(String userStore, String accessToken, File csvFile, HashMap<String, String> mappings,
                           String host, String tenant) throws URISyntaxException {
        System.out.println("File processing complete...");
        System.out.println("Importing the users...");
        // Create a get request to retrieve list users from SCIM 2.0.
        URIBuilder builderForScim = new URIBuilder(host + "/t/" + tenant + PATH_SEPARATOR + SCIM_BULK_ENDPOINT);

        try {
            JSONCreator jsonCreator = new JSONCreator();
            JSONArray operations = jsonCreator.getJsonBody(csvFile, mappings, userStore, accessToken);
            BulkImportHandler.importBulkUser(builderForScim, operations, accessToken);
        } catch (BulkImportException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws
            URISyntaxException {

        String host = "https://dev.api.asgardeo.io";
        String host1 = "https://dev.api.asgardeo.io";
        String tenant = "";
        String username = "";
        String password = "";
        String userStore = "CUSTOMER-DEFAULT";

        if (args.length < 5) {
            logger.info("Invalid arguments! Please provide valid arguments to host address, username and password.");
            return;
        }

        username = args[0];
        password = args[1];
        host = args[2];
        tenant = args[3];
        userStore = args[4];

        // Authenticate the user.
        URIBuilder builderForToken = new URIBuilder(host1 + PATH_SEPARATOR + TOKEN_ENDPOINT);
        AuthenticationHandler authenticationHandler = new AuthenticationHandler();
        String accessToken = null;
        try {
            accessToken = authenticationHandler.authenticate(builderForToken, username, password);
        } catch (AuthenticationException e) {
            logger.error("Authentication failed for " + username, e);
        }

        if (accessToken == null) {
            return;
        }

        // Get file input
        JSONArray operations = null;
        JSONCreator jsonCreator = new JSONCreator();

        File csvFile;
        try {
            csvFile = UserInputUtils.getCsvFile();
            String[] headers = jsonCreator.getHeaders(csvFile);
            HashMap<String, String> mappings = jsonCreator.getAttributeMapping(headers);
            BulkImport.bulkImport(userStore, accessToken, csvFile, mappings, host, tenant );
        } catch (BulkImportException e) {
            logger.error("Error in importing bulk users.", e);
        }
    }
}
