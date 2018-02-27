package by.wgdetective.candidaterepotester;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * @author Wladimir Litvinov
 */
public class Parser {
    public static void main(String[] args) {


        List<String> paramStr = readParam();
        List<String> masStr = readString();
        Set<String[]> result;
        try {
            result = searchStrByReg(paramStr, masStr);
        } catch (PatternSyntaxException pt) {
            result = searchStr(paramStr, masStr);
        }
        print(result);
    }

    public static List<String> readParam() {
        List<String> paramStr = new ArrayList<>();
        Scanner in = new Scanner(System.in);
        String str;
        System.out.println("Введите строку или регулярное выражение");
        str = in.nextLine();
        for (String retval : str.split(" ")) {
            paramStr.add(retval);
        }
        return paramStr;
    }

    public static List<String> readString() {
        List<String> masStr = new ArrayList<>();
        Scanner in = new Scanner(System.in);
        String str;
        boolean index = true;
        System.out.println("Введите строки для проверки");
        while (index) {
            str = in.nextLine();
            if (str.isEmpty()) {
                index = false;
                break;
            }
            masStr.add(str);
        }
        return masStr;
    }

    public static Set<String[]> searchStr(List<String> param, List<String> mas) {
        List<String[]> mS = new ArrayList<>();
        Set<String[]> result = new LinkedHashSet<>();
        for (String s : mas) {
            mS.add(s.split(" "));
        }
        for (String paramStr : param) {
            for (String[] masStr : mS) {
                for (int i = 0; i < masStr.length; i++) {
                    if (masStr[i].equals(paramStr)) {
                        result.add(masStr);
                        break;
                    }
                }
            }
        }
        return result;
    }

    public static Set<String[]> searchStrByReg(List<String> param, List<String> mas) {
        List<String[]> mS = new ArrayList<>();
        Set<String[]> result = new LinkedHashSet<>();
        for (String s : mas) {
            mS.add(s.split(" "));
        }
        for (String paramStr : param) {
            Pattern p = Pattern.compile(paramStr);
            for (String[] str : mS) {
                for (int i = 0; i < str.length; i++) {
                    Matcher m = p.matcher(str[i]);
                    if (m.matches()) {
                        result.add(str);
                        break;
                    }
                }

            }
        }
        return result;
    }

    public static void print(Set<String[]> result) {
        for (String[] mas : result) {
            for (int i = 0; i < mas.length; i++) {
                System.out.print(mas[i] + " ");
            }
            System.out.println();
        }
    }
}