package io.branch;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.annotation.TargetApi;
import android.net.Uri;
import android.os.Build;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

import io.branch.indexing.BranchUniversalObject;
import io.branch.referral.Branch;
import io.branch.referral.PrefHelper;
import io.branch.referral.BranchError;
import io.branch.referral.BranchViewHandler;
import io.branch.referral.SharingHelper;
import io.branch.referral.util.BRANCH_STANDARD_EVENT;
import io.branch.referral.util.BranchEvent;
import io.branch.referral.util.CommerceEvent;
import io.branch.referral.util.CurrencyType;
import io.branch.referral.util.Product;
import io.branch.referral.util.ProductCategory;
import io.branch.referral.util.ShareSheetStyle;

import io.branch.referral.ServerRequestGetLATD.BranchLastAttributedTouchDataListener;
import io.branch.referral.ServerRequestGetCPID.BranchCrossPlatformIdListener;
import io.branch.referral.util.BranchCPID;

public class BranchSDK extends CordovaPlugin {

    static class BranchLinkProperties extends io.branch.referral.util.LinkProperties {
    }

    // Standard Debugging Variables
    private static final String LCAT = "CordovaBranchSDK";

    private static final String BRANCH_PLUGIN_TYPE = "CordovaIonic";
    private static final String BRANCH_PLUGIN_VERSION = "%BRANCH_PLUGIN_VERSION%";

    // Private Method Properties
    private ArrayList<BranchUniversalObjectWrapper> branchObjectWrappers;
    private Activity activity;
    private Branch instance;
    private String deepLinkUrl;
    private CallbackContext initSessionCallbackContext;
    private boolean initSessionCallbackContextKeepCallback;

    /**
     * Class Constructor
     */
    public BranchSDK() {

        this.activity = null;
        this.instance = null;
        this.branchObjectWrappers = new ArrayList<BranchUniversalObjectWrapper>();
        this.initSessionCallbackContext = null;
        this.initSessionCallbackContextKeepCallback = false;
    }

    /**
     * Called after plugin construction and fields have been initialized.
     */
    @Override
    protected void pluginInitialize() {

        this.activity = this.cordova.getActivity();
        Branch.disableInstantDeepLinking(true);
        Branch.registerPlugin(BRANCH_PLUGIN_TYPE, BRANCH_PLUGIN_VERSION);
        if (this.instance == null) {
            this.instance = Branch.getAutoInstance(this.activity.getApplicationContext());
        }
    }

    /**
     * Called when the activity receives a new intent.
     */
    public void onNewIntent(Intent intent) {
        intent.putExtra("branch_force_new_session", true);
        this.activity.setIntent(intent);
        Branch.sessionBuilder(this.activity).withCallback(branchReferralInitListener).reInit();
    }

    /**
     * Handle depreciated call to sendJavaScript for more recent method
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void sendJavascript(final String javascript) {
        webView.getView().post(new Runnable() {
            @Override
            public void run() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    webView.sendJavascript(javascript);
                } else {
                    webView.loadUrl("javascript:" + javascript);
                }
            }
        });
    }

    /**
     * <p>
     * cordova.exec() method reference.
     * All exec() calls goes to this part.
     * </p>
     *
     * @param action          A {@link String} value method to be executed.
     * @param args            A {@link JSONArray} value parameters passed along with the action param.
     * @param callbackContext A {@link CallbackContext} function passed for executing callbacks.
     */
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {

        Log.d(LCAT, "Execute: " + action);

        Runnable r = new RunnableThread(action, args, callbackContext);

        if (action.equals("setDebug")) {
            cordova.getActivity().runOnUiThread(r);
            return true;
        } else if (action.equals("setCookieBasedMatching")) {
            cordova.getActivity().runOnUiThread(r);
            return true;
       } else if (action.equals("disableTracking")) {
            cordova.getActivity().runOnUiThread(r);
            return true;
        } else if (action.equals("initSession")) {
            cordova.getActivity().runOnUiThread(r);
            return true;
        } else if (action.equals("setRequestMetadata")) {
            cordova.getActivity().runOnUiThread(r);
            return true;
        } else {
            if (this.instance != null) {
                if (action.equals("setIdentity")) {
                    cordova.getActivity().runOnUiThread(r);
                    return true;
                } else if (action.equals("userCompletedAction")) {
                    if (args.length() < 1 && args.length() > 2) {
                        callbackContext.error(String.format("Parameter mismatched. 1-2 is required but %d is given", args.length()));
                        return false;
                    }
                    cordova.getActivity().runOnUiThread(r);
                    return true;
                } else if (action.equals("sendCommerceEvent")) {
                    if (args.length() < 1 && args.length() > 2) {
                        callbackContext.error(String.format("Parameter mismatched. 1-2 is required but %d is given", args.length()));
                        return false;
                    }
                    cordova.getActivity().runOnUiThread(r);
                    return true;
                } else if (action.equals("sendBranchEvent")) {
                    if (args.length() < 1 && args.length() > 2) {
                        callbackContext.error(String.format("Parameter mismatched. 1-2 is required but %d is given", args.length()));
                        return false;
                    }
                    cordova.getActivity().runOnUiThread(r);
                    return true;
                } else if (action.equals("getFirstReferringParams")) {
                    cordova.getActivity().runOnUiThread(r);
                    return true;
                } else if (action.equals("getLatestReferringParams")) {
                    cordova.getActivity().runOnUiThread(r);
                    return true;
                } else if (action.equals("logout")) {
                    cordova.getActivity().runOnUiThread(r);
                    return true;
                } else if (action.equals("loadRewards")) {
                    cordova.getActivity().runOnUiThread(r);
                    return true;
                } else if (action.equals("redeemRewards")) {
                    if (args.length() < 1 && args.length() > 2) {
                        callbackContext.error(String.format("Parameter mismatched. 1-2 is required but %d is given", args.length()));
                        return false;
                    }
                    cordova.getActivity().runOnUiThread(r);
                    return true;
                } else if (action.equals("getCreditHistory")) {
                    cordova.getActivity().runOnUiThread(r);
                    return true;
                } else if (action.equals("createBranchUniversalObject")) {
                    if (args.length() != 1) {
                        callbackContext.error(String.format("Parameter mismatched. 1 is required but %d is given", args.length()));
                        return false;
                    }
                    cordova.getActivity().runOnUiThread(r);
                    return true;

                } else if (action.equals("crossPlatformIds")) {
                    cordova.getActivity().runOnUiThread(r);
                    return true;

                } else if (action.equals("lastAttributedTouchData")) {
                    cordova.getActivity().runOnUiThread(r);
                    return true; 

                } else if (action.equals(("generateShortUrl"))) {
                    if (args.length() != 3) {
                        callbackContext.error(String.format("Parameter mismatched. 3 is required but %d is given", args.length()));
                        return false;
                    }
                    cordova.getActivity().runOnUiThread(r);
                    return true;
                } else if (action.equals("registerView")) {
                    if (args.length() != 1) {
                        callbackContext.error(String.format("Parameter mismatched. 1 is required but %d is given", args.length()));
                        return false;
                    }
                    cordova.getActivity().runOnUiThread(r);
                    return true;
                } else if (action.equals("showShareSheet")) {
                    if (args.length() < 3) {
                        callbackContext.error(String.format("Parameter mismatched. 3 is required but %d is given", args.length()));
                        return false;
                    }
                    cordova.getActivity().runOnUiThread(r);
                    return true;
                } else if (action.equals("onShareLinkDialogLaunched")) {

                    BranchUniversalObjectWrapper branchObjWrapper = (BranchUniversalObjectWrapper) branchObjectWrappers.get(args.getInt(0));
                    branchObjWrapper.onShareLinkDialogLaunched = callbackContext;

                    branchObjectWrappers.set(args.getInt(0), branchObjWrapper);

                } else if (action.equals("onShareLinkDialogDismissed")) {

                    BranchUniversalObjectWrapper branchObjWrapper = (BranchUniversalObjectWrapper) branchObjectWrappers.get(args.getInt(0));
                    branchObjWrapper.onShareLinkDialogDismissed = callbackContext;

                    branchObjectWrappers.set(args.getInt(0), branchObjWrapper);

                } else if (action.equals("onLinkShareResponse")) {

                    BranchUniversalObjectWrapper branchObjWrapper = (BranchUniversalObjectWrapper) branchObjectWrappers.get(args.getInt(0));
                    branchObjWrapper.onLinkShareResponse = callbackContext;

                    branchObjectWrappers.set(args.getInt(0), branchObjWrapper);

                } else if (action.equals("onChannelSelected")) {

                    BranchUniversalObjectWrapper branchObjWrapper = (BranchUniversalObjectWrapper) branchObjectWrappers.get(args.getInt(0));
                    branchObjWrapper.onChannelSelected = callbackContext;

                    branchObjectWrappers.set(args.getInt(0), branchObjWrapper);

                }

                return true;

            } else {
                callbackContext.error("Branch instance not set. Please execute initSession() first.");
            }
        }

        return false;

    }
    
