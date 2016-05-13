package frequentFlyer.flight_diary.auth;

import io.vertx.core.VertxException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.lang3.RandomStringUtils;

/**
 * 
 * ApplicationSHAEncoder
 * 
 * This module does encryption using SHA-512 algorithm
 * 
 * @author Sasa Radovanovic
 *
 */
public class ApplicationSHAEncoder {


	public ApplicationSHAEncoder() {
	}
	

    private static final char[] HEX_CHARS = "0123456789ABCDEF".toCharArray();

    public static String getSalt() {
    	return RandomStringUtils.randomAlphanumeric(64).toUpperCase();
    }
     
    
    public static String bytesToHex(byte[] bytes) {
      char[] chars = new char[bytes.length * 2];
      for (int i = 0; i < bytes.length; i++) {
        int x = 0xFF & bytes[i];
        chars[i * 2] = HEX_CHARS[x >>> 4];
        chars[1 + i * 2] = HEX_CHARS[0x0F & x];
      }
      return new String(chars);
    }

    public static String computeHash(String password, String salt) {
        try {
          MessageDigest md = MessageDigest.getInstance("SHA-512");
          String concat = (salt == null ? "" : salt) + password;
          byte[] bHash = md.digest(concat.getBytes(StandardCharsets.UTF_8));
          return bytesToHex(bHash);
        } catch (NoSuchAlgorithmException e) {
          throw new VertxException(e);
        }
      }
    
}
