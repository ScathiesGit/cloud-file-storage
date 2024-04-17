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
public class RenameController {

    private final FileSystemObjectService fileSystemObjectService;

    private final SearchFileSystemObjectService searchFileSystemObjectService;

    @PostMapping("/change")
    public String changeName(@SessionAttribute User user, String path, String oldName, String newName, Model model) {
        fileSystemObjectService.rename(user, path, oldName, newName);
        if (path != null) {
            model.addAttribute("content", searchFileSystemObjectService.getFolderContent(user, path));
            model.addAttribute("path", path);
            model.addAttribute("breadcrumb", BreadcrumbUtil.createBreadcrumbs(path));
        } else {
            model.addAttribute("content", searchFileSystemObjectService.getRootFolderContent(user));
        }
        return "test";
    }
}
