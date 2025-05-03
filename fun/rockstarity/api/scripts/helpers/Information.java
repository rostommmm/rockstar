package fun.rockstarity.api.scripts.helpers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.Enumeration;
import java.util.Map;

import lombok.Cleanup;

public class Information {
	public static String getHWID() {
		StringBuilder sb = new StringBuilder();
		/*
		String hwid = "";
		try {
			Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
			StringBuilder macAddress = new StringBuilder();
			while (networkInterfaces.hasMoreElements()) {
				NetworkInterface networkInterface = networkInterfaces.nextElement();
				byte[] mac = networkInterface.getHardwareAddress();
				if (mac != null) {
					for (int i = 0; i < mac.length; i++) {
						macAddress.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
					}
				}
			}
			hwid = macAddress.toString();
		} catch (SocketException e) {
			e.printStackTrace();
		}
		*/
		try {
			MessageDigest messageDigest = MessageDigest.getInstance("MD5");
			byte[] bytes = messageDigest.digest(generateHWID().getBytes());

			for (byte dig : bytes) {
				sb.append(Integer.toString((dig & 0xFF) + 256, 16).substring(1));
			}
		} catch (Exception e) {
		}
		return sb.toString();
	}
	
	public static String generateHWID() {
        
        return "Disk Serial Number: " + getInfoWithCmd("wmic diskdrive get serialnumber") + "\n" + // Серийный номер диска
        		"Disk Name: " + getInfoWithCmd("wmic diskdrive get Caption") + "\n" + // Имя диска
        		"Os Architecture: " + System.getProperty("os.arch") + "\n" + // Архитектура операционной системы
        		"Product id: " + getInfoWithCmd("wmic csproduct get uuid") + "\n" + // Айди продукта (Скорее всего ключ винды)
        		"Processor id: " + getInfoWithCmd("wmic cpu get ProcessorId") + "\n" + // Инфа о процессоре
        		"Processor name: " + getInfoWithCmd("wmic CPU get NAME"); // Имя процессора
    }
	
	public static String getInfoWithCmd(String info) {
		try {
            Process process = Runtime.getRuntime().exec(info);
            process.getOutputStream().close();
            @Cleanup
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            StringBuilder hwid = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    hwid.append(line);
                }
            }

            reader.close();
            return hwid.toString();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(0);
            return null;
        }
	}
	
	public static String getParamsString(final Map<String, String> params) {
		final StringBuilder result = new StringBuilder();

		params.forEach((name, value) -> {
			try {
				result.append(URLEncoder.encode(name, "UTF-8"));
				result.append('=');
				result.append(URLEncoder.encode(value, "UTF-8"));
				result.append('&');
			} catch (final UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		});

		final String resultString = result.toString();
		return !resultString.isEmpty() ? resultString.substring(0, resultString.length() - 1) : resultString;
	}
}