package fun.rockstarity.api.helpers.secure;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import ru.kotopushka.antiautistleak.obfuscator.includes.annotations.compile.ReleaseCompileToNativeCalls;
import ru.kotopushka.j2c.sdk.annotations.NativeInclude;
import ru.kotopushka.j2c.sdk.annotations.VMProtect;
import ru.kotopushka.j2c.sdk.enums.VMProtectType;

/**
 * @author ConeTin
 * @since 22 июн. 2024 г.
 */

public class KeyGeneration {
	
	/*
	public static void main(String[] args) {
		//System.out.println(Web.read("https://rockstar.moscow/api/v1/premium/utility/crypt/sync.php"));
		//System.out.println(staticDecrypt(Web.read("https://rockstar.moscow/api/v1/premium/utility/crypt/sync.php").trim()));
		//System.out.println(encrypt("pasta"));
		initialize();
		//System.out.println(decrypt(Web.read("https://rockstar.moscow/api/v1/premium/utility/test.php").trim()));
	}
	*/
	
	private static long startTime, timeDiff;
	
	public static String encrypt(String data) {
        try {
            byte[] key = getAdjustedKey();
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            byte[] iv = secret().getBytes("UTF-8");
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
            SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");

            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
            byte[] encrypted = cipher.doFinal(data.getBytes("UTF-8"));

            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            return null;
        }
    }

	public static String decrypt(String encryptedData) throws Exception {
        //try {
            byte[] key = getAdjustedKey();
            byte[] iv = secret().getBytes("UTF-8");
            byte[] decoded = Base64.getDecoder().decode(encryptedData.getBytes("UTF-8"));

            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
            SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
            byte[] decrypted = cipher.doFinal(decoded);

            return new String(decrypted, "UTF-8");
        //} catch (Exception e) {
        //	e.printStackTrace();
        //    return null;
        //}
    }
    
    public static String staticDecrypt(String encryptedData) {
        try {
            byte[] key = getStaticKey();
            byte[] iv = staticSecret().getBytes("UTF-8");
            byte[] decoded = Base64.getDecoder().decode(encryptedData.getBytes("UTF-8"));

            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
            SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
            byte[] decrypted = cipher.doFinal(decoded);

            return new String(decrypted, "UTF-8");
        } catch (Exception e) {
        	e.printStackTrace();
            return null;
        }
    }

    public static void initialize() {
//    	String a = staticDecrypt(Web.read("https://rockstar.moscow/api/v1/premium/utility/crypt/sync.php").trim());
    	//System.out.println(a);
    	startTime = System.currentTimeMillis();
    	ZonedDateTime nowInMoscow = ZonedDateTime.now(ZoneId.of("Europe/Moscow"));
        ZonedDateTime epochInMoscow = ZonedDateTime.ofInstant(Instant.ofEpochSecond(0), ZoneId.of("Europe/Moscow"));
        long sec = Duration.between(epochInMoscow, nowInMoscow).toSeconds();
        timeDiff = sec - startTime;
    }
    
    private static String secret() {
    	ZonedDateTime nowInMoscow = ZonedDateTime.now(ZoneId.of("Europe/Moscow"));
        ZonedDateTime epochInMoscow = ZonedDateTime.ofInstant(Instant.ofEpochSecond(0), ZoneId.of("Europe/Moscow"));
        long sec = Duration.between(epochInMoscow, nowInMoscow).toSeconds() - timeDiff;
        long time = Math.round(sec / 60D);
         
    	String keyStr = "r30cka6f" + time;
    	//System.out.println(keyStr);
    	
    	return keyStr;
    }

    private static String staticSecret() {
    	return "71s3k9sc4f3b4b8a";
    }
    
    private static byte[] getStaticKey() {
    	try {
	        byte[] key = staticSecret().getBytes("UTF-8");
	        byte[] adjustedKey = new byte[32];
	        for (int i = 0; i < adjustedKey.length; i++) {
	            adjustedKey[i] = i < key.length ? key[i] : 0;
	        }
	        return adjustedKey;
	    } catch (Exception e) {
	        return null;
	    }
    }

    private static byte[] getAdjustedKey() {
    	try {
	        byte[] key = secret().getBytes("UTF-8");
	        byte[] adjustedKey = new byte[32];
	        for (int i = 0; i < adjustedKey.length; i++) {
	            adjustedKey[i] = i < key.length ? key[i] : 0;
	        }
	        return adjustedKey;
	    } catch (Exception e) {
	        return null;
	    }
    }
    
}
