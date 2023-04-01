import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * 
 * @author Afonso Santos - FC56368
 * @author Alexandre Figueiredo - FC57099
 * @author Raquel Domingos - FC56378
 *
 */
public abstract class Catalogo {
	protected static final String SEPARATOR = ":";
	
	protected static void changeLine(String targetLine, String toReplace, File file) {
		StringBuilder sb = new StringBuilder();
		String line = null;
		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			line = reader.readLine();
			while(line != null) {
				if (!line.equals(targetLine)) {
					sb.append(line +"\n");
				}
				else if (toReplace != null) {
					sb.append(toReplace + "\n");
				}
				line = reader.readLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//Write the content in the StringBuilder to the file
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
			writer.write(sb.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
	
	public abstract void load();
}
