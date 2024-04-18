package git.scathies.cloudfilestorage.controller;

import git.scathies.cloudfilestorage.model.User;
import git.scathies.cloudfilestorage.service.FileSystemObjectService;
import git.scathies.cloudfilestorage.service.SearchFileSystemObjectService;
import git.scathies.cloudfilestorage.util.BreadcrumbUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@RequestMapping("/")
@RequiredArgsConstructor
public class FileStorageController {

    private final FileSystemObjectService fileSystemObjectService;

    private final SearchFileSystemObjectService searchFileSystemObjectService;

    @GetMapping
    public String getFileStorage(@SessionAttribute User user, String path, Model model) {
        initModel(user, path, model);
        return "file-storage-page";
    }

    @PostMapping
    public String upload(@SessionAttribute User user, List<MultipartFile> files, String path, Model model) {
        fileSystemObjectService.upload(user, path, files);
        initModel(user, path, model);
        return "file-storage-page";
    }

    @PutMapping
    public String rename(@SessionAttribute User user, String path, String oldName, String newName, Model model) {
        fileSystemObjectService.rename(user, path, oldName, newName);
        initModel(user, path, model);
        return "file-storage-page";
    }

    @DeleteMapping
    public String remove(@SessionAttribute User user, String path, String name, Model model) {
        fileSystemObjectService.remove(user, path, name);
        initModel(user, path, model);
        return "file-storage-page";
    }

    private void initModel(User user, String path, Model model) {
        model.addAttribute("content", path == null
                ? searchFileSystemObjectService.getRootFolderContent(user)
                : searchFileSystemObjectService.getFolderContent(user, path));
        if (path != null && !path.isEmpty()) {
            model.addAttribute("breadcrumb", BreadcrumbUtil.createBreadcrumbs(path));
            model.addAttribute("path", path);
        }
    }
}
