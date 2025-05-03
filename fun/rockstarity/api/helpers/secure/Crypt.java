package fun.rockstarity.api.helpers.secure;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import fun.rockstarity.api.secure.Debugger;
import ru.kotopushka.antiautistleak.obfuscator.includes.annotations.compile.ReleaseCompileToNativeCalls;

/**
 * @author ConeTin
 * @since 3 РґРµРє. 2023 Рі.
 */

@ReleaseCompileToNativeCalls
public class Crypt {
	
	private static byte[] key;
	private static SecretKeySpec secretKey;
	
	public static String encrypt(String strToEncrypt) 
    {
        try
        {
            setKey("PassWorld");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            
            return Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes("UTF-8")));
        } 
        catch (Exception e) 
        {
        	Debugger.print(e);
        }
        return null;
    }
 
    public static String decrypt(String strToDecrypt) 
    {
        try
        {
            setKey("PassWorld");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            
            return new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)));
        } 
        catch (Exception e) 
        {
        	Debugger.print(e);
        }
        return null;
    }
    
    public static void setKey(String myKey) 
    {
        MessageDigest sha = null;
        try {
            key = myKey.getBytes("UTF-8");
            sha = MessageDigest.getInstance("SHA-1");
            key = sha.digest(key);
            key = Arrays.copyOf(key, 16); 
            secretKey = new SecretKeySpec(key, "AES");
        } 
        catch (NoSuchAlgorithmException e) {
        	Debugger.print(e);
        } 
        catch (UnsupportedEncodingException e) {
        	Debugger.print(e);
        }
    }
	
}
