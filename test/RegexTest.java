

import java.io.Console;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class RegexTest {

    public static void main(String[] args){
        
        String regex = "(.*) Cruz de (.*)";
        String inputString = "Santa Cruz de Tenerife, Puerto de";
        
            Pattern pattern = Pattern.compile(regex);

            Matcher matcher = pattern.matcher(inputString);

            boolean found = false;
            while (matcher.find()) {
                System.out.println("I found the text");
                found = true;
            }
            if(!found){
            	System.out.println("No match found.");
            }
    }
}
