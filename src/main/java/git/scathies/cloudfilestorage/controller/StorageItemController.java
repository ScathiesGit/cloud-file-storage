package git.scathies.cloudfilestorage.controller;

import com.google.common.net.HttpHeaders;
import git.scathies.cloudfilestorage.dto.request.*;
import git.scathies.cloudfilestorage.model.DownloadObject;
import git.scathies.cloudfilestorage.model.User;
import git.scathies.cloudfilestorage.service.StorageItemService;
import git.scathies.cloudfilestorage.util.BreadcrumbUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;

@Controller
@RequestMapping("/")
@RequiredArgsConstructor
public class StorageItemController {

    private final StorageItemService storageItemService;

    @GetMapping
    public String getContentFolder(@SessionAttribute User user,
                                   ReadStorageItemReq readDto,
                                   Model model) {
        initModel(user, readDto, model);
        return "file-storage-page";
    }

    @PostMapping
    public String upload(@SessionAttribute User user,
                         UploadStorageItemReq uploadDto,
                         ReadStorageItemReq readDto,
                         Model model) {
        storageItemService.upload(user, uploadDto);
        initModel(user, readDto, model);
        return "file-storage-page";
    }

    @PatchMapping
    public String rename(@SessionAttribute User user,
                         RenameStorageItemReq renameDto,
                         ReadStorageItemReq readDto,
                         Model model) {
        storageItemService.renameStorageItem(user, renameDto);
        initModel(user, readDto, model);
        return "file-storage-page";
    }

    @DeleteMapping
    public String remove(@SessionAttribute User user,
                         ReadStorageItemReq readDto,
                         DeleteStorageItemReq deleteDto,
                         Model model) {
        storageItemService.deleteStorageItem(user, deleteDto);
        initModel(user, readDto, model);
        return "file-storage-page";
    }

    @PostMapping("/create")
    public String createFolder(@SessionAttribute User user,
                               CreateFolderReq createDto,
                               ReadStorageItemReq readDto,
                               Model model) {
        storageItemService.createFolder(user, createDto);
        initModel(user, readDto, model);
        return "file-storage-page";
    }

    @GetMapping("/download")
    public ResponseEntity<ByteArrayResource> download(@SessionAttribute User user, DownloadStorageItemReq dto) {
        DownloadObject downloaded = storageItemService.downloadStorageItem(user, dto);

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" +
                                UriUtils.encode(downloaded.name(), StandardCharsets.UTF_8) + "\""
                )
                .contentType(MediaType.parseMediaType(downloaded.contentType()))
                .body(new ByteArrayResource(downloaded.content()));
    }

    private void initModel(User user, ReadStorageItemReq dto, Model model) {
        model.addAttribute("content", storageItemService.getFolderContent(user, dto));
        model.addAttribute("breadcrumb", BreadcrumbUtil
                .createBreadcrumbs(dto.getPath() + dto.getName()));

        model.addAttribute("path", dto.getPath());
        model.addAttribute("folder", dto.getName());
    }
}



