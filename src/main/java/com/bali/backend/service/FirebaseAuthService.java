package com.bali.backend.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Verifies Firebase ID tokens received from the mobile client.
 * Extracts the Firebase UID and phone number from verified tokens.
 */
@Service
public class FirebaseAuthService {

    private static final Logger logger = LoggerFactory.getLogger(FirebaseAuthService.class);

    /**
     * Verifies a Firebase ID token and returns the decoded token.
     *
     * @param idToken the Firebase ID token from the client
     * @return FirebaseToken containing uid and claims
     * @throws FirebaseAuthException if the token is invalid or expired
     */
    public FirebaseToken verifyIdToken(String idToken) throws FirebaseAuthException {
        FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
        logger.info("Verified Firebase token for UID: {}", decodedToken.getUid());
        return decodedToken;
    }

    /**
     * Extract phone number from a verified Firebase token.
     */
    public String getPhoneNumber(FirebaseToken token) {
        return (String) token.getClaims().get("phone_number");
    }
}
