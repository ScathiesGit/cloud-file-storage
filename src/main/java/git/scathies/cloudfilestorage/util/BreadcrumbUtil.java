package git.scathies.cloudfilestorage.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BreadcrumbUtil {

    public static List<Map.Entry<String, String>> createBreadcrumbs(String path) {
        var breadcrumb = new ArrayList<Map.Entry<String, String>>();
        var parts = path.split("/");
        var accumulate = "/";
        for (int i = 1; i < parts.length; i++) {
            breadcrumb.add(Map.entry(parts[i], accumulate));
            accumulate += parts[i] + "/";
        }
        return breadcrumb;
    }
}
