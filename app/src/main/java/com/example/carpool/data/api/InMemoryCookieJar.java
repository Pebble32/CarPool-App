package com.example.carpool.data.api;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

/**
 * An improved implementation of the {@link CookieJar} interface that stores cookies in memory
 * with added logging to help diagnose session-related issues.
 */
public class InMemoryCookieJar implements CookieJar {
    private static final String TAG = "InMemoryCookieJar";
    private final Map<String, List<Cookie>> cookieStore = new HashMap<>();

    @Override
    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
        String host = url.host();
        Log.d(TAG, "Saving " + cookies.size() + " cookies for host: " + host);

        // Log cookie details to help debug session issues
        for (Cookie cookie : cookies) {
            Log.d(TAG, "Cookie: " + cookie.name() + "=" + (cookie.name().equals("JSESSIONID") ?
                    "[SESSION ID]" : cookie.value().substring(0, Math.min(cookie.value().length(), 10)) + "...") +
                    ", expires: " + (cookie.expiresAt() > 0 ? cookie.expiresAt() : "session") +
                    ", secure: " + cookie.secure() +
                    ", httpOnly: " + cookie.httpOnly());
        }

        cookieStore.put(host, cookies);
    }

    @Override
    public List<Cookie> loadForRequest(HttpUrl url) {
        String host = url.host();
        List<Cookie> cookies = cookieStore.get(host);

        if (cookies != null && !cookies.isEmpty()) {
            Log.d(TAG, "Loading " + cookies.size() + " cookies for host: " + host);

            // Check for expired cookies
            List<Cookie> validCookies = new ArrayList<>();
            for (Cookie cookie : cookies) {
                if (cookie.expiresAt() > System.currentTimeMillis() || cookie.expiresAt() == 0) {
                    validCookies.add(cookie);
                } else {
                    Log.w(TAG, "Cookie " + cookie.name() + " has expired, removing from store");
                }
            }

            // If we filtered out any expired cookies, update the store
            if (validCookies.size() < cookies.size()) {
                cookieStore.put(host, validCookies);
                cookies = validCookies;
            }

            // Look for session cookies to help debug authentication issues
            boolean hasSessionCookie = false;
            for (Cookie cookie : cookies) {
                if (cookie.name().equals("JSESSIONID") || cookie.name().toLowerCase().contains("session")) {
                    hasSessionCookie = true;
                    break;
                }
            }

            if (!hasSessionCookie) {
                Log.w(TAG, "No session cookie found for host: " + host);
            }
        } else {
            Log.d(TAG, "No cookies found for host: " + host);
        }

        return cookies != null ? cookies : new ArrayList<>();
    }

    /**
     * Clears all cookies from the store.
     * Useful when logging out or resetting the session.
     */
    public void clearAll() {
        Log.d(TAG, "Clearing all cookies from store");
        cookieStore.clear();
    }
}