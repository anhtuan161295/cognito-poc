package com.serverless.cognito.configuration;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProviderClient;

public class CognitoIdentity {

	private static AWSCognitoIdentityProvider cognitoClient = null;
	
	private CognitoIdentity() {
	}
	
	public static AWSCognitoIdentityProvider getClient() {

		if (cognitoClient == null) {
			cognitoClient = AWSCognitoIdentityProviderClient.builder()
					.withCredentials(new AWSStaticCredentialsProvider(
							new BasicAWSCredentials(Settings.getAWSAccessKey(), Settings.getAWSSecretkey())))
					.withRegion(Regions.AP_SOUTHEAST_1).build();
		}
		return cognitoClient;
	}
}
