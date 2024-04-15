package git.scathies.cloudfilestorage.util;

import git.scathies.cloudfilestorage.CloudFileStorageApplication;
import git.scathies.cloudfilestorage.service.FileSystemObjectServiceImpl;
import git.scathies.cloudfilestorage.service.SearchFileSystemObjectServiceImpl;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

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

    public static void main(String[] args) {
        System.out.println(isContains(
                "user-10-files/tracker-master/src/main/java/i/bobrov/tracker/store/Store.java",
                "tracker")
        );

        System.out.println(getPathsTo(
                "user-10-files/tracker-master/src/main/java/i/bobrov/tracker/store/Store.java",
                "tracker")
        );
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
