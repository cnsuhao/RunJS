package net.oschina.runjs.provider;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import net.oschina.common.utils.HttpConnManager;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.brickred.socialauth.AbstractProvider;
import org.brickred.socialauth.AuthProvider;
import org.brickred.socialauth.Contact;
import org.brickred.socialauth.Permission;
import org.brickred.socialauth.Profile;
import org.brickred.socialauth.exception.ProviderStateException;
import org.brickred.socialauth.exception.SocialAuthConfigurationException;
import org.brickred.socialauth.exception.SocialAuthException;
import org.brickred.socialauth.exception.UserDeniedPermissionException;
import org.brickred.socialauth.util.AccessGrant;
import org.brickred.socialauth.util.OAuthConfig;
import org.brickred.socialauth.util.Response;
import org.brickred.socialauth.util.SocialAuthUtil;
import org.json.JSONObject;

@SuppressWarnings("serial")
public class OSChinaAuthProvider extends AbstractProvider implements
		AuthProvider, Serializable {

	private static final String AUTHORIZATION_URL = "https://www.oschina.net/action/oauth2/authorize?client_id=%1$s&redirect_uri=%2$s&response_type=code";
	private static final String ACCESS_TOKEN_URL = "https://www.oschina.net/action/oauth2/token";
	private static final String PROFILE_URL = "https://www.oschina.net/action/oauth2/user?access_token=%1$s";
	private static final String TWEET_URL = "http://www.oschina.net/action/oauth2/tweet";
	private final Log LOG = LogFactory.getLog(OSChinaAuthProvider.class);

	private String accessToken;
	private String successUrl;
	private boolean isVerify;
	private Permission scope;
	private OAuthConfig config;
	private Profile userProfile;
	private AccessGrant accessGrant;

	/**
	 * Stores configuration for the provider
	 * 
	 * @param providerConfig
	 *            It contains the configuration of application like consumer key
	 *            and consumer secret
	 * @throws Exception
	 */
	public OSChinaAuthProvider(final OAuthConfig providerConfig)
			throws Exception {
		config = providerConfig;
		if (config.getCustomPermissions() != null) {
			this.scope = Permission.CUSTOM;
		}
	}

	/**
	 * Stores access grant for the provider
	 * 
	 * @param accessGrant
	 *            It contains the access token and other information
	 * @throws Exception
	 */
	@Override
	public void setAccessGrant(final AccessGrant accessGrant) throws Exception {
		this.accessGrant = accessGrant;
		accessToken = accessGrant.getKey();
		isVerify = true;
	}

	/**
	 * This is the most important action. It redirects the browser to an
	 * appropriate URL which will be used for authentication with the provider
	 * that has been set using setId()
	 * 
	 * @throws Exception
	 */

	@Override
	public String getLoginRedirectURL(final String successUrl) throws Exception {
		LOG.info("Determining URL for redirection");
		setProviderState(true);
		this.successUrl = successUrl;
		String url = String.format(AUTHORIZATION_URL, config.get_consumerKey(),
				this.successUrl);
		String scopeStr = getScope();
		if (scopeStr != null) {
			url += "&scope=" + scopeStr;
		}
		LOG.info("Redirection to following URL should happen : " + url);
		return url;
	}

	/**
	 * Verifies the user when the external provider redirects back to our
	 * application.
	 * 
	 * @return Profile object containing the profile information
	 * @param request
	 *            Request object the request is received from the provider
	 * @throws Exception
	 */

	@Override
	public Profile verifyResponse(final HttpServletRequest request)
			throws Exception {
		Map<String, String> params = SocialAuthUtil
				.getRequestParametersMap(request);
		return doVerifyResponse(params);

	}

	/**
	 * Verifies the user when the external provider redirects back to our
	 * application.
	 * 
	 * 
	 * @param requestParams
	 *            request parameters, received from the provider
	 * @return Profile object containing the profile information
	 * @throws Exception
	 */
	@Override
	public Profile verifyResponse(final Map<String, String> requestParams)
			throws Exception {
		return doVerifyResponse(requestParams);
	}

	private Profile doVerifyResponse(final Map<String, String> requestParams)
			throws Exception {
		LOG.info("Retrieving Access Token in verify response function");
		if (requestParams.get("error") != null
				&& "access_denied".equals(requestParams.get("error"))) {
			throw new UserDeniedPermissionException();
		}
		if (!isProviderState()) {
			throw new ProviderStateException();
		}
		String code = requestParams.get("code");
		if (code == null || code.length() == 0) {
			throw new SocialAuthException("Verification code is null");
		}
		String result = null;
		HttpClient client = HttpConnManager.getHttpClient();
		HttpPost post = new HttpPost(ACCESS_TOKEN_URL);

		List<NameValuePair> formparams = new ArrayList<NameValuePair>();

		formparams.add(new BasicNameValuePair("client_id", config
				.get_consumerKey()));
		formparams.add(new BasicNameValuePair("client_secret", config
				.get_consumerSecret()));
		formparams.add(new BasicNameValuePair("grant_type",
				"authorization_code"));
		formparams.add(new BasicNameValuePair("redirect_uri", this.successUrl));
		formparams.add(new BasicNameValuePair("code", code));
		post.setEntity(new UrlEncodedFormEntity(formparams, "UTF-8"));

		try {
			HttpResponse resp = client.execute(post);
			HttpEntity entity = resp.getEntity();
			result = IOUtils.toString(entity.getContent(), "UTF-8");
		} catch (Exception e) {
			throw new SocialAuthException("Error in url : " + ACCESS_TOKEN_URL
					+ e);
		}

		if (StringUtils.isBlank(result)) {
			throw new SocialAuthConfigurationException(
					"Problem in getting Access Token. Application key or Secret key may be wrong."
							+ "The server running the application should be same that was registered to get the keys.");
		}

		JSONObject resp = new JSONObject(result);
		accessToken = resp.getString("access_token");
		LOG.debug("Access Token : " + accessToken);

		if (accessToken != null) {
			isVerify = true;
			accessGrant = new AccessGrant();
			accessGrant.setKey(accessToken);
			if (scope != null) {
				accessGrant.setPermission(scope);
			} else {
				accessGrant.setPermission(Permission.ALL);
			}
			accessGrant.setProviderId(getProviderId());

			return getProfile();
		} else {
			throw new SocialAuthException(
					"Access token and expires not found from "
							+ ACCESS_TOKEN_URL);
		}
	}

	/**
	 * Gets the list of contacts of the user and their email.
	 * 
	 * @return List of profile objects representing Contacts. Only name and
	 *         email will be available
	 * @throws Exception
	 */

	@Override
	public List<Contact> getContactList() throws Exception {
		return null;
	}

	/**
	 * Updates the status on the chosen provider if available. This may not be
	 * implemented for all providers.
	 * 
	 * @param msg
	 *            Message to be shown as user's status
	 * @throws Exception
	 */
	@Override
	public void updateStatus(final String msg) throws Exception {
		HttpClient client = HttpConnManager.getHttpClient();
		HttpPost post = new HttpPost(TWEET_URL);
		
		List<NameValuePair> formparams = new ArrayList<NameValuePair>();
		formparams.add(new BasicNameValuePair("access_token", this.accessGrant
				.getKey()));
		formparams.add(new BasicNameValuePair("msg", msg));
		formparams.add(new BasicNameValuePair("magic", "OSCHINA"));
		post.setEntity(new UrlEncodedFormEntity(formparams, "UTF-8"));

		try {
			client.execute(post);
		} catch (Exception e) {
			throw new SocialAuthException("Error in url : " + ACCESS_TOKEN_URL
					+ e);
		}
	}

	/**
	 * Logout
	 */
	@Override
	public void logout() {
		accessToken = null;
		accessGrant = null;
	}

	private Profile getProfile() throws Exception {
		if (!isVerify || accessToken == null) {
			throw new SocialAuthException(
					"Please call verifyResponse function first to get Access Token and then update status");
		}

		Profile p = new Profile();
		String result = null;
		HttpClient client = HttpConnManager.getHttpClient();
		HttpGet get = new HttpGet(String.format(PROFILE_URL, accessToken));

		try {
			HttpResponse resp = client.execute(get);
			HttpEntity entity = resp.getEntity();
			result = IOUtils.toString(entity.getContent(), "UTF-8");
		} catch (Exception e) {
			throw new SocialAuthException("Error in url : " + ACCESS_TOKEN_URL
					+ e);
		}

		if (StringUtils.isBlank(result)) {
			throw new SocialAuthException(
					"Problem in getting user profile. Access Token maybe invalid.");
		}

		try {
			JSONObject resp = new JSONObject(result);
			if (resp.has("id")) {
				p.setValidatedId(resp.getString("id"));
			}
			if (resp.has("name")) {
				p.setFullName(resp.getString("name"));
			}
			if (resp.has("url")) {
				p.setLocation(resp.getString("url"));
			}
			if (resp.has("gender")) {
				p.setGender(resp.getString("gender"));
			}
			if (resp.has("avatar")) {
				p.setProfileImageURL(resp.getString("avatar"));
			}
			if (resp.has("email")) {
				p.setEmail(resp.getString("email"));
			}
			p.setProviderId(getProviderId());
			userProfile = p;
			return p;
		} catch (Exception e) {
			throw new SocialAuthException(
					"Failed to parse the user profile json : " + result, e);
		}
	}

	/**
	 * 
	 * @param p
	 *            Permission object which can be Permission.AUHTHENTICATE_ONLY,
	 *            Permission.ALL, Permission.DEFAULT
	 */
	@Override
	public void setPermission(final Permission p) {
		this.scope = p;
	}

	/**
	 * Makes HTTP request to a given URL.
	 * 
	 * @param url
	 *            URL to make HTTP request.
	 * @param methodType
	 *            Method type can be GET, POST or PUT
	 * @param params
	 *            Any additional parameters whose signature need to compute.
	 *            Only used in case of "POST" and "PUT" method type.
	 * @param headerParams
	 *            Any additional parameters need to pass as Header Parameters
	 * @param body
	 *            Request Body
	 * @return Response object
	 * @throws Exception
	 */
	@Override
	public Response api(final String url, final String methodType,
			final Map<String, String> params,
			final Map<String, String> headerParams, final String body)
			throws Exception {
		return null;
	}

	/**
	 * Retrieves the user profile.
	 * 
	 * @return Profile object containing the profile information.
	 */
	@Override
	public Profile getUserProfile() throws Exception {
		if (userProfile == null && accessGrant != null) {
			getProfile();
		}
		return userProfile;
	}

	@Override
	public AccessGrant getAccessGrant() {
		return accessGrant;
	}

	@Override
	public String getProviderId() {
		return config.getId();
	}

	private String getScope() {
		String scopeStr = null;
		if (Permission.CUSTOM.equals(scope)) {
			scopeStr = config.getCustomPermissions();
		}
		return scopeStr;
	}
}