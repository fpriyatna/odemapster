package es.upm.fi.dia.oeg.obdi.wrapper.r2o.datatranslator;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

import es.upm.fi.dia.oeg.obdi.core.exception.PostProcessorException;


public class R2OFreddyPostProcessor extends R2ODataTranslator {
	
	public static void main(String args[]) {
		System.out.println(R2OFreddyPostProcessor.camelize("hEllo EveRyOne"));
	}

	@Override
	protected Object processCustomFunctionTransformationExpression(List<Object> arguments) 
	throws PostProcessorException {
		

		String functionId = (String) arguments.get(0);

		if(functionId.equalsIgnoreCase("substring1")) {
			List<String> result = new ArrayList<String>();
			String theString = (String) arguments.get(1);
			Integer beginIndex = (Integer) arguments.get(2);
			Integer endIndex = (Integer) arguments.get(3);
			String substringResult = theString.substring(beginIndex, endIndex);
			result.add(substringResult);
			return result;			
		} else if(functionId.equalsIgnoreCase("substring2")) {
			List<String> result = new ArrayList<String>();
			String theString = (String) arguments.get(1);
			Integer beginIndex = (Integer) arguments.get(2);
			Integer endIndex = (Integer) arguments.get(3);
			String substringResult = theString.substring(beginIndex, endIndex);
			result.add(substringResult + "1");
			result.add(substringResult  + "2");
			result.add(substringResult  + "3");
			result.add(substringResult + "4");
			result.add(substringResult + "5");
			return result;			
		} else if(functionId.equalsIgnoreCase("concat-all")) {
			String result = "";
			for(int i=1; i<arguments.size(); i++) {
				result += arguments.get(i).toString();
			}
			return result;
		} else if(functionId.equalsIgnoreCase("hash-geometry")) {
			Object argument1 = arguments.get(1);
			return this.getHash(argument1.toString()); 
		} else if(functionId.equalsIgnoreCase("not-null")) {
			String theString = (String) arguments.get(1);
			if(theString !=null && !theString.equals("") && !theString.equals("\"\"")) {
				return theString; 
			} else {
				return null;
			}
		} else if(functionId.equalsIgnoreCase("camelize")) {
			String theString = (String) arguments.get(1);
			String camelizedString = R2OFreddyPostProcessor.camelize(theString); 
			return camelizedString;
		} else {
			throw new PostProcessorException("Unsupported operator!");
		}
	}



	public static String camelize(String input) {
		try {
			String splitInputs[] = input.split(" ");
			input = "";
			for(int i=0; i<splitInputs.length; i++) {
				String splitTag = splitInputs[i];
				splitTag = splitTag.substring(0,1).toUpperCase() + splitTag.substring(1).toLowerCase();
				
				input += splitTag + " ";
			}
			input = input.substring(0,1).toUpperCase() + input.substring(1, input.length() -1);

			//tag = tag.replace(' ','_');

		} catch(Exception e) {
			
		} finally {
			return input;
		}
		
	}
	
    public String getHash(String message) {
        MessageDigest md;
        byte[] buffer, digest;
        String hash = "";

        try {
            buffer = message.getBytes("UTF-8");
            md = MessageDigest.getInstance("SHA1");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        md.update(buffer);
        digest = md.digest();
        for (byte aux : digest) {
            int b = aux & 0xff;
            String s = Integer.toHexString(b);
            if (s.length() == 1) {
                hash += "0";
            }
            hash += s;
        }
        return hash;
    }

	@Override
	protected Object processCustomFunctionTransformationExpression(
			Object argument) throws PostProcessorException {
		// TODO Auto-generated method stub
		return null;
	}

}
