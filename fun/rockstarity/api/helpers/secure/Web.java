package fun.rockstarity.api.helpers.secure;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HttpsURLConnection;


import fun.rockstarity.api.helpers.render.Converter;
import fun.rockstarity.api.secure.Debugger;
import fun.rockstarity.api.secure.nativeapi.NativeHelper;
import lombok.experimental.UtilityClass;
import ru.kotopushka.antiautistleak.obfuscator.includes.annotations.compile.ReleaseCompileToNativeCalls;

/**
 * @author ConeTin
 * @since 3 дек. 2023 г.
 */

@ReleaseCompileToNativeCalls
@UtilityClass
public class Web {
	
	public String protectedRead(String url) {
        StringBuilder stringBuilder = new StringBuilder();

        try {
            HttpsURLConnection httpsClient = (HttpsURLConnection) new URL(url).openConnection();
            httpsClient.setRequestProperty("User-Agent", KeyGeneration.encrypt("RockAgent").replace("/", "").replace("=", "").replace("+", "") + "/1.0");
            httpsClient.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            httpsClient.setRequestProperty("Content-Length", Integer.toString("Крякер иди нахуй".getBytes().length));
            httpsClient.setRequestProperty("Content-Language", "en-US");
            httpsClient.setUseCaches(false);
            httpsClient.setDoOutput(true);
            
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpsClient.getInputStream(), "UTF-8"));
            String line;
            while ((line = bufferedReader.readLine()) != null)
                stringBuilder.append(line).append('\n');

            bufferedReader.close();
        } catch (Exception e) {
            Debugger.print(e);
        }

        return stringBuilder.toString();
    }
	
	public String read(String url) {
		StringBuilder stringBuilder = new StringBuilder();

		try {
//			HttpsURLConnection httpsClient = (HttpsURLConnection) new URL(url).openConnection();
//			httpsClient.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.157 Safari/537.36");
//			httpsClient.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
//			httpsClient.setRequestProperty("Content-Length", Integer.toString("Крякер иди нахуй".getBytes().length));
//			httpsClient.setRequestProperty("Content-Language", "en-US");
//			httpsClient.setUseCaches(false);
//			httpsClient.setDoOutput(true);

            String resourcePath = (url.startsWith("/assets/") ? "" : "/assets/minecraft/rockstar/") + url;
            InputStream inputStream = Web.class.getResourceAsStream(resourcePath);
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
			String line;
			while ((line = bufferedReader.readLine()) != null)
				stringBuilder.append(line).append('\n');

		} catch (Exception e) {
			//Debugger.print(e);
		}
		
		return stringBuilder.toString();
	}
	
	public boolean openWebpage(String url) {
		String os = System.getProperty("os.name").toLowerCase();

		try {
			if (os.contains("win")) {
				// Для Windows
				Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
			} else if (os.contains("mac")) {
				// Для macOS
				Runtime.getRuntime().exec("open " + url);
			} else if (os.contains("nix") || os.contains("nux") || os.contains("mac")) {
				// Для Linux и Unix
				Runtime.getRuntime().exec("xdg-open " + url);
			} else {
			}
		} catch (IOException e) {
		}
		return false;
	}
	
	public String protectedPostRequest(Map<String, String> params, String url) {
		try {
            HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(url).openConnection();
            httpURLConnection.setRequestProperty("User-Agent", KeyGeneration.encrypt("RockAgent").replace("/", "").replace("=", "").replace("+", "") + "/1.0");
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            byte[] postData = getParamsString(params).getBytes(StandardCharsets.UTF_8);
            httpURLConnection.setRequestProperty("Content-Length", Integer.toString(postData.length));

            httpURLConnection.setUseCaches(false);
            httpURLConnection.setDoOutput(true);

            try (DataOutputStream wr = new DataOutputStream(httpURLConnection.getOutputStream())) {
                wr.write(postData);
            }

            StringBuilder response = new StringBuilder();
            try (BufferedReader buffer = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()))) {
                String line;
                while ((line = buffer.readLine()) != null) {
                    response.append(line);
                }
            }

            return response.toString();
        } catch (Exception e) {
        	Debugger.print(e);
        }
        return "";
    }
	
	public static void downloadFile(String fileUrl, String destinationPath) {
		try {
			URL url = new URL(fileUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.157 Safari/537.36");

            try (InputStream inputStream = connection.getInputStream();
                FileOutputStream outputStream = new FileOutputStream(destinationPath)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public String postRequest(Map<String, String> params, String url) {
		try {
            HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(url).openConnection();
            httpURLConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.157 Safari/537.36");
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            byte[] postData = getParamsString(params).getBytes(StandardCharsets.UTF_8);
            httpURLConnection.setRequestProperty("Content-Length", Integer.toString(postData.length));

            httpURLConnection.setUseCaches(false);
            httpURLConnection.setDoOutput(true);

            try (DataOutputStream wr = new DataOutputStream(httpURLConnection.getOutputStream())) {
                wr.write(postData);
            }

            StringBuilder response = new StringBuilder();
            try (BufferedReader buffer = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()))) {
                String line;
                while ((line = buffer.readLine()) != null) {
                    response.append(line);
                }
            }

            return response.toString();
        } catch (Exception e) {
        	Debugger.print(e);
        }
        return "";
    }
	
	public final Font getFont(String name, int style, int size) {
        Font font = null;

        try {
            String resourcePath = NativeHelper.getFontResource(name);

            try (InputStream inputStream = Web.class.getResourceAsStream(resourcePath)) {
                font = Font.createFont(Font.TRUETYPE_FONT, inputStream)
                        .deriveFont(style, size);
            }
        } catch (FontFormatException | IOException e) {
        	Debugger.print(e);
        }

        return font;
    }
	
	public final Font getFullFont(String link, int style, int size) {
        Font font = null;

        try {
            URL fontUrl = new URL(link);
            try (InputStream fontStream = fontUrl.openStream()) {
                font = Font.createFont(Font.TRUETYPE_FONT, fontStream)
                        .deriveFont(style, size);
            }
        } catch (FontFormatException | IOException e) {
        	Debugger.print(e);
        }

        return font;
    }
	
	public String getParamsString(final Map<String, String> params) {
		final StringBuilder result = new StringBuilder();

		params.forEach((name, value) -> {
			try {
				result.append(URLEncoder.encode(name, "UTF-8"));
				result.append('=');
				result.append(URLEncoder.encode(value, "UTF-8"));
				result.append('&');
			} catch (final UnsupportedEncodingException e) {
				Debugger.print(e);
			}
		});

		final String resultString = result.toString();
		return !resultString.isEmpty() ? resultString.substring(0, resultString.length() - 1) : resultString;
	}
	
}
