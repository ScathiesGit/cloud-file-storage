package git.scathies.cloudfilestorage.util;

import java.util.HashMap;
import java.util.Map;

public class BreadcrumbUtil {

    public static Map<String, String> createBreadcrumbs(String path) {
        Map<String, String> breadcrumb = new HashMap<>();
        var parts = path.split("/");
        var accumulate = "";
        for (int i = 0; i < parts.length; i++) {
            accumulate += parts[i] + "/";
            breadcrumb.put(parts[i], accumulate);
        }
        return breadcrumb;
    }
}
