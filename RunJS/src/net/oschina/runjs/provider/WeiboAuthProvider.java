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
public class WeiboAuthProvider extends AbstractProvider implements
		AuthProvider, Serializable {

	private static final String GET_UID_URL = "https://api.weibo.com/2/account/get_uid.json";
	private static final String PROFILE_URL = "https://api.weibo.com/2/users/show.json";
	private static final Map<String, String> ENDPOINTS;
	private final Log LOG = LogFactory.getLog(WeiboAuthProvider.class);

	private Permission scope;
	private OAuthConfig config;
	private Profile userProfile;
	private AccessGrant accessGrant;
	private OAuthStrategyBase authenticationStrategy;

	// set this to the list of extended permissions you want
	// Sina Weibo doesn't support scope yet.
	private static final String AllPerms = new String("");
	private static final String AuthenticateOnlyPerms = new String("");

	static {
		ENDPOINTS = new HashMap<String, String>();
		ENDPOINTS.put(Constants.OAUTH_AUTHORIZATION_URL,
				"https://api.weibo.com/oauth2/authorize");
		ENDPOINTS.put(Constants.OAUTH_ACCESS_TOKEN_URL,
				"https://api.weibo.com/oauth2/access_token");
	}

	/**
	 * Stores configuration for the provider
	 * 
	 * @param providerConfig
	 *            It contains the configuration of application like consumer key
	 *            and consumer secret
	 * @throws Exception
	 */
	public WeiboAuthProvider(final OAuthConfig providerConfig) throws Exception {
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
			HttpPost http_post = new HttpPost(
					"https://api.weibo.com/2/statuses/update.json");

			List<NameValuePair> formparams = new ArrayList<NameValuePair>();
			formparams.add(new BasicNameValuePair("access_token", accessGrant
					.getKey()));
			formparams.add(new BasicNameValuePair("status", msg));
			
			http_post.setEntity(new UrlEncodedFormEntity(formparams,"UTF-8"));

			hc.execute(http_post);
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
		String uid = getUid();
		StringBuilder sb = new StringBuilder(PROFILE_URL);
		sb.append("?uid=").append(uid);
		try {
			serviceResponse = authenticationStrategy.executeFeed(sb.toString());
		} catch (Exception e) {
			throw new SocialAuthException(
					"Failed to retrieve the user profile   " + PROFILE_URL, e);
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
			if (resp.has("id")) {
				p.setValidatedId(resp.getString("id"));
				p.setEmail(resp.getString("id"));
			}
			if (resp.has("screen_name")) {
				p.setDisplayName(resp.getString("screen_name"));
				p.setLastName(resp.getString("screen_name"));
			}
			if (resp.has("name")) {
				p.setFullName(resp.getString("name"));
			}
			if (resp.has("url")) {
				p.setLocation(resp.getString("url"));
			}
			if (resp.has("gender")) {
				if ("f".equals(resp.getString("gender")))
					p.setGender("female");
				else
					p.setGender("male");
			}
			if (resp.has("avatar_large")) {
				p.setProfileImageURL(resp.getString("avatar_large"));
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

	private String getUid() throws Exception {
		Response serviceResponse;
		try {
			serviceResponse = authenticationStrategy.executeFeed(GET_UID_URL);
		} catch (Exception e) {
			throw new SocialAuthException("Failed to retrieve the uid from  "
					+ GET_UID_URL, e);
		}

		String result;
		try {
			result = serviceResponse
					.getResponseBodyAsString(Constants.ENCODING);
			LOG.debug("Uid :" + result);
		} catch (Exception e) {
			throw new SocialAuthException("Failed to read response from  "
					+ GET_UID_URL, e);
		}
		try {
			JSONObject resp = new JSONObject(result);
			return resp.getString("uid");
		} catch (Exception e) {
			throw new SocialAuthException("Failed to parse the get_uid json : "
					+ result, e);
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
