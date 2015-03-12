package org.sahagin.share;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.jar.Manifest;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.openqa.selenium.io.IOUtils;

public class CommonUtils {

    public static String formatVersion() {
        return "0.6";
    }

    // - nulls last
    // - returns positive if left > right, returns negative if left < right,
    //   0 if equals
    public static int compare(String left, String right) {
        if (left == null) {
            if (right == null) {
                return 0;
            } else {
                return 1; // nulls last
            }
        } else if (right == null) {
            return -1; // nulls last
        }
        return left.compareTo(right);
    }

    private static boolean filePathEquals(String path1, String path2) {
        // Mac is case-insensitive, but IOCase.SYSTEM.isCaseSenstive returns true,
        // so don't use this value for Mac.
        // (TODO but Mac can become case-sensitive if an user changes system setting..)
        if (SystemUtils.IS_OS_MAC || SystemUtils.IS_OS_MAC_OSX) {
            return StringUtils.equalsIgnoreCase(path1, path2);
        }
        if (IOCase.SYSTEM.isCaseSensitive()) {
            return StringUtils.equals(path1, path2);
        } else {
            return StringUtils.equalsIgnoreCase(path1, path2);
        }
    }

    // cannot use Path.relativize since Sahagin support Java 1.6 or later
    // TODO ignore case for windows
    public static File relativize(File target, File baseDir) {
        String separator = File.separator;
        try {
            String absTargetPath = target.getAbsolutePath();
            absTargetPath = FilenameUtils.normalizeNoEndSeparator(
                    FilenameUtils.separatorsToSystem(absTargetPath));
            String absBasePath = baseDir.getAbsolutePath();
            absBasePath = FilenameUtils.normalizeNoEndSeparator(
                    FilenameUtils.separatorsToSystem(absBasePath));

            if (filePathEquals(absTargetPath, absBasePath)) {
                throw new IllegalArgumentException("target and base are equal: " + absTargetPath);
            }

            String[] absTargets = absTargetPath.split(Pattern.quote(separator));
            String[] absBases = absBasePath.split(Pattern.quote(separator));

            int minLength = Math.min(absTargets.length, absBases.length);

            int lastCommonRoot = -1;
            for (int i = 0; i < minLength; i++) {
                if (filePathEquals(absTargets[i], absBases[i])) {
                    lastCommonRoot = i;
                } else {
                    break;
                }
            }

            if (lastCommonRoot == -1) {
                // This case can happen on Windows when drive of two file paths differ.
                throw new IllegalArgumentException("no common root");
            }

            String relativePath = "";

            for (int i = lastCommonRoot + 1; i < absBases.length; i++) {
                relativePath = relativePath + ".." + separator;
            }

            for (int i = lastCommonRoot + 1; i < absTargets.length; i++) {
                relativePath = relativePath + absTargets[i];
                if (i != absTargets.length - 1) {
                    relativePath = relativePath + separator;
                }
            }
            return new File(relativePath);
        } catch (Exception e) {
            throw new RuntimeException(String.format(
                    "target: %s; baseDir: %s; separator: %s", target, baseDir, separator), e);
        }
    }

    public static Manifest readManifestFromExternalJar(File jarFile) {
        if (!jarFile.getName().endsWith(".jar")) {
            throw new IllegalArgumentException("not jar file : " + jarFile);
        }
        InputStream in = null;
        String urlStr = "jar:file:" + jarFile.getAbsolutePath() + "!/META-INF/MANIFEST.MF";
          try {
            URL inputURL = new URL(urlStr);
            JarURLConnection conn = (JarURLConnection) inputURL.openConnection();
            in = conn.getInputStream();
            return new Manifest(in);
          } catch (MalformedURLException e) {
              throw new RuntimeException(e);
          } catch (IOException e) {
              throw new RuntimeException(e);
          } finally {
              IOUtils.closeQuietly(in);
          }
    }

}

