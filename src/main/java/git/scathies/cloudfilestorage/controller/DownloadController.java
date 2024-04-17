package git.scathies.cloudfilestorage.controller;

import com.google.common.net.HttpHeaders;
import git.scathies.cloudfilestorage.model.User;
import git.scathies.cloudfilestorage.service.FileSystemObjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.SessionAttribute;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

@Controller
@RequiredArgsConstructor
public class DownloadController {

    private final FileSystemObjectService fileSystemObjectService;

    @GetMapping("/download")
    public ResponseEntity<ByteArrayResource> download(@SessionAttribute User user, String path) {
        var downloaded = fileSystemObjectService.download(user, path);
        var filename = Paths.get(path).getFileName().toString();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename="
                        + URLEncoder.encode(filename, StandardCharsets.UTF_8))
                .contentType(MediaType.parseMediaType(downloaded.contentType()))
                .body(new ByteArrayResource(downloaded.content()));
    }
}
