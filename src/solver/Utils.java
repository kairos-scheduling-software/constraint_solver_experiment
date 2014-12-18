package solver;

import java.io.InputStream;
import java.util.Scanner;

public class Utils {
	public static String readInput(InputStream is) {
		Scanner sc = new Scanner(is);
		
		String line = "";
		StringBuilder sb = new StringBuilder();
		do {
			sb.append(line);
		} while ((line = sc.nextLine()).length() > 0);
		sc.close();
		
		return sb.toString();
	}
}
