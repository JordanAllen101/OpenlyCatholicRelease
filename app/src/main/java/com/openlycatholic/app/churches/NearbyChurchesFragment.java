package com.openlycatholic.app.churches;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.openlycatholic.app.R;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NearbyChurchesFragment extends Fragment {

    private static final String TAG = "NearbyChurches";
    private static final int LOCATION_PERMISSION_REQUEST = 4102;
    private static final long LOCATION_TIMEOUT_MS = 20000L;
    private static final long MASSTIMES_TIMEOUT_MS = 35000L;
    private static final int DEFAULT_RADIUS_MILES = 50;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final ExecutorService networkExecutor = Executors.newSingleThreadExecutor();
    private LocationManager locationManager;
    private LocationListener locationListener;
    private Runnable locationTimeout;
    private Runnable massTimesTimeout;
    private Button searchButton;
    private Button distanceButton;
    private TextView statusView;
    private LinearLayout resultsContainer;
    private WebView massTimesWebView;
    private boolean searchInProgress;
    private boolean massTimesPageReady;
    private int searchRadiusMiles = DEFAULT_RADIUS_MILES;
    private List<MassTimesClient.Church> nearbyParishes = Collections.emptyList();
    private double pendingLatitude;
    private double pendingLongitude;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_nearby_churches, container, false);
        searchButton = view.findViewById(R.id.useLocationButton);
        distanceButton = view.findViewById(R.id.distanceButton);
        statusView = view.findViewById(R.id.locationStatus);
        resultsContainer = view.findViewById(R.id.churchResults);
        massTimesWebView = view.findViewById(R.id.massTimesWebView);
        searchButton.setOnClickListener(ignored -> beginSearch());
        updateDistanceButton();
        distanceButton.setOnClickListener(this::showDistanceOptions);
        boolean hasPermission = hasLocationPermission();
        searchButton.setVisibility(hasPermission ? View.GONE : View.VISIBLE);
        if (!hasPermission) {
            statusView.setText("Location access is optional. Grant permission to search nearby.");
        }
        prepareMassTimesPage();
        if (hasPermission) {
            view.post(this::beginSearch);
        }
        return view;
    }

    private void updateDistanceButton() {
        if (distanceButton != null) {
            distanceButton.setText("Within " + searchRadiusMiles + " miles");
        }
    }

    private void showDistanceOptions(View anchor) {
        PopupMenu menu = new PopupMenu(requireContext(), anchor);
        menu.getMenu().add(0, 50, 0, "Within 50 miles");
        menu.getMenu().add(0, 100, 1, "Within 100 miles");
        menu.setOnMenuItemClickListener(item -> {
            searchRadiusMiles = item.getItemId();
            updateDistanceButton();
            if (!nearbyParishes.isEmpty()) {
                showResults(nearbyParishes);
            }
            return true;
        });
        menu.show();
    }

    private void prepareMassTimesPage() {
        WebSettings settings = massTimesWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setLoadsImagesAutomatically(false);
        settings.setBlockNetworkImage(true);
        massTimesWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                if (!isMassTimesUrl(url)) {
                    return;
                }
                massTimesPageReady = true;
                if (searchInProgress && isFinite(pendingLatitude)
                        && isFinite(pendingLongitude)) {
                    fetchMassTimes();
                }
            }

            @Override
            public void onReceivedError(
                    WebView view,
                    WebResourceRequest request,
                    WebResourceError error
            ) {
                if (request.isForMainFrame() && searchInProgress) {
                    Log.e(TAG, "Could not load the MassTimes page: " + error.getDescription());
                    finishSearch("Could not connect to the schedule service. Check your internet connection "
                            + "and try again.");
                }
            }

            @Override
            public void onReceivedHttpError(
                    WebView view,
                    WebResourceRequest request,
                    android.webkit.WebResourceResponse response
            ) {
                if (request.isForMainFrame() && response.getStatusCode() >= 400
                        && searchInProgress) {
                    Log.e(TAG, "MassTimes page returned HTTP " + response.getStatusCode());
                    finishSearch("The schedule service could not be reached (HTTP "
                            + response.getStatusCode() + "). Please try again later.");
                }
            }

            @Override
            public boolean shouldOverrideUrlLoading(
                    WebView view,
                    WebResourceRequest request
            ) {
                return !isMassTimesUrl(request.getUrl().toString());
            }
        });
        massTimesWebView.loadUrl("https://masstimes.org/map");
    }

    private boolean isMassTimesUrl(String url) {
        Uri uri = Uri.parse(url);
        return "https".equals(uri.getScheme())
                && "masstimes.org".equals(uri.getHost());
    }

    private void beginSearch() {
        if (searchInProgress) {
            return;
        }
        if (!hasLocationPermission()) {
            searchButton.setVisibility(View.VISIBLE);
            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST
            );
            return;
        }
        searchButton.setVisibility(View.GONE);
        startLocationRequest();
    }

    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED;
    }

    private void startLocationRequest() {
        Context context = getContext();
        if (context == null || searchButton == null || statusView == null) {
            return;
        }

        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        List<String> providers = getEnabledProviders();
        if (providers.isEmpty()) {
            statusView.setText("Location is turned off. Enable location services and try again.");
            return;
        }

        searchInProgress = true;
        searchButton.setEnabled(false);
        statusView.setText("Loading nearby parishes…");
        resultsContainer.removeAllViews();
        nearbyParishes = Collections.emptyList();

        Location recentLocation = getRecentLocation(providers);
        if (recentLocation != null) {
            onLocationAvailable(recentLocation);
            return;
        }

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                onLocationAvailable(location);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(@NonNull String provider) {
            }

            @Override
            public void onProviderDisabled(@NonNull String provider) {
            }
        };

        try {
            for (String provider : providers) {
                locationManager.requestLocationUpdates(
                        provider,
                        0L,
                        0f,
                        locationListener,
                        Looper.getMainLooper()
                );
            }
        } catch (SecurityException exception) {
            Log.w(TAG, "Location permission was revoked while starting the request.", exception);
            finishSearch("Location permission is required to search nearby.");
            return;
        } catch (IllegalArgumentException exception) {
            Log.e(TAG, "A location provider became unavailable.", exception);
            finishSearch("Could not access location services. Please try again.");
            return;
        }

        locationTimeout = () -> finishSearch(
                "Could not determine your location. Check your signal and try again."
        );
        mainHandler.postDelayed(locationTimeout, LOCATION_TIMEOUT_MS);
    }

    private List<String> getEnabledProviders() {
        if (locationManager == null) {
            return Collections.emptyList();
        }
        try {
            List<String> enabled = locationManager.getProviders(true);
            if (enabled == null) {
                return Collections.emptyList();
            }
            List<String> usable = new ArrayList<>();
            for (String provider : enabled) {
                if (LocationManager.NETWORK_PROVIDER.equals(provider)) {
                    usable.add(provider);
                }
            }
            return usable;
        } catch (SecurityException exception) {
            Log.w(TAG, "Location permission was revoked while checking providers.", exception);
            return Collections.emptyList();
        }
    }

    @Nullable
    private Location getRecentLocation(List<String> providers) {
        long now = System.currentTimeMillis();
        for (String provider : providers) {
            try {
                Location lastKnown = locationManager.getLastKnownLocation(provider);
                if (lastKnown != null && now - lastKnown.getTime() <= 10 * 60 * 1000L) {
                    return lastKnown;
                }
            } catch (SecurityException exception) {
                Log.w(TAG, "Location permission was revoked while reading the last location.",
                        exception);
                return null;
            }
        }
        return null;
    }

    private void onLocationAvailable(Location location) {
        if (!searchInProgress) {
            return;
        }
        removeLocationUpdates();
        pendingLatitude = location.getLatitude();
        pendingLongitude = location.getLongitude();
        statusView.setText("Loading nearby parishes…");
        massTimesTimeout = () -> finishSearch(
                "The schedule service did not respond in time. Check your connection and try again."
        );
        mainHandler.postDelayed(massTimesTimeout, MASSTIMES_TIMEOUT_MS);
        if (massTimesPageReady) {
            fetchMassTimes();
        }
    }

    private void fetchMassTimes() {
        if (!searchInProgress || massTimesWebView == null) {
            return;
        }
        statusView.setText("Loading nearby parishes…");
        String script = String.format(
                Locale.US,
                "(function(){window.__ocParishResult=null;"
                        + "(async function(){try{var response=await fetch("
                        + "'/Churchs/?lat=%.6f&long=%.6f&pg=1',"
                        + "{credentials:'same-origin'});"
                        + "if(!response.ok)throw new Error('HTTP '+response.status);"
                        + "var page;try{page=await response.json()}catch(error){"
                        + "throw new Error('invalid response format')}"
                        + "if(!Array.isArray(page))throw new Error('unexpected response format');"
                        + "window.__ocParishResult=JSON.stringify({churches:page});"
                        + "}catch(error){window.__ocParishResult=JSON.stringify("
                        + "{error:String(error)});}})();})()",
                pendingLatitude,
                pendingLongitude
        );
        massTimesWebView.evaluateJavascript(script,
                ignored -> mainHandler.postDelayed(this::readMassTimesResult, 100L));
    }

    private void readMassTimesResult() {
        if (!searchInProgress || !isAdded() || massTimesWebView == null) {
            return;
        }
        massTimesWebView.evaluateJavascript("window.__ocParishResult || null", value -> {
            if (!searchInProgress || !isAdded()) {
                return;
            }
            if (value == null || "null".equals(value)) {
                mainHandler.postDelayed(this::readMassTimesResult, 100L);
                return;
            }
            try {
                Object decoded = new JSONTokener(value).nextValue();
                if (!(decoded instanceof String)) {
                    throw new JSONException("Unexpected schedule response type.");
                }
                JSONObject result = new JSONObject((String) decoded);
                if (result.has("error")) {
                    Log.e(TAG, "Schedule request failed: " + result.getString("error"));
                    finishSearch("Could not load nearby schedules. Please try again.");
                    return;
                }
                String response = result.getJSONArray("churches").toString();
                double latitude = pendingLatitude;
                double longitude = pendingLongitude;
                networkExecutor.execute(() -> {
                    try {
                        List<MassTimesClient.Church> churches = MassTimesClient.parseNearby(
                                response,
                                latitude,
                                longitude
                        );
                        mainHandler.post(() -> showResults(churches));
                    } catch (JSONException exception) {
                        Log.e(TAG, "Could not parse schedule data.", exception);
                        mainHandler.post(() -> finishSearch(
                                "Schedule data could not be read. Please try again later."
                        ));
                    }
                });
            } catch (JSONException exception) {
                Log.e(TAG, "Could not decode schedule response.", exception);
                finishSearch("Schedule data could not be read. Please try again later.");
            }
        });
    }

    private void showResults(List<MassTimesClient.Church> churches) {
        if (!isAdded() || resultsContainer == null) {
            return;
        }
        searchInProgress = false;
        searchButton.setEnabled(true);
        clearMassTimesTimeout();
        nearbyParishes = new ArrayList<>(churches);
        pendingLatitude = Double.NaN;
        pendingLongitude = Double.NaN;
        renderParishes();
    }

    private void renderParishes() {
        if (resultsContainer == null) {
            return;
        }
        List<MassTimesClient.Church> churches = new ArrayList<>();
        for (MassTimesClient.Church church : nearbyParishes) {
            if (church.distanceMiles <= searchRadiusMiles) {
                churches.add(church);
            }
        }
        resultsContainer.removeAllViews();
        if (churches.isEmpty()) {
            statusView.setText("No parishes with listed schedules were found within "
                    + searchRadiusMiles + " miles.");
            return;
        }
        Collections.sort(churches,
                (left, right) -> Double.compare(left.distanceMiles, right.distanceMiles));
        statusView.setText("Confirm schedules with parish.");
        for (MassTimesClient.Church church : churches) {
            resultsContainer.addView(createChurchCard(church));
        }
    }

    private View createChurchCard(MassTimesClient.Church church) {
        Context context = requireContext();
        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(14), dp(16), dp(14));
        card.setBackgroundResource(R.drawable.button_secondary);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        cardParams.bottomMargin = dp(12);
        card.setLayoutParams(cardParams);

        addText(card, church.name, 18, R.color.primary, true);
        String distance = Double.isNaN(church.distanceMiles)
                ? ""
                : String.format(Locale.getDefault(), "%.1f mi", church.distanceMiles);
        String address = joinNonEmpty(", ", church.type, church.streetAddress, church.city,
                church.region, church.postalCode);
        if (!address.isEmpty() || !distance.isEmpty()) {
            addText(card, joinNonEmpty(" · ", address, distance), 14, R.color.text_secondary,
                    false);
        }

        for (String category : new String[]{"Mass", "Confession", "Adoration"}) {
            List<String> entries = getScheduleEntries(church, category);
            if (!entries.isEmpty()) {
                addText(card, category, 15, R.color.primary_light, true);
                for (String entry : entries) {
                    addText(card, entry, 14, R.color.text_primary, false);
                }
            }
        }
        if (church.schedules.isEmpty()) {
            addText(card, "No Mass, Confession, or Adoration times are listed.",
                    14, R.color.text_tertiary, false);
        }

        LinearLayout actions = new LinearLayout(context);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams actionParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        actionParams.topMargin = dp(8);
        card.addView(actions, actionParams);
        if (!Double.isNaN(church.latitude) && !Double.isInfinite(church.latitude)
                && !Double.isNaN(church.longitude) && !Double.isInfinite(church.longitude)) {
            addAction(actions, "Directions", () -> openUri(
                    Uri.parse(String.format(
                            Locale.US,
                            "geo:%.6f,%.6f",
                            church.latitude,
                            church.longitude
                    ))
            ));
        }
        if (!church.phone.isEmpty()) {
            addAction(actions, "Call", () -> openUri(Uri.fromParts("tel", church.phone, null)));
        }
        if (isWebUrl(church.website)) {
            addAction(actions, "Website", () -> openUri(Uri.parse(church.website)));
        }
        return card;
    }

    private List<String> getScheduleEntries(MassTimesClient.Church church, String category) {
        List<String> entries = new ArrayList<>();
        for (MassTimesClient.Schedule schedule : church.schedules) {
            if (!category.equals(schedule.type)) {
                continue;
            }
            String time = MassTimesClient.formatTime(schedule.start);
            String end = MassTimesClient.formatTime(schedule.end);
            if (!end.isEmpty()) {
                time = joinNonEmpty("–", time, end);
            }
            String line = joinNonEmpty(" · ", schedule.day, time, schedule.language);
            if (!schedule.comment.isEmpty()) {
                line = joinNonEmpty(" — ", line, schedule.comment);
            }
            if (!line.isEmpty()) {
                entries.add(line);
            }
        }
        return entries;
    }

    private void addText(
            LinearLayout parent,
            String text,
            int sizeSp,
            int color,
            boolean bold
    ) {
        if (text == null || text.trim().isEmpty()) {
            return;
        }
        TextView view = new TextView(requireContext());
        view.setText(text);
        view.setTextSize(sizeSp);
        view.setTextColor(ContextCompat.getColor(requireContext(), color));
        if (bold) {
            view.setTypeface(view.getTypeface(), android.graphics.Typeface.BOLD);
        }
        view.setLineSpacing(dp(2), 1f);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.bottomMargin = dp(5);
        parent.addView(view, params);
    }

    private void addAction(LinearLayout parent, String label, Runnable action) {
        Button button = new Button(requireContext());
        button.setBackgroundResource(R.drawable.button_secondary);
        button.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_light));
        button.setText(label);
        button.setTextSize(12);
        button.setMinHeight(dp(40));
        button.setPadding(dp(8), 0, dp(8), 0);
        button.setOnClickListener(ignored -> action.run());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.rightMargin = dp(8);
        parent.addView(button, params);
    }

    private void openUri(Uri uri) {
        try {
            String action = "tel".equals(uri.getScheme())
                    ? Intent.ACTION_DIAL
                    : Intent.ACTION_VIEW;
            startActivity(new Intent(action, uri));
        } catch (ActivityNotFoundException exception) {
            Log.w(TAG, "No app can open " + uri.getScheme() + " links.", exception);
            Toast.makeText(requireContext(), "No app available to open this link.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isWebUrl(String url) {
        return url != null && (url.startsWith("https://") || url.startsWith("http://"));
    }

    private String joinNonEmpty(String separator, String... values) {
        StringBuilder result = new StringBuilder();
        for (String value : values) {
            if (value == null || value.trim().isEmpty()) {
                continue;
            }
            if (result.length() > 0) {
                result.append(separator);
            }
            result.append(value.trim());
        }
        return result.toString();
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private boolean isFinite(double value) {
        return !Double.isNaN(value) && !Double.isInfinite(value);
    }

    private void finishSearch(String message) {
        if (!isAdded() || searchButton == null || statusView == null) {
            return;
        }
        searchInProgress = false;
        pendingLatitude = Double.NaN;
        pendingLongitude = Double.NaN;
        searchButton.setEnabled(true);
        statusView.setText(message);
        removeLocationUpdates();
        clearMassTimesTimeout();
    }

    private void clearMassTimesTimeout() {
        if (massTimesTimeout != null) {
            mainHandler.removeCallbacks(massTimesTimeout);
            massTimesTimeout = null;
        }
    }

    private void removeLocationUpdates() {
        if (locationTimeout != null) {
            mainHandler.removeCallbacks(locationTimeout);
            locationTimeout = null;
        }
        if (locationManager != null && locationListener != null) {
            try {
                locationManager.removeUpdates(locationListener);
            } catch (SecurityException exception) {
                Log.w(TAG, "Location permission was revoked while ending the request.", exception);
            }
            locationListener = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != LOCATION_PERMISSION_REQUEST) {
            return;
        }
        if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (searchButton != null) {
                searchButton.setVisibility(View.GONE);
            }
            beginSearch();
        } else if (statusView != null) {
            if (searchButton != null) {
                searchButton.setVisibility(View.VISIBLE);
            }
            statusView.setText("Location permission was not granted. You can enable it in app "
                    + "settings whenever you want to search nearby.");
        }
    }

    @Override
    public void onDestroyView() {
        removeLocationUpdates();
        clearMassTimesTimeout();
        searchInProgress = false;
        pendingLatitude = Double.NaN;
        pendingLongitude = Double.NaN;
        if (massTimesWebView != null) {
            massTimesWebView.stopLoading();
            massTimesWebView.setWebViewClient(new WebViewClient());
            massTimesWebView.destroy();
            massTimesWebView = null;
        }
        searchButton = null;
        statusView = null;
        resultsContainer = null;
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        networkExecutor.shutdownNow();
        super.onDestroy();
    }
}
