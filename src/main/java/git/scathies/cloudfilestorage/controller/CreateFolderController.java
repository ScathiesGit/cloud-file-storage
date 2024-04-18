package git.scathies.cloudfilestorage.controller;

import git.scathies.cloudfilestorage.model.User;
import git.scathies.cloudfilestorage.service.FileSystemObjectService;
import git.scathies.cloudfilestorage.service.SearchFileSystemObjectService;
import git.scathies.cloudfilestorage.util.BreadcrumbUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;

@Controller
@RequiredArgsConstructor
public class CreateFolderController {

    private final FileSystemObjectService fileSystemObjectService;

    private final SearchFileSystemObjectService searchFileSystemObjectService;

    @PostMapping("/create")
    public String createFolder(@RequestParam(required = false) String path,
                               @SessionAttribute User user,
                               String name,
                               Model model) {
        fileSystemObjectService.createFolder(user, path, name);
        model.addAttribute("content", searchFileSystemObjectService.getFolderContent(user, path));
        if (path != null) {
            model.addAttribute("path", path);
            model.addAttribute("breadcrumb", BreadcrumbUtil.createBreadcrumbs(path));
        }
        return "file-storage-page";
    }
}
