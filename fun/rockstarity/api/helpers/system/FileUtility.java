package fun.rockstarity.api.helpers.system;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import lombok.Cleanup;
import lombok.experimental.UtilityClass;
import ru.kotopushka.antiautistleak.obfuscator.includes.annotations.compile.ReleaseCompileToNativeCalls;

/**
 * @author ConeTin
 * @since 3 дек. 2023 г.
 */

@ReleaseCompileToNativeCalls
@UtilityClass
public class FileUtility {
	
	public String readInputStream(InputStream inputStream) {
        StringBuilder stringBuilder = new StringBuilder();

        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = bufferedReader.readLine()) != null)
                stringBuilder.append(line).append('\n');

        } catch (Exception e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }
	
	public String readFile(File file) {
		StringBuilder stringBuilder = new StringBuilder();

		try {
			FileInputStream fileInputStream = new FileInputStream(file);
			@Cleanup
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));
			String line;
			while ((line = bufferedReader.readLine()) != null)
				stringBuilder.append(line).append('\n');

		} catch (Exception e) {
			e.printStackTrace();
		}
		return stringBuilder.toString();
	}

	public static void writeFile(File file, String content) {
		try {
			FileWriter fw = new FileWriter(file);
			fw.write(content);
			fw.flush();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
}
