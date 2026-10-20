package com.openlycatholic.app.settings;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Locale;

final class AppUpdateManager {

    private static final String RELEASE_API =
            "https://api.github.com/repos/JordanAllen101/OpenlyCatholicRelease/releases/latest";
    private static final String RELEASE_DOWNLOAD_PREFIX =
            "https://github.com/JordanAllen101/OpenlyCatholicRelease/releases/download/";
    private static final String APK_ASSET_NAME = "OpenlyCatholic-release.apk";
    private static final long MAX_APK_SIZE = 100L * 1024L * 1024L;

    private AppUpdateManager() {
    }

    static UpdateResult checkForUpdate(Context context) throws IOException {
        JsonObject release = requestRelease();
        String tag = getRequiredString(release, "tag_name");
        int[] latestVersion = parseVersion(tag);
        String installedVersionName;
        try {
            installedVersionName = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0)
                    .versionName;
        } catch (PackageManager.NameNotFoundException exception) {
            throw new IOException("Unable to determine the installed app version.", exception);
        }
        int[] installedVersion = parseVersion(installedVersionName);
        String displayVersion = tag.startsWith("v") ? tag.substring(1) : tag;

        if (compareVersions(latestVersion, installedVersion) <= 0) {
            return new UpdateResult(displayVersion, null);
        }

        JsonObject asset = findApkAsset(release.getAsJsonArray("assets"));
        String downloadUrl = getRequiredString(asset, "browser_download_url");
        if (!downloadUrl.startsWith(RELEASE_DOWNLOAD_PREFIX)) {
            throw new IOException("The release APK URL is not from the configured GitHub repository.");
        }

        JsonElement size = asset.get("size");
        if (size == null || !size.isJsonPrimitive() || !size.getAsJsonPrimitive().isNumber()) {
            throw new IOException("The release APK has an invalid file size.");
        }
        long expectedSize = size.getAsLong();
        if (expectedSize <= 0 || expectedSize > MAX_APK_SIZE) {
            throw new IOException("The release APK has an invalid file size.");
        }

        String expectedDigest = getRequiredString(asset, "digest").toLowerCase(Locale.ROOT);
        if (!expectedDigest.matches("sha256:[0-9a-f]{64}")) {
            throw new IOException("The release does not provide a valid SHA-256 checksum.");
        }

        File updateDirectory = new File(context.getCacheDir(), "updates");
        if (!updateDirectory.isDirectory() && !updateDirectory.mkdirs()) {
            throw new IOException("Unable to create the update download directory.");
        }

        File apkFile = new File(updateDirectory, APK_ASSET_NAME);
        File temporaryFile = new File(updateDirectory, APK_ASSET_NAME + ".part");
        downloadApk(downloadUrl, temporaryFile, expectedSize, expectedDigest.substring(7));
        if (apkFile.exists() && !apkFile.delete()) {
            temporaryFile.delete();
            throw new IOException("Unable to replace the previously downloaded update.");
        }
        if (!temporaryFile.renameTo(apkFile)) {
            temporaryFile.delete();
            throw new IOException("Unable to save the downloaded update.");
        }

