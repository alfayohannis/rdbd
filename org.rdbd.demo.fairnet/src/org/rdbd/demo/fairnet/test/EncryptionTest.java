package org.rdbd.demo.fairnet.test;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.junit.Test;
import org.rdbd.demo.fairnet.util.EncryptionUtil;

/***
 * A class to test the encryption and decryption of EncryptionUtil.java
 * @author Alfa Yohannis
 *
 */
public class EncryptionTest {

	private KeyPair pair;
	private KeyFactory kf;

	public EncryptionTest() throws NoSuchAlgorithmException {
		KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(EncryptionUtil.ALGORITHM);
		kf = KeyFactory.getInstance(EncryptionUtil.ALGORITHM);
		keyPairGen.initialize(EncryptionUtil.KEY_LENGTH);

		pair = keyPairGen.generateKeyPair();
	}

	/***
	 * Test encryption and decryption with private and public keys are firstly saved to text files and then re-loaded to perform encryption and decryption.  
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 * @throws InvalidKeyException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 * @throws NoSuchPaddingException
	 */
	@Test
	public void testDecryptionWithKeysFromFiles() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException {
		String message = "Foo Bar!";

		PublicKey publicKey = pair.getPublic();
		PrivateKey privateKey = pair.getPrivate();

		String privateKeyString = Base64.getEncoder().encodeToString(privateKey.getEncoded());
		String publicKeyString = Base64.getEncoder().encodeToString(publicKey.getEncoded());

		String privateKeyPath = "keys" + File.separator + "private.key";
		String publicKeyPath = "keys" + File.separator + "public.key";
		Files.write(Paths.get(privateKeyPath), privateKeyString.getBytes());
		Files.write(Paths.get(publicKeyPath), publicKeyString.getBytes());

		String loadedPrivateKeyString = new String(Files.readAllBytes(Paths.get(privateKeyPath)));
		String loadedPublicKeyString = new String(Files.readAllBytes(Paths.get(publicKeyPath)));
		
		byte[] privateKeyBytes = Base64.getDecoder().decode(loadedPrivateKeyString);
		PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
		PrivateKey loadedPrivateKey = kf.generatePrivate(privateKeySpec);
		
		byte[] publicKeyBytes = Base64.getDecoder().decode(loadedPublicKeyString);
		X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
		PublicKey loadedPublicKey = kf.generatePublic(publicKeySpec);
		
		
		String encryptedMessage = EncryptionUtil.encrypt(message, loadedPrivateKey);
		String decryptedMessage = EncryptionUtil.decrypt(encryptedMessage, loadedPublicKey);

		assertEquals(privateKeyString, loadedPrivateKeyString);
		assertEquals(publicKeyString, loadedPublicKeyString);
		assertEquals(message, decryptedMessage);
	}

	/***
	 * Test encryption and decryption with a short message input
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeyException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 * @throws UnsupportedEncodingException
	 * @throws NoSuchPaddingException
	 * @throws IOException
	 */
	@Test
	public void testShortTextDecryption()
			throws NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException,
			UnsupportedEncodingException, NoSuchPaddingException, IOException {

		String message = "Foo Bar!";

		PublicKey publicKey = pair.getPublic();
		PrivateKey privateKey = pair.getPrivate();

		String privateKeyString = Base64.getEncoder().encodeToString(privateKey.getEncoded());
		String publicKeyString = Base64.getEncoder().encodeToString(publicKey.getEncoded());

		// original
		System.out.println("Private key: " + privateKeyString);
		System.out.println("Public key: " + publicKeyString);
		System.out.println("Original Message: " + message);

		// encrypt with private key
		String encryptedMessage = EncryptionUtil.encrypt(message, privateKey);
		System.out.println("Encrypted Message: " + encryptedMessage);

		// decrypt with public key
		String decryptedMessage = EncryptionUtil.decrypt(encryptedMessage, publicKey);
		System.out.println("Decrypted Message: " + decryptedMessage);

		assertEquals(message, decryptedMessage);
	}

	/***
	 * Test encryption and decryption with a long message input. Message bytes are streamed and then decrypted per 64 bytes.  
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeyException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 * @throws UnsupportedEncodingException
	 * @throws NoSuchPaddingException
	 * @throws IOException
	 */
	@Test
	public void testLongDecryption() throws NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException,
			BadPaddingException, UnsupportedEncodingException, NoSuchPaddingException, IOException {

		String message = "Not all sequences of bytes can be mapped to characters in UTF-16. "
				+ "Not all sequences of bytes can be mapped to characters in UTF-16. "
				+ "Not all sequences of bytes can be mapped to characters in UTF-16. ";

		PublicKey publicKey = pair.getPublic();
		PrivateKey privateKey = pair.getPrivate();

		String privateKeyString = Base64.getEncoder().encodeToString(privateKey.getEncoded());
		String publicKeyString = Base64.getEncoder().encodeToString(publicKey.getEncoded());

		// original
		System.out.println("Private key: " + privateKeyString);
		System.out.println("Public key: " + publicKeyString);
		System.out.println("Original Message: " + message);

		// encrypt with private key
		String encryptedMessage = EncryptionUtil.encrypt(message, privateKey);
		System.out.println("Encrypted Message: " + encryptedMessage);

		// decrypt with public key
		String decryptedMessage = EncryptionUtil.decrypt(encryptedMessage, publicKey);
		System.out.println("Decrypted Message: " + decryptedMessage);

		assertEquals(message, decryptedMessage);
	}
}
