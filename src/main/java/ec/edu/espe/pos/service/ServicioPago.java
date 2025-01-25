package ec.edu.espe.pos.service;

import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.util.Base64;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.nio.charset.StandardCharsets;
import javax.crypto.spec.GCMParameterSpec;

import ec.edu.espe.pos.exception.EncryptionException;

@Service
@Transactional
public class ServicioPago {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int TAG_LENGTH_BIT = 128;
    private static final int IV_LENGTH_BYTE = 12;
    private final SecretKey secretKey;

    public ServicioPago() {
        try {
            this.secretKey = generateKey();
        } catch (NoSuchAlgorithmException e) {
            throw new EncryptionException("AES-256", "Inicialización de clave");
        }
    }

    public String encryptCardData(String cardNumber, String expiryDate, String cvv) {
        try {
            byte[] iv = generateIv();
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec);

            // Combine data with delimiters
            String sensitiveData = String.format("%s|%s|%s", cardNumber, expiryDate, cvv);
            byte[] encryptedData = cipher.doFinal(sensitiveData.getBytes(StandardCharsets.UTF_8));
            
            // Combine IV and encrypted data
            byte[] combined = new byte[iv.length + encryptedData.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encryptedData, 0, combined, iv.length, encryptedData.length);
            
            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            throw new EncryptionException("datos de tarjeta", "encriptación");
        }
    }

    private SecretKey generateKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(256);
        return keyGenerator.generateKey();
    }

    private byte[] generateIv() {
        byte[] iv = new byte[IV_LENGTH_BYTE];
        new SecureRandom().nextBytes(iv);
        return iv;
    }
}