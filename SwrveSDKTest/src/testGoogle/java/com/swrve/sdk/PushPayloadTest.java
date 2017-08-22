package com.swrve.sdk;

import com.google.gson.JsonSyntaxException;
import com.swrve.sdk.model.PayloadButton;
import com.swrve.sdk.model.PayloadMedia;
import com.swrve.sdk.model.PushPayload;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.junit.Assert.assertNull;

public class PushPayloadTest extends SwrveBaseTest {

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void testPushPayloadHappyCase() {
        String json = "{\n" +
                " \"title\": \"title\",\n" +
                " \"subtitle\": \"subtitle\",\n" +
                " \"icon_url\": \"https://icon.png\",\n" +
                " \"version\": 1,\n" +
                " \"media\": { \"title\": \"[rich title]\",\n" +
                "              \"subtitle\": \"[rich subtitle]\",\n" +
                "              \"body\": \"[rich body]\",\n"+
                "              \"type\": \"image\",\n" +
                "              \"url\": \"https://media.jpg\",\n" +
                "              \"fallback_type\": \"image\",\n" +
                "              \"fallback_url\": \"https://fallback.jpg\",\n" +
                "              \"fallback_sd\": \"https://video.com\"\n }," +
                "\"expanded\": { \"title\": \"[expanded title]\",\n" +
                "                \"body\": \"[expanded body]\",\n" +
                "                \"icon_url\": \"https://expanded_icon.png\"}, \n" +
                "\"buttons\": [{\"title\": \"[button text 1]\",\n" +
                "                 \"action_type\": \"open_url\",\n" +
                "                 \"action\": \"https://lovelyURL\"},\n" +
                "                {\"title\": \"[button text 2]\",\n" +
                "                 \"action_type\": \"open_app\"},\n" +
                "                {\"title\": \"[button text 3]\",\n" +
                "                 \"action_type\": \"dismiss\"}]\n" +
                "}\n";

        PushPayload payload = PushPayload.fromJson(json);
        assertNotNull(payload);
        assertEquals("title", payload.getTitle());
        assertEquals("subtitle", payload.getSubtitle());
        assertEquals("https://icon.png", payload.getIconUrl());

        assertNotNull(payload.getMedia());
        assertEquals("[rich title]", payload.getMedia().getTitle());
        assertEquals("[rich body]", payload.getMedia().getBody());
        assertEquals(PayloadMedia.MediaType.IMAGE, payload.getMedia().getType());
        assertEquals("https://media.jpg", payload.getMedia().getUrl());
        assertEquals(PayloadMedia.MediaType.IMAGE, payload.getMedia().getFallbackType());
        assertEquals("https://fallback.jpg", payload.getMedia().getFallbackUrl());
        assertEquals("https://video.com", payload.getMedia().getFallbackSd());

        assertNotNull(payload.getExpanded());
        assertEquals("[expanded title]", payload.getExpanded().getTitle());
        assertEquals("[expanded body]", payload.getExpanded().getBody());
        assertEquals("https://expanded_icon.png", payload.getExpanded().getIconUrl());

        assertNotNull(payload.getButtons());
        assertEquals("[button text 1]", payload.getButtons().get(0).getTitle());
        assertEquals("[button text 2]", payload.getButtons().get(1).getTitle());
        assertEquals("[button text 3]", payload.getButtons().get(2).getTitle());
        assertEquals(PayloadButton.ActionType.OPEN_URL, payload.getButtons().get(0).getActionType());
        assertEquals(PayloadButton.ActionType.OPEN_APP, payload.getButtons().get(1).getActionType());
        assertEquals(PayloadButton.ActionType.DISMISS, payload.getButtons().get(2).getActionType());
        assertEquals("https://lovelyURL", payload.getButtons().get(0).getAction());
    }

