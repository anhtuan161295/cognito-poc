package com.serverless.cognito.function;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

public class Prepaid implements RequestHandler<Object, String> {

	@Override
	public String handleRequest(Object input, Context context) {
		return "This is Prepaid.";
	}
}
