package es.upm.fi.dia.oeg.obdi.wrapper.r2o.test;

import java.io.Console;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class RegexTest {

    public static void main(String[] args){
        
        String regex = "Man";
        String inputString = "Asistant Manager";
        
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
