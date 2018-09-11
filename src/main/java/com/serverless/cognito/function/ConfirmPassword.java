package com.serverless.cognito.function;

import java.util.HashMap;
import java.util.Map;

import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.model.AdminInitiateAuthRequest;
import com.amazonaws.services.cognitoidp.model.AdminInitiateAuthResult;
import com.amazonaws.services.cognitoidp.model.AdminRespondToAuthChallengeRequest;
import com.amazonaws.services.cognitoidp.model.AdminRespondToAuthChallengeResult;
import com.amazonaws.services.cognitoidp.model.AuthFlowType;
import com.amazonaws.services.cognitoidp.model.ChallengeNameType;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.util.StringUtils;
import com.serverless.cognito.configuration.CognitoIdentity;
import com.serverless.cognito.configuration.Settings;
import com.serverless.cognito.model.AccountLogin;

public class ConfirmPassword implements RequestHandler<AccountLogin, String>{
	
	public String confirm(String username,String password, String session){
		 String idToken = null;
		 Map<String,String> challengeResponses = new HashMap<>();
		 challengeResponses.put("USERNAME", username);
		 challengeResponses.put("PASSWORD", password);
		 challengeResponses.put("NEW_PASSWORD", password);
		 AdminRespondToAuthChallengeRequest finalRequest = new AdminRespondToAuthChallengeRequest()
		     .withChallengeName(ChallengeNameType.NEW_PASSWORD_REQUIRED)
		     .withChallengeResponses(challengeResponses)
		     .withClientId(Settings.getClientId())
		     .withUserPoolId(Settings.getUserPoolId())
		     .withSession(session);
		 AdminRespondToAuthChallengeResult challengeResponse = CognitoIdentity.getClient().adminRespondToAuthChallenge(finalRequest);
		 if(StringUtils.isNullOrEmpty(challengeResponse.getChallengeName())){
		   idToken = challengeResponse.getAuthenticationResult().getIdToken();
		 }
		 return idToken;
		}

		@Override
		public String handleRequest(AccountLogin input, Context context) {
			
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
			
			return confirm(input.getUsername(), input.getPassword(), result.getSession());			
		}
}
