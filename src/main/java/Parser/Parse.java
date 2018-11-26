package Parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

import static org.apache.commons.lang3.StringUtils.*;

public class Parse {

    private boolean doneWithToken = true;
    private static HashSet<Character> specialCharSet = initSpecialSet();
    private static HashSet<String> monthSet = initMonthSet();
    private static HashSet<String> stopWords = initStopWords();
    private static HashMap<String, String> monthDictionary = initMonthMap();


    private static HashSet<String> initStopWords() {
        String baseDir = (String) System.getProperties().get("user.dir");
        String filesPath = baseDir + "/src/main/resources/stop_words.txt";
        stopWords = new HashSet<String>();
        Scanner s = null;
        try {
            s = new Scanner(new File(filesPath));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        while (s.hasNext()) stopWords.add(s.nextLine());
        return stopWords;
    }

    private static HashMap<String, String> initMonthMap() {
        monthDictionary = new HashMap<String, String>();
        String[] months = {"JANUARY", "01", "JAN", "01", "FEBRUARY", "02", "FEB", "02", "MARCH", "03", "MAR", "03",
                "APRIL", "04", "APR", "04", "MAY", "05", "JUNE", "06", "JUN", "06", "JULY", "07", "JUL", "07",
                "AUGUST", "08", "AUG", "08", "SEPTEMBER", "09", "SEP", "09", "OCTOBER", "10", "OCT", "10",
                "NOVEMBER", "11", "NOV", "11", "DECEMBER", "12", "DEC", "12"};
        for (int i = 0; i < months.length - 1; i++) {
            monthDictionary.put(months[i++], months[i]);
        }
        return monthDictionary;
    }

    private static HashSet<String> initMonthSet() {
        monthSet = new HashSet<>();
        String[] months = {"JANUARY", "JAN", "FEBRUARY", "FEB", "MARCH", "MAR", "APRIL", "APR",
                "MAY", "JUNE", "JUN", "JULY", "JUL", "AUGUST", "AUG", "SEPTEMBER", "SEP",
                "OCTOBER", "OCT", "NOVEMBER", "NOV", "DECEMBER", "DEC"};

        for (int i = 0; i < months.length; ++i) {
            monthSet.add(months[i]);
        }

        return monthSet;
    }

    private static HashSet<Character> initSpecialSet() {
        specialCharSet = new HashSet<>();
        Character[] characters = new Character[]{' ', '\r', '\n', '[', '(', '{', '`', ')', '<', '|', '&', '~', '+', '^', '@', '*', '?', '.', '>', ';', '_', '\'', ':', ']', '/', '\\', '}', '!', '=', '#', ',', '"', '-'};

        for (int i = 0; i < characters.length; ++i) {
            specialCharSet.add(characters[i]);
        }

        return specialCharSet;
    }


    public HashMap<String, String> parse(String[] str) {
        HashMap<String, String> termsDict = new HashMap<>();
        parseTokens(termsDict, str);
        return termsDict;
    }

    private void parseTokens(HashMap<String, String> termsDict, String[] str) {
//        String[] s = str[0].split(" ");
//        StringBuilder stringBuilder = new StringBuilder(str[0]);
        String[] s = split(str[0], " ");
        boolean expressionFlag;
        for (int i = 0, lastIndex = s.length - 1; i <= lastIndex; i++) {
            expressionFlag = false;
            doneWithToken = true;
            String[] token = new String[]{s[i], "0,"};
            cleanToken(token);
            if (token[0].equals("")) {
                continue;
            }
            char firstCharOfToken = token[0].charAt(0);
            if (token[0].toLowerCase().equals("between")) {
                if (i + 1 < s.length) {
                    String[] check = {s[i + 1]};
                    if ((checkIfFracture(check[0]) || checkIfNumber(check[0]))) {
                        expressionFlag = true;
                        doneWithToken = false;
                        i = isTokenBetweenExpression(termsDict, token, s, i);
                    }
                }
            }
            if (!expressionFlag && checkIfNumber(token[0])) {           // might be an expression starting with number without '-'
                if (i + 1 < s.length) {
                    String[] check = {s[i + 1]};
                    cleanToken(check);
                    if (check[0].contains("-") && !check[0].contains("--")) { // an expression --> first a number and then an expression
                        check = split(s[i + 1], "-");
                        if (checkIfRepresentingNumber(check) || checkIfFracture(check[0])) {
                            expressionFlag = true;
                            i = beforeSlashForNumbers(termsDict, token, s, i);
                            token[0] += "-";
                            i = afterSlashForNumbers(termsDict, token, s, i);
//                        String[] numOfTokens = token[0].split(" ");
                            String[] numOfTokens = split(token[0], " ");
                            i += numOfTokens.length - 1;
                            if (i + 1 < s.length) {
                                String[] tmp = {s[i + 1]}; // Extreme Case for last word is number representation
                                cleanToken(tmp);
                                if (checkIfRepresentingNumber(tmp)) {
                                    i++;
                                }
                            }
                        }
                    }
                }
            }
            if (!expressionFlag && token[0].contains("-") && !token[0].contains("--")) {            // might be an expression containing a '-' and this expression is NOT registered in the dictionary
                expressionFlag = true;
                doneWithToken = false;
//                token[0] = replace(token[0],",", "");
                token[0] = replace(token[0], ",", "");
                i = expressionStartsWithSlash(termsDict, token, s, i);
                i = numberAfterSlashInExpressionStartsWithSlash(termsDict, token, s, i);
            }
            if (!expressionFlag) {
                if (specialCharSet.contains(firstCharOfToken)) {                 //1. if token starts with a symbol
                    token[0] = token[0].substring(1) + token[0].charAt(0);
                    firstCharOfToken = token[0].charAt(0);
                }
                if (Character.isDigit(firstCharOfToken) || firstCharOfToken == '$') {                //2. if token starts with a digit
                    double tok = numerize(token);
                    if (tok != -1) {                                             //2.1. if token is a number
                        if (i < lastIndex) {                                     //2.1.1. if token has next
                            String[] nextToken = {s[i + 1], "0,"};
                            if (!token[0].contains(".")) {                       //2.1.1.1. if token is an Integer
                                if (isTokenADay(tok)) {                           //2.1.1.1.1. if token is a Day
                                    if (monthSet.contains(nextToken[0].toUpperCase())) {
                                        insertDate(termsDict, token, nextToken, null);
                                        s[i] = token[0];
                                        s[++i] = nextToken[0];
                                        continue;
                                    }
                                }
                            }
                        }
                    }                                                    //2.3. if token is a code (3dj14s..)
                    i = insertTokenWithNext(termsDict, token, i, s);
                    continue;
                } else {                                                         //3. if token starts with a letter
                    if (Character.isUpperCase(firstCharOfToken)) {               //3.1. if token starts with an UPPERCASE letter
                        if (monthSet.contains(token[0].toUpperCase())) {         //3.1.1. if token is a Month
                            if (i < lastIndex) {                                 //3.1.1.1. if token has next
                                String[] nextToken = {s[i + 1], "0,"};
                                double nextT = numerize(nextToken);
                                if (nextT != -1) {                               //3.1.1.1.1. if token is a number
                                    if (isTokenADay(nextT)) {                    //3.1.1.1.1.1. if next token is a day (1-31)  MM-DD
                                        insertDate(termsDict, nextToken, token, null);
                                        s[i++] = "";
                                        s[i] = "";
                                        continue;
                                    } else if (isTokenAYear(nextT)) {            //3.1.1.1.1.2. if next token is a year         MM-YYYY
                                        insertDate(termsDict, null, token, nextToken);
                                        s[i++] = "";
                                        s[i] = "";
                                        continue;
                                    }
                                }
                            }
                        }
                    }
                    checkCaseAndInsertToDictionary(termsDict, token);
                    s[i] = "";
                    continue;
                }
            }
        }
    }


    private int isTokenBetweenExpression(HashMap<String, String> termsDict, String[] token, String[] s, int i) {
        String[] betweenExpression = {"", "0,0"};
        String[] numInBetweenExpession = {s[i + 1], "0,"};
        cleanToken(numInBetweenExpession);
        i++;
        i = checkIfTokenIsNum(termsDict, numInBetweenExpession, i, s);
        betweenExpression[0] = "between" + " " + numInBetweenExpession[0];
        if (i + 1 < s.length && s[i].toLowerCase().equals("and")) {
            numInBetweenExpession[0] = s[i + 1];
            cleanToken(numInBetweenExpession);
//            numInBetweenExpession[0] = numInBetweenExpession[0].replace(",", "");
            numInBetweenExpession[0] = replace(numInBetweenExpession[0], ",", "");
            i++;
            i = checkIfTokenIsNum(termsDict, numInBetweenExpession, i, s);
            betweenExpression[0] += " and" + " " + numInBetweenExpession[0];
            insertToDictionary(termsDict, betweenExpression);
            i--;
        }
        return i;
    }

    private int numberAfterSlashInExpressionStartsWithSlash(HashMap<String, String> termsDict, String[] token, String[] s, int i) {
        String[] tokenByDelimiter = split(token[0], "-");
        if (checkIfNumber(tokenByDelimiter[1]) || checkIfFracture(tokenByDelimiter[1])) {
            String[] tmpToken = {tokenByDelimiter[1], "0,"};
            checkIfTokenIsNum(termsDict, tmpToken, i, s);
            String[] finalToken = {tokenByDelimiter[0] + "-" + tmpToken[0], "0,0"};
            insertToDictionary(termsDict, finalToken);
            if (i + 1 < s.length) {
                String[] check = {s[i + 1]};
                cleanToken(check);
                if (checkIfRepresentingNumber(check) || checkIfFracture(check[0])) {
                    return i + 1;
                }
            }
        }
        return i;
    }

    private int expressionStartsWithSlash(HashMap<String, String> termsDict, String[] token, String[] strings, int i) {
        String[] strTmp = {token[0], token[1]};
        String[] finalToken = {"", "0,"};
        cleanToken(strTmp);
//        strTmp[0] = strTmp[0].replace(",", "");
        strTmp[0] = replace(strTmp[0], ",", "");
        String[] expressionTokens = split(strTmp[0], "-");
        strTmp[0] = expressionTokens[0];
        if (checkIfNumber(expressionTokens[0]) || checkIfFracture(expressionTokens[0])) {      // expression starts with a num #-..
            checkIfTokenIsNum(termsDict, strTmp, 0, expressionTokens);
            finalToken[0] = strTmp[0];
            String[] changeToken = split(token[0], "-");
            token[0] = strTmp[0] + "-";
            for (int j = 1; j < changeToken.length; j++) {
//                token[0] += changeToken[j];
                token[0] += changeToken[j];
                if (j < changeToken.length - 1) {
                    token[0] += "-";
                }
            }
        } else {                                                                              // expression starts with a num w-..
            checkCaseAndInsertToDictionary(termsDict, strTmp);
            finalToken[0] = expressionTokens[0];
            if (!checkIfNumber(expressionTokens[1]) && !checkIfFracture(expressionTokens[1])) {    // expression of words: w-w-w-w-......
                for (int j = 1; j < expressionTokens.length; j++) {
                    String[] oneWordFromExpression = {expressionTokens[j], "0,"};
                    finalToken[0] += "-" + oneWordFromExpression[0];
                    checkCaseAndInsertToDictionary(termsDict, oneWordFromExpression);
                }
                finalToken[1] += "0";
                insertToDictionary(termsDict, finalToken);
                return i;
            }
        }
        if (!checkIfNumber(expressionTokens[1]) && !checkIfFracture(expressionTokens[1])) {     // expression continues with word: #-w
            strTmp[0] = expressionTokens[1];
            finalToken[0] += "-" + strTmp[0];
            checkCaseAndInsertToDictionary(termsDict, strTmp);
            finalToken[1] += "0";
            insertToDictionary(termsDict, finalToken);
        }
        return i;
    }

    private int afterSlashForNumbers(HashMap<String, String> termsDict, String[] token, String[] strings, int i) {
        String[] strTmp = {strings[i + 1]};
        cleanToken(strTmp);
        strTmp[0] = replace(strTmp[0], ",", "");
//        strTmp[0] = strTmp[0].replace(",", "");
        String[] expressionToken = split(strTmp[0], "-");
        String[] tmpToken = {expressionToken[1], "0,"};
        if (checkIfNumber(expressionToken[1])) {
            checkIfTokenIsNum(termsDict, tmpToken, i + 1, strings);
            token[0] += tmpToken[0];
        } else {
            insertToDictionary(termsDict, tmpToken);
        }
        if (!checkIfNumber(expressionToken[1])) {
            token[0] += expressionToken[1];
        }
        if (token[1].endsWith(",")) {
            token[1] += "0";
        }
        insertToDictionary(termsDict, token);
        String[] check = {expressionToken[0]};
        if (checkIfRepresentingNumber(check)) {
            return i + 1;
        }
        return i;
    }

    private int beforeSlashForNumbers(HashMap<String, String> termsDict, String[] token, String[] strings, int i) {
        String[] expressionToken = split(strings[i + 1], "-");
        if (checkIfFracture(expressionToken[0]) || checkIfRepresentingNumber(expressionToken)) {
            doneWithToken = false;
            checkIfTokenIsNum(termsDict, token, -1, expressionToken);
            if (!checkIfRepresentingNumber(expressionToken) && !checkIfFracture(expressionToken[0])) {
                token[0] += expressionToken[0];
            }
        }
        return i;
    }


    private int insertTokenWithNext(HashMap<String, String> termsDict, String[] token, int i, String[] strings) {
        int delta = checkIfTokenIsMoney(termsDict, token, i, strings);
        if (delta == i) {      // it's not money
            delta = checkIfTokenIsPercentage(termsDict, token, i, strings);
        }
        if (delta == i) {        // it's not percentage
            delta = checkIfTokenIsDateExtremeCase(termsDict, token, i, strings);    //check if the date starts with a day and then month
        }
        if (delta == i) {         // must be number if token apply to any rule
            delta = checkIfTokenIsNum(termsDict, token, i, strings);
        }
        if (delta == i) {         // must be number if token apply to any rule
//            delta = checkIfTokenHasPlace(termsDict, token, i, strings);
            checkCaseAndInsertToDictionary(termsDict, token);
            return i;
        }
        return delta - 1;

    }

//    private int checkIfTokenHasPlace(HashMap<String, String> termsDict, String[] token, int i, String[] strings) {
//    }


    private boolean checkIfFracture(String token) {
        if (token.contains("/")) {
//            token = token.replace(",", "");
            token = replace(token, ",", "");
            String[] check = split(token, "/");
            if (check.length < 2){
                return false;
            }
            try {
                Integer.parseInt(check[0]);
                Integer.parseInt(check[1]);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return false;
    }

    private boolean checkIfRepresentingNumber(String[] s) {
        return s[0].equals("thousand") || s[0].equals("million") || s[0].equals("billion") || s[0].equals("trillion");
    }

    private void prepareNumberRepresentationForTerm(String[] token, String[] s) {
        if (s[0].equals("thousand")) {
            token[0] += 'K';
        } else if (s[0].equals("million")) {
            token[0] += 'M';
        } else if (s[0].equals("billion")) {
            token[0] += 'B';
        } else if (s[0].equals("trillion")) {
            token[0] += "000" + 'B';
        }
    }

    private int numberSmallerThanThousand(String[] token, int i, String[] strings) {
        if (!token[0].contains(".")) {              // no decimal --> option for fracture
            if (i + 1 < strings.length) {
                String[] s = {strings[i + 1]};
                cleanToken(s);
                if (checkIfFracture(s[0])) {
//                    s[0] = s[0].replace(",", "");
                    s[0] = replace(s[0], ",", "");
//                    token[0] += " " + strings[i + 1];
                    token[0] += " " + s[0];
                    return i + 1;
                }
            }
        }
        return i;
    }

    /**
     * Seperate the Integer from the Decimal number/
     *
     * @param str: the String that representing the whole number
     * @return String array. [0] - Integer; [1] - Decimal.
     */
    private String[] cutDecimal(String[] str) {
        String[] result = new String[2];
        int index = str[0].indexOf('.');
        if (index != -1) {
            String rightSide = "";
            String leftSide = "";
            rightSide = str[0].substring(index + 1, str[0].length());
            leftSide = str[0].substring(0, index);
            result[0] = leftSide;
            result[1] = rightSide;
            return result;
        }
        return str;
    }

    /**
     * works like Integer.parseInt, converts a String to a Double number
     *
     * @param token contains the string in "token[0]"
     * @return if the token is a number, returns it.
     * else returns -1
     */
    private double numerize(String[] token) {
        try {
//            token[0] = token[0].replaceAll(",", "");
            token[0] = replace(token[0], ",", "");
            return Double.parseDouble(token[0]);
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * FUNCTION WAS TAKEN FROM:
     * https://stackoverflow.com/questions/14984664/remove-trailing-zero-in-java
     *
     * @param s : the string with potential trailing zeroes
     * @return s without the trailing zeroes
     */
    private String[] removeTrailingZero(String[] s) {
        s[0] = s[0].indexOf(".") < 0 ? s[0] : s[0].replaceAll("0*$", "").replaceAll("\\.$", "");
        return s;
    }

    /**
     * like numerize (above) but gets a string and returns boolean
     *
     * @param s : the string we wish to check without changing
     * @return : true if a number
     */
    private boolean checkIfNumber(String s) {
        try {
            s = replace(s, ",", "");
            Double.parseDouble(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private int checkIfTokenIsNum(HashMap<String, String> termsDict, String[] token, int i, String[] strings) {
        try {
            boolean flag = false;
            Double.parseDouble(token[0]);
            String[] tmp = {token[0], ""};
            String[] num = cutDecimal(tmp);
            if (i + 1 < strings.length) {
                String[] s = {strings[i + 1].toLowerCase()};
                cleanToken(s);
                if (checkIfRepresentingNumber(s)) {
                    prepareNumberRepresentationForTerm(token, s);
                    i += 1;
                    flag = true;
                }
            }
            if (!flag) {
                if (num[0].length() < 4) { // smaller then Thousand
                    i = numberSmallerThanThousand(token, i, strings);
                } else if (num[0].length() <= 6) { // Thousand
                    num[1] = num[0].substring(num[0].length() - 3) + num[1];
                    num[0] = num[0].substring(0, num[0].length() - 3);
                    token[0] = num[0] + "." + num[1];
                    token = removeTrailingZero(token);
                    token[0] = token[0] + "K";
                } else if (num[0].length() <= 9) { // Million
                    num[1] = num[0].substring(num[0].length() - 6) + num[1];
                    num[0] = num[0].substring(0, num[0].length() - 6);
                    token[0] = num[0] + "." + num[1];
                    token = removeTrailingZero(token);
                    token[0] = token[0] + "M";
                } else { // more than Million --> represented with B (Billion)
                    num[1] = num[0].substring(num[0].length() - 9) + num[1];
                    num[0] = num[0].substring(0, num[0].length() - 9);
                    token[0] = num[0] + "." + num[1];
                    token = removeTrailingZero(token);
                    token[0] = token[0] + "B";
                }
            }
            if (token[1].endsWith(",")) {
                token[1] += "0";
            }
            insertToDictionary(termsDict, token);
            return i + 1;
        } catch (NumberFormatException e) {
            if (checkIfFracture(token[0])) {             // EXTREME CASE IF FRACTURE WITHOUT NUMBER BEFORE IT
                if (token[1].endsWith(",")) {
                    token[1] += "0";
                }
                insertToDictionary(termsDict, token);
                i++;
            }
            return i;
        }
    }


    private int checkIfTokenIsPercentage(HashMap<String, String> termsDict, String[] token, int i, String[] strings) {
        String tmp = replace(token[0], "%", "");
        if (checkIfNumber(tmp)) {
            if (token[0].contains("%")) {
                token[0] = replace(token[0], "%", "");
                token[0] += "%";
                token[1] += "0";
                insertToDictionary(termsDict, token);
                return i + 1;
            } else if (i < strings.length - 1 && (strings[i + 1].toLowerCase().startsWith("percent") || strings[i + 1].toLowerCase().startsWith("percentage"))) {
                token[0] += "%";
                token[1] += "0";
                insertToDictionary(termsDict, token);
                return i + 2;
            }
        }
        return i;
    }


    private void convertToLowerCase(HashMap<String, String> termsDict, String[] token) {
        String oldKey = termsDict.remove(token[0].toUpperCase());
        termsDict.put(token[0].toLowerCase(), oldKey);
        token[1] = oldKey;
    }


    private boolean isTokenAnInt(double num) {
        int x = (int) num;
        return num - x == 0;
    }

    private boolean isTokenAYear(double year) {
        return isTokenAnInt(year) && year < 3000 && year > 0;
    }

    private boolean isTokenADay(double day) {
        return isTokenAnInt(day) && day > 0 && day < 31;
    }

    private int checkIfTokenIsDateExtremeCase(HashMap<String, String> termsDict, String[] token, int i, String[] strings) {
        if (i + 1 < strings.length && checkIfNumber(token[0])) {
            String[] month = {strings[i + 1]};
            cleanToken(month);
            if (monthSet.contains(month[0].toUpperCase())) {
                insertDate(termsDict, token, month, null);
                return i + 2;
            }
        }
        return i;
    }

    private void insertDate(HashMap<String, String> termsDict, String[] day, String[] month, String[] year) {
        month[0] = monthDictionary.get(month[0].toUpperCase());
        if (day != null) {
            if (day[0].length() == 1) {
                day[0] = "0" + day[0];
            }
            String[] date = {month[0] + "-" + day[0], "0,0"};
            insertToDictionary(termsDict, date);
        } else if (year != null) {
            String[] date = {year[0] + "-" + month[0], "0,0"};
            insertToDictionary(termsDict, date);
        }
    }


    private int checkIfTokenIsMoney(HashMap<String, String> termsDict, String[] token, int i, String[] strings) {
        if (token[0].contains("$")) {
//            token[0] = token[0].replace("$", "");      //$# or #$
            token[0] = replace(token[0], "$", "");      //$# or #$
            i = addQuantityToToken(termsDict, token, i, strings, true);
            token[0] += " Dollars";
            token[1] += "0";
            insertToDictionary(termsDict, token);
            return i + 1;
        } else if (i + 1 < strings.length && strings[i + 1].toLowerCase().startsWith("dollar")) {    //# dollars
            if (checkIfNumber(token[0]) || token[0].toLowerCase().endsWith("m") || token[0].toLowerCase().endsWith("bn")) {
                i = addQuantityToToken(termsDict, token, i, strings, true);
                token[0] += " Dollars";
                token[1] += "0";
                insertToDictionary(termsDict, token);
                return i + 2;
            }
        } else {
            if (checkIfNumber(token[0]) && i + 1 < strings.length) {
                String[] tmp = {token[0], ""};
                String[] num = cutDecimal(tmp);
                if (num[0].length() <= 5) {  // less of million (might have fraction)
                    if (checkIfFracture(strings[i + 1]) && i + 2 < strings.length && strings[i + 2].toLowerCase().startsWith("dollar")) {
                        token[0] += " " + strings[i + 1] + " Dollars";
                        token[1] += "0";
                        insertToDictionary(termsDict, token);
                        return i + 3;
                    }
                }
                if (checkIfCanBeMoney(strings, i)) {
                    for (int j = i; j < strings.length && j <= i + 3; j++) { // more then a million
                        if (checkIfFracture(strings[j])) {
                            token[0] += " " + strings[j];
                            j++;
                        }
                        if (strings[j].toLowerCase().startsWith("dollar")) {
                            addQuantityToToken(termsDict, token, i, strings, true);
                            token[0] += " Dollars";
                            token[1] += "0";
                            insertToDictionary(termsDict, token);
                            return j + 1;
                        }
                    }
                }
            }
        }
        return i;
    }

    private boolean checkIfCanBeMoney(String[] strings, int i) {
        for (int j = i + 1; j <= i + 3; j++) {
            if (j < strings.length && strings[j].toLowerCase().contains("dollar")) {
                return true;
            }
        }
        return false;
    }

    private void moneyParse(String[] token, int i) {
        if (i == 0) {
            String[] num = cutDecimal(token);
            boolean flag = false;
            if (num[1].equals("0,")) {
                flag = true;
            }
            if (!num[0].contains(" ") && num[0].length() >= 7) {
                num[0] = num[0].substring(0, num[0].length() - 6) + "." + num[0].substring(num[0].length() - 6);
                if (!flag) {
                    token[0] = num[0] + num[1];
                } else {
                    token[0] = num[0];
                }
                removeTrailingZero(token);
                token[0] += " M";
            }
        } else {
            double m = numerize(token);
            m = m * Math.pow(10, i);
            token[0] = String.format("%.12f", m);
            removeTrailingZero(token);
            token[0] += " M";
        }
    }

    private int addQuantityToToken(HashMap<String, String> termsDict, String[] token, int i, String[] strings, boolean isMoney) {

        if (isMoney) {
            if (token[0].toLowerCase().endsWith("m")) {
                token[0] = replace(token[0], "m", " M");
                moneyParse(token, 0);
                return i;
            }
            if (token[0].toLowerCase().endsWith("b") || token[0].toLowerCase().endsWith("bn")) {
//                token[0] = token[0].replace("b", "");
//                token[0] = token[0].replace("n", "");
                token[0] = replace(token[0], "b", "");
                token[0] = replace(token[0], "n", "");
                moneyParse(token, 3);
                return i;
            }
            if (i < strings.length - 1) {
                if (strings[i + 1].toLowerCase().startsWith("trillion")) {
                    moneyParse(token, 6);
                    return i + 1;
                } else if (strings[i + 1].toLowerCase().startsWith("billion")
                        || strings[i + 1].equalsIgnoreCase("bn")
                        || strings[i + 1].equalsIgnoreCase("b")) {
                    moneyParse(token, 3);
                    return i + 1;
                }
                if (strings[i + 1].toLowerCase().startsWith("million")
                        || strings[i + 1].equalsIgnoreCase("m")) {
                    token[0] += " M";
                    return i + 1;
                }
            }
            moneyParse(token, 0);
            return i;
        }
        return i;
    }


    private void cleanToken(String[] token) {
        while (token[0].length() > 0 && specialCharSet.contains(token[0].charAt(0))) {
            token[0] = token[0].substring(1);
        }
        while (token[0].length() >= 1 && specialCharSet.contains(token[0].charAt(token[0].length() - 1))) {
            token[0] = token[0].substring(0, token[0].length() - 1);
        }
    }

    private void checkCaseAndInsertToDictionary(HashMap<String, String> termsDict, String[] token) {
        if (Character.isLowerCase(token[0].charAt(0))) {
            if (termsDict.containsKey(token[0].toUpperCase())) {
                convertToLowerCase(termsDict, token);
            }
        }
        if (termsDict.containsKey(token[0].toLowerCase())) {
            token[0] = token[0].toLowerCase();
        } else if (Character.isUpperCase(token[0].charAt(0))) {
            token[0] = token[0].toUpperCase();
        }
        insertToDictionary(termsDict, token);

    }

    private void insertToDictionary(HashMap<String, String> termsDict, String[] token) {
        if (!stopWords.contains(token[0].toLowerCase())) {
            if (token[0].toLowerCase().endsWith("'s")) {
                token[0] = token[0].substring(0, token[0].length() - 2);
            }
            if (token[1].endsWith(",")) {
                token[1] += token[0].length() < 4 ? "0" : "1";
            }
            if (termsDict.containsKey(token[0])) {
                addApearanceInDictionary(termsDict, token);
            } else {
                termsDict.put(token[0], token[1]);
                addApearanceInDictionary(termsDict, token);
            }
        }
        if (doneWithToken) {
            token[0] = "";
        }
    }

    private void addApearanceInDictionary(HashMap<String, String> termsDict, String[] token) {
        String[] s = split(termsDict.get(token[0]), ",");
        int x = (int) numerize(s);
        x++;
        termsDict.put(token[0], "" + x + "," + s[1]);
    }


    public static void main(String[] args) {
        String test = "now we are going to take all the rules and aply a final test \n" +
                "first of all lets look at some numbers: \n" +
                "10,123 \n" +
                "123 Thousand \n" +
                "some words in between \n" +
                "1010.56 this is a number also \n" +
                "10,123,000 should apeare different in the dictionary \n" +
                "some words before the number: 55 Million. YEY! \n" +
                "Yey. Name, name, naming, again some numbers 10,123,000,000 \n" +
                "55 Billion, and the number 7 Trillion. \n" +
                "numbers under thousand: 22, 24, 4 1/2 \n" +
                "lets look at some precentage.. \n" +
                "22.22% also you can wirte it like 22.22 precent or 22.22 precentage \n" +
                "that is it for precntage. now lets see prices. this is going to be a long one \n" +
                "1.7320 Dollars, 22 2/4 Dollars and also $450,000 \n" +
                "now the other price rules: 1,000,000 Dollars, $450,000,000 \n" +
                "$100 million and 20.6m Dollars or 20.6 m Dollars \n" +
                "100$ billion \n" +
                "100 bn dollars or 100bn Dollars need to be the same. \n" +
                "lets see if all U.S. prices works \n" +
                "100 billion U.S. dollars and 320 million U.S. dollars and 1 trillion U.S dollars.. \n" +
                "now some dates:: 14 MAY, 14 May the same thing.. \n" +
                "June 4, JUNE 4 still the same. now with years \n" +
                "May 1994 or MAY 1994 thats are all the dates.. \n" +
                "now the expressions!!! \n" +
                "To-Be-Or-Not-To-Be.. 10-parts, 10 2/3-pages, 10 thousand-pages, 10-20 2/4 \n" +
                "pages-20 2/4, pages-10 thousand, 10 million-2 trillion. \n" +
                "and now betweens... YEY!!!! \n" +
                "between 2 and 7, between 2 2/4 and 10 thousand. between 1 million and 2 million \n" +
                "between 2 3/4 and 10 2/4. \n" +
                "thats it!";
        String[] s = new String[]{test, ""};
        String[] one = {"There are basically three ways of parsing data interchange formats such as JSON or XML. The first one is deserialization into a data object model. In this approach, the structure of JSON or XML document is represented by classes which can be created manually by a programmer or automatically generated with a tool.", ""};
        Parse p1 = new Parse();
        Parse p2 = new Parse();
        HashMap<String, String> termsDict1 = p2.parse(one);
        HashMap<String, String> termsDict2 = p2.parse(s);
        System.out.println();
        System.out.println(one[0]);
        System.out.println();
        for (Map.Entry<String, String> entry : termsDict1.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            System.out.println("Token: " + key + " ; Flags: " + value);
        }
        System.out.println();
        System.out.println(s[0]);
        System.out.println();

        for (Map.Entry<String, String> entry : termsDict2.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            System.out.println("Token: " + key + " ; Flags: " + value);
        }
    }
}
