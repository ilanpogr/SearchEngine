package Parser;

import Controller.PropertiesFile;

import java.io.*;
import java.util.*;

import static org.apache.commons.lang3.StringUtils.*;

/**
 * the class gets a string containing any text.
 * by a set of rules pre decided, parsing the text and cerating a dictionary of
 * all the terms inside this specific text.
 */
public class Parser {

    private static String parametersDelimiter = PropertiesFile.getProperty("token.parameters.delimiter");
    private static String gapDelimiter = PropertiesFile.getProperty("gaps.delimiter");
    private boolean doneWithToken = true;
    private static HashSet<Character> specialCharSet = initSpecialSet();
    private static HashSet<String> monthSet = initMonthSet();
    private static HashSet<String> stopWords = initStopWords();
    private static HashMap<String, String> monthDictionary = initMonthMap();
    private int currentPosition = 0;
    private int stopWordsCounter = 0;

    /**
     * A wrapper function that starting the whole process.
     *
     * @param str : an Array containing 1 cell of the whole text.
     * @return : the dictionary containing all the terms from the text. Key <Term> ; Value <counter,position,isToStem>
     */
    public HashMap<String, String> parse(String[] str) {
        HashMap<String, String> termsDict = new HashMap<>();
        parseTokens(termsDict, str);
        return termsDict;
    }

    /**
     * Initialize set of all the wanted stop-words.
     *
     * @return : the set of stop-words
     */
    private static HashSet<String> initStopWords() {
//        String filesPath = PropertiesFile.getProperty("data.set.path") + "stop_words.txt";
        String filesPath;
        try {
            filesPath = PropertiesFile.getProperty("data.set.path") + "\\stop_words.txt";
        } catch (Exception e){
            filesPath = "src\\main\\resources\\stop_words.txt";
        }
        stopWords = new HashSet<>();
        Scanner s = null;
        try {
            s = new Scanner(new File(filesPath));
            while (s!=null && s.hasNext()) stopWords.add(s.nextLine());
            stopWords.remove("between");
            stopWords.remove("may");
        } catch (FileNotFoundException e) {
            System.out.println("couldn't fine file");
            return null;
        }
        return stopWords;
    }

    /**
     * Initialize set of all month's name representations
     *
     * @return : the Set of month names.
     */
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

    /**
     * Initialize Map of all months that will be represented by number after parse.
     *
     * @return : the Map of months representation.
     */
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

    /**
     * Initialize set of all chars that wished to be cleaned out of the token. --> TOKEN <--
     *
     * @return : the Set after initializing.
     */
    private static HashSet<Character> initSpecialSet() {
        specialCharSet = new HashSet<>();
        Character[] characters = new Character[]{' ', '\r', '\n', '[', '(', '{', '`', ')', '<', '|', '&', '~', '+', '^', '@', '*', '?', '.', '>', ';', '_', '\'', ':', ']', '/', '\\', '}', '!', '=', '#', ',', '"', '-'};

        specialCharSet.addAll(Arrays.asList(characters));

        return specialCharSet;
    }

    public static CharSequence getGapDelimiter() {
        return gapDelimiter;
    }

    /**
     * The main function that splits the single string with text, to an array of words.
     * each word (token) will be checked with multiple rules an be applied as needed.
     * the RULES: you can find than in the project description.
     *
     * @param termsDict : the Dictionary that will contain all the terms
     * @param str       : Array with one cell, containing the text.
     */
    private void parseTokens(HashMap<String, String> termsDict, String[] str) {
        String[] s = split(str[0], " ");
        boolean expressionFlag;
        stopWordsCounter = 0;
        if (stopWords==null)
            stopWords = initStopWords();
        for (int i = 0, lastIndex = s.length - 1; i <= lastIndex; i++) {
            currentPosition = i+1 - stopWordsCounter;
            expressionFlag = false;
            doneWithToken = true;
            String[] token = new String[]{s[i], parametersDelimiter};
            cleanToken(token);
            if (stopWords.contains(token[0].toLowerCase()) || token[0].equals("") || containsOnly(token[0], '$') || containsOnly(token[0], '%')) {
                stopWordsCounter++;
                continue;
            }
            if (contains(token[0],"%")){
                String[] tmp = {replace(token[0],"%",""),""};
                if (!checkIfNumber(tmp[0])){
                    cleanToken(tmp);
                    if (contains(token[0],"$"))
                        tmp[0] = replace(tmp[0],"$","");
                    token[0] = tmp[0];
                }
            }
            if (token[0].toLowerCase().endsWith("'s")) {                        //  ADDITIONAL RULE: remove all " 's " from tokens
                token[0] = token[0].substring(0, token[0].length() - 2);
                cleanToken(token);
            }
            char firstCharOfToken = token[0].charAt(0);

            String[] ourRuleToken = {s[i]};
            if (Character.isUpperCase(firstCharOfToken) && !specialCharSet.contains(ourRuleToken[0].charAt(ourRuleToken[0].length() - 1))) {
                if (i + 1 < s.length) {
                    char firstCharOfNextToken = s[i + 1].charAt(0);
                    if (Character.isUpperCase(firstCharOfNextToken) || s[i + 1].toLowerCase().equals("of")) {          //    ADDITIONAL RULE: continues expression of upper case words.
                        token[0] = replace(token[0], "--", "-");
                        continuesUpperCaseExpression(termsDict, token, s, i);
                        continue;
                    }
                }
            }

            if (token[0].toLowerCase().equals("between")) {  // expression --> between # and #
                if (i + 1 < s.length) {
                    String[] check = {s[i + 1]};
                    if ((checkIfFracture(check[0]) || checkIfNumber(check[0]))) {
                        expressionFlag = true;
                        doneWithToken = false;
                        i = isTokenBetweenExpression(termsDict, s, i);
                    } else {
                        continue;
                    }
                } else {
                    continue;
                }
            }
            if (!expressionFlag && checkIfNumber(token[0])) {           // might be an expression starting with number without '-'
                if (i + 1 < s.length) {
                    String[] check = {s[i + 1]};
                    cleanToken(check);
                    if (check[0].contains("-")) { // an expression --> first a number and then an expression
                        s[i + 1] = replace(s[i + 1], "--", "-");
                        check = split(s[i + 1], "-");
                        if (checkIfRepresentingNumber(check) || checkIfFracture(check[0])) {
                            expressionFlag = true;
                            i = beforeSlashForNumbers(termsDict, token, s, i);
                            token[0] += "-";
                            i = afterSlashForNumbers(termsDict, token, s, i);
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
            if (!expressionFlag && token[0].contains("-")) {            // might be an expression containing a '-' and this expression is NOT registered in the dictionary
                token[0] = replace(token[0], "--", "-");
                expressionFlag = true;
                doneWithToken = false;
                token[0] = replace(token[0], ",", "");
                i = expressionStartsWithSlash(termsDict, token, i);
                i = numberAfterSlashInExpressionStartsWithSlash(termsDict, token, s, i);
            }
            if (!expressionFlag) {
                if (specialCharSet.contains(firstCharOfToken)) {                 //1. if token starts with a symbol
                    token[0] = token[0].substring(1) + token[0].charAt(0);
                    firstCharOfToken = token[0].charAt(0);
                }
                if (Character.isDigit(firstCharOfToken) || firstCharOfToken == '$' || firstCharOfToken=='%') {                //2. if token starts with a digit
                    double tok = numerize(token);
                    if (tok != -1) {                                             //2.1. if token is a number
                        if (i < lastIndex) {                                     //2.1.1. if token has next
                            String[] nextToken = {s[i + 1], ""};
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
                } else {                                                         //3. if token starts with a letter
                    if (Character.isUpperCase(firstCharOfToken)) {               //3.1. if token starts with an UPPERCASE letter
                        if (monthSet.contains(token[0].toUpperCase())) {         //3.1.1. if token is a Month
                            if (i < lastIndex) {                                 //3.1.1.1. if token has next
                                String[] nextToken = {s[i + 1], ""};
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
                    if (!token[0].toLowerCase().equals("may"))
                        checkCaseAndInsertToDictionary(termsDict, token);
                    s[i] = "";
                }
            }
        }
    }

    /**
     * Rule: Grouping up to 4 tokens that are continues with upper cases. in exception if the token "of"/"the"
     * appear check the next token after, same rules are applied.
     * continues expression stops when symbol appear after the word.
     * next iteretion position will increase in 1 and the substring of continues expression will also be applied.
     * For example the String : "PRESIDENT of the UNITED STATES of AMERICA, Bacon Rules!
     * PRESIDENT UNITED STATES AMERICA
     * UNITED STATES AMERICA
     * STATES AMERICA
     * PRESIDENT
     * UNITED
     * STATES
     * AMERICA
     * BACON RULES
     * BACON
     * RULES
     *
     * @param termsDict : the Dictionary that will contain all the terms
     * @param token     : the current token we are working with
     * @param s         : an Array containing all tokens of the text (pointer)
     * @param i         : current index in 's'
     */
    private void continuesUpperCaseExpression(HashMap<String, String> termsDict, String[] token, String[] s, int i) {
        doneWithToken = false;
        token[0] = token[0].toUpperCase();
        StringBuilder continuesExpression = new StringBuilder();
        if (contains(token[0], "-")) {
            String[] splitToken = split(token[0], '-');
            String[] tokenOne = {splitToken[0], ""};
            String[] tokenTwo = {splitToken[1], ""};
            if (!stopWords.contains(tokenOne[0].toLowerCase())) {

                continuesExpression.append(tokenOne[0]).append(" ");
            }
            if (!stopWords.contains(tokenTwo[0].toLowerCase())) {
                insertToDictionary(termsDict, tokenTwo);
                continuesExpression.append(tokenTwo[0]);
            }
        } else {
            insertToDictionary(termsDict, token);
            continuesExpression.append(token[0]).append(" ");
        }
        boolean stopFlag = false;
        int counter = 0;
        while (!stopFlag && i + 1 < s.length && counter < 2) {
            String[] nextToken = {s[i + 1]};
            while ((nextToken[0].equalsIgnoreCase("of") || nextToken[0].equalsIgnoreCase("the")) && i + 2 < s.length) {
                i++;
                nextToken[0] = s[i + 1];
            }
            if ((Character.isUpperCase(nextToken[0].charAt(0))) && !specialCharSet.contains(nextToken[0].charAt(0))) {
                if (specialCharSet.contains(nextToken[0].charAt(nextToken[0].length() - 1))) {
                    stopFlag = true;
                    cleanToken(nextToken);
                }
                nextToken[0] = nextToken[0].toUpperCase();
                nextToken[0] = replace(nextToken[0], "--", "-");
                nextToken[0] = replace(nextToken[0], "'S", "");
                continuesExpression.append(nextToken[0]).append(' ');
                i++;
                counter++;
            } else {
                stopFlag = true;
            }
        }
        String[] finalToken = {continuesExpression.toString(), ""};
        finalToken[1] = "0" + parametersDelimiter + currentPosition;
        insertToDictionary(termsDict, finalToken);
    }

    /**
     * Handles the token that the expression is applied to rule "BETWEEN ___ AND ___".
     * the tokens in the expression can be only numbers. they will be sent to the number rules handling function.
     *
     * @param termsDict : the Dictionary that will contain all the terms
     * @param s         : an Array containing all tokens of the text (pointer)
     * @param i         : current index in 's'
     * @return : the current index of handled token in 's'
     */
    private int isTokenBetweenExpression(HashMap<String, String> termsDict, String[] s, int i) {
        String[] betweenExpression = {"", "0" + parametersDelimiter + currentPosition};
        String[] numInBetweenExpession = {s[i + 1], ""};
        cleanToken(numInBetweenExpession);
        i++;
        i = checkIfTokenIsNum(termsDict, numInBetweenExpession, i, s);
        betweenExpression[0] = "between" + " " + numInBetweenExpession[0];
        if (i + 1 < s.length && s[i].toLowerCase().equals("and")) {
            numInBetweenExpession[0] = s[i + 1];
            cleanToken(numInBetweenExpession);
            numInBetweenExpession[0] = replace(numInBetweenExpession[0], ",", "");
            i++;
            i = checkIfTokenIsNum(termsDict, numInBetweenExpession, i, s);
            betweenExpression[0] += " and" + " " + numInBetweenExpession[0];
            insertToDictionary(termsDict, betweenExpression);
            i--;
        }
        return i;
    }

    /**
     * Handles a token during expression rule, that the number rule applied here. token-"# (null/number representation/fracture)"
     *
     * @param termsDict : the Dictionary that will contain all the terms
     * @param token     : the current token we are working with
     * @param s         : an Array containing all tokens of the text (pointer)
     * @param i         : current index in 's'
     * @return : the current index of handled token in 's'
     */
    private int numberAfterSlashInExpressionStartsWithSlash(HashMap<String, String> termsDict, String[] token, String[] s, int i) {
        String[] tokenByDelimiter = split(token[0], "-");
        if (checkIfNumber(tokenByDelimiter[1]) || checkIfFracture(tokenByDelimiter[1])) {

            String[] tmpToken = {tokenByDelimiter[1], ""};
            checkIfTokenIsNum(termsDict, tmpToken, i, s);
            tokenByDelimiter[0] = replace(tokenByDelimiter[0], "$", "");
            String[] finalToken = {tokenByDelimiter[0] + "-" + tmpToken[0], "0" + parametersDelimiter + currentPosition};
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

    /**
     * Handles a token during expression rule, the first token af this rule (current) contains '-'.
     * Options to handle: word-... , #-...
     * sends each option to be handled separately. in this function need only the current token
     * without checking next tokens
     *
     * @param termsDict : the Dictionary that will contain all the terms
     * @param token     : the current token we are working with
     * @param i         : current index in the text tokens array
     * @return : the current index of handled token in text tokens array
     */
    private int expressionStartsWithSlash(HashMap<String, String> termsDict, String[] token, int i) {
        String[] strTmp = {token[0], token[1]};
        String[] finalToken = {"", ""};
        cleanToken(strTmp);
        strTmp[0] = replace(strTmp[0], ",", "");
        String[] expressionTokens = split(strTmp[0], "-");
        strTmp[0] = expressionTokens[0];
        cleanToken(strTmp);
        cleanToken(expressionTokens);
        if (checkIfNumber(expressionTokens[0]) || checkIfFracture(expressionTokens[0])) {      // expression starts with a num #-..
            checkIfTokenIsNum(termsDict, strTmp, 0, expressionTokens);
            finalToken[0] = strTmp[0];
            String[] changeToken = split(token[0], "-");
            token[0] = strTmp[0] + "-";
            for (int j = 1; j < changeToken.length; j++) {
                token[0] += changeToken[j];
                if (j < changeToken.length - 1) {
                    token[0] += "-";
                }
            }
        } else {
            strTmp[0] = replace(strTmp[0], "$", "");
            expressionTokens[0] = replace(expressionTokens[0], "$", "");
            checkCaseAndInsertToDictionary(termsDict, strTmp);
            finalToken[0] = expressionTokens[0];
            if (!checkIfNumber(expressionTokens[1]) && !checkIfFracture(expressionTokens[1])) {    // expression of words: w-w-w-w-......
                for (int j = 1; j < expressionTokens.length; j++) {
                    String[] oneWordFromExpression = {expressionTokens[j], ""};
                    oneWordFromExpression[0] = replace(oneWordFromExpression[0], "$", "");
                    cleanToken(oneWordFromExpression);
                    finalToken[0] += "-" + oneWordFromExpression[0];
                    checkCaseAndInsertToDictionary(termsDict, oneWordFromExpression);
                }
                finalToken[1] = "0" + parametersDelimiter + currentPosition;
                finalToken[0] = replace(finalToken[0], "--", "-");
                insertToDictionary(termsDict, finalToken);
                return i;
            }
        }
        if (!checkIfNumber(expressionTokens[1]) && !checkIfFracture(expressionTokens[1])) {     // expression continues with word: #-w
            strTmp[0] = expressionTokens[1];
            cleanToken(strTmp);
            strTmp[0] = replace(strTmp[0], "$", "");
            finalToken[0] += "-" + strTmp[0];
            checkCaseAndInsertToDictionary(termsDict, strTmp);
            finalToken[1] = "0" + parametersDelimiter + currentPosition;
            insertToDictionary(termsDict, finalToken);
        }
        return i;
    }

    /**
     * handles all rules after the slash that applied to the rules of expressions: token token'-'token token (optional)
     *
     * @param termsDict : the Dictionary that will contain all the terms
     * @param token     : the current token we are working with
     * @param strings   : an Array containing all tokens of the text (pointer)
     * @param i         : current index in 'strings'
     * @return : the current index of handled token in 'strings'
     */
    private int afterSlashForNumbers(HashMap<String, String> termsDict, String[] token, String[] strings, int i) {
        String[] strTmp = {strings[i + 1]};
        cleanToken(strTmp);
        strTmp[0] = replace(strTmp[0], ",", "");

        String[] expressionToken = split(strTmp[0], "-");
        String[] tmpToken = {expressionToken[1], ""};

        if (checkIfNumber(expressionToken[1])) {
            checkIfTokenIsNum(termsDict, tmpToken, i + 1, strings);
            token[0] += tmpToken[0];
        } else {
            insertToDictionary(termsDict, tmpToken);
        }
        if (!checkIfNumber(expressionToken[1])) {
            token[0] += expressionToken[1];
        }
        if (token[1].endsWith(parametersDelimiter)) {
            token[1] = "0" + parametersDelimiter + currentPosition;
        }
        insertToDictionary(termsDict, token);
        String[] check = {expressionToken[0]};
        if (checkIfRepresentingNumber(check)) {
            return i + 1;
        }
        return i;
    }

    /**
     * handles all rules before the slash that applied to the rules of expressions: token token'-'tokens
     *
     * @param termsDict : the Dictionary that will contain all the terms
     * @param token     : the current token we are working with
     * @param strings   : an Array containing all tokens of the text (pointer)
     * @param i         : current index in 'strings'
     * @return : the current index of handled token in 'strings'
     */
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

    /**
     * checks 4 rules for parsing (if rule aplied, i++ for knowing if token was applied to one of those rules).
     * rules that token might be applied to: MONEY, PERCENTAGE, NUMBER (and not in an expression),
     * and extreme case for date (the rest of the rule dealt in the main parsing function before
     * reaching here.
     *
     * @param termsDict : the Dictionary that will contain all the terms
     * @param token     : the current token we are working with
     * @param strings   : an Array containing all tokens of the text (pointer)
     * @param i         : current index in the text tokens array
     * @return : the current index of handled token in 'strings' (if token dealt with any rule, i++, so returning i--);
     */
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
            checkCaseAndInsertToDictionary(termsDict, token);
            return i;
        }
        return delta - 1;

    }

    /**
     * checks if token is fracture: # '/' #.
     *
     * @param token : the token.
     * @return : isFracture.
     */
    private boolean checkIfFracture(String token) {
        if (token.contains("/")) {
            token = replace(token, ",", "");
            String[] check = split(token, "/");
            if (check.length < 2) {
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

    /**
     * check if the string representing a number size. (array for using token as pointer)
     *
     * @param s : token
     * @return :isNumberSizeRepresentation.
     */
    private boolean checkIfRepresentingNumber(String[] s) {
        return s[0].equals("thousand") || s[0].equals("million") || s[0].equals("billion") || s[0].equals("trillion");
    }

    /**
     * changing the number representing to be represented as the parsing rules.
     *
     * @param token : current token with number.
     * @param s     : the number representation.
     */
    private void prepareNumberRepresentationForTerm(String[] token, String[] s) {
        switch (s[0]) {
            case "thousand":
                token[0] += 'K';
                break;
            case "million":
                token[0] += 'M';
                break;
            case "billion":
                token[0] += 'B';
                break;
            case "trillion":
                token[0] += "000" + 'B';
                break;
        }
    }

    /**
     * handles all token numbers that are smaller than thousand.
     * rules that paid attention to: if the number is not decimal,
     * might be the next token a fracture.
     *
     * @param token   : current number token
     * @param i       : current position in 'strings'
     * @param strings : the whole text by tokens.
     * @return : current position in strings after handling the token.
     */
    private int numberSmallerThanThousand(String[] token, int i, String[] strings) {
        if (!token[0].contains(".")) {              // no decimal --> option for fracture
            if (i + 1 < strings.length) {
                String[] s = {strings[i + 1]};
                cleanToken(s);
                if (checkIfFracture(s[0])) {
                    s[0] = replace(s[0], ",", "");
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
            String rightSide = str[0].substring(index + 1);
            String leftSide = str[0].substring(0, index);
            result[0] = leftSide;
            result[1] = rightSide;
            return result;
        }
        return str;
    }

    /**
     * removing all trailing zeros if the token (number) is decimal.
     * FUNCTION WAS TAKEN FROM:
     * https://stackoverflow.com/questions/14984664/remove-trailing-zero-in-java
     *
     * @param s : the string with potential trailing zeroes
     * @return s without the trailing zeroes
     */
    private String[] removeTrailingZero(String[] s) {
        s[0] = !s[0].contains(".") ? s[0] : s[0].replaceAll("0*$", "").replaceAll("\\.$", "");
        return s;
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

    /**
     * checks all the possible options that a token can be a number (by the rules for numbers), and sends the token
     * to the right function to be handled there.
     * if the token is not a number, i stays the same.
     *
     * @param termsDict : the Dictionary that will contain all the terms
     * @param token     : the current token we are working with
     * @param strings   : an Array containing all tokens of the text (pointer)
     * @param i         : current index in 'strings'
     * @return : the current index of handled token in 'strings'
     */
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
            if (!token[1].startsWith("0" + parametersDelimiter)) {
                token[1] = "0" + parametersDelimiter + currentPosition;
            }
            insertToDictionary(termsDict, token);
            return i + 1;
        } catch (NumberFormatException e) {
            if (checkIfFracture(token[0])) {             // EXTREME CASE IF FRACTURE WITHOUT NUMBER BEFORE IT
                if (token[1].endsWith(parametersDelimiter)) {
                    token[1] = "0" + parametersDelimiter + currentPosition;
                }
                insertToDictionary(termsDict, token);
                i++;
            }
            return i;
        }
    }

    /**
     * checks all the possible options that a token can be a percentage representation (by the rules for percentage), and sends the token
     * to the right function to be handled there.
     * if the token is not a percentage representation, i stays the same.
     *
     * @param termsDict : the Dictionary that will contain all the terms
     * @param token     : the current token we are working with
     * @param strings   : an Array containing all tokens of the text (pointer)
     * @param i         : current index in 'strings'
     * @return : the current index of handled token in 'strings'
     */
    private int checkIfTokenIsPercentage(HashMap<String, String> termsDict, String[] token, int i, String[] strings) {
        String tmp = replace(token[0], "%", "");
        if (checkIfNumber(tmp)) {
            if (token[0].contains("%")) {
                token[0] = replace(token[0], "%", "");
                token[0] += "%";
                token[1] = "0" + parametersDelimiter + currentPosition;
                insertToDictionary(termsDict, token);
                return i + 1;
            } else if (i < strings.length - 1 && (strings[i + 1].toLowerCase().startsWith("percent") || strings[i + 1].toLowerCase().startsWith("percentage"))) {
                token[0] += "%";
                token[1] = "0" + parametersDelimiter + currentPosition;
                insertToDictionary(termsDict, token);
                return i + 2;
            }
        }
        return i;
    }

    /**
     * if dictionary contains same token with Lower Case Letters, token will be removed from dictionary
     * and added to the counter of Lower Case Letters token.
     *
     * @param termsDict : the Dictionary that will contain all the terms
     * @param token     : current token is with Upper Case Letters.
     */
    private void convertToLowerCase(HashMap<String, String> termsDict, String[] token) {
        String oldKey = termsDict.remove(token[0].toUpperCase());
        termsDict.put(token[0].toLowerCase(), oldKey);
        token[1] = oldKey;
    }


    /**
     * checks if number is an int.
     *
     * @param num : number
     * @return : isInt
     */
    private boolean isTokenAnInt(double num) {
        int x = (int) num;
        return num - x == 0;
    }

    /**
     * checks if token is a year representation
     *
     * @param year : the token
     * @return : isYear
     */
    private boolean isTokenAYear(double year) {
        return isTokenAnInt(year) && year < 3000 && year > 0;
    }

    /**
     * checks if token is a day representation
     *
     * @param day : the token
     * @return : isYear
     */
    private boolean isTokenADay(double day) {
        return isTokenAnInt(day) && day > 0 && day < 31;
    }

    /**
     * check if the token is date representation (#-MONTH).
     *
     * @param termsDict : the Dictionary that will contain all the terms
     * @param token     : the current token we are working with
     * @param strings   : an Array containing all tokens of the text (pointer)
     * @param i         : current index in 'strings'
     * @return : the current index of handled token in 'strings'
     */
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

    /**
     * all token that are applied to the date parsing rules called here and inserted to
     * the dictionary. (only 2 out of 3 parameters 'day', 'month', 'year') will be not null.
     *
     * @param termsDict : the Dictionary that will contain all the terms
     * @param day       : the day
     * @param month     : the month
     * @param year      : th year
     */
    private void insertDate(HashMap<String, String> termsDict, String[] day, String[] month, String[] year) {
        month[0] = monthDictionary.get(month[0].toUpperCase());
        if (day != null) {
            if (day[0].length() == 1) {
                day[0] = "0" + day[0];
            }
            String[] date = {month[0] + "-" + day[0], "0" + parametersDelimiter + currentPosition};
            insertToDictionary(termsDict, date);
        } else if (year != null) {
            String[] date = {year[0] + "-" + month[0], "0" + parametersDelimiter + currentPosition};
            insertToDictionary(termsDict, date);
        }
    }

    /**
     * checks all the possible options that a token can be a money representation (by the rules for money), and sends the token
     * to the right function to be handled there.
     * if the token is not a money representation, i stays the same.
     *
     * @param termsDict : the Dictionary that will contain all the terms
     * @param token     : the current token we are working with
     * @param strings   : an Array containing all tokens of the text (pointer)
     * @param i         : current index in 'strings'
     */
    private int checkIfTokenIsMoney(HashMap<String, String> termsDict, String[] token, int i, String[] strings) {
        if (token[0].contains("$")) {
//            token[0] = token[0].replace("$", "");      //$# or #$
            token[0] = replace(token[0], "$", "");      //$# or #$
            i = addQuantityToToken(token, i, strings);
            token[0] += " Dollars";
            token[1] = "0" + parametersDelimiter + currentPosition;
            insertToDictionary(termsDict, token);
            return i + 1;
        } else if (i + 1 < strings.length && strings[i + 1].toLowerCase().startsWith("dollar")) {    //# dollars
            if (checkIfNumber(token[0]) || token[0].toLowerCase().endsWith("m") || token[0].toLowerCase().endsWith("bn")) {
                i = addQuantityToToken(token, i, strings);
                token[0] += " Dollars";
                token[1] = "0" + parametersDelimiter + currentPosition;
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
                        token[1] = "0" + parametersDelimiter + currentPosition;
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
                            addQuantityToToken(token, i, strings);
                            token[0] += " Dollars";
                            token[1] = "0" + parametersDelimiter + currentPosition;
                            insertToDictionary(termsDict, token);
                            return j + 1;
                        }
                    }
                }
            }
        }
        return i;
    }

    /**
     * checks if the token can be money representation
     *
     * @param strings the whole text as array of tokens
     * @param i       : current position in 'strings'
     * @return : isCanBeMoney
     */
    private boolean checkIfCanBeMoney(String[] strings, int i) {
        for (int j = i + 1; j <= i + 3; j++) {
            if (j < strings.length && strings[j].toLowerCase().contains("dollar")) {
                return true;
            }
        }
        return false;
    }

    /**
     * handles tokens that found to be money representation and changes the token
     * as the rules applies
     *
     * @param token : current token
     * @param i     : option to take care of the token.
     */
    private void moneyParse(String[] token, int i) {
        if (i == 0) {
            String[] num = cutDecimal(token);
            boolean flag = false;
            if (!token[0].equals(num[0]) && !token[1].equals(num[1])) {
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

    /**
     * if money token is money' checks if represented with with quantity and the rules
     * are applied as specified for money quantity representation.
     *
     * @param token     : the current token we are working with
     * @param strings   : an Array containing all tokens of the text (pointer)
     * @param i         : current index in 'strings'
     * @return : the current index of handled token in 'strings'
     */
    private int addQuantityToToken(String[] token, int i, String[] strings) {
            if (token[0].toLowerCase().endsWith("m")) {
                token[0] = replace(token[0], "m", " M");
                moneyParse(token, 0);
                return i;
            }
            if (token[0].toLowerCase().endsWith("b") || token[0].toLowerCase().endsWith("bn")) {
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

    /**
     * cleaning the token from all symbol at 'specialCharSet' starting at the beginning until reaching
     * letter/number and than from the end.
     *
     * @param token current token we are working with
     */
    private void cleanToken(String[] token) {
        while (token[0].length() > 0 && specialCharSet.contains(token[0].charAt(0))) {
            token[0] = token[0].substring(1);
        }
        while (token[0].length() >= 1 && specialCharSet.contains(token[0].charAt(token[0].length() - 1))) {
            token[0] = token[0].substring(0, token[0].length() - 1);
        }
    }

    /**
     * if token is with Upper Case Letter, function checks if the dictionary
     * already contains same token with Lower Case Letters.
     *
     * @param termsDict : the Dictionary that will contain all the terms
     * @param token     : current token we are working with.
     */
    private void checkCaseAndInsertToDictionary(HashMap<String, String> termsDict, String[] token) {
        if (token[0].length() < 1) return;
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

    /**
     * inserts the token to the dictionary. removing all 's from the ending of a token, and if
     * the token didn't apply to any parse rule, add a flag that the token is good for stemming.
     *
     * @param termsDict : the Dictionary that will contain all the terms.
     * @param token     : current token we are working with.
     */
    private void insertToDictionary(HashMap<String, String> termsDict, String[] token) {
        if (!token[1].startsWith("0" + parametersDelimiter) && !token[1].startsWith("1" + parametersDelimiter)) {
            token[1] = (token[0].length() < 4 ? "0" + parametersDelimiter : "1" + parametersDelimiter) + currentPosition;
        }
        cleanToken(token);
        groupTokenPositions(termsDict, token);
        if (doneWithToken) {
            token[0] = "";
        }
    }

    /**
     * Takes current position of the token, and changing it to
     * be represented as the gap between the position and last position.
     * number of unique words in dictionary updated in this function.
     *
     * @param termsDict : the Dictionary that will contain all the terms.
     * @param token     : current token we are working with.
     * @return : the gap between current position and last position.
     */
    private int getLastGap(HashMap<String, String> termsDict, String[] token) {
        int res = 0;
        String positions = substringAfter(termsDict.get(token[0]), parametersDelimiter);
        String[] toCount = split(positions, gapDelimiter);
        for (String num : toCount) {
            res += Integer.parseInt(num);
        }
        res = currentPosition - res;
        return res;
    }

    /**
     * for each term add the position in the text (represented by gaps) and
     * insert to dictionary
     *
     * @param termsDict : the Dictionary that will contain all the terms.
     * @param token     : current token we are working with.
     */
    private void groupTokenPositions(HashMap<String, String> termsDict, String[] token) {
        if (termsDict.containsKey(token[0])) {
            termsDict.put(token[0], termsDict.get(token[0]) + gapDelimiter + getLastGap(termsDict, token));
        } else {
            termsDict.put(token[0], token[1]);
        }
    }
}

