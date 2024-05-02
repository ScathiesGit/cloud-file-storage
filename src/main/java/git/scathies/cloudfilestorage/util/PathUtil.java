package git.scathies.cloudfilestorage.util;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class PathUtil {

    public static boolean isContains(String path, String node) {
        for (var pathPart : path.split("/")) {
            if (areEqual(pathPart, node)) {
                return true;
            }
        }
        return false;
    }

    public static void main(String[] args) {
        System.out.println(getPathsTo("base/path/rename.txt", "rename.txt"));
    }

    public static List<String> getPathsTo(String source, String desiredObject) {
        var path = Paths.get(source);
        var iterator = path.iterator();
        var requiredPaths = new ArrayList<String>();
        int i = 0;
        while (iterator.hasNext()) {

            if (areEqual(iterator.next().toString(), desiredObject)) {
                var requiredPath = i == 0 ? "/"
                        : path.subpath(0, i).toString().replace("\\", "/") + "/";
                requiredPaths.add(requiredPath);
            }

            i++;
        }
        return requiredPaths;
    }

    private static boolean areEqual(String s1, String s2) {
        if (s1.contains(".") && s2.contains(".")
                || !s1.contains(".") && !s2.contains(".")) {
            return s1.equals(s2);
        } else {
            return s1.contains(".") ? s1.substring(0, s1.indexOf(".")).equals(s2)
                    : s2.substring(0, s2.indexOf(".")).equals(s1);
        }
    }
}
