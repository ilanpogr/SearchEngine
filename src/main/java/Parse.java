import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class Parse {

    // todo - remove all stop words without the word AND!.. for BETWEEN expression..

    private static boolean doneWithToken = true;
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
        Character[] characters = new Character[]{'[', '(', '{', '`', ')', '<', '|', '&', '~', '+', '^', '@', '*', '?', '.', '>', ';', '_', '\'', ':', ']', '/', '\\', '}', '!', '=', '#', ',', '"', '-'};

        for (int i = 0; i < characters.length; ++i) {
            specialCharSet.add(characters[i]);
        }

        return specialCharSet;
    }


    public static HashMap<String, String> parse(String[] str) {
        HashMap<String, String> termsDict = new HashMap<>();
        parseTokens(termsDict, str);
        return termsDict;
    }

    private static void parseTokens(HashMap<String, String> termsDict, String[] str) {
        //todo - split words by spaces even if there are more then one
        String[] s = str[0].split(" ");
        boolean expressionFlag;
        for (int i = 0, lastIndex = s.length - 1; i <= lastIndex; i++) {
            expressionFlag = false;
            String[] token = new String[]{s[i], "0,"};
            cleanToken(token);
            char firstCharOfToken = token[0].charAt(0);
            //todo - between # and #
            if (checkIfNumber(token[0])) {           // might be an expression starting with number without '-'
                if (i + 1 < s.length && s[i + 1].contains("-")) { // an expression --> first a number and then an expression
                    expressionFlag = true;
                    i = beforeSlashForNumbers(termsDict, token, s, i);
                    token[0] += "-";
                    i = afterSlashForNumbers(termsDict, token, s, i);
                    String[] numOfTokens = token[0].split(" ");
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
            if (!expressionFlag && token[0].contains("-")) {            // might be an expression containing a '-' and this expression is NOT registered in the dictionary


            }
            if (!expressionFlag) {
                if (specialCharSet.contains(firstCharOfToken)) {                 //1. if token starts with a symbol
                    token[0] = token[0].substring(1) + token[0].charAt(0);
                    firstCharOfToken = token[0].charAt(0);
                    //function to dollars and percents
                    //function for dashes
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
                                //is next fraction
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
                    //todo - check special cases (between...)
                    checkCaseAndInsertToDictionary(termsDict, token);
                    s[i] = "";
                    continue;
                }
            }
        }
    }

    private static int afterSlashForNumbers(HashMap<String, String> termsDict, String[] token, String[] strings, int i) {
        String[] strTmp = {strings[i + 1]};
        cleanToken(strTmp);
        strTmp[0] = strTmp[0].replace(",", "");
        String[] expressionToken = strTmp[0].split("-");
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

    private static int beforeSlashForNumbers(HashMap<String, String> termsDict, String[] token, String[] strings, int i) {
        String[] expressionToken = strings[i + 1].split("-");
        if (checkIfFracture(expressionToken[0]) || checkIfRepresentingNumber(expressionToken)) {
            doneWithToken = false;
            checkIfTokenIsNum(termsDict, token, -1, expressionToken);
            if (!checkIfRepresentingNumber(expressionToken) && !checkIfFracture(expressionToken[0])) {
                token[0] += expressionToken[0];
            }
        }
        return i;
    }


    private static int insertTokenWithNext(HashMap<String, String> termsDict, String[] token, int i, String[] strings) {
        String quntifier = "";
        boolean usedNext = false;
        boolean isMoney = false;
        // check if has next
        //is next percent
        //is next dollar
        //is next quantifier
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


//        if (strings != null) {
//            if (strings[0].matches("\\d+/\\d+")) {
//                strings[0] = nextToken[0] + " " + strings[0];
////                return false;
//            }
//
//        }
//        if (nextToken[0].endsWith("$")) {
//            nextToken[0] = nextToken[0].substring(0, nextToken[0].length() - 1) + quntifier + " Dollar";
//        } else if (nextToken[0].contains("/")) {
//            usedNext = false;
//        } else {
//            nextToken[0] += quntifier;
//        }
//        numberParse(nextToken);
//        if (strings != null) {
//            if (strings[0].toLowerCase().startsWith("percent") ||
//                    strings[0].equalsIgnoreCase("%")) {
//                nextToken[0] += "%";
//                insertToDictionary(termsDict, nextToken);
////                return true;
//            }
//            if (strings[0].toLowerCase().startsWith("dollar") ||
//                    strings[0].equalsIgnoreCase("$")) {
//                nextToken[0] += " Dollars";
//                insertToDictionary(termsDict, nextToken);
////                return true;
//            }
//        }
//        insertToDictionary(termsDict, nextToken);
        return delta - 1;

    }


    private static boolean checkIfFracture(String token) {
        if (token.contains("/")) {
            token = token.replace(",", "");
            String[] check = token.split("/");
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


    /**
     * Seperate the Integer from the Decimal number/
     *
     * @param str: the String that representing the whole number
     * @return String array. [0] - Integer; [1] - Decimal.
     */
    private static String[] cutDecimal(String[] str) {
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
     * FUNCTION WAS TAKEN FROM:
     * https://stackoverflow.com/questions/14984664/remove-trailing-zero-in-java
     *
     * @param s : the string with potential trailing zeroes
     * @return s without the trailing zeroes
     */
    private static String[] removeTrailingZero(String[] s) {
        s[0] = s[0].indexOf(".") < 0 ? s[0] : s[0].replaceAll("0*$", "").replaceAll("\\.$", "");
        return s;
    }

    private static int checkIfTokenIsNum(HashMap<String, String> termsDict, String[] token, int i, String[] strings) {
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
            return i;
        }
    }

    private static void convertToLowerCase(HashMap<String, String> termsDict, String[] token) {
        String oldKey = termsDict.remove(token[0].toUpperCase());
        termsDict.put(token[0].toLowerCase(), oldKey);
        token[1] = oldKey;
    }

    private static boolean checkIfRepresentingNumber(String[] s) {
        return s[0].equals("thousand") || s[0].equals("million") || s[0].equals("billion") || s[0].equals("trillion");
    }

    private static void prepareNumberRepresentationForTerm(String[] token, String[] s) {
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

    private static int numberSmallerThanThousand(String[] token, int i, String[] strings) {
        if (!token[0].contains(".")) {              // no decimal --> option for fracture
            if (checkIfFracture(strings[i + 1])) {
                strings[i + 1] = strings[i + 1].replace(",", "");
                token[0] += " " + strings[i + 1];
                return i + 1;
            }
        }
        return i;
    }

    private static int checkIfTokenIsDateExtremeCase(HashMap<String, String> termsDict, String[] token, int i, String[] strings) {
        if (checkIfNumber(token[0])) {
            String[] month = {strings[i + 1]};
            cleanToken(month);
            if (monthSet.contains(month[0].toUpperCase())) {
                insertDate(termsDict, token, month, null);
                return i + 2;
            }
        }
        return i;
    }

    private static int checkIfTokenIsPercentage(HashMap<String, String> termsDict, String[] token, int i, String[] strings) {
        String tmp = token[0].replace("%", "");
        if (checkIfNumber(tmp)) {
            if (token[0].contains("%")) {
                token[0] = token[0].replace("%", "");
                token[0] += "%";
                token[1] += "0";
                insertToDictionary(termsDict, token);
                return i + 1;
            } else if (strings[i + 1].toLowerCase().startsWith("percent") || strings[i + 1].toLowerCase().startsWith("percentage")) {
                token[0] += "%";
                token[1] += "0";
                insertToDictionary(termsDict, token);
                return i + 2;
            }
        }
        return i;
    }

    private static int checkIfTokenIsMoney(HashMap<String, String> termsDict, String[] token, int i, String[] strings) {
        if (token[0].contains("$")) {
            token[0] = token[0].replace("$", "");      //$# or #$
            i = addQuantityToToken(termsDict, token, i, strings, true);
            token[0] += " Dollars";
            token[1] += "0";
            insertToDictionary(termsDict, token);
            return i + 1;
        } else if (i + 1 < strings.length && strings[i + 1].toLowerCase().startsWith("dollar")) {    //# dollars
            if (checkIfNumber(token[0])) {
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
                        return i + 2;
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

    private static boolean checkIfCanBeMoney(String[] strings, int i) {
        for (int j = i + 1; j <= i + 3; j++) {
            if (j < strings.length && strings[j].toLowerCase().contains("dollar")) {
                return true;
            }
        }
        return false;
    }

    private static int addQuantityToToken(HashMap<String, String> termsDict, String[] token, int i, String[] strings, boolean isMoney) {

        if (isMoney) {
            if (token[0].toLowerCase().endsWith("m")) {
                token[0] = token[0].replace("m", "");
                moneyParse(token, 0);
                return i;
            }
            if (token[0].toLowerCase().endsWith("b") || token[0].toLowerCase().endsWith("bn")) {
                token[0] = token[0].replace("b", "");
                token[0] = token[0].replace("n", "");
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
//                    moneyParse(token,0);
                    token[0] += " M";
                    return i + 1;
                }
            }
            moneyParse(token, 0);
            return i;
        } else {                // not money, must be a number

        }
        return i;
    }

    private static void checkCaseAndInsertToDictionary(HashMap<String, String> termsDict, String[] token) {
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

    private static boolean isTokenAYear(double year) {
        return isTokenAnInt(year) && year < 3000 && year > 0;
    }

    private static boolean isTokenAnInt(double num) {
        int x = (int) num;
        return num - x == 0;
    }

    /**
     * works like Integer.parseInt, converts a String to a Double number
     *
     * @param token contains the string in "token[0]"
     * @return if the token is a number, returns it.
     * else returns -1
     */
    private static double numerize(String[] token) {
        try {
            token[0] = token[0].replaceAll(",", "");
            return Double.parseDouble(token[0]);
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * like numerize (above) but gets a string and returns boolean
     *
     * @param s : the string we wish to check without changing
     * @return : true if a number
     */
    private static boolean checkIfNumber(String s) {
        try {
            s = s.replaceAll(",", "");
            Double.parseDouble(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static boolean isTokenADay(double day) {
        return isTokenAnInt(day) && day > 0 && day < 31;
    }

    private static void insertToDictionary(HashMap<String, String> termsDict, String[] token) {
        if (!stopWords.contains(token[0])) {
            if (token[0].endsWith("'s")) {
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

    private static void addApearanceInDictionary(HashMap<String, String> termsDict, String[] token) {
        String[] s = termsDict.get(token[0]).split(",");
        int x = (int) numerize(s);
        x++;
        termsDict.put(token[0], "" + x + "," + s[1]);
    }

    private static void insertDate(HashMap<String, String> termsDict, String[] day, String[] month, String[] year) {
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

    private static void cleanToken(String[] token) {
        while (token[0].length() > 0 && specialCharSet.contains(token[0].charAt(0))) {
            token[0] = token[0].substring(1);
        }
        while (token[0].length() >= 1 && specialCharSet.contains(token[0].charAt(token[0].length() - 1))) {
            token[0] = token[0].substring(0, token[0].length() - 1);
        }
    }

    //todo - parse better
    private static void moneyParse(String[] token, int i) {
        if (i == 0) {
            String[] num = cutDecimal(token);
            if (!num[0].contains(" ") && num[0].length() >= 7) {
                num[0] = num[0].substring(0, num[0].length() - 6) + "." + num[0].substring(num[0].length() - 6);
                token[0] = num[0] + num[1] + " M";
            }
//            } else {
//                token[0] += " M";
//            }
        } else {
            double m = numerize(token);
            m = m * Math.pow(10, i);
            token[0] = (int) m + " M";
        }
    }


    public static void main(String[] args) {
        String s_tmp = "50 million-parts, 50 3/2-pages, 5 thousand-7 trillion, 2 thousand-2,000,000, 50 3/2-5 thousand";
        String[] s = new String[]{"77 5/9-2 thousand, 2-5, 2/4-200", ""};
        HashMap<String, String> termsDict = parse(s);
        System.out.println();
        System.out.println(s[0]);
        System.out.println();
        System.out.println(termsDict.toString());
    }
}
