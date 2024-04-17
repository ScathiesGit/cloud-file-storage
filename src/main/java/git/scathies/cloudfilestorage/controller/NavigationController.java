package git.scathies.cloudfilestorage.controller;

import git.scathies.cloudfilestorage.model.User;
import git.scathies.cloudfilestorage.service.FileSystemObjectService;
import git.scathies.cloudfilestorage.service.SearchFileSystemObjectService;
import git.scathies.cloudfilestorage.util.BreadcrumbUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/")
@RequiredArgsConstructor
public class NavigationController {

    private final SearchFileSystemObjectService searchFileSystemObjectService;

    @GetMapping
    public String homePage(@RequestParam(required = false) String path,
                           @SessionAttribute User user,
                           Model model) {
        if (user != null) {
            model.addAttribute("content", path == null
                    ? searchFileSystemObjectService.getRootFolderContent(user)
                    : searchFileSystemObjectService.getFolderContent(user, path));
            if (path != null) {
                model.addAttribute("path", path);
                model.addAttribute("breadcrumb", BreadcrumbUtil.createBreadcrumbs(path));
            }
        }
        return "test";
    }
}
