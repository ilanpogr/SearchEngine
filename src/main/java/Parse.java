import javafx.util.Pair;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;

public class Parse {

    private static boolean nextUsed = false;
    public static String testString = "2/3 just checking some of the rules, Ilan is a name so it sopposed to be capital letters November is a month and 11 NOV 2018 is a date. the number 1,203,453.22 should change and 99 percent that we will catch the numbers 1,243,535,238.3 without spending 2000 dollar. there are few more rules like NBA names and stuff. dollar checking checking checking May 1993 Oct 2015 Feb 13" +
            " Let's make some strings and write numbers.. we should see only the numbers changing! Kid with 4312 bananas wished to sell 1341 2/5 of them to russian mafia the rail is 4,124,634,135,235,346.23515 km of devined happiness 7 3/2 Thousand is a smaller number then 40 Trillion 1010.56 is in the assignment pdf 10,123,000,000 is in the assignment pdf 55.88 Billion is in the assignment pdf 10,123,000 is in the assignment pdf lets look at some fractions: 8 6/4 kids, 5 3/4 thousand cups, please lets not go there.. one more: 6,000,111,000 last one: lets test the zeroes after the dot.. 542000.1235 and one more 5,435,340,000.78 done!";
    public static HashSet<Character> specialCharSet = initSpecialSet();
    public static HashSet<String> monthSet = initMonthSet();
    public static HashSet<String> stopWords = initStopWords();

    public static HashMap<String, String> monthDictionary = initMonthMap();

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

    /**
     * Seperate the Integer from the Decimal number/
     *
     * @param str: the String that representing the whole number
     * @return String array. [0] - Integer; [1] - Decimal.
     */
    private static String[] cutDecimal(String str) {
        String[] result = new String[2];
        int index = str.indexOf('.');
        String tmp = "";
        if (index != -1) {
            tmp = str.substring(index + 1, str.length());
            str = str.substring(0, index);
        }
        result[0] = str;
        result[1] = tmp;
        return result;
    }

    /**
     * FUNCTION WAS TAKEN FROM:
     * https://stackoverflow.com/questions/14984664/remove-trailing-zero-in-java
     *
     * @param s : the string with potential trailing zeroes
     * @return s without the trailing zeroes
     */
    private static String removeTrailingZero(String s) {
        s = s.indexOf(".") < 0 ? s : s.replaceAll("0*$", "").replaceAll("\\.$", "");
        return s;
    }

    /**
     * Parsing numbers between thousand and million
     * Parameter String[] number: number[0] is Integer, number[1] is decimal.
     *
     * @param current: the current number we work with
     * @return String after parsing.
     */
    private static String numberThousand(String current) {
        current = current.replace(",", "");
        String[] number = cutDecimal(current);
        int integer = Integer.parseInt(number[0]);
        if (integer >= 1000 && integer < 1000000) {
            int decimal = integer % 1000;
            String tmp = extremeCaseDecimal(decimal, 'K');
            integer = integer / 1000;
            number[0] = Integer.toString(integer);
            number[1] = tmp + number[1];
            if (number[1].length() > 0) {
                current = number[0] + '.' + number[1];
            } else {
                current = number[0] + number[1];
            }
        }
        current = removeTrailingZero(current);
        current += 'K';
        return current;
    }

    /**
     * Parsing numbers between million and billion
     * Parameter String[] number: number[0] is Integer, number[1] is decimal.
     *
     * @param current: the current number we work with
     * @return String after parsing.
     */
    private static String numberMillion(String current) {
        current = current.replace(",", "");
        String[] number = cutDecimal(current);
        int integer = Integer.parseInt(number[0]);
        if (integer < 1000000000 && integer >= 1000000) {
            int decimal = integer % 1000000;
            String tmp = extremeCaseDecimal(decimal, 'M');
            integer = integer / 1000000;
            number[0] = Integer.toString(integer);
            number[1] = tmp + number[1];
            if (number[1].length() > 0) {
                current = number[0] + '.' + number[1];
            } else {
                current = number[0] + number[1];
            }
        }
        current = removeTrailingZero(current);
        current += 'M';
        return current;
    }

