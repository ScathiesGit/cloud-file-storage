package git.scathies.cloudfilestorage.controller;

import git.scathies.cloudfilestorage.model.User;
import git.scathies.cloudfilestorage.service.FileSystemObjectService;
import git.scathies.cloudfilestorage.service.SearchFileSystemObjectService;
import git.scathies.cloudfilestorage.util.BreadcrumbUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class UploadController {

    private final FileSystemObjectService fileSystemObjectService;

    private final SearchFileSystemObjectService searchFileSystemObjectService;

    @PostMapping("/upload")
    public String upload(@SessionAttribute User user, List<MultipartFile> files, String path, Model model, HttpServletRequest req ) {
        var basePath = "user-%s-files/%s".formatted(user.getId(), path != null ? path : "");
        fileSystemObjectService.upload(basePath, files);
        model.addAttribute("content", searchFileSystemObjectService.getFolderContent(basePath, user.getId()));
        model.addAttribute("path", path);
        model.addAttribute("breadcrumb", BreadcrumbUtil.createBreadcrumbs(path != null ? path : ""));
        return "test";
    }
}