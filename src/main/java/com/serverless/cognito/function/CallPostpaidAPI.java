package com.serverless.cognito.function;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.cognitoidentity.AmazonCognitoIdentity;
import com.amazonaws.services.cognitoidentity.AmazonCognitoIdentityClientBuilder;
import com.amazonaws.services.cognitoidentity.model.Credentials;
import com.amazonaws.services.cognitoidentity.model.GetCredentialsForIdentityRequest;
import com.amazonaws.services.cognitoidentity.model.GetCredentialsForIdentityResult;
import com.amazonaws.services.cognitoidentity.model.GetIdRequest;
import com.amazonaws.services.cognitoidentity.model.GetIdResult;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.model.AdminInitiateAuthRequest;
import com.amazonaws.services.cognitoidp.model.AdminInitiateAuthResult;
import com.amazonaws.services.cognitoidp.model.AuthFlowType;
import com.amazonaws.services.cognitoidp.model.AuthenticationResultType;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.gson.Gson;
import com.serverless.cognito.configuration.AuthenticationHelper;
import com.serverless.cognito.configuration.CognitoIdentity;
import com.serverless.cognito.configuration.CognitoSecretHash;
import com.serverless.cognito.configuration.Settings;
import com.serverless.cognito.model.AccountLogin;

public class CallPostpaidAPI implements RequestHandler<AccountLogin, Credentials> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CallPostpaidAPI.class);

	private static final Gson gson = new Gson();

	@Override
	public Credentials handleRequest(AccountLogin input, Context context) {

		Credentials credentials = loginByUserSRPAuth(input);

		return credentials;
	}

	private Credentials loginByUserSRPAuth(AccountLogin input) {

		AuthenticationHelper authenticationHelper = new AuthenticationHelper(Settings.getUserPoolId(), Settings.getClientId(), Settings.getClientSecret(), Settings.getRegion());
		String idToken = authenticationHelper.performSRPAuthentication(input.getUsername(), input.getPassword());

		Credentials credentials = getTemporaryCredentials(input.getUsername(), idToken);

		return credentials;
	}

	private Credentials loginByAdminNoSRPAuth(AccountLogin input) {
		// initialize the Cognito identity client with a set
		// of anonymous AWS credentials

		AWSCognitoIdentityProvider cognitoClient = CognitoIdentity.getClient();
		HashMap<String, String> authParams = new HashMap<>();
		authParams.put("USERNAME", input.getUsername());
		authParams.put("PASSWORD", input.getPassword());

		if (StringUtils.isNotBlank(Settings.getClientSecret())) {
			String hash = CognitoSecretHash.getSecretHash(input.getUsername(), Settings.getClientId(), Settings.getClientSecret());
			authParams.put("SECRET_HASH", hash);
		}

		LOGGER.info("ClientId: " + Settings.getClientId());
		LOGGER.info("ClientSecret: " + Settings.getClientSecret());
		LOGGER.info("UserPoolId: " + Settings.getUserPoolId());
		LOGGER.info("AuthParams: " + authParams);

		AdminInitiateAuthRequest adminInitiateAuthRequest = new AdminInitiateAuthRequest()
				.withClientId(Settings.getClientId())
				.withUserPoolId(Settings.getUserPoolId())
				.withAuthFlow(AuthFlowType.ADMIN_NO_SRP_AUTH).withAuthParameters(authParams);

		AdminInitiateAuthResult adminInitiateAuthResult = cognitoClient.adminInitiateAuth(adminInitiateAuthRequest);

		if (adminInitiateAuthResult == null) {
			LOGGER.info("No result available");
			return null;
		}

		AuthenticationResultType authenticationResultType = adminInitiateAuthResult.getAuthenticationResult();
		LOGGER.info("Identity Provider: " + gson.toJson(authenticationResultType));
		Credentials credentials = getTemporaryCredentials(input.getUsername(), authenticationResultType.getIdToken());

		return credentials;
	}

	private Credentials getTemporaryCredentials(String userId, String idToken) {
		String secret = Settings.getClientSecret();

		if (StringUtils.isNotBlank(Settings.getClientSecret())) {
			secret = CognitoSecretHash.getSecretHash(userId, Settings.getClientId(), Settings.getClientSecret());
		}

		BasicAWSCredentials appCredentials = new BasicAWSCredentials(Settings.getClientId(), secret);

		AmazonCognitoIdentity identityClient = AmazonCognitoIdentityClientBuilder
				.standard()
				.withCredentials(new AWSStaticCredentialsProvider(appCredentials))
				.withRegion(Settings.getRegion())
				.build();

		Map<String, String> providerTokens = new HashMap<>();
		String region = Settings.getRegion();
		String userPoolId = Settings.getUserPoolId();
		String providerName = "cognito-idp." + region + ".amazonaws.com/" + userPoolId;
		providerTokens.put(providerName, idToken);

		GetIdResult idResult = getId(identityClient, providerTokens);

		String identityId = idResult.getIdentityId();
		LOGGER.info("User identity id: " + identityId);

		Credentials credentials = getCredentialsForIdentity(identityClient, providerTokens, identityId);

		LOGGER.info("Credentials: " + gson.toJson(credentials));

		return credentials;
	}

	private Credentials getCredentialsForIdentity(AmazonCognitoIdentity identityClient, Map<String, String> providerTokens, String identityId) {
		GetCredentialsForIdentityRequest identityRequest = new GetCredentialsForIdentityRequest();
		identityRequest.setIdentityId(identityId);
		identityRequest.setLogins(providerTokens);

		GetCredentialsForIdentityResult result = identityClient.getCredentialsForIdentity(identityRequest);

		return result.getCredentials();
	}

	private GetIdResult getId(AmazonCognitoIdentity identityClient, Map<String, String> providerTokens) {
		GetIdRequest idRequest = new GetIdRequest();
		idRequest.setAccountId(Settings.getAWSAccountId());
		idRequest.setIdentityPoolId(Settings.getIdentityPoolId());
		idRequest.setLogins(providerTokens);
		return identityClient.getId(idRequest);
	}

}