    public void crossPlatformIds(CallbackContext callbackContext) {
        this.instance.getCrossPlatformIds(new BranchCPIDListener(callbackContext));
    }

    public void lastAttributedTouchData(CallbackContext callbackContext) {
        this.instance.getLastAttributedTouchData(new BranchLATDListener(callbackContext), 30);
    }

    //////////////////////////////////////////////////
    //----------- CLASS PRIVATE METHODS ------------//
    //////////////////////////////////////////////////

    /**
     * <p>Initialises a session with the Branch API, assigning a {@link Branch.BranchUniversalReferralInitListener}
     * to perform an action upon successful initialisation.</p>
     *
     * @param callbackContext A callback to execute at the end of this method
     */
    private void initSession(boolean isKeepCallBack, CallbackContext callbackContext) {

        this.activity = this.cordova.getActivity();

        Uri data = activity.getIntent().getData();

        if (data != null && data.isHierarchical()) {
            this.deepLinkUrl = data.toString();
        }

        this.initSessionCallbackContext = callbackContext;
        this.initSessionCallbackContextKeepCallback = isKeepCallBack;

        Branch.sessionBuilder(activity).withCallback(branchReferralInitListener).withData(data).init();
    }

    private Branch.BranchReferralInitListener branchReferralInitListener = new Branch.BranchReferralInitListener() {
        @Override
        public void onInitFinished(JSONObject referringParams, BranchError error) {
            if (error == null && referringParams != null && initSessionCallbackContext != null) {
                PluginResult result = new PluginResult(PluginResult.Status.OK, referringParams);
                if(initSessionCallbackContextKeepCallback){
                    result.setKeepCallback(true);
                }
                initSessionCallbackContext.sendPluginResult(result);
            } else {
                JSONObject message = new JSONObject();
                try {
                    message.put("error", error.getMessage());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (initSessionCallbackContext != null) {
                    initSessionCallbackContext.error(message);
                }
            }
        }
    };

    /**
     * <p>This method should be called if you know that a different person is about to use the app. For example,
     * if you allow users to log out and let their friend use the app, you should call this to notify Branch
     * to create a new user for this device. This will clear the first and latest params, as a new session is created.</p>
     *
     * @param callbackContext A callback to execute at the end of this method
     */
    private void logout(CallbackContext callbackContext) {

        this.instance.logout(new LogoutStatusListener(callbackContext));

    }

    /**
     * <p>Redeems the specified number of credits from the "default" bucket, if there are sufficient
     * credits within it. If the number to redeem exceeds the number available in the bucket, all of
     * the available credits will be redeemed instead.</p>
     *
     * @param value           An {@link Integer} specifying the number of credits to attempt to redeem from
     *                        the bucket.
     * @param callbackContext A callback to execute at the end of this method
     */
    private void redeemRewards(final int value, CallbackContext callbackContext) {

        this.instance.redeemRewards(value, new RedeemRewardsListener(callbackContext));

    }

    /**
     * <p>Redeems the specified number of credits from the "default" bucket, if there are sufficient
     * credits within it. If the number to redeem exceeds the number available in the bucket, all of
     * the available credits will be redeemed instead.</p>
     *
     * @param value           An {@link Integer} specifying the number of credits to attempt to redeem from
     *                        the bucket.
     * @param bucket          The name of the bucket to remove the credits from.
     * @param callbackContext A callback to execute at the end of this method
     */
    private void redeemRewards(int value, String bucket, CallbackContext callbackContext) {

        this.instance.redeemRewards(bucket, value, new RedeemRewardsListener(callbackContext));

    }

    /**
     * <p>Retrieves rewards for the current session, with a callback to perform a predefined
     * action following successful report of state change. You'll then need to call getCredits
     * in the callback to update the credit totals in your UX.</p>
     *
     * @param callbackContext A callback to execute at the end of this method
     * @param bucket          Load reward of a specific bucket
     */
    private void loadRewards(String bucket, CallbackContext callbackContext) {

        this.instance.loadRewards(new LoadRewardsListener(bucket, callbackContext, this.instance));

    }

    /**
     * <p>Retrieves rewards for the current session, with a callback to perform a predefined
     * action following successful report of state change. You'll then need to call getCredits
     * in the callback to update the credit totals in your UX.</p>
     *
     * @param callbackContext A callback to execute at the end of this method
     */
    private void loadRewards(CallbackContext callbackContext) {

        this.instance.loadRewards(new LoadRewardsListener(callbackContext, this.instance));

    }

    /**
     * <p>Returns the parameters associated with the link that referred the session. If a user
     * clicks a link, and then opens the app, initSession will return the paramters of the link
     * and then set them in as the latest parameters to be retrieved by this method. By default,
     * sessions persist for the duration of time that the app is in focus. For example, if you
     * minimize the app, these parameters will be cleared when closeSession is called.</p>
     *
     * @param callbackContext A callback to execute at the end of this method
     * @return A {@link JSONObject} containing the latest referring parameters as
     * configured locally.
     */
    private void getLatestReferringParams(CallbackContext callbackContext) {

        JSONObject sessionParams = this.instance.getLatestReferringParams();

        if (sessionParams == null || sessionParams.length() == 0) {
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, /* send boolean: false as the data */ false));
        } else {
            Log.d(LCAT, sessionParams.toString());
            callbackContext.success(sessionParams);
        }

    }

    /**
     * <p>Returns the parameters associated with the link that referred the user. This is only set once,
     * the first time the user is referred by a link. Think of this as the user referral parameters.
     * It is also only set if isReferrable is equal to true, which by default is only true
     * on a fresh install (not upgrade or reinstall). This will change on setIdentity (if the
     * user already exists from a previous device) and logout.</p>
     *
     * @param callbackContext A callback to execute at the end of this method
     */
    private void getFirstReferringParams(CallbackContext callbackContext) {

        JSONObject installParams = this.instance.getFirstReferringParams();

        if (installParams == null || installParams.length() == 0) {
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, /* send boolean: false as the data */ false));
        } else {
            Log.d(LCAT, installParams.toString());
            callbackContext.success(installParams);
        }

    }

    /**
     * <p>
     * Create a BranchUniversalObject with the given content.
     * </p>
     *
     * @param options A {@link JSONObject} value to set for the branch universal object properties.
     * @return A {@link JSONObject} value of BranchUniversalObject instance.
     */
    private void createBranchUniversalObject(JSONObject options, CallbackContext callbackContext) throws JSONException {

        BranchUniversalObject branchObj = new BranchUniversalObject();

        // Set object properties
        // Facebook Properties
        if (options.has("canonicalIdentifier")) {
            branchObj.setCanonicalIdentifier(options.getString("canonicalIdentifier"));
        }
        if (options.has("title")) {
            branchObj.setTitle(options.getString("title"));
        }
        if (options.has("contentDescription")) {
            branchObj.setContentDescription(options.getString("contentDescription"));
        }
        if (options.has("contentImageUrl")) {
            branchObj.setContentImageUrl(options.getString("contentImageUrl"));
        }
        if (options.has("contentType")) {
            branchObj.setContentType(options.getString("contentType"));
        }

        // Set content visibility
        if (options.has("contentIndexingMode")) {
            if (options.getString("contentIndexingMode").equals("private")) {
                branchObj.setContentIndexingMode(BranchUniversalObject.CONTENT_INDEX_MODE.PRIVATE);
            } else {
                branchObj.setContentIndexingMode(BranchUniversalObject.CONTENT_INDEX_MODE.PUBLIC);
            }
        }

        // Add custom keys/values to the deep link data
        if (options.has("contentMetadata")) {

            JSONObject contentMetaData = options.getJSONObject("contentMetadata");
            Iterator<?> keys = contentMetaData.keys();

            while (keys.hasNext()) {
                String key = (String) keys.next();
                String value = contentMetaData.optString(key);
                Log.d(LCAT, contentMetaData.getString(key));
                branchObj.addContentMetadata(key, value);
            }
        }

        BranchUniversalObjectWrapper branchObjWrapper = new BranchUniversalObjectWrapper(branchObj);

        this.branchObjectWrappers.add(branchObjWrapper);
        JSONObject response = new JSONObject();
        response.put("message", "Success");
        response.put("branchUniversalObjectId", this.branchObjectWrappers.size() - 1);

        callbackContext.success(response);

    }

    /**
     * Display a popup of the share sheet.
     *
     * @param instanceIdx   The instance index from branchObjects array
     * @param options       A {@link JSONObject} value to set URL options.
     * @param controlParams A {@link JSONObject} value to set the URL control parameters.
     */
    private void showShareSheet(int instanceIdx, JSONObject options, JSONObject controlParams, JSONObject strings) throws JSONException {

        ShareSheetStyle shareSheetStyle = new ShareSheetStyle(this.activity, strings.getString("shareTitle"), strings.getString("shareText"))
                .setCopyUrlStyle(this.activity.getResources().getDrawable(android.R.drawable.ic_menu_send), strings.getString("copyToClipboard"), strings.getString("clipboardSuccess"))
                .setMoreOptionStyle(this.activity.getResources().getDrawable(android.R.drawable.ic_menu_search), strings.getString("more"))
                .addPreferredSharingOption(SharingHelper.SHARE_WITH.FACEBOOK)
                .addPreferredSharingOption(SharingHelper.SHARE_WITH.EMAIL)
                .addPreferredSharingOption(SharingHelper.SHARE_WITH.MESSAGE)
                .addPreferredSharingOption(SharingHelper.SHARE_WITH.TWITTER)
                .setAsFullWidthStyle(true)
                .setSharingTitle(strings.getString("shareWith"));

        BranchUniversalObjectWrapper branchObjWrapper = (BranchUniversalObjectWrapper) this.branchObjectWrappers.get(instanceIdx);
        BranchLinkProperties linkProperties = createLinkProperties(options, controlParams);
        BranchUniversalObject branchObj = branchObjWrapper.branchUniversalObj;

        branchObj.showShareSheet(this.activity, linkProperties, shareSheetStyle,
                new ShowShareSheetListener(branchObjWrapper.onShareLinkDialogLaunched, branchObjWrapper.onShareLinkDialogDismissed, branchObjWrapper.onLinkShareResponse, branchObjWrapper.onChannelSelected));

    }

    /**
     * Generates a share link.
     *
     * @param options       A {@link JSONObject} value to set URL options.
     * @param controlParams A {@link JSONObject} value to set the URL control parameters.
     * @return A {@link io.branch.referral.util.LinkProperties} value.
     */
    private BranchLinkProperties createLinkProperties(JSONObject options, JSONObject controlParams) throws JSONException {

        Log.d(LCAT, "Creating link properties");

        BranchLinkProperties linkProperties = new BranchLinkProperties();

        // Add link properties
        if (options.has("feature")) {
            linkProperties.setFeature(options.getString("feature"));
        }
        if (options.has("alias")) {
            linkProperties.setAlias(options.getString("alias"));
        }
        if (options.has("channel")) {
            linkProperties.setChannel(options.getString("channel"));
        }
        if (options.has("stage")) {
            linkProperties.setStage(options.getString("stage"));
        }
        if (options.has("campaign")) {
            linkProperties.setCampaign(options.getString("campaign"));
        }
        if (options.has("duration")) {
            linkProperties.setDuration(options.getInt("duration"));
        }
        if (options.has("tags")) {
            JSONArray array = (JSONArray) options.get("tags");
            if (array != null) {
                for (int i = 0; i < array.length(); i++) {
                    linkProperties.addTag(array.get(i).toString());
                }
            }
        }

        Log.d(LCAT, "Adding control parameters:");

        // Add and iterate control parameters properties
        Iterator<?> keys = controlParams.keys();

        while (keys.hasNext()) {
            String key = keys.next().toString();

            Log.d(LCAT, String.format("key: %s", key));

            linkProperties.addControlParameter(key, controlParams.getString(key));
        }

        return linkProperties;

    }

    /**
     * Mark the content referred by this object as viewed. This increment the view count of the contents referred by this object.
     *
     * @param instanceIdx The instance index from branchObjects array
     */
    private void registerView(int instanceIdx, CallbackContext callbackContext) {

        BranchUniversalObjectWrapper branchUniversalWrapper = (BranchUniversalObjectWrapper) this.branchObjectWrappers.get(instanceIdx);

        branchUniversalWrapper.branchUniversalObj.registerView(new RegisterViewStatusListener(callbackContext));

        callbackContext.success("Success");

    }

    /**
     * Generate a URL.
     *
     * @param instanceIdx   The instance index from branchObjects array
     * @param options       A {@link JSONObject} value to set URL options.
     * @param controlParams A {@link JSONObject} value to set the URL control parameters.
     */
    private void generateShortUrl(int instanceIdx, JSONObject options, JSONObject controlParams, CallbackContext callbackContext) throws JSONException {

        BranchLinkProperties linkProperties = createLinkProperties(options, controlParams);

        BranchUniversalObjectWrapper branchUniversalWrapper = (BranchUniversalObjectWrapper) this.branchObjectWrappers.get(instanceIdx);

        branchUniversalWrapper.branchUniversalObj.generateShortUrl(this.activity, linkProperties, new GenerateShortUrlListener(callbackContext));

    }

    /**
     * <p>Sets the cookie based matching for all incoming requests.</p>
     * <p>If you want cookie based matching, call this <b>before</b> initUserSession</p>
     *
     * @param linkDomain      A {@link String} value to of the link domain for cookie based matching.
     * @param callbackContext A callback to execute at the end of this method
     */
    private void setCookieBasedMatching(String linkDomain, CallbackContext callbackContext) {

        this.activity = this.cordova.getActivity();

        if (linkDomain != null) {
            Branch.enableCookieBasedMatching(linkDomain);
        }

        callbackContext.success("Success");
    }

    /**
     * <p>Sets the library to function in debug mode, enabling logging of all requests.</p>
     * <p>If you want to flag debug, call this <b>before</b> initUserSession</p>
     *
     * @param isEnable        A {@link Boolean} value to enable/disable debugging mode for the app.
     * @param callbackContext A callback to execute at the end of this method
     */
    private void setDebug(boolean isEnable, CallbackContext callbackContext) {
        this.activity = this.cordova.getActivity();
        Branch.enableLogging();
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, isEnable));
    }

    /**
     * <p>Disables tracking for GDPR compliance.</p>
     * <p>Can flag at any time.</p>
     * <p>Limits all Branch network requests and forces long link generation.</p>
     *
     * @param isEnable        A {@link Boolean} value to enable/disable debugging mode for the app.
     * @param callbackContext A callback to execute at the end of this method
     */
    private void disableTracking(boolean isEnable, CallbackContext callbackContext) {
        this.activity = this.cordova.getActivity();

        this.instance.disableTracking(isEnable);
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, isEnable));
    }

    /**
     * <p>Identifies the current user to the Branch API by supplying a unique
     * identifier as a {@link String} value.</p>
     *
     * @param newIdentity     A {@link String} value containing the unique identifier of the user.
     * @param callbackContext A callback to execute at the end of this method
     */
    private void setIdentity(String newIdentity, CallbackContext callbackContext) {

        this.instance.setIdentity(newIdentity, new SetIdentityListener(callbackContext));

    }

    /**
     * <p>Allow Branch SDK to pass the user's Mixpanel distinct id to our servers. Branch will then pass that Distinct ID to Mixpanel when logging any event.</p>
     *
     * @param key           A {@link String} value containing the unique identifier of the Mixpanel user.
     * @param callbackContext A callback to execute at the end of this method
     */
    private void setRequestMetadata(String key, String val, CallbackContext callbackContext) {

        Branch.getInstance().setRequestMetadata(key, val);

        callbackContext.success("Success");

    }

    /**
     * <p>A void call to indicate that the user has performed a specific action and for that to be
     * reported to the Branch API.</p>
     *
     * @param action          A {@link String} value to be passed as an action that the user has carried out.
     *                        For example "logged in" or "registered".
     * @param callbackContext A callback to execute at the end of this method
     */
    private void userCompletedAction(String action, CallbackContext callbackContext) {

        this.instance.userCompletedAction(action);

        callbackContext.success("Success");

    }

    /**
     * <p>A void call to indicate that the user has performed a specific action and for that to be
     * reported to the Branch API.</p>
     *
     * @param action          A {@link String} value to be passed as an action that the user has carried
     *                        out. For example "logged in" or "registered".
     * @param metaData        A {@link JSONObject} containing app-defined meta-data to be attached to a
     *                        user action that has just been completed.
     * @param callbackContext A callback to execute at the end of this method
     */
    private void userCompletedAction(String action, JSONObject metaData, CallbackContext callbackContext) {

        this.instance.userCompletedAction(action, metaData);

        callbackContext.success("Success");

    }

    /**
     * <p>A void call to indicate that the user has performed a specific commerce event and for that to be
     * reported to the Branch API.</p>
     *
     * @param action          A {@link String} value to be passed as an commerce event that the user has
     *                        carried out.
     * @param metaData        A {@link JSONObject} containing app-defined meta-data to be attached to a
     *                        user action that has just been completed.
     * @param callbackContext A callback to execute at the end of this method
     */
    private void sendCommerceEvent(JSONObject action, JSONObject metaData, CallbackContext callbackContext) throws JSONException {

        CommerceEvent commerce = new CommerceEvent();
        Iterator<String> keys = action.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            String val;
            try {
                val = action.getString(key);
            } catch (JSONException e) {
                e.printStackTrace();
                callbackContext.error("Invalid key-value for " + key);
                return;
            }
            if (key.equals("revenue")) {
                commerce.setRevenue(Double.parseDouble(val));
            } else if (key.equals("currency")) {
                commerce.setCurrencyType(CurrencyType.values()[Integer.parseInt(val)]);
            } else if (key.equals("transactionID")) {
                commerce.setTransactionID(val);
            } else if (key.equals("coupon")) {
                commerce.setCoupon(val);
            } else if (key.equals("shipping")) {
                commerce.setShipping(Double.parseDouble(val));
            } else if (key.equals("tax")) {
                commerce.setTax(Double.parseDouble(val));
            } else if (key.equals("affiliation")) {
                commerce.setAffiliation(val);
            } else if (key.equals("products")) {
                JSONArray products = new JSONArray();
                try {
                    products = action.getJSONArray(key);
                } catch (JSONException e) {
                    e.printStackTrace();
                    callbackContext.error("Invalid key-value for " + key);
                    return;
                }
                for (int i = 0; i < products.length(); ++i) {
                    Product product = new Product();
                    JSONObject item = products.getJSONObject(i);
                    keys = item.keys();
                    while (keys.hasNext()) {
                        key = keys.next();
                        try {
                            val = item.getString(key);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            callbackContext.error("Invalid key-value for product for " + key);
                            return;
                        }
                        if (key.equals("sku")) {
                            product.setSku(val);
                        } else if (key.equals("name")) {
                            product.setName(val);
                        } else if (key.equals("price")) {
                            product.setPrice(Double.parseDouble(val));
                        } else if (key.equals("quantity")) {
                            product.setQuantity(Integer.parseInt(val));
                        } else if (key.equals("brand")) {
                            product.setBrand(val);
                        } else if (key.equals("category")) {
                            product.setCategory(ProductCategory.values()[Integer.parseInt(val)]);
                        } else if (key.equals("variant")) {
                            product.setVariant(val);
                        }
                    }
                    commerce.addProduct(product);
                }
            }
        }

        this.instance.sendCommerceEvent(commerce, metaData, new BranchViewEventsListener(callbackContext));

    }

    public void sendBranchEvent(String eventName, CallbackContext callbackContext) throws JSONException {

        BranchEvent event;
        try {
            BRANCH_STANDARD_EVENT standardEvent = BRANCH_STANDARD_EVENT.valueOf(eventName);
            event = new BranchEvent(standardEvent);
        } catch(IllegalArgumentException e) {
            event = new BranchEvent(eventName);
        }

        event.logEvent(this.activity);
        //callbackContext.success();
    }

    public void sendBranchEvent(String eventName, JSONObject metaData, CallbackContext callbackContext) throws JSONException {

        BranchEvent event;
        try {
            BRANCH_STANDARD_EVENT standardEvent = BRANCH_STANDARD_EVENT.valueOf(eventName);
            event = new BranchEvent(standardEvent);
        } catch(IllegalArgumentException e) {
            event = new BranchEvent(eventName);
        }

        Iterator < String > keys = metaData.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            if (key.equals("revenue")) {
                event.setRevenue(Double.parseDouble(metaData.getString("revenue")));
            } else if (key.equals("currency")) {
                String currencyString = metaData.getString("currency");
                CurrencyType currency = CurrencyType.getValue(currencyString);
                if (currency != null) {
                    event.setCurrency(currency);
                }
                else {
                    Log.d(LCAT, "Invalid currency " + currencyString);
                }
            } else if (key.equals("transactionID")) {
                event.setTransactionID(metaData.getString("transactionID"));
            } else if (key.equals("coupon")) {
                event.setCoupon(metaData.getString("coupon"));
            } else if (key.equals("shipping")) {
                event.setShipping(Double.parseDouble(metaData.getString("shipping")));
            } else if (key.equals("tax")) {
                event.setTax(Double.parseDouble(metaData.getString("tax")));
            } else if (key.equals("affiliation")) {
                event.setAffiliation(metaData.getString("affiliation"));
            } else if (key.equals("description")) {
                event.setDescription(metaData.getString("description"));
            } else if (key.equals("searchQuery")) {
                event.setSearchQuery(metaData.getString("searchQuery"));
            } else if (key.equals("customerEventAlias")) {
                event.setCustomerEventAlias(metaData.getString("customerEventAlias"));
            } else if (key.equals("customData")) {
                JSONObject customData = metaData.getJSONObject("customData");
                keys = customData.keys();

                while (keys.hasNext()) {
                    String keyValue = (String) keys.next();
                    event.addCustomDataProperty(keyValue, customData.getString(keyValue));
                }
            }

        }
        event.logEvent(this.activity);
        //callbackContext.success();
    }

    /**
     * <p>Gets the credit history of the specified bucket and triggers a callback to handle the
     * response.</p>
     *
     * @param callbackContext A callback to execute at the end of this method
     */
    private void getCreditHistory(CallbackContext callbackContext) {

        this.instance.getCreditHistory(new CreditHistoryListener(callbackContext));

    }

    /**
     * @access protected
     * @class BranchUniversalObjectWrapper
     */
    protected class BranchUniversalObjectWrapper {

        public BranchUniversalObject branchUniversalObj;
        public CallbackContext onShareLinkDialogDismissed;
        public CallbackContext onShareLinkDialogLaunched;
        public CallbackContext onLinkShareResponse;
        public CallbackContext onChannelSelected;

        /**
         * @param branchUniversalObj branchUniversalObj
         * @constructor
         */
        public BranchUniversalObjectWrapper(BranchUniversalObject branchUniversalObj) {
            this.branchUniversalObj = branchUniversalObj;
            this.onShareLinkDialogDismissed = null;
            this.onShareLinkDialogLaunched = null;
            this.onLinkShareResponse = null;
            this.onChannelSelected = null;
        }

    }

    //////////////////////////////////////////////////
    //----------- INNER CLASS LISTENERS ------------//
    //////////////////////////////////////////////////

    protected class BranchCPIDListener implements BranchCrossPlatformIdListener {
        private CallbackContext _callbackContext;

        public BranchCPIDListener(CallbackContext callbackContext) {
            this._callbackContext = callbackContext;
        }

        @Override
        public void onDataFetched(BranchCPID branchCPID, BranchError error) {
            if (error != null) {
                Log.d(LCAT, "CPID unavailable");
                this._callbackContext.error("CPID unavailable");
            } else {

                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("developer_identity", branchCPID.getDeveloperIdentity());
                    jsonObject.put("cross_platform_id", branchCPID.getCrossPlatformID());
                    jsonObject.put("past_cross_platform_ids", branchCPID.getPastCrossPlatformIds());
                    jsonObject.put("prob_cross_platform_ids", branchCPID.getProbabilisticCrossPlatformIds());
                } catch (JSONException e) {
                    // just send back and empty object on json error
                    jsonObject = new JSONObject();
                }

                Log.d(LCAT, jsonObject.toString());

                PluginResult result = new PluginResult(PluginResult.Status.OK, jsonObject);
                this._callbackContext.sendPluginResult(result);
            }
        }
    }

    protected class BranchLATDListener implements BranchLastAttributedTouchDataListener {
        private CallbackContext _callbackContext;

        public BranchLATDListener(CallbackContext callbackContext) {
            this._callbackContext = callbackContext;
        }

        @Override
        public void onDataFetched(JSONObject jsonObject, BranchError error) {
            if (error != null) {
                Log.d(LCAT, "LATD unavailable");
                this._callbackContext.error("LATD unavailable");
            } else {
                Log.d(LCAT, jsonObject.toString());

                PluginResult result = new PluginResult(PluginResult.Status.OK, jsonObject);
                this._callbackContext.sendPluginResult(result);
            }
        }
    }

    protected class BranchViewEventsListener implements BranchViewHandler.IBranchViewEvents {

        private CallbackContext _callbackContext;

        public BranchViewEventsListener(CallbackContext callbackContext) {
            callbackContext.success("Success");
        }

        @Override
        public void onBranchViewVisible(String action, String branchViewID) {

        }

        @Override
        public void onBranchViewAccepted(String action, String branchViewID) {

        }

        @Override
        public void onBranchViewCancelled(String action, String branchViewID) {

        }

        @Override
        public void onBranchViewError(int errorCode, String errorMsg, String action) {

        }
    }

    protected class SessionListener implements Branch.BranchReferralInitListener {
        private CallbackContext _callbackContext;

        public SessionListener(CallbackContext callbackContext) {
            this._callbackContext = callbackContext;
        }

        //Listener that implements BranchReferralInitListener for initSession
        @Override
        public void onInitFinished(JSONObject referringParams, BranchError error) {

            String out;

            if (error == null && referringParams != null) {
                if (this._callbackContext != null) {
                    this._callbackContext.success(referringParams);
                }
            } else {
                JSONObject message = new JSONObject();
                try {
                    message.put("error", error.getMessage());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (this._callbackContext != null) {
                    this._callbackContext.error(message);
                }
            }


        }

    }

    protected class LogoutStatusListener implements Branch.LogoutStatusListener {
        private CallbackContext _callbackContext;

        // Constructor that takes in a required callbackContext object
        public LogoutStatusListener(CallbackContext callbackContext) {
            this._callbackContext = callbackContext;
        }

        /**
         * Called on finishing the the logout process
         *
         * @param loggedOut A {@link Boolean} which is set to true if logout succeeded
         * @param error     An instance of {@link BranchError} to notify any error occurred during logout.
         *                  A null value is set if logout succeeded.
         */
        @Override
        public void onLogoutFinished(boolean loggedOut, BranchError error) {
            if (error == null) {

                branchObjectWrappers = new ArrayList<BranchUniversalObjectWrapper>();

                this._callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, /* send boolean: is logged out */ loggedOut));

            } else {

                Log.d(LCAT, "error on logout");

                this._callbackContext.error(error.getMessage());
            }
        }
    }

    protected class SetIdentityListener implements Branch.BranchReferralInitListener {
        private CallbackContext _callbackContext;

        public SetIdentityListener(CallbackContext callbackContext) {
            this._callbackContext = callbackContext;
        }

        //Listener that implements BranchReferralInitListener for setIdentity
        @Override
        public void onInitFinished(JSONObject referringParams, BranchError error) {

            if (error == null) {

                this._callbackContext.success(referringParams);

            } else {

                String errorMessage = error.getMessage();

                Log.d(LCAT, errorMessage);

                this._callbackContext.error(errorMessage);

            }

        }


    }

    protected class RegisterViewStatusListener implements BranchUniversalObject.RegisterViewStatusListener {

        private CallbackContext _callbackContext;

        public RegisterViewStatusListener(CallbackContext callbackContext) {
            this._callbackContext = callbackContext;
        }

        @Override
        public void onRegisterViewFinished(boolean registered, BranchError error) {

            if (error == null) {
                this._callbackContext.success("Success");
            } else {

                String errorMessage = error.getMessage();

                Log.d(LCAT, errorMessage);

                this._callbackContext.error(errorMessage);
            }
        }
    }

    protected class LoadRewardsListener implements Branch.BranchReferralStateChangedListener {
        private CallbackContext _callbackContext;
        private Branch _instance;
        private String _bucket;

        public LoadRewardsListener(String bucket, CallbackContext callbackContext, Branch instance) {
            this._callbackContext = callbackContext;
            this._instance = instance;
            this._bucket = bucket;
        }

        public LoadRewardsListener(CallbackContext callbackContext, Branch instance) {
            this._callbackContext = callbackContext;
            this._instance = instance;
            this._bucket = "";
        }

        // Listener that implements BranchReferralStateChangedListener for loadRewards
        @Override
        public void onStateChanged(boolean changed, BranchError error) {
            if (error == null) {

                int credits = 0;

                if (this._bucket.length() > 0) {
                    credits = this._instance.getCreditsForBucket(this._bucket);
                } else {
                    credits = this._instance.getCredits();
                }

                this._callbackContext.success(credits);

            } else {

                String errorMessage = error.getMessage();

                Log.d(LCAT, errorMessage);

                this._callbackContext.error(errorMessage);

            }

        }
    }

    protected class RedeemRewardsListener implements Branch.BranchReferralStateChangedListener {
        private CallbackContext _callbackContext;

        // Constructor that takes in a required callbackContext object
        public RedeemRewardsListener(CallbackContext callbackContext) {
            this._callbackContext = callbackContext;
        }

        // Listener that implements BranchReferralStateChangedListener for redeemRewards
        @Override
        public void onStateChanged(boolean changed, BranchError error) {

            if (error == null) {

                this._callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, /* send boolean: is changed */ changed));

            } else {

                String errorMessage = error.getMessage();

                Log.d(LCAT, errorMessage);

                this._callbackContext.error(errorMessage);
            }
        }
    }

    protected class GenerateShortUrlListener implements Branch.BranchLinkCreateListener {
        private CallbackContext _callbackContext;

        // Constructor that takes in a required callbackContext object
        public GenerateShortUrlListener(CallbackContext callbackContext) {

            this._callbackContext = callbackContext;

        }

        @Override
        public void onLinkCreate(String url, BranchError error) {

            JSONObject response = new JSONObject();

            if (error == null || url != null) {

                try {
                    response.put("url", url);
                } catch (JSONException e) {
                    e.printStackTrace();
                    return;
                }

                Log.d(LCAT, response.toString());
                this._callbackContext.success(response);

            } else {

                String errorMessage = error.getMessage();

                Log.d(LCAT, errorMessage);

                try {
                    response.put("error", errorMessage);
                } catch (JSONException e) {
                    e.printStackTrace();
                    return;
                }

                Log.d(LCAT, response.toString());
                this._callbackContext.error(response);

            }

        }

    }

    protected class ShowShareSheetListener implements Branch.BranchLinkShareListener {

        private CallbackContext _onShareLinkDialogLaunched;
        private CallbackContext _onShareLinkDialogDismissed;
        private CallbackContext _onLinkShareResponse;
        private CallbackContext _onChannelSelected;

        /**
         * @param onShareLinkDialogLaunched
         * @param onShareLinkDialogDismissed
         * @param onLinkShareResponse
         * @param onChannelSelected
         * @constructor
         */
        public ShowShareSheetListener(CallbackContext onShareLinkDialogLaunched, CallbackContext onShareLinkDialogDismissed, CallbackContext onLinkShareResponse, CallbackContext onChannelSelected) {

            this._onShareLinkDialogDismissed = onShareLinkDialogDismissed;
            this._onShareLinkDialogLaunched = onShareLinkDialogLaunched;
            this._onLinkShareResponse = onLinkShareResponse;
            this._onChannelSelected = onChannelSelected;

        }

        @Override
        public void onShareLinkDialogLaunched() {

            if (_onShareLinkDialogLaunched == null) {
                return;
            }

            PluginResult result = new PluginResult(PluginResult.Status.OK);

            result.setKeepCallback(true);

            this._onShareLinkDialogLaunched.sendPluginResult(result);

        }

        @Override
        public void onShareLinkDialogDismissed() {

            if (_onShareLinkDialogDismissed == null) {
                return;
            }

            PluginResult result = new PluginResult(PluginResult.Status.OK);

            result.setKeepCallback(true);

            this._onShareLinkDialogDismissed.sendPluginResult(result);

        }

        @Override
        public void onLinkShareResponse(String sharedLink, String sharedChannel, BranchError error) {

            if (_onLinkShareResponse == null) {
                return;
            }

            JSONObject response = new JSONObject();

            if (error == null) {

                try {
                    response.put("sharedLink", sharedLink);
                    response.put("sharedChannel", sharedChannel);
                } catch (JSONException e) {
                    e.printStackTrace();
                    return;
                }

                Log.d(LCAT, "sharedLink: " + sharedLink);
                Log.d(LCAT, "sharedChannel: " + sharedChannel);
            } else {
                String errorMessage = error.getMessage();

                Log.d(LCAT, errorMessage);

                try {
                    response.put("error", errorMessage);
                } catch (JSONException e) {
                    e.printStackTrace();
                    return;
                }
            }

            Log.d(LCAT, response.toString());

            PluginResult result = new PluginResult(PluginResult.Status.OK, response);

            result.setKeepCallback(true);

            this._onLinkShareResponse.sendPluginResult(result);

        }

        @Override
        public void onChannelSelected(String channelName) {

            if (_onChannelSelected == null) {
                return;
            }

            JSONObject response = new JSONObject();

            try {
                response.put("channelName", channelName);
            } catch (JSONException e) {
                e.printStackTrace();
                return;
            }

            Log.d(LCAT, response.toString());

            PluginResult result = new PluginResult(PluginResult.Status.OK, response);

            result.setKeepCallback(true);

            this._onChannelSelected.sendPluginResult(result);

        }
    }

    protected class CreditHistoryListener implements Branch.BranchListResponseListener {
        private CallbackContext _callbackContext;

        // Constructor that takes in a required callbackContext object
        public CreditHistoryListener(CallbackContext callbackContext) {
            this._callbackContext = callbackContext;
        }

        // Listener that implements BranchListResponseListener for getCreditHistory()
        @Override
        public void onReceivingResponse(JSONArray list, BranchError error) {

            ArrayList<String> errors = new ArrayList<String>();

            if (error == null) {

                JSONArray data = new JSONArray();

                if (list != null) {

                    for (int i = 0, limit = list.length(); i < limit; ++i) {

                        JSONObject entry;

                        try {
                            entry = list.getJSONObject(i);
                            data.put(entry);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            errors.add(e.getMessage());
                        }

                    }

                }

                if (errors.size() > 0) {
                    StringBuilder sb = new StringBuilder();
                    for (String s : errors) {
                        sb.append(s);
                        sb.append("\n");
                    }
                    this._callbackContext.error(sb.toString());
                } else {
                    this._callbackContext.success(data);
                }
            } else {

                String errorMessage = error.getMessage();

                Log.d(LCAT, errorMessage);

                this._callbackContext.error(errorMessage);

            }
        }
    }

    protected class RunnableThread implements Runnable {

        private String action;
        private JSONArray args;
        private CallbackContext callbackContext;

        public RunnableThread(String action, JSONArray args, CallbackContext callbackContext) {
            this.callbackContext = callbackContext;
            this.action = action;
            this.args = args;
        }

        public void run() {
            try {
                Log.d(LCAT, "Runnable: " + this.action);

                if (this.action.equals("setDebug")) {
                    setDebug(this.args.getBoolean(0), this.callbackContext);
                } else if (this.action.equals("setCookieBasedMatching")) {
                    setCookieBasedMatching(this.args.getString(0), this.callbackContext);
                } else if (this.action.equals("disableTracking")) {
                    disableTracking(this.args.getBoolean(0), this.callbackContext);
                } else if (this.action.equals("initSession")) {
                    boolean keepCallBack = this.args.length() != 0 && this.args.getBoolean(0);
                    initSession(keepCallBack, this.callbackContext);
                } else if (this.action.equals("setRequestMetadata")) {
                    setRequestMetadata(this.args.getString(0), this.args.getString(1), this.callbackContext);
                } else {
                    if (this.action.equals("setIdentity")) {
                        setIdentity(this.args.getString(0), this.callbackContext);
                    } else if (this.action.equals("userCompletedAction")) {
                        if (this.args.length() == 2) {
                            userCompletedAction(this.args.getString(0), this.args.getJSONObject(1), this.callbackContext);
                        } else if (this.args.length() == 1) {
                            userCompletedAction(this.args.getString(0), this.callbackContext);
                        }
                    } else if (this.action.equals("sendCommerceEvent")) {
                        sendCommerceEvent(this.args.getJSONObject(0), this.args.getJSONObject(1), this.callbackContext);
                    } else if (this.action.equals("sendBranchEvent")) {
                        if (this.args.length() == 2) {
                            sendBranchEvent(this.args.getString(0), this.args.getJSONObject(1), this.callbackContext);
                        } else if (this.args.length() == 1) {
                            sendBranchEvent(this.args.getString(0), this.callbackContext);
                        }
                    } else if (this.action.equals("getFirstReferringParams")) {
                        getFirstReferringParams(this.callbackContext);
                    } else if (this.action.equals("getLatestReferringParams")) {
                        getLatestReferringParams(this.callbackContext);
                    } else if (this.action.equals("logout")) {
                        logout(this.callbackContext);
                    } else if (this.action.equals("loadRewards")) {
                        if (this.args.length() == 1) {
                            loadRewards(this.args.getString(0), this.callbackContext);
                        } else {
                            loadRewards(this.callbackContext);
                        }
                    } else if (this.action.equals("redeemRewards")) {
                        if (this.args.length() == 1) {
                            redeemRewards(this.args.getInt(0), this.callbackContext);
                        } else if (this.args.length() == 2) {
                            redeemRewards(this.args.getInt(0), this.args.getString(1), this.callbackContext);
                        }
                    } else if (this.action.equals("getCreditHistory")) {
                        getCreditHistory(this.callbackContext);
                    } else if (this.action.equals("createBranchUniversalObject")) {
                        createBranchUniversalObject(this.args.getJSONObject(0), this.callbackContext);
                    } else if (this.action.equals("crossPlatformIds")) {
                        crossPlatformIds(this.callbackContext);
                    } else if (this.action.equals("lastAttributedTouchData")) {
                        lastAttributedTouchData(this.callbackContext);
                    } else if (this.action.equals(("generateShortUrl"))) {
                        generateShortUrl(this.args.getInt(0), this.args.getJSONObject(1), this.args.getJSONObject(2), this.callbackContext);
                    } else if (this.action.equals("registerView")) {
                        registerView(this.args.getInt(0), this.callbackContext);
                    } else if (this.action.equals("showShareSheet")) {
                        Object userLocalization = this.args.opt(3);
                        JSONObject localization = new JSONObject();
                        //Limitation is that the user must override all the strings
                        if (userLocalization != null && userLocalization instanceof JSONObject) {
                            localization = (JSONObject) userLocalization;
                        } else {
                            localization.put("shareText", this.args.getString(3) != null ? this.args.getString(3) : "This stuff is awesome: ");
                            localization.put("shareTitle", "Check this out!");
                            localization.put("copyToClipboard", "Copy");
                            localization.put("clipboardSuccess", "Added to clipboard");
                            localization.put("more", "Show More");
                            localization.put("shareWith", "Share With");
                        }
                        showShareSheet(this.args.getInt(0), this.args.getJSONObject(1), this.args.getJSONObject(2), localization);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
