package git.scathies.cloudfilestorage.controller;

import com.google.common.net.HttpHeaders;
import git.scathies.cloudfilestorage.model.User;
import git.scathies.cloudfilestorage.service.FileSystemObjectService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.buf.UriUtil;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.SessionAttribute;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@Controller
@RequiredArgsConstructor
public class DownloadController {

    private final FileSystemObjectService fileSystemObjectService;

    private final Charset charset = Charset.forName(System.getProperty("file.encoding"));

    @GetMapping("/download")
    public ResponseEntity<ByteArrayResource> download(@SessionAttribute User user, String path) {
        var fullPath = "user-%s-files/%s".formatted(user.getId(), path);
        var downloaded = fileSystemObjectService.download(fullPath);
        var filename = path.endsWith("/")
                ? fullPath.substring(fullPath.lastIndexOf("/", fullPath.length() - 2) + 1)
                : fullPath.substring(fullPath.lastIndexOf("/") + 1);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=" + URLEncoder.encode(filename, StandardCharsets.UTF_8))
                .contentType(MediaType.parseMediaType(downloaded.contentType()))
                .body(new ByteArrayResource(downloaded.content()));
    }
}
