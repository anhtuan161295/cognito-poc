package com.serverless.cognito.configuration;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.util.Base64;
import com.amazonaws.util.StringUtils;

public class CognitoSecretHash {

	private static final Logger LOGGER = LoggerFactory.getLogger(CognitoSecretHash.class);

	private final static String HMAC_SHA_256 = "HmacSHA256";

	/**
	 * Generates secret hash. Uses HMAC SHA256.
	 *
	 * @param userId REQUIRED: User ID
	 * @param clientId REQUIRED: Client ID
	 * @param clientSecret REQUIRED: Client secret
	 * @return secret hash as a {@code String}, {@code null } if {@code clientSecret is null}
	 */
	public static String getSecretHash(String userId, String clientId, String clientSecret) {
		String result = null;

		// Arguments userId and clientId have to be not-null.
		if (userId == null) {
			LOGGER.error("user ID cannot be null");
			return null;
		}

		if (clientId == null) {
			LOGGER.error("client ID cannot be null");
			return null;
		}

		// Return null as secret hash if clientSecret is null.
		if (clientSecret == null) {
			return null;
		}

		SecretKeySpec signingKey = new SecretKeySpec(clientSecret.getBytes(StringUtils.UTF8),
				HMAC_SHA_256);

		try {
			Mac mac = Mac.getInstance(HMAC_SHA_256);
			mac.init(signingKey);
			mac.update(userId.getBytes(StringUtils.UTF8));
			byte[] rawHmac = mac.doFinal(clientId.getBytes(StringUtils.UTF8));
			result = new String(Base64.encode(rawHmac));
		} catch (Exception e) {
			LOGGER.error("errors in HMAC calculation");
		}

		return result;
	}
}
