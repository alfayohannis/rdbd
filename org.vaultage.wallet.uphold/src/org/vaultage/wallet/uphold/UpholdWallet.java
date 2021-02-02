package org.vaultage.wallet.uphold;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.vaultage.wallet.Wallet;
import org.vaultage.wallet.WalletException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class UpholdWallet implements Wallet {

	WalletEnvironment walletEnvironment;
	private final String host;
	private final String sandboxOtpToken = "000000";
	public static final String API_HOST_PRODUCTION = "api.uphold.com";
	public static final String API_HOST_SANDBOX = "api-sandbox.uphold.com";
	public static final String HOST_PRODUCTION = "uphold.com";
	public static final String HOST_SANDBOX = "sandbox.uphold.com";

	private String clientId;
	private String clientSecret;
	private String username;
	private String password;

	private String type = "Uphold";
	private String name;
	private String billAddress;
	private String bank;
	private String bankAddress;
	private String currency = "USD";
	private String accountNumber;

	public UpholdWallet() {
		walletEnvironment = WalletEnvironment.SANDBOX;
		host = UpholdWallet.API_HOST_SANDBOX;
	}

	public UpholdWallet(WalletEnvironment walletEnvironment) {
		this.walletEnvironment = walletEnvironment;
		if (this.walletEnvironment == WalletEnvironment.SANDBOX) {
			host = UpholdWallet.API_HOST_SANDBOX;
		} else {
			host = UpholdWallet.API_HOST_PRODUCTION;
		}
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getClientSecret() {
		return clientSecret;
	}

	public void setClientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String authorise(String clientId, String scope, String state)
			throws URISyntaxException, IOException, ClientProtocolException, WalletException {

		URI uri = null;

		if (walletEnvironment == WalletEnvironment.SANDBOX) {
			uri = new URIBuilder() //
					.setScheme("https") //
					.setHost(HOST_SANDBOX) //
					.setPath("/authorize/" + clientId) //
					.setParameter("scope", scope) //
					.setParameter("state", state) //
					.build();
		} else {
			uri = new URIBuilder() //
					.setScheme("https") //
					.setHost(HOST_PRODUCTION) //
					.setPath("/authorize/" + clientId) //
					.setParameter("scope", scope) //
					.setParameter("state", state) //
					.build();
		}

		CloseableHttpClient httpClient = HttpClients.createDefault();

		HttpPost request = new HttpPost(uri);
		CloseableHttpResponse response = httpClient.execute(request);

		HttpEntity entity = response.getEntity();
		String json = EntityUtils.toString(entity);

		if (response.getStatusLine().getStatusCode() != 200) {
			throw new WalletException(response.getStatusLine().getStatusCode() + " "
					+ response.getStatusLine().getReasonPhrase() + " " + json);
		}

		httpClient.close();
		response.close();

		return json;
	}

	/***
	 * Get an access token to be used for further operation or transaction.
	 * 
	 * @throws IOException
	 * @throws ClientProtocolException
	 * @throws WalletException
	 */
	public String getAccessToken(String clientId, String clientSecret)
			throws IOException, WalletException {
		String auth = UpholdUtil.credential(clientId, clientSecret);

		String uri = "https://" + host + "/oauth2/token";

		HttpPost request = new HttpPost(uri);

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("grant_type", "client_credentials"));
		request.setEntity(new UrlEncodedFormEntity(params));

		request.addHeader(HttpHeaders.AUTHORIZATION, "Basic " + auth);
		request.addHeader("content-type", "application/x-www-form-urlencoded");

		CloseableHttpClient httpClient = HttpClients.createDefault();
		CloseableHttpResponse response = httpClient.execute(request);

		HttpEntity entity = response.getEntity();
		String json = EntityUtils.toString(entity);

		if (response.getStatusLine().getStatusCode() != 200) {
			throw new WalletException(response.getStatusLine().getStatusCode() + " "
					+ response.getStatusLine().getReasonPhrase() + " " + json);
		}

		httpClient.close();
		response.close();

		JsonNode jsonNode = (new ObjectMapper()).readTree(json);
		String accessToken = jsonNode.get("access_token").asText();
		return accessToken;
	}

	public JsonNode getUserInfo(String accessToken) throws IOException, WalletException {

		String uri = "https://" + host + "/v0/me";

		HttpGet request = new HttpGet(uri);

		request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);

		CloseableHttpClient httpClient = HttpClients.createDefault();
		CloseableHttpResponse response = httpClient.execute(request);

		HttpEntity entity = response.getEntity();
		String json = EntityUtils.toString(entity);

		if (response.getStatusLine().getStatusCode() != 200) {
			throw new WalletException(response.getStatusLine().getStatusCode() + " "
					+ response.getStatusLine().getReasonPhrase() + " " + json);
		}

		httpClient.close();
		response.close();

		JsonNode jsonNode = (new ObjectMapper()).readTree(json);
		return jsonNode;

	}

	public JsonNode getAuthenticationMethods(String username, String password)
			throws ClientProtocolException, IOException, WalletException {

		String auth = UpholdUtil.credential(username, password);

		String uri = "https://" + host + "/v0/me/authentication_methods";

		HttpGet request = new HttpGet(uri);

		request.addHeader(HttpHeaders.AUTHORIZATION, "Basic " + auth);

		CloseableHttpClient httpClient = HttpClients.createDefault();
		CloseableHttpResponse response = httpClient.execute(request);

		HttpEntity entity = response.getEntity();
		String json = EntityUtils.toString(entity);

		if (response.getStatusLine().getStatusCode() != 200) {
			throw new WalletException(response.getStatusLine().getStatusCode() + " "
					+ response.getStatusLine().getReasonPhrase() + " " + json);
		}

		httpClient.close();
		response.close();

		JsonNode jsonNode = (new ObjectMapper()).readTree(json);
		return jsonNode;
	}

	public String createNewPersonalAccessToken(String description, String username, String password, String otpMethodId,
			String otpToken) throws ParseException, IOException, WalletException {

		String auth = UpholdUtil.credential(username, password);
		// YWxmYS55b2hhbm5pc0BnbWFpbC5jb206TG9sb2wzeCE=
		// YWxmYS55b2hhbm5pc0BnbWFpbC5jb206TG9sb2wzeCE=

		if (otpToken != null) {
			otpToken = this.sandboxOtpToken;
		}
		String uri = "https://" + host + "/v0/me/tokens";

		HttpPost request = new HttpPost(uri);

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("description", description));
		request.setEntity(new UrlEncodedFormEntity(params));

		request.addHeader(HttpHeaders.AUTHORIZATION, "Basic " + auth);
		request.addHeader("content-type", "application/x-www-form-urlencoded");
		if (otpMethodId != null) {
			request.addHeader("OTP-Method-Id", otpMethodId);
			request.addHeader("OTP-Token", otpToken);
		}

		CloseableHttpClient httpClient = HttpClients.createDefault();
		CloseableHttpResponse response = httpClient.execute(request);

		HttpEntity entity = response.getEntity();
		String json = EntityUtils.toString(entity);

		if (response.getStatusLine().getStatusCode() != 200) {
			throw new WalletException(response.getStatusLine().getStatusCode() + " "
					+ response.getStatusLine().getReasonPhrase() + " " + json);
		}

		httpClient.close();
		response.close();

		JsonNode jsonNode = (new ObjectMapper()).readTree(json);
		String accessToken = jsonNode.get("accessToken").asText();
		return accessToken;
	}

	public JsonNode getPersonalAccessTokens(String personalAccessToken)
			throws ParseException, IOException, WalletException {

		String uri = "https://" + host + "/v0/me/tokens";

		HttpGet request = new HttpGet(uri);

		request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + personalAccessToken);

		CloseableHttpClient httpClient = HttpClients.createDefault();
		CloseableHttpResponse response = httpClient.execute(request);

		HttpEntity entity = response.getEntity();
		String json = EntityUtils.toString(entity);

		if (response.getStatusLine().getStatusCode() != 200) {
			throw new WalletException(response.getStatusLine().getStatusCode() + " "
					+ response.getStatusLine().getReasonPhrase() + " " + json);
		}

		httpClient.close();
		response.close();

		JsonNode jsonNode = (new ObjectMapper()).readTree(json);
//		String accessToken = jsonNode.get("access_token").asText();
		return jsonNode;
	}

	public JsonNode getAccounts(String accessToken) throws ClientProtocolException, IOException, WalletException {

		String uri = "https://" + host + "/v0/me/cards";

		HttpGet request = new HttpGet(uri);

		request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);

		CloseableHttpClient httpClient = HttpClients.createDefault();
		CloseableHttpResponse response = httpClient.execute(request);

		HttpEntity entity = response.getEntity();
		String json = EntityUtils.toString(entity);

		if (response.getStatusLine().getStatusCode() != 200) {
			throw new WalletException(response.getStatusLine().getStatusCode() + " "
					+ response.getStatusLine().getReasonPhrase() + " " + json);
		}

		httpClient.close();
		response.close();

		JsonNode jsonNode = (new ObjectMapper()).readTree(json);
//		System.out.println(jsonNode.toPrettyString());
		return jsonNode;
	}

	public JsonNode transfer(String originCardId, String destinationCardId, String currency, double amount,
			String accessToken, String message, String reference) throws WalletException, IOException {

		String uri = "https://" + host + "/v0/me/cards/" + originCardId + "/transactions?commit=true";
		System.out.println(uri);

		HttpPost request = new HttpPost(uri);

//		List<NameValuePair> params = new ArrayList<NameValuePair>();
//		params.add(new BasicNameValuePair("denomination", "{ \"amount\": \"" + 0.1 + "\", \"currency\": \"" + currency + "\" }"));
//		params.add(new BasicNameValuePair("destination", destinationCardId));
//		request.setEntity(new UrlEncodedFormEntity(params));

		String inputJson = "{ \"denomination\": " + "{ \"amount\": \"" + amount + "\"" + ", \"currency\": \"" + currency
				+ "\"" + " }, "
						+ " \"message\": \"" + message + "\", "
						+ " \"reference\": \"" + reference + "\", "
						+ "\"destination\": \"" + destinationCardId + "\" }";
		System.out.println(inputJson);
		StringEntity stringEntity = new StringEntity(inputJson, ContentType.APPLICATION_JSON);
		request.setEntity(stringEntity);

		request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
//		request.addHeader("Accept", "application/json");
		request.addHeader("content-type", "application/json");
//		request.addHeader("content-type", "application/x-www-form-urlencoded");
//		request.addHeader("OTP-Token", "000000");

		CloseableHttpClient httpClient = HttpClients.createDefault();
		CloseableHttpResponse response = httpClient.execute(request);

		HttpEntity entity = response.getEntity();
		String json = EntityUtils.toString(entity);

		if (response.getStatusLine().getStatusCode() != 200) {
			throw new WalletException(response.getStatusLine().getStatusCode() + " "
					+ response.getStatusLine().getReasonPhrase() + " " + json);
		}

		httpClient.close();
		response.close();

		JsonNode jsonNode = (new ObjectMapper()).readTree(json);
		return jsonNode;
	}

	public String getBillAddress() {
		return billAddress;
	}

	public void setBillAddress(String billAddress) {
		this.billAddress = billAddress;
	}

	public String getBankAddress() {
		return bankAddress;
	}

	public void setBankAddress(String bankAddress) {
		this.bankAddress = bankAddress;
	}

	public String getType() {
		return type;
	}

	public String getBank() {
		return bank;
	}

	public void setBank(String bank) {
		this.bank = bank;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAccountNumber() {
		return accountNumber;
	}

	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}
}
