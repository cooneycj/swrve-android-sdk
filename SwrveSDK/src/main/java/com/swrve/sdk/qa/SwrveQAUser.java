package com.swrve.sdk.qa;

import android.os.Bundle;

import com.swrve.sdk.SwrveCampaignDisplayer;
import com.swrve.sdk.SwrveHelper;
import com.swrve.sdk.SwrveLogger;
import com.swrve.sdk.SwrveSDK;
import com.swrve.sdk.conversations.SwrveConversation;
import com.swrve.sdk.messaging.SwrveMessage;
import com.swrve.sdk.rest.IRESTClient;
import com.swrve.sdk.rest.IRESTResponseListener;
import com.swrve.sdk.rest.RESTResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Used internally to offer QA user functionality.
 */
public class SwrveQAUser {
    public static final int QA_API_VERSION = 1;
    protected static final long REST_SESSION_INTERVAL = 1000;
    protected static final long REST_TRIGGER_INTERVAL = 500;
    private static Set<WeakReference<SwrveQAUser>> bindedObjects = new HashSet<>();
    protected final SimpleDateFormat deviceTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ", Locale.US);
    protected ExecutorService restClientExecutor;
    protected int appId;
    protected String apiKey;
    protected String userId;
    private IRESTClient restClient;
    private boolean resetDevice;
    private boolean logging;
    private String loggingUrl;
    private long lastSessionRequestTime;
    private long lastTriggerRequestTime;

    public SwrveQAUser(int appId, String apiKey, String userId, IRESTClient restClient, JSONObject jsonQa) {
        this.appId = appId;
        this.apiKey = apiKey;
        this.userId = userId;
        this.resetDevice = jsonQa.optBoolean("reset_device_state", false);
        this.logging = jsonQa.optBoolean("logging", false);
        if (logging) {
            this.restClient = restClient;
            this.restClientExecutor = Executors.newSingleThreadExecutor();
            this.loggingUrl = jsonQa.optString("logging_url", null);
        }
    }

    public static Set<SwrveQAUser> getBindedListeners() {
        HashSet<SwrveQAUser> result = new HashSet<>();
        Iterator<WeakReference<SwrveQAUser>> iter = bindedObjects.iterator();
        while (iter.hasNext()) {
            SwrveQAUser sdkListener = iter.next().get();
            if (sdkListener == null) {
                iter.remove();
            } else {
                result.add(sdkListener);
            }
        }

        return result;
    }

    public boolean isResetDevice() {
        return resetDevice;
    }

    public boolean isLogging() {
        return logging && (loggingUrl != null);
    }

    public void talkSession(Map<Integer, String> campaignsDownloaded) {
        try {
            if (canMakeSessionRequest()) {
                String endpoint = loggingUrl + "/talk/game/" + apiKey + "/user/" + userId + "/session";
                JSONObject talkSessionJson = new JSONObject();

                // Add campaigns (downloaded or not) to request
                JSONArray campaignsJson = new JSONArray();
                for(int campaignId: campaignsDownloaded.keySet()) {
                    String reason = campaignsDownloaded.get(campaignId);

                    JSONObject campaignInfo = new JSONObject();
                    campaignInfo.put("id", campaignId);
                    campaignInfo.put("reason", (reason == null) ? "" : reason);
                    campaignInfo.put("loaded", (reason == null));
                    campaignsJson.put(campaignInfo);
                }
                talkSessionJson.put("campaigns", campaignsJson);
                // Add device info to request
                JSONObject deviceJson = SwrveSDK.getDeviceInfo();
                talkSessionJson.put("device", deviceJson);

                makeRequest(endpoint, talkSessionJson);
            }
        } catch (Exception exp) {
            SwrveLogger.e("QA request talk session failed", exp);
        }
    }

    public void triggerFailure(String event, String globalReason) {
        try {
            if (canMakeTriggerRequest()) {
                String endpoint = loggingUrl + "/talk/game/" + apiKey + "/user/" + userId + "/trigger";
                JSONObject triggerJson = new JSONObject();
                triggerJson.put("trigger_name", event);
                triggerJson.put("displayed", false);
                triggerJson.put("reason", globalReason);
                triggerJson.put("campaigns", new JSONArray());

                makeRequest(endpoint, triggerJson);
            }
        } catch (Exception exp) {
            SwrveLogger.e("QA request talk session failed", exp);
        }
    }


    public void trigger(String event, SwrveConversation conversationShown, Map<Integer,
            SwrveCampaignDisplayer.Result> campaignDisplayResults, Map<Integer, Integer> campaignConversations) {
        try {
            if (canMakeTriggerRequest()) {
                String endpoint = loggingUrl + "/talk/game/" + apiKey + "/user/" + userId + "/trigger";
                JSONObject triggerJson = new JSONObject();
                triggerJson.put("trigger_name", event);
                triggerJson.put("displayed", (conversationShown != null));
                triggerJson.put("reason", (conversationShown == null) ? "The loaded campaigns returned no conversation" : "");

                // Add campaigns that were not displayed
                JSONArray campaignsJson = new JSONArray();
                for (int campaignId : campaignDisplayResults.keySet()) {
                    SwrveCampaignDisplayer.Result result = campaignDisplayResults.get(campaignId);
                    Integer conversationId = campaignConversations.get(campaignId);

                    JSONObject campaignInfo = new JSONObject();
                    campaignInfo.put("id", campaignId);
                    campaignInfo.put("displayed", false);
                    campaignInfo.put("conversation_id", (conversationId == null) ? -1 : conversationId);
                    campaignInfo.put("reason", (result == null) ? "" : result.resultText);
                    campaignsJson.put(campaignInfo);
                }

                // Add campaign that was shown, if available
                if (conversationShown != null) {
                    JSONObject campaignInfo = new JSONObject();
                    campaignInfo.put("id", conversationShown.getCampaign().getId());
                    campaignInfo.put("displayed", true);
                    campaignInfo.put("conversation_id", conversationShown.getId());
                    campaignInfo.put("reason", "");
                    campaignsJson.put(campaignInfo);
                }
                triggerJson.put("campaigns", campaignsJson);

                makeRequest(endpoint, triggerJson);
            }
        } catch (Exception exp) {
            SwrveLogger.e("QA request talk session failed", exp);
        }
    }

    public void trigger(String event, SwrveMessage messageShown, Map<Integer,
            SwrveCampaignDisplayer.Result> campaignDisplayResults, Map<Integer, Integer> campaignMessages) {
        try {
            if (canMakeTriggerRequest()) {
                String endpoint = loggingUrl + "/talk/game/" + apiKey + "/user/" + userId + "/trigger";
                JSONObject triggerJson = new JSONObject();
                triggerJson.put("trigger_name", event);
                triggerJson.put("displayed", (messageShown != null));
                triggerJson.put("reason", (messageShown == null) ? "The loaded campaigns returned no message" : "");

                // Add campaigns that were not displayed
                JSONArray campaignsJson = new JSONArray();
                for (int campaignId : campaignDisplayResults.keySet()) {
                    SwrveCampaignDisplayer.Result result = campaignDisplayResults.get(campaignId);
                    Integer messageId = campaignMessages.get(campaignId);

                    JSONObject campaignInfo = new JSONObject();
                    campaignInfo.put("id", campaignId);
                    campaignInfo.put("displayed", false);
                    campaignInfo.put("message_id", (messageId == null) ? -1 : messageId);
                    campaignInfo.put("reason", (result == null) ? "" : result.resultText);
                    campaignsJson.put(campaignInfo);
                }

                // Add campaign that was shown, if available
                if (messageShown != null) {
                    JSONObject campaignInfo = new JSONObject();
                    campaignInfo.put("id", messageShown.getCampaign().getId());
                    campaignInfo.put("displayed", true);
                    campaignInfo.put("message_id", messageShown.getId());
                    campaignInfo.put("reason", "");
                    campaignsJson.put(campaignInfo);
                }
                triggerJson.put("campaigns", campaignsJson);

                makeRequest(endpoint, triggerJson);
            }
        } catch (Exception exp) {
            SwrveLogger.e("QA request talk session failed", exp);
        }
    }

