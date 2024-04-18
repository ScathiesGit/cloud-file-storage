package git.scathies.cloudfilestorage.controller;

import git.scathies.cloudfilestorage.model.User;
import git.scathies.cloudfilestorage.service.FileSystemObjectService;
import git.scathies.cloudfilestorage.service.SearchFileSystemObjectService;
import git.scathies.cloudfilestorage.util.BreadcrumbUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.Collections.*;

@RequestMapping("/")
@RequiredArgsConstructor
public class FileStorageController {

    private final FileSystemObjectService fileSystemObjectService;

    private final SearchFileSystemObjectService searchFileSystemObjectService;

    @GetMapping
    public String getFileStorage() {
        return "file-storage-page";
    }

    @PostMapping
    public String upload(@SessionAttribute User user, List<MultipartFile> files, String path) {
        fileSystemObjectService.upload(user, path, files);
        return "file-storage-page";
    }

    @PutMapping
    public String rename(@SessionAttribute User user, String path, String oldName, String newName) {
        fileSystemObjectService.rename(user, path, oldName, newName);
        return "file-storage-page";
    }

    @DeleteMapping
    public String remove(@SessionAttribute User user, String path, String name) {
        fileSystemObjectService.remove(user, path, name);
        return "file-storage-page";
    }

    @ModelAttribute("content")
    private List<String> getFolderContent(@SessionAttribute User user, String path) {
        if (user == null) {
            return emptyList();
        }
        return path == null
                ? searchFileSystemObjectService.getRootFolderContent(user)
                : searchFileSystemObjectService.getFolderContent(user, path);
    }

    @ModelAttribute("breadcrumb")
    private Map<String, String> getBreadcrumb(String path) {
        return path != null ? BreadcrumbUtil.createBreadcrumbs(path) : emptyMap();
    }

    @ModelAttribute("path")
    private String getPath(String path) {
        return path != null ? path : "";
    }
}
