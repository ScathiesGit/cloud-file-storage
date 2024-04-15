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

import java.nio.file.Paths;

@Controller
@RequiredArgsConstructor
public class RemoveController {

    private final FileSystemObjectService fileSystemObjectService;

    private final SearchFileSystemObjectService searchFileSystemObjectService;

    @PostMapping("/remove")
    public String remove(@SessionAttribute User user, String path, Model model) {
        var fullPath = "user-%s-files/%s".formatted(user.getId(), path);
        if (path.endsWith("/")) {
            fileSystemObjectService.removeFolder(fullPath);
        } else {
            fileSystemObjectService.removeFile(fullPath);
        }
        // user-10-files/
        //               text.txt
        //               folder/
        //               some/path/text.txt
        //               path/to/

        String parentFolder = Paths.get(fullPath).getParent() + "/";
        parentFolder = parentFolder.replace("\\", "/");
        model.addAttribute("content", searchFileSystemObjectService.getContentFolder(parentFolder));
        model.addAttribute("breadcrumb",
                BreadcrumbUtil.createBreadcrumbs(parentFolder.replace("user-" + user.getId() + "-files/","")));
        model.addAttribute("path", parentFolder.replace("user-" + user.getId() + "-files/",""));
        return "test";
    }
}