    public void logDeviceInfo(JSONObject deviceJson) {
        try {
            if (canMakeRequest()) {
                String endpoint = loggingUrl + "/talk/game/" + apiKey + "/user/" + userId + "/device_info";
                makeRequest(endpoint, deviceJson);
            }
        } catch (Exception exp) {
            SwrveLogger.e("QA request device info failed", exp);
        }
    }

    public void pushNotification(String trackingId, Bundle msg) {
        try {
            if (canMakeTriggerRequest()) {
                if (!SwrveHelper.isNullOrEmpty(trackingId)) {
                    String endpoint = loggingUrl + "/talk/game/" + apiKey + "/user/" + userId + "/push";
                    JSONObject pushJson = new JSONObject();
                    pushJson.put("id", trackingId);
                    pushJson.put("alert", msg.getString("text"));
                    pushJson.put("sound", msg.getString("sound"));
                    pushJson.put("badge", "");
                    makeRequest(endpoint, pushJson);
                } else {
                    SwrveLogger.e("Push notification does not have a proper _p value");
                }
            }
        } catch (Exception exp) {
            SwrveLogger.e("QA request talk session failed", exp);
        }
    }

    public void bindToServices() {
        for (WeakReference<SwrveQAUser> weakQaUser : bindedObjects) {
            SwrveQAUser sdkListener = weakQaUser.get();
            if (sdkListener == this) {
                return;
            }
        }
        bindedObjects.add(new WeakReference<>(this));
    }

    public void unbindToServices() {
        // Remove the weak reference to the listener
        Iterator<WeakReference<SwrveQAUser>> iter = bindedObjects.iterator();
        while (iter.hasNext()) {
            SwrveQAUser sdkListener = iter.next().get();
            if (sdkListener == this) {
                iter.remove();
                break;
            }
        }
    }

    private void makeRequest(final String endpoint, JSONObject json) throws JSONException {
        json.put("version", QA_API_VERSION);
        json.put("client_time", deviceTimeFormat.format(new Date()));
        final String body = json.toString();

        restClientExecutor.execute(new Runnable() {
            @Override
            public void run() {
                restClient.post(endpoint, body, new RESTResponseListener(endpoint));
            }
        });
    }

    private boolean canMakeRequest() {
        return isLogging();
    }

    private boolean canMakeSessionRequest() {
        if (canMakeRequest()) {
            long currentTime = (new Date()).getTime();
            if (lastSessionRequestTime == 0 || (currentTime - lastSessionRequestTime) > REST_SESSION_INTERVAL) {
                lastSessionRequestTime = currentTime;
                return true;
            }
        }

        return false;
    }

    private boolean canMakeTriggerRequest() {
        if (canMakeRequest()) {
            long currentTime = (new Date()).getTime();
            if (lastTriggerRequestTime == 0 || (currentTime - lastTriggerRequestTime) > REST_TRIGGER_INTERVAL) {
                lastTriggerRequestTime = currentTime;
                return true;
            }
        }

        return false;
    }

    private class RESTResponseListener implements IRESTResponseListener {
        private String endpoint;

        public RESTResponseListener(String endpoint) {
            this.endpoint = endpoint;
        }

        @Override
        public void onResponse(RESTResponse response) {
            if (!SwrveHelper.successResponseCode(response.responseCode)) {
                SwrveLogger.e("QA request to %s failed with error code %s: %s", endpoint, response.responseCode, response.responseBody);
            }
        }

        @Override
        public void onException(Exception exp) {
            SwrveLogger.e("QA request to %s failed", exp, endpoint);
        }
    }
}
