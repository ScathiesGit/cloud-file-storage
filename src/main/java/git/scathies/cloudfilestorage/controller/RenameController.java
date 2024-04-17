package git.scathies.cloudfilestorage.controller;

import git.scathies.cloudfilestorage.model.User;
import git.scathies.cloudfilestorage.service.FileSystemObjectService;
import git.scathies.cloudfilestorage.service.SearchFileSystemObjectService;
import git.scathies.cloudfilestorage.util.BreadcrumbUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.SessionAttribute;

import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
@RequiredArgsConstructor
public class ChangeNameController {

    private final FileSystemObjectService fileSystemObjectService;

    private final SearchFileSystemObjectService searchFileSystemObjectService;

    @PostMapping("/change")
    public String changeName(@SessionAttribute User user, String path, String newName, Model model) {
        var rootFolder = "user-%s-files/".formatted(user.getId());
        var fullPath = rootFolder + path;
        if (path.endsWith("/")) {
            fileSystemObjectService.renameFolder(fullPath, newName);
        } else {
            fileSystemObjectService.renameFile(fullPath, newName);
        }
        Path parent = Paths.get(path).getParent();
        String path1 = parent != null ? parent.toString().replace("\\", "/") + "/" : "";
        model.addAttribute("content", searchFileSystemObjectService.getFolderContent(
                rootFolder + path1, user.getId()
        ));
        model.addAttribute("path", path1);
        model.addAttribute("breadcrumb", BreadcrumbUtil.createBreadcrumbs(path1));

        return "test";
    }
}
