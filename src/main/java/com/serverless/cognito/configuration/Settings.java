package com.serverless.cognito.configuration;

public class Settings {


	private Settings() {
	}

	public static String getUserPoolId() {
		return System.getenv("USER_POOL_ID");
	}

	public static String getClientId() {
		return System.getenv("CLIENT_ID");
	}

	public static String getClientSecret() {
		return System.getenv("CLIENT_SECRET");
	}

	public static String getAWSAccessKey() {
		return System.getenv("MY_AWS_ACCESS_KEY");
	}

	public static String getAWSSecretkey() {
		return System.getenv("MY_AWS_SECRET_KEY");
	}

	public static String getIdentityPoolId() {
		return System.getenv("IDENTITY_POOL_ID");
	}

	public static String getAWSAccountId() {
		return System.getenv("AWS_ACCOUNT_ID");
	}

	public static String getRegion() {
		return System.getenv("REGION");
	}

	public static String getPostpaidFunctionName() {
		return System.getenv("POSTPAID_FUNCTION");
	}

	public static String getPrepaidFunctionName() {
		return System.getenv("PREPAID_FUNCTION");
	}

	public static String getCustomRoleARN() {
		return System.getenv("CUSTOM_ROLE_ARN");
	}

	public static String getEndpoint() {
		return System.getenv("ENDPOINT");
	}


}
