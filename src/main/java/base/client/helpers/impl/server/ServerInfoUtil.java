package base.client.helpers.impl.server;

import net.minecraft.client.Minecraft;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ServerInfoUtil {
    private static final Pattern VERSION_PATTERN = Pattern.compile("(\\D+)?\\s*(\\d+)\\.(\\d+)(?:\\.(\\d+))?.*");
    private static final Set<String> KNOWN_BRANDS = Set.of("Spigot", "Paper", "Bukkit", "Vanilla", "Forge", "Fabric");

   public static String getServerVersion(){

 return Minecraft.getInstance().getConnection().getServerData().version.getString();
    }

    public static String getServerBrand(String versionString) {
        if (versionString == null || versionString.isEmpty()) {
            return "Unknown";
        }

        for (String brand : KNOWN_BRANDS) {
            if (versionString.contains(brand)) {
                return brand;
            }
        }

        return "Custom";
    }

    public static int compareVersions(String version1, String version2) {
        VersionInfo v1 = parseVersion(version1);
        VersionInfo v2 = parseVersion(version2);

        return v1.compareTo(v2);
    }

    public static boolean isVersionNewer(String version1, String version2) {
        return compareVersions(version1, version2) > 0;
    }

    public static boolean isVersionOlder(String version1, String version2) {
        return compareVersions(version1, version2) < 0;
    }

    public static boolean isSameVersion(String version1, String version2) {
        return compareVersions(version1, version2) == 0;
    }

    private static VersionInfo parseVersion(String versionString) {
        if (versionString == null) {
            return new VersionInfo("Unknown", 0, 0, 0);
        }

        Matcher matcher = VERSION_PATTERN.matcher(versionString);
        if (matcher.find()) {
            String brand = matcher.group(1) != null ? matcher.group(1).trim() : "Unknown";
            int major = safeParseInt(matcher.group(2));
            int minor = safeParseInt(matcher.group(3));
            int patch = matcher.group(4) != null ? safeParseInt(matcher.group(4)) : 0;

            return new VersionInfo(brand, major, minor, patch);
        }

        return new VersionInfo("Unknown", 0, 0, 0);
    }

    private static int safeParseInt(String number) {
        try {
            return Integer.parseInt(number);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static class VersionInfo implements Comparable<VersionInfo> {
        private final String brand;
        private final int major;
        private final int minor;
        private final int patch;

        public VersionInfo(String brand, int major, int minor, int patch) {
            this.brand = brand;
            this.major = major;
            this.minor = minor;
            this.patch = patch;
        }

        @Override
        public int compareTo(VersionInfo other) {
            if (this.major != other.major) {
                return Integer.compare(this.major, other.major);
            }
            if (this.minor != other.minor) {
                return Integer.compare(this.minor, other.minor);
            }
            return Integer.compare(this.patch, other.patch);
        }
    }

}
