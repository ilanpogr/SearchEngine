import java.util.List;
import java.util.ListIterator;

public class Parse {

    /**
     * Seperate the Integer from the Decimal number/
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
            number[1] = tmp + number[1] + 'K';
            if (number[1].length() > 1) {
                current = number[0] + '.' + number[1];
            } else {
                current = number[0] + number[1];
            }
        }
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
            number[1] = tmp + number[1] + 'M';
            if (number[1].length() > 1) {
                current = number[0] + '.' + number[1];
            } else {
                current = number[0] + number[1];
            }
        }
        return current;
    }

    private static String extremeCaseDecimal(int decimal, char type) {
        String str = Integer.toString(decimal);
        if(type == 'K'){
            if (decimal < 10) {
                str = "00" + str;
            } else if (decimal < 100) {
                str = '0' + str;
            }
        }else if (type == 'M'){
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

    /*TODO -
        1. if next token is 'Thousand' or 1000 < int < 1000000 --> numberThousand
      */
    public static void main(String[] args) {
        String num = numberMillion("100,007,703.784");
        System.out.println(num);
    }


}
