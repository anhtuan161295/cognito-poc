package com.serverless.cognito.function;

import java.util.HashMap;

import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.model.AdminInitiateAuthRequest;
import com.amazonaws.services.cognitoidp.model.AdminInitiateAuthResult;
import com.amazonaws.services.cognitoidp.model.AuthFlowType;
import com.amazonaws.services.cognitoidp.model.AuthenticationResultType;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.gson.Gson;
import com.serverless.cognito.configuration.CognitoIdentity;
import com.serverless.cognito.configuration.Settings;
import com.serverless.cognito.model.AccountLogin;

public class SignIn implements RequestHandler<AccountLogin, AuthenticationResultType> {

	
	@Override
	public AuthenticationResultType handleRequest(AccountLogin input, Context context) {

		// initialize the Cognito identity client with a set
		// of anonymous AWS credentials

		AWSCognitoIdentityProvider cognitoClient = CognitoIdentity.getClient();
		HashMap<String, String> authParams = new HashMap<>();
		authParams.put("USERNAME", input.getUsername());
		authParams.put("PASSWORD", input.getPassword());
		
		StringBuilder contentLogs = new StringBuilder();
		contentLogs.append("ClientId ").append(Settings.getClientId()).append(" - ")
		.append("UserPoolId: ").append(" - ")
				.append(AuthFlowType.USER_SRP_AUTH).append("-")
				.append(" AuthoParams: ").append(authParams);
		context.getLogger().log(contentLogs.toString());
		
		AdminInitiateAuthRequest adminInitiateAuthRequest = new AdminInitiateAuthRequest()
				.withClientId(Settings.getClientId())
				.withUserPoolId(Settings.getUserPoolId())
				.withAuthFlow(AuthFlowType.ADMIN_NO_SRP_AUTH).withAuthParameters(authParams);
		
		AdminInitiateAuthResult result = cognitoClient.adminInitiateAuth(adminInitiateAuthRequest);
		
		if (result != null) {
			System.out.println("AdminInitiateAuthResult:");
			context.getLogger().log(new Gson().toJson(result));
			result.getAuthenticationResult();
		} else {
			System.out.println("No result available");
		}

		return null;
	}


}
