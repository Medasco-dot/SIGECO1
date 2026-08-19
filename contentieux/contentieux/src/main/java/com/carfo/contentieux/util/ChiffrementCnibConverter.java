package com.carfo.contentieux.util;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Chiffre le numéro CNIB (pièce d'identité nationale) au repos, au niveau du champ, avec
 * AES-256-GCM : même en cas d'accès direct à la base (fuite de dump, requête non prévue),
 * la valeur en clair n'est jamais stockée. Le chiffrement/déchiffrement est transparent pour
 * le reste de l'application (JPA appelle ce convertisseur automatiquement).
 */
@Converter
@Component
public class ChiffrementCnibConverter implements AttributeConverter<String, String> {

    private static final int GCM_TAG_LENGTH_BITS = 128;
    private static final int GCM_IV_LENGTH_BYTES = 12;

    private static byte[] cle;

    @Value("${app.encryption.key}")
    public void setCle(String cleBase64) {
        ChiffrementCnibConverter.cle = Base64.getDecoder().decode(cleBase64);
    }

    @Override
    public String convertToDatabaseColumn(String clair) {
        if (clair == null || clair.isBlank()) {
            return clair;
        }
        try {
            byte[] iv = new byte[GCM_IV_LENGTH_BYTES];
            new SecureRandom().nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(cle, "AES"), new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));
            byte[] chiffre = cipher.doFinal(clair.getBytes(StandardCharsets.UTF_8));

            ByteBuffer buffer = ByteBuffer.allocate(iv.length + chiffre.length);
            buffer.put(iv).put(chiffre);
            return Base64.getEncoder().encodeToString(buffer.array());
        } catch (Exception e) {
            throw new IllegalStateException("Echec du chiffrement du numero CNIB", e);
        }
    }

    @Override
    public String convertToEntityAttribute(String stocke) {
        if (stocke == null || stocke.isBlank()) {
            return stocke;
        }
        try {
            byte[] donnees = Base64.getDecoder().decode(stocke);
            byte[] iv = new byte[GCM_IV_LENGTH_BYTES];
            ByteBuffer buffer = ByteBuffer.wrap(donnees);
            buffer.get(iv);
            byte[] chiffre = new byte[buffer.remaining()];
            buffer.get(chiffre);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(cle, "AES"), new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));
            return new String(cipher.doFinal(chiffre), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("Echec du dechiffrement du numero CNIB", e);
        }
    }
}