        try {
            verifyPackageAndSignature(context, apkFile);
        } catch (IOException exception) {
            apkFile.delete();
            throw exception;
        }
        return new UpdateResult(displayVersion, apkFile);
    }

    private static JsonObject requestRelease() throws IOException {
        HttpURLConnection connection = openConnection(RELEASE_API);
        connection.setRequestProperty("Accept", "application/vnd.github+json");
        connection.setRequestProperty("X-GitHub-Api-Version", "2022-11-28");
        try {
            int status = connection.getResponseCode();
            if (status != HttpURLConnection.HTTP_OK) {
                throw new IOException("GitHub returned HTTP " + status + " while checking for updates.");
            }
            String response = readBounded(connection.getInputStream(), 1024 * 1024);
            return JsonParser.parseString(response).getAsJsonObject();
        } catch (JsonParseException | IllegalStateException exception) {
            throw new IOException("GitHub returned an invalid release response.", exception);
        } finally {
            connection.disconnect();
        }
    }

    private static JsonObject findApkAsset(JsonArray assets) throws IOException {
        if (assets != null) {
            for (JsonElement element : assets) {
                JsonObject asset = element.getAsJsonObject();
                if (APK_ASSET_NAME.equals(getRequiredString(asset, "name"))) {
                    return asset;
                }
            }
        }
        throw new IOException("The latest GitHub release does not include " + APK_ASSET_NAME + ".");
    }

    private static void downloadApk(
            String downloadUrl,
            File destination,
            long expectedSize,
            String expectedDigest
    ) throws IOException {
        HttpURLConnection connection = openConnection(downloadUrl);
        boolean verified = false;
        try {
            int status = connection.getResponseCode();
            if (status != HttpURLConnection.HTTP_OK) {
                throw new IOException("GitHub returned HTTP " + status + " while downloading the update.");
            }
            if (!"https".equalsIgnoreCase(connection.getURL().getProtocol())) {
                throw new IOException("The APK download did not use a secure connection.");
            }

            MessageDigest digest = sha256();
            long bytesRead = 0;
            byte[] buffer = new byte[8192];
            try (InputStream input = connection.getInputStream();
                 FileOutputStream output = new FileOutputStream(destination)) {
                int count;
                while ((count = input.read(buffer)) != -1) {
                    bytesRead += count;
                    if (bytesRead > expectedSize || bytesRead > MAX_APK_SIZE) {
                        throw new IOException("The downloaded APK is larger than the published asset.");
                    }
                    digest.update(buffer, 0, count);
                    output.write(buffer, 0, count);
                }
            }

            if (bytesRead != expectedSize) {
                throw new IOException("The downloaded APK size does not match the GitHub release.");
            }
            if (!expectedDigest.equals(toHex(digest.digest()))) {
                throw new IOException("The downloaded APK checksum does not match the GitHub release.");
            }
            verified = true;
        } finally {
            connection.disconnect();
            if (!verified && destination.exists()) {
                destination.delete();
            }
        }
    }

    private static void verifyPackageAndSignature(Context context, File apkFile) throws IOException {
        PackageManager packageManager = context.getPackageManager();
        PackageInfo archive = packageManager.getPackageArchiveInfo(
                apkFile.getAbsolutePath(),
                PackageManager.GET_SIGNATURES
        );
        if (archive == null || !context.getPackageName().equals(archive.packageName)) {
            throw new IOException("The downloaded APK is not an update for this app.");
        }

        try {
            PackageInfo installed = packageManager.getPackageInfo(
                    context.getPackageName(),
                    PackageManager.GET_SIGNATURES
            );
            if (!sameSignatures(installed.signatures, archive.signatures)) {
                throw new IOException("The release APK is signed with a different key than this installation.");
            }
        } catch (PackageManager.NameNotFoundException exception) {
            throw new IOException("Unable to verify the installed app's signing certificate.", exception);
        }
    }

    private static boolean sameSignatures(Signature[] installed, Signature[] downloaded)
            throws IOException {
        if (installed == null || downloaded == null || installed.length == 0
                || installed.length != downloaded.length) {
            return false;
        }

        String[] installedDigests = signatureDigests(installed);
        String[] downloadedDigests = signatureDigests(downloaded);
        Arrays.sort(installedDigests);
        Arrays.sort(downloadedDigests);
        return Arrays.equals(installedDigests, downloadedDigests);
    }

    private static String[] signatureDigests(Signature[] signatures) throws IOException {
        String[] digests = new String[signatures.length];
        for (int index = 0; index < signatures.length; index++) {
            digests[index] = toHex(sha256().digest(signatures[index].toByteArray()));
        }
        return digests;
    }

    private static HttpURLConnection openConnection(String address) throws IOException {
        URL url = new URL(address);
        if (!"https".equalsIgnoreCase(url.getProtocol())) {
            throw new IOException("Update requests must use HTTPS.");
        }
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(15000);
        connection.setReadTimeout(30000);
        connection.setInstanceFollowRedirects(true);
        connection.setRequestProperty("User-Agent", "OpenlyCatholic-Android");
        return connection;
    }

    private static String readBounded(InputStream input, int maxBytes) throws IOException {
        try (InputStream stream = input; ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            int total = 0;
            int count;
            while ((count = stream.read(buffer)) != -1) {
                total += count;
                if (total > maxBytes) {
                    throw new IOException("The GitHub release response is unexpectedly large.");
                }
                output.write(buffer, 0, count);
            }
            return output.toString("UTF-8");
        }
    }

    private static String getRequiredString(JsonObject object, String key) throws IOException {
        JsonElement value = object.get(key);
        if (value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString()) {
            throw new IOException("The GitHub release is missing " + key + ".");
        }
        return value.getAsString();
    }

    private static int[] parseVersion(String value) throws IOException {
        String normalized = value.startsWith("v") ? value.substring(1) : value;
        String[] components = normalized.split("\\.", -1);
        if (components.length != 3) {
            throw new IOException("The app version must use MAJOR.MINOR.PATCH format.");
        }
        int[] version = new int[3];
        try {
            for (int index = 0; index < components.length; index++) {
                if (!components[index].matches("[0-9]+")) {
                    throw new NumberFormatException();
                }
                version[index] = Integer.parseInt(components[index]);
            }
        } catch (NumberFormatException exception) {
            throw new IOException("The app version must use MAJOR.MINOR.PATCH format.", exception);
        }
        return version;
    }

    private static int compareVersions(int[] left, int[] right) {
        for (int index = 0; index < left.length; index++) {
            int comparison = Integer.compare(left[index], right[index]);
            if (comparison != 0) {
                return comparison;
            }
        }
        return 0;
    }

    private static MessageDigest sha256() throws IOException {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException exception) {
            throw new IOException("SHA-256 is unavailable on this device.", exception);
        }
    }

    private static String toHex(byte[] bytes) {
        StringBuilder result = new StringBuilder(bytes.length * 2);
        for (byte value : bytes) {
            result.append(String.format(Locale.ROOT, "%02x", value & 0xff));
        }
        return result.toString();
    }

    static final class UpdateResult {
        final String version;
        final File apkFile;

        UpdateResult(String version, File apkFile) {
            this.version = version;
            this.apkFile = apkFile;
        }

        boolean hasUpdate() {
            return apkFile != null;
        }
    }
}