    private static String numberBillion(String current) {
        current = current.replace(",", "");
        String[] number = cutDecimal(current);
        int index = number[0].length() - 9;
        number[1] = number[0].substring(index) + number[1];
        number[0] = number[0].substring(0, index);
        if (number[1].length() > 0) {
            current = number[0] + '.' + number[1];
        } else {
            current = number[0] + number[1];
        }
        current = removeTrailingZero(current);
        current += 'B';
        return current;
    }

    private static String extremeCaseDecimal(int decimal, char type) {
        String str = Integer.toString(decimal);
        if (type == 'K') {
            if (decimal < 10) {
                str = "00" + str;
            } else if (decimal < 100) {
                str = '0' + str;
            }
        } else if (type == 'M') {
            if (decimal < 10) {
                str = "00000" + str;
            } else if (decimal < 100) {
                str = "0000" + str;
            } else if (decimal < 1000) {
                str = "000" + str;
            } else if (decimal < 10000) {
                str = "00" + str;
            } else if (decimal < 100000) {
                str = '0' + str;
            }
        }
        return str;
    }

    //TODO - add the fraction number to current if exist
    private static String numberRepresentationSwitchCase(String current, String next) {
        String endStr = "";
        next.toLowerCase();
        switch (next) {
            case "thousand":
                endStr = "K";
                nextUsed = true;
            case "million":
                endStr = "K";
                nextUsed = true;
            case "billion":
                endStr = "B";
                nextUsed = true;
            case "trillion":
                String[] num = cutDecimal(current);
                num[0] += "000";
                if (num[1].length() > 0) {
                    current = num[0] + '.' + num[1];
                } else {
                    current = num[0] + num[1];
                }
                endStr += "B";
                nextUsed = true;
        }
        return current += endStr;
    }

    private static String directNumberToFunc(String current, String next) {

        String tmp = current;
        current = numberRepresentationSwitchCase(current, next);
        if (current.equals(tmp)) {
            String[] number = cutDecimal(current);
            if (number[0].length() >= 4 && number[0].length() < 7) {
                current = numberThousand(current);
            } else if (number[0].length() >= 7 && number[0].length() < 9) {
                current = numberMillion(current);
            } else if (number[0].length() >= 10) {
                current = numberBillion(current);
            }
        }
        if (next.matches("\\d+/\\d+")) {
            current += next;
            nextUsed = true;
        }
        return current;
    }

    /*TODO - follow the next lines:
        0. if the number is less than Thousand: keep the number as is.
        1. if next token is 'Thousand' or 1000 < int < 1000000 --> numberThousand and same for Million
        2. 'Billion' next token convert to Billion..
        3. if 'Trillion' next token - add 3 zeroes
        4/ if next token is 'Mathematical fracture' add to previous token!
      */

    public static HashMap<String, String> parse(String[] str) {
        HashMap<String, String> termsDict = new HashMap<>();
        parseTokens(termsDict, str);
        if (str[1].length() != 0) {
            parseTokens(termsDict, str);
        }
        System.out.println(termsDict.toString());
        return termsDict;
    }

    private static void parseTokens(HashMap<String, String> termsDict, String[] str) {
        String[] s = str[0].split(" ");
        for (int i = 0, lastIndex = s.length - 1; i <= lastIndex; i++) {
            String[] token = new String[]{s[i], "0,"};
            cleanToken(token);
            char firstCharOfToken = token[0].charAt(0);
            //todo - take care of dashes
            //todo - rearrange the functions of numbers
            //todo - add U.S. Dollars eve after quentifieres
            //todo - between # and #
            if (specialCharSet.contains(firstCharOfToken)) {                 //1. if token starts with a symbol
                //todo - understand what to do with symbols
                token[0] = token[0].substring(1) + token[0].charAt(0);
                firstCharOfToken = token[0].charAt(0);
            }
            if (Character.isDigit(firstCharOfToken)) {                //2. if token starts with a digit
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
                        //todo - insert
                        if (insertTokenWithNext(termsDict, token, nextToken)) {
                            s[i] = token[0];
                            s[++i] = nextToken[0];
                            continue;
                        }
                        if (i < lastIndex)
                            s[i + 1] = nextToken[0];
                        continue;
                    } else {                                                 //2.1.2. if token has No next

                    }
                } else {                                                     //2.3. if token is a code (3dj14s..)
                    //todo - add token to dictionary

                    if (i < lastIndex) {                                        //2.3.1 if token has next
                        String[] nextToken = {s[i + 1], "0,"};
                        if (insertTokenWithNext(termsDict, token, nextToken)) {
                            s[i] = token[0];
                            s[++i] = nextToken[0];
                            continue;
                        }
                        s[i] = token[0];
                        continue;
                    }

                }
                insertTokenWithNext(termsDict, token, null);
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
                //todo - add token to dictionary and check case

            }
        }
        Collections
        for (String ignored :
                termsDict.keySet()) {
            System.out.println(ignored);
        }


