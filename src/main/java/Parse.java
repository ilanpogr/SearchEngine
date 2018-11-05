import javafx.util.Pair;

import java.util.*;

public class Parse {

    private static boolean nextUsed = false;
    public static String testString = "just checking some of the rules, Ilan is a name so it sopposed to be capital letters November is a month and 11 NOV 2018 is a date. the number 1,203,453.22 should change and 99 percent that we will catch the numbers 1,243,535,238.3 without spending 2000 dollar. there are few more rules like NBA names and stuff.";
    public static HashSet<Character> specialCharSet = initSpecialSet();
    public static HashSet<String> monthSet = initMonthSet();

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
        Character[] characters = new Character[]{'[', '(', '{', '`', ')', '<', '|', '&', '~', '+', '^', '@', '*', '?', '$', '.', '>', ';', '_', '\'', ':', ']', '/', '\\', '}', '!', '=', '#', ',', '"', '-'};

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

        return termsDict;
    }

    private static void parseTokens(HashMap<String, String> termsDict, String[] str) {
        String[] s = str[0].split(" ");
        for (int i=0, lastIndex = s.length - 1; i <= lastIndex; ++i) {
            String[] token = new String[]{s[i], "0,"};
            removeSuffix(token);                //removes the suffix from the token (prefix too)
            if (monthSet.contains(token[0].toUpperCase())){
                String [] nextToken = {"","0,"};
                if (i<lastIndex) {
                    nextToken[0] = s[i+1];
                    removeSuffix(nextToken);    //removes the suffix from the token (prefix too)
                    nextToken[1]=nextToken[1].substring(0,nextToken[1].length()-1); //remove the stemmer bool
                }
            }


        }

    }

    private static void removeSuffix(String[] token) {
        boolean isSuffixRemoved = false;
        String tok = token[0];
        for (; tok.length() > 0 && specialCharSet.contains(tok.charAt(0)); tok = token[0] = tok.substring(1)) ;
        while (tok.length() > 1 && specialCharSet.contains(tok.charAt(tok.length() - 1))) {
            tok = token[0] = tok.substring(0, tok.length() - 1);
            isSuffixRemoved = true;
        }
        token[1] += isSuffixRemoved ? "1" : "0";
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

        for (int i = 0; i < characters.length*2000; i++) {
            if (speChar.contains(""+characters[i%characters.length]))
                System.out.print(i+',');
        }

        double end = (double) System.currentTimeMillis();
        System.out.println();
        System.out.println(end - start);
        System.out.println();
        start = (double) System.currentTimeMillis();

        for (int i = 0; i < characters.length*2000; i++) {
            if (specialCharSet.contains(characters[i%characters.length]))
                System.out.print(i+',');
        }

        end = (double) System.currentTimeMillis();
        System.out.println();
        System.out.println(end - start);
        System.out.println();

        start = (double) System.currentTimeMillis();
        speChar.contains("\\");
        end = (double) System.currentTimeMillis();
        System.out.println(end - start);
        start = (double) System.currentTimeMillis();
        specialCharSet.contains("\\");
        end = (double) System.currentTimeMillis();
        System.out.println(end - start);*/

        String s1 = "7.032133";
        Double d = Double.parseDouble(s1);
        System.out.println(d);
        System.out.println();
        String[] s = new String[]{testString, ""};
        parse(s);
        System.out.println(s[0]);
    }
}
