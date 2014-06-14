package net.oschina.runjs.provider;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.brickred.socialauth.AbstractProvider;
import org.brickred.socialauth.AuthProvider;
import org.brickred.socialauth.Contact;
import org.brickred.socialauth.Permission;
import org.brickred.socialauth.Profile;
import org.brickred.socialauth.exception.SocialAuthException;
import org.brickred.socialauth.oauthstrategy.OAuth2;
import org.brickred.socialauth.oauthstrategy.OAuthStrategyBase;
import org.brickred.socialauth.util.AccessGrant;
import org.brickred.socialauth.util.Constants;
import org.brickred.socialauth.util.OAuthConfig;
import org.brickred.socialauth.util.Response;
import org.brickred.socialauth.util.SocialAuthUtil;
import org.json.JSONObject;

@SuppressWarnings("serial")
public class QQAuthProvider extends AbstractProvider implements AuthProvider,
		Serializable {

	private static final String PROFILE_URL = "https://graph.qq.com/user/get_info";
	private static final String OPENID_URL = "https://graph.qq.com/oauth2.0/me";
	private static final Map<String, String> ENDPOINTS;
	private final Log LOG = LogFactory.getLog(QQAuthProvider.class);

	private String openid;
	private Permission scope;
	private OAuthConfig config;
	private Profile userProfile;
	private AccessGrant accessGrant;
	private OAuthStrategyBase authenticationStrategy;

	// set this to the list of extended permissions you want
	private static final String AllPerms = new String(
			"user,public_repo,repo,gists,get_info,add_t");
	private static final String AuthenticateOnlyPerms = new String("user");

	static {
		ENDPOINTS = new HashMap<String, String>();
		ENDPOINTS.put(Constants.OAUTH_AUTHORIZATION_URL,
				"https://graph.qq.com/oauth2.0/authorize");
		ENDPOINTS.put(Constants.OAUTH_ACCESS_TOKEN_URL,
				"https://graph.qq.com/oauth2.0/token");
	}

	/**
	 * Stores configuration for the provider
	 * 
	 * @param providerConfig
	 *            It contains the configuration of application like consumer key
	 *            and consumer secret
	 * @throws Exception
	 */
	public QQAuthProvider(final OAuthConfig providerConfig) throws Exception {
		config = providerConfig;
		if (config.getCustomPermissions() != null) {
			this.scope = Permission.CUSTOM;
		}
		authenticationStrategy = new OAuth2(config, ENDPOINTS);
		authenticationStrategy.setPermission(scope);
		authenticationStrategy.setScope(getScope());
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
		scope = accessGrant.getPermission();
		authenticationStrategy.setAccessGrant(accessGrant);
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
		return authenticationStrategy.getLoginRedirectURL(successUrl);
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

		accessGrant = authenticationStrategy.verifyResponse(requestParams);

		if (accessGrant != null) {
			LOG.debug("Obtaining user profile");
			return getProfile();
		} else {
			throw new SocialAuthException("Unable to get Access token");
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
		HttpClient hc = new DefaultHttpClient();
		try {
			HttpPost http_post = new HttpPost("https://graph.qq.com/t/add_t");

			List<NameValuePair> formparams = new ArrayList<NameValuePair>();
			formparams.add(new BasicNameValuePair("openid", openid));
			formparams.add(new BasicNameValuePair("access_token", accessGrant
					.getKey()));
			formparams.add(new BasicNameValuePair("oauth_consumer_key", config
					.get_consumerKey()));
			formparams.add(new BasicNameValuePair("format", "json"));
			formparams.add(new BasicNameValuePair("content", msg));

			http_post.setEntity(new UrlEncodedFormEntity(formparams, "UTF-8"));
			hc.execute(http_post).getEntity();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Logout
	 */
	@Override
	public void logout() {
		accessGrant = null;
		authenticationStrategy.logout();
	}

	private Profile getProfile() throws Exception {
		Profile p = new Profile();
		Response serviceResponse;
		// 先获取openid
		try {
			serviceResponse = authenticationStrategy.executeFeed(OPENID_URL
					+ "?format=json");
		} catch (Exception e) {
			throw new SocialAuthException(
					"Failed to retrieve the user profile from  " + PROFILE_URL,
					e);
		}
		String openid_result;
		try {
			openid_result = serviceResponse
					.getResponseBodyAsString(Constants.ENCODING);
			LOG.debug("User OPENID :" + openid_result);
		} catch (Exception e) {
			throw new SocialAuthException("Failed to read response from  "
					+ OPENID_URL, e);
		}
		JSONObject openid_resp = new JSONObject(openid_result.replace(
				"callback( ", "").replace(");", ""));
		if (openid_resp.has("openid"))
			this.openid = openid_resp.getString("openid");
		else
			throw new SocialAuthException("Failed to parse the openid json : "
					+ openid_result);

		// 再获取个人信息
		try {
			serviceResponse = authenticationStrategy.executeFeed(PROFILE_URL
					+ "?openid=" + openid + "&oauth_consumer_key="
					+ config.get_consumerKey());
		} catch (Exception e) {
			throw new SocialAuthException(
					"Failed to retrieve the user profile from  " + PROFILE_URL,
					e);
		}

		String result;
		try {
			result = serviceResponse
					.getResponseBodyAsString(Constants.ENCODING);
			LOG.debug("User Profile :" + result);
		} catch (Exception e) {
			throw new SocialAuthException("Failed to read response from  "
					+ PROFILE_URL, e);
		}
		try {
			JSONObject resp = new JSONObject(result);

			p.setValidatedId(openid);
			p.setEmail(openid);
			if (resp.has("data")) {
				JSONObject data=resp.getJSONObject("data");
				if (data.has("nick")) {
					p.setFullName(data.getString("nick"));
				} else if (data.has("name")) {
					p.setFullName(data.getString("name"));
				}
				if (data.has("sex")) {
					if ("1".equals(data.getString("sex")))
						p.setGender("male");
					else
						p.setGender("female");
				}
				if (data.has("head")) {
					p.setProfileImageURL(data.getString("head")+"/100");
				}
			}
			serviceResponse.close();
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
		LOG.debug("Calling URL : " + url);
		Response serviceResponse;
		try {
			serviceResponse = authenticationStrategy.executeFeed(url,
					methodType, params, headerParams, body);
		} catch (Exception e) {
			throw new SocialAuthException(
					"Error while making request to URL : " + url, e);
		}
		if (serviceResponse.getStatus() != 200) {
			LOG.debug("Return status for URL " + url + " is "
					+ serviceResponse.getStatus());
			throw new SocialAuthException("Error while making request to URL :"
					+ url + "Status : " + serviceResponse.getStatus());
		}
		return serviceResponse;
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
		if (Permission.AUTHENTICATE_ONLY.equals(scope)) {
			scopeStr = AuthenticateOnlyPerms;
		} else if (Permission.CUSTOM.equals(scope)) {
			scopeStr = config.getCustomPermissions();
		} else {
			scopeStr = AllPerms;
		}
		return scopeStr;
	}
}