//        for (int i = 0; i < s.length; i++) {
//            if (s[i] != "")
//                System.out.print(s[i]);
//        }
}

    /**
     * @param termsDict
     * @param token
     * @param nextToken
     * @return if inserted with the next token - true
     * else false
     */
    private static boolean insertTokenWithNext(HashMap<String, String> termsDict, String[] token, String[] nextToken) {
        String quntifier = "";
        boolean usedNext = false;

        if (nextToken != null) {
            if (nextToken[0].matches("\\d+/\\d+")) {
                nextToken[0] = token[0] + " " + nextToken[0];
                return false;
            }
            if (nextToken[0].toLowerCase().startsWith("thousand")) {
                quntifier = "000";
                usedNext = true;
            }
            if (nextToken[0].toLowerCase().startsWith("million")
                || nextToken[0].equalsIgnoreCase("m")) {
                quntifier = "000000";
                usedNext = true;
            }
            if (nextToken[0].toLowerCase().startsWith("billion")
                    || nextToken[0].equalsIgnoreCase("bn")
                    || nextToken[0].equalsIgnoreCase("b")) {
                quntifier = "000000000";
                usedNext = true;
            }
            if (nextToken[0].toLowerCase().startsWith("trillion")) {
                quntifier = "000000000000";
                usedNext = true;
            }
        }
        if (token[0].endsWith("$")) {
            token[0] = token[0].substring(0, token[0].length() - 1) + quntifier + " Dollar";
        } else if (token[0].contains("/")) {
            usedNext = false;
        } else {
            token[0] += quntifier;
        }
        numberParse(token);
        if (nextToken != null) {
            if (nextToken[0].toLowerCase().startsWith("percent") ||
                    nextToken[0].equalsIgnoreCase("%")) {
                token[0] += "%";
                insertToDictionary(termsDict, token);
                return true;
            }
            if (nextToken[0].toLowerCase().startsWith("dollar") ||
                    nextToken[0].equalsIgnoreCase("$")) {
                token[0] += " Dollars";
                insertToDictionary(termsDict, token);
                return true;
            }
        }
        insertToDictionary(termsDict, token);
        return usedNext;

    }

    private static void checkCaseAndInsertToDictionary(HashMap<String, String> termsDict, String[] token) {
        if (Character.isLowerCase(token[0].charAt(0))) {
            if (termsDict.containsKey(token[0].toUpperCase())) {
                //take the value of the uppercase to the lower case and remove the upper case
            }
        }
        if (termsDict.containsKey(token[0].toLowerCase())) {
            token[0] = token[0].toLowerCase();
        } else if (Character.isUpperCase(token[0].charAt(0))) {
            token[0] = token[0].toUpperCase();
        }
        insertToDictionary(termsDict, token);
    }


    //todo - implement isTokenAYear
    private static boolean isTokenAYear(double year) {
        return isTokenAnInt(year) && year < 2500 && year > 0;
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
    //todo - implement numerize
    private static double numerize(String[] token) {
        try {
            token[0] = token[0].replaceAll(",", "");
            return Double.parseDouble(token[0]);
        } catch (Exception e) {
            return -1;
        }
    }


    //todo - implement isTokenADay
    private static boolean isTokenADay(double day) {
        return isTokenAnInt(day) && day > 0 && day < 31;
    }

    /*private static void parseTokens(HashMap<String, String> termsDict, String[] str) {
        String[] s = str[0].split(" ");
        for (int i = 0, lastIndex = s.length - 1; i <= lastIndex; i++) {
            String[] token = new String[]{s[i], "0,"};
            removeSuffix(token);                //removes the suffix from the token (prefix too)
            if (monthSet.contains(token[0].toUpperCase())) {                                                            // 1. if the token is a month (by name)
                String[] nextToken = {"", "0,"};
                if (i < lastIndex) {                                                                                    // 1.1. if there is next token
                    nextToken[0] = s[i + 1];
                    nextToken[0] = nextToken[0].startsWith("'") ? "19" + nextToken[0].substring(1) : nextToken[0];      // 1.1.1 special case: next token represents a year  i.e: '99 , '87
                    removeSuffix(nextToken);    //removes the suffix from the token (prefix too)
//                    nextToken[1]=nextToken[1].substring(0,nextToken[1].length()-1); //remove the stemmer bool
                    try {
                        int nt = Integer.parseInt(nextToken[0]);                                                        // 1.1.2. if the next token is a number
                        if (nt > 0 && nt <= 31) {                                                                       // 1.1.2.1. if the next token is a day in a month
                            insertDate(termsDict, nextToken, token, null);                                         // 1.1.2.1.1.  then it's MM DD format
                            continue;
                        } else if (nt > 0 && nt < 2500) {                                                               // 1.1.2.2. if the next token is a year
                            insertDate(termsDict, null, token, nextToken);                                         // 1.1.2.2.1.  then it's MM YYYY
                            continue;
                        } else {                                                                                        // 1.1.2.3 if next token is just a number
                            insertToDictionary(termsDict, token);
                        }
                    } catch (Exception e) {                                                                             // 1.1.3. if the next token is NOT a number
                        if (stopWords.contains(token[0].toLowerCase())) continue;                                       // 1.1.3.1. if it's a stop word, continue
                        else insertToDictionary(termsDict, token);                                                      // 1.1.3.2. else add it to dictionary
                    }//catch
                }//if has next token
                else {                                                                                                  // 1.2.  if there is NOT next token
                    if (stopWords.contains(token[0].toLowerCase())) continue;                                           // 1.2.1.  if it's a stop word, continue
                    else insertToDictionary(termsDict, token);                                                          // 1.2.2.  else add it to dictionary
                }
            }//if token is a month
            //TODO - get rid of the regEx
            else if (token[0].matches("^\\d*$")){                                                                 // 2. if token is a number
                try {
                    int nt = Integer.parseInt(token[0]);
                    if (nt > 0 && nt <=31){                                                                             // 2.1. if the token is a day in a month
                        String[] nextToken = {"", "0,"};
                        if (i < lastIndex) {                                                                            // 2.1.1. if there is next token
                            nextToken[0] = s[i + 1];
                        }
                    }
                }catch (Exception e){

                }

            }


        }

    }*/

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
        token[0] = "";
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
        String tok = token[0];
        for (; tok.length() > 0 && specialCharSet.contains(tok.charAt(0)); tok = token[0] = tok.substring(1)) ;
        while (tok.length() > 1 && specialCharSet.contains(tok.charAt(tok.length() - 1))) {
            tok = token[0] = tok.substring(0, tok.length() - 1);
        }
    }

    private static void numberParse(String[] token) {
        double m = numerize(token);
        if (m == -1) return;
        int e = (int) Math.log10(m);
        int dotIndex = 1;
        String add = "";
        if (e >= 3) {
            dotIndex += e % 3;
            String num = "" + m;
            if (e >= 9) {
                dotIndex += (e - 10);
                add += "B";
            } else if (e >= 6) {
                add += "M";
            } else if (e >= 3) {
                add += "K";
            }
            String res = num.split("E")[0];
            res = removeTrailingZero(res);
            for (int i = res.length(); i < e - 1; i++) {
                res += "0";
            }
            token[0] = res.replace(".", "");
            token[0] = token[0].substring(0, dotIndex) + "." + token[0].substring(dotIndex);
            token[0] = removeTrailingZero(token[0]);
//            if (token[0].endsWith("0") && add != "") {
//                token[0] = token[0].substring(0, token[0].length() - 1);
//            }
            token[0] += add;
        }

    }


    public static void main(String[] args) {
        /*String str1 = "Let's make some strings and write numbers.. we should see only the numbers changing!";
        String[] str2 = {"Kid", "with", "4312", "bananas", "wished", "to", "sell", "1341", "2/5", "of", "them", "to", "russian", "mafia"};
        String[] str4 = {"7", "3/2", "Thousand", "is", "a", "smaller", "number", "then", "40", "Trillion"};
        String[] str5 = {"1010.56", "is", "in", "the", "assignment", "pdf"};
        String[] str6 = {"10,123,000,000", "is", "in", "the", "assignment", "pdf"};
        String[] str7 = {"55.88", "Billion", "is", "in", "the", "assignment pdf"};
        String[] str8 = {"10,123,000", "is", "in", "the", "assignment", "pdf"};
        String[] str3 = {"the", "rail", "is", "4,124,634,135,235,346.23515", "km", "of", "devined", "happiness"};
        String[] str9 = {"lets", "look", "at", "some", "fractions:", "8", "6/4", "kids,", "5", "3/4", "thousand", "cups,", "please", "lets", "not", "go", "there..", "one", "more:", "6,000,111,000"};
        String[] str10 = {"last", "one:", "lets", "test", "the", "zeroes", "after", "the", "dot..", "542000.1235", "and", "one", "more", "5,435,340,000.78", "done!"};
        String[][] strArray = {str2,str3,str4,str5,str6,str7,str8,str9,str10};
        System.out.println(str1);
        System.out.println("------------------------------------------------------------");
        for (int i = 0; i < strArray.length; i++) {
            for (int j = 0; j < strArray[i].length ; j++) {

            }
        }*/
        /*Character[] characters = new Character[]{'[', '(', '{', '`', ')', '<', '|', '&', '~', '+', '^', '@', '*', '?', '$', '.', '>', ';', '_', '\'', ':', ']', '/', '\\', '}', '!', '=', '#', ',', '"', '-'};
        double start = (double) System.currentTimeMillis();


        String s = "sadhlfkajf;lkjdas;fkljdsa;lkvcm;aslkncnjavkdn;kdjvnkdajsvnkajsdnaflduihdslakjf";


        for (int i = 0; i < 200000; i++) {
            System.out.print(s.length());
        }


        double end = (double) System.currentTimeMillis();
        System.out.println();
        System.out.println(end - start);
        System.out.println();
        start = (double) System.currentTimeMillis();


        int x = s.length();
        for (int i = 0; i < 200000; i++) {
            System.out.print(x);
        }

        end = (double) System.currentTimeMillis();
        System.out.println();
        System.out.println(end - start);
        System.out.println();*/

//        start = (double) System.currentTimeMillis();
//        speChar.contains("\\");
//        end = (double) System.currentTimeMillis();
//        System.out.println(end - start);
//        start = (double) System.currentTimeMillis();
//        specialCharSet.contains("\\");
//        end = (double) System.currentTimeMillis();
//        System.out.println(end - start);

//        System.out.println(monthDictionary.toString());
//        String[] c = {"12345.00012"};
//        numberParse(c);
//        int y = Integer.parseInt(c[0]);
//        System.out.println(c[0]);
        String[] s = new String[]{testString, ""};
        parse(s);
        System.out.println(s[0]);

    }
}