    @Test
    public void testPushPayloadOnlyExtended() {
        String json = "{\n" +
                " \"title\": \"title\",\n" +
                " \"subtitle\": \"subtitle\",\n" +
                " \"icon_url\": \"https://icon.png\",\n" +
                " \"version\": 1,\n" +
                " \"expanded\": { \"title\": \"[expanded title]\",\n" +
                "                 \"body\": \"[expanded body]\",\n" +
                "                 \"icon_url\": \"https://expanded_icon.png\"}\n" +
                "}\n";

        PushPayload payload = PushPayload.fromJson(json);
        assertNotNull(payload);
        assertEquals("title", payload.getTitle());
        assertEquals("subtitle", payload.getSubtitle());
        assertEquals("https://icon.png", payload.getIconUrl());

        assertNull(payload.getMedia());

        assertNotNull(payload.getExpanded());
        assertEquals("[expanded title]", payload.getExpanded().getTitle());
        assertEquals("[expanded body]", payload.getExpanded().getBody());
        assertEquals("https://expanded_icon.png", payload.getExpanded().getIconUrl());

        assertNull(payload.getButtons());
    }

    @Test
    public void testPushPayloadMediaOnly() {
        String json = "{\n" +
                " \"title\": \"title\",\n" +
                " \"subtitle\": \"subtitle\",\n" +
                " \"icon_url\": \"https://icon.png\",\n" +
                " \"version\": 1,\n" +
                " \"media\": { \"title\": \"[rich title]\",\n" +
                "              \"subtitle\": \"[rich subtitle]\",\n" +
                "              \"body\": \"[rich body]\",\n"+
                "              \"type\": \"image\",\n" +
                "              \"url\": \"https://media.jpg\",\n" +
                "              \"fallback_type\": \"image\",\n" +
                "              \"fallback_url\": \"https://fallback.jpg\",\n" +
                "              \"fallback_sd\": \"https://video.com\"\n }" +
                "}\n";

        PushPayload payload = PushPayload.fromJson(json);
        assertNotNull(payload);
        assertEquals("title", payload.getTitle());
        assertEquals("subtitle", payload.getSubtitle());
        assertEquals("https://icon.png", payload.getIconUrl());

        assertNotNull(payload.getMedia());
        assertEquals("[rich title]", payload.getMedia().getTitle());
        assertEquals("[rich body]", payload.getMedia().getBody());
        assertEquals(PayloadMedia.MediaType.IMAGE, payload.getMedia().getType());
        assertEquals("https://media.jpg", payload.getMedia().getUrl());
        assertEquals(PayloadMedia.MediaType.IMAGE, payload.getMedia().getFallbackType());
        assertEquals("https://fallback.jpg", payload.getMedia().getFallbackUrl());
        assertEquals("https://video.com", payload.getMedia().getFallbackSd());

        assertNull(payload.getExpanded());
        assertNull(payload.getButtons());
    }

    @Test
    public void testPushPayloadNoButtons() {

        String json = "{\n" +
                " \"title\": \"title\",\n" +
                " \"subtitle\": \"subtitle\",\n" +
                " \"icon_url\": \"https://icon.png\",\n" +
                " \"version\": 1,\n" +
                " \"media\": { \"title\": \"[rich title]\",\n" +
                "              \"subtitle\": \"[rich subtitle]\",\n" +
                "              \"body\": \"[rich body]\",\n"+
                "              \"type\": \"image\",\n" +
                "              \"url\": \"https://media.jpg\",\n" +
                "              \"fallback_type\": \"image\",\n" +
                "              \"fallback_url\": \"https://fallback.jpg\",\n" +
                "              \"fallback_sd\": \"https://video.com\"\n }," +
                "\"expanded\": { \"title\": \"[expanded title]\",\n" +
                "                \"body\": \"[expanded body]\",\n" +
                "                \"icon_url\": \"https://expanded_icon.png\"} \n" +
                "}\n";
        PushPayload payload = PushPayload.fromJson(json);
        assertNotNull(payload);
        assertEquals("title", payload.getTitle());
        assertEquals("subtitle", payload.getSubtitle());
        assertEquals("https://icon.png", payload.getIconUrl());

        assertNotNull(payload.getMedia());
        assertEquals("[rich title]", payload.getMedia().getTitle());
        assertEquals("[rich body]", payload.getMedia().getBody());
        assertEquals(PayloadMedia.MediaType.IMAGE, payload.getMedia().getType());
        assertEquals("https://media.jpg", payload.getMedia().getUrl());
        assertEquals(PayloadMedia.MediaType.IMAGE, payload.getMedia().getFallbackType());
        assertEquals("https://fallback.jpg", payload.getMedia().getFallbackUrl());
        assertEquals("https://video.com", payload.getMedia().getFallbackSd());

        assertNotNull(payload.getExpanded());
        assertEquals("[expanded title]", payload.getExpanded().getTitle());
        assertEquals("[expanded body]", payload.getExpanded().getBody());
        assertEquals("https://expanded_icon.png", payload.getExpanded().getIconUrl());

        assertNull(payload.getButtons());
    }

    @Test
    public void testPushPayloadAdditionalParameters() {

        String json = "{\n" +
                " \"title\": \"title\",\n" +
                " \"subtitle\": \"subtitle\",\n" +
                " \"ticker\": \"ticker message\",\n" +
                " \"icon_url\": \"https://icon.png\",\n" +
                " \"version\": 1,\n" +
                " \"notification_id\": 222,\n" +
                " \"accent\": \"#00FF0000\",\n" +
                " \"visibility\": \"public\",\n" +
                " \"lock_screen_msg\": \"Lock Screen Message\",\n" +
                " \"media\": { \"title\": \"[rich title]\",\n" +
                "              \"subtitle\": \"[rich subtitle]\",\n" +
                "              \"body\": \"[rich body]\",\n"+
                "              \"type\": \"image\",\n" +
                "              \"url\": \"https://media.jpg\"}," +
                "\"expanded\": { \"title\": \"[expanded title]\",\n" +
                "                \"body\": \"[expanded body]\",\n" +
                "                \"icon_url\": \"https://expanded_icon.png\"}, \n" +
                " \"channel_id\": \"my_notification_channel_id\"\n" +
                "}\n";

        PushPayload payload = PushPayload.fromJson(json);
        assertNotNull(payload);
        assertEquals(1, payload.getVersion());
        assertEquals("title", payload.getTitle());
        assertEquals("subtitle", payload.getSubtitle());
        assertEquals("ticker message", payload.getTicker());
        assertEquals("https://icon.png", payload.getIconUrl());
        assertEquals(222, payload.getNotificationId());
        assertEquals("#00FF0000", payload.getAccent());
        assertEquals("Lock Screen Message", payload.getLockScreenMsg());
        assertEquals(PushPayload.VisibilityType.PUBLIC, payload.getVisibility());

        assertNotNull(payload.getMedia());
        assertEquals("[rich title]", payload.getMedia().getTitle());
        assertEquals("[rich body]", payload.getMedia().getBody());
        assertEquals(PayloadMedia.MediaType.IMAGE, payload.getMedia().getType());
        assertEquals("https://media.jpg", payload.getMedia().getUrl());

        assertNotNull(payload.getExpanded());
        assertEquals("[expanded title]", payload.getExpanded().getTitle());
        assertEquals("[expanded body]", payload.getExpanded().getBody());
        assertEquals("https://expanded_icon.png", payload.getExpanded().getIconUrl());
        assertEquals("my_notification_channel_id", payload.getChannelId());

        assertNull(payload.getButtons());
    }

    @Test
    public void testPushPayloadWrongFormat() throws JsonSyntaxException {
        String json = "{\n" +
                " \"title\": {},\n" +
                " \"subtitle\": \"subtitle\",\n" +
                " \"icon_url\": \"https://icon.png\",\n" +
                " \"version\": 1,\n" +
                " \"media\": { \"title\": \"[rich title]\",\n" +
                "              \"subtitle\": \"[rich subtitle]\",\n" +
                "              \"body\": \"[rich body]\",\n"+
                "              \"type\": \"image\",\n" +
                "              \"url\": \"https://media.jpg\",\n" +
                "              \"fallback_type\": \"image\",\n" +
                "              \"fallback_url\": \"https://fallback.jpg\",\n" +
                "              \"fallback_sd\": \"https://video.com\"\n }," +
                "\"expanded\": { \"title\": \"[expanded title]\",\n" +
                "                \"body\": \"[expanded body]\",\n" +
                "                \"icon_url\": \"https://expanded_icon.png\"} \n" +
                "}\n";
        PushPayload payload = PushPayload.fromJson(json);
        assertNull(payload);
    }
}