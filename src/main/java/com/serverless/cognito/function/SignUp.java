package com.serverless.cognito.function;

import com.amazonaws.services.apigateway.model.TooManyRequestsException;
import com.amazonaws.services.cognitoidp.model.AdminCreateUserRequest;
import com.amazonaws.services.cognitoidp.model.AttributeType;
import com.amazonaws.services.cognitoidp.model.DeliveryMediumType;
import com.amazonaws.services.cognitoidp.model.UsernameExistsException;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.serverless.cognito.configuration.CognitoIdentity;
import com.serverless.cognito.configuration.Settings;
import com.serverless.cognito.model.Account;

public class SignUp implements RequestHandler<Account, String>{
	
	@Override
	public String handleRequest(Account input, Context context) {
		try
		{
		    AdminCreateUserRequest cognitoRequest = new AdminCreateUserRequest()
		            .withUserPoolId(Settings.getUserPoolId())
		            .withUsername(input.getUsername())
		            .withTemporaryPassword(input.getPassword())
		            .withUserAttributes(
		                    new AttributeType()
		                        .withName("email")
		                        .withValue(input.getEmail()),
		                    new AttributeType()
		                        .withName("email_verified")
		                        .withValue("true"))
		            .withDesiredDeliveryMediums(DeliveryMediumType.EMAIL)
		            .withForceAliasCreation(Boolean.FALSE);

		    CognitoIdentity.getClient().adminCreateUser(cognitoRequest);
		    
		}
		catch (UsernameExistsException ex)
		{
		    context.getLogger().log("user already exists: " + ex.getMessage());
		}
		catch (TooManyRequestsException ex)
		{
			 context.getLogger().log("caught TooManyRequestsException, delaying then retrying");
		}
		return null;
	}

}

