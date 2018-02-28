package by.wgdetective.candidaterepotester;

import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * @author Wladimir Litvinov
 */
public class TextFilter {

    public static void main(String[] args) {
        args = new String[]{"abcd"};
        new TextFilter().executeTextFiltering(args);
    }

    private void executeTextFiltering(String[] args) {
        System.out.println("Enter \'exit\' for program exit.");
        Scanner scanner = new Scanner(System.in);
        List<String> strings = new LinkedList<>();
        boolean exit;
        while (true) {
            try {
                System.out.println("Enter strings : ");

                exit = getStringsFromInputText(scanner, strings);

                if (exit) {
                    break;
                }

                filteringText(args, strings);

                System.out.println();
                strings.clear();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
        scanner.close();
    }

    private boolean getStringsFromInputText(Scanner scanner, List<String> strings) {
        while (true) {
            String input = scanner.nextLine();
            if (input.equals("exit"))
                return true;

            if (input.equals(""))
                break;

            strings.add(input.endsWith(";") ? input.substring(0, input.length() - 1) : input);
        }
        return false;
    }

    private void filteringText(String[] args, List<String> strings) {
        System.out.println("Output : ");
        for (String line : strings) {
            boolean exist;
            String[] words = line.split(" ");
            for (String word : words) {
                exist = isWordExistInArgs(word, args);
                if (exist) {
                    System.out.println(line + ";");
                    break;
                }
            }
        }
    }

    private boolean isWordExistInArgs(String word, String[] param) {
        for (String arg : param) {

            if (isValidRegex(arg)) {

                StringBuilder regex = new StringBuilder();
                if (!arg.startsWith("^"))
                    regex.append("^");

                regex.append(arg);

                if (!arg.endsWith("$"))
                    regex.append("$");

                if (word.matches(regex.toString())) {
                    return true;
                }

            } else {
                //if arg is a not regular expression -> work as a string
                if (word.equals(arg)) {
                    return true;
                }
            }

        }
        return false;
    }

    private boolean isValidRegex(String arg) {
        try {
            Pattern.compile(arg);
            return true;
        } catch (PatternSyntaxException e) {
            return false;
        }
    }
}