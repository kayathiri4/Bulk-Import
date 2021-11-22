package org.wso2.scim.bulkImport.Utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Validation {

    public static final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[\\u00C0-\\u00FFa-zA-Z0-9.\\-_]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,10}",
                    Pattern.CASE_INSENSITIVE);

    /**
     * Validate the email against regex.
     *
     * @param email - Email of the user.
     *
     * @return {boolean} - Returns whether the email is valid or not.
     */
    public static boolean validateEmail(String email) {
        if (email == null) {
            return false;
        }
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(email);
        return matcher.find();
    }
}
