package git.scathies.cloudfilestorage.util;

import java.util.ArrayList;
import java.util.List;

public class PathUtil {

    public static boolean isContains(String path, String node) {
        var isContains = false;
        var pathParts = path.split("/");
        for (var pathPart : pathParts) {
            isContains = pathPart.contains(".") ? pathPart.substring(0, pathPart.indexOf(".")).equals(node)
                    : pathPart.equals(node);
            if (isContains) {
                return true;
            }
        }
        return false;
    }

    public static List<String> getPathsTo(String path, String destination) {
        List<String> requiredPaths = new ArrayList<>();
        var pathParts = path.split(destination);
        for (int i = 0; i < pathParts.length - 1; i++) {
            var reqPath = i == 0 ? pathParts[0] : requiredPaths.get(i - 1) + destination + pathParts[i];
            requiredPaths.add(reqPath);
        }

        var lastNode = path.substring(path.lastIndexOf("/") + 1);
        if (!lastNode.contains(".") && !lastNode.contains("/")) {
            requiredPaths.add(requiredPaths.get(requiredPaths.size() - 1) + destination + "/");
        }

        return requiredPaths;
    }
}
