/**
 *
 * @author Afonso Santos - FC56368
 * @author Alexandre Figueiredo - FC57099
 * @author Raquel Domingos - FC56378
 *
 */
public class ValidationLib {

	private static final String NOT_ACCPETED_CHARS = "(){}[]|`! \"$%^&*<>:;#~_-+=,@.";

    public static boolean isIntegerNumber(String str) {
        try {
            int i = Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    public static boolean isValidNumber(String num) {
        try {
            double d = Double.parseDouble(num);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    public static boolean verifyString(String name) {
    	int i = 0;
        while (i < NOT_ACCPETED_CHARS.length()) {
            if(name.contains(Character.toString(NOT_ACCPETED_CHARS.charAt(i)))) return false;
            i++;
        }
        return true;
    }

    public static boolean hasValidExtension(String filename) {
        String extension = filename.contains(".") ? filename.substring(
        		filename.lastIndexOf(".")+1) : null;
        return extension != null && (extension.equals("jpg") ||
        							 extension.equals("jpeg") ||
        							 extension.equals("png"));
    }


}
