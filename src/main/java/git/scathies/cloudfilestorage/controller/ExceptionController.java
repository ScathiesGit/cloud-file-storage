package git.scathies.cloudfilestorage.controller;

import git.scathies.cloudfilestorage.exception.DeleteException;
import git.scathies.cloudfilestorage.exception.DownloadException;
import git.scathies.cloudfilestorage.exception.FileStorageException;
import git.scathies.cloudfilestorage.exception.UploadException;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.io.IOException;

@ControllerAdvice
public class ExceptionController {

    @ExceptionHandler({DeleteException.class})
    public void handleDeleteException(HttpServletResponse resp, Model model) {
        writeResp(resp, model, "ошибка при удалении");
    }

    @ExceptionHandler({DownloadException.class})
    public void handleDownloadException(HttpServletResponse resp, Model model) {
        writeResp(resp, model, "ошибка при скачивании");
    }

    @ExceptionHandler({FileStorageException.class})
    public void handleFileStorageException(HttpServletResponse resp, Model model) {
        writeResp(resp, model, "ошибка при работе с файловым сервисом");
    }

    @ExceptionHandler({UploadException.class})
    public void handleUploadException(HttpServletResponse resp, Model model) {
        writeResp(resp, model, "ошибка при загрузке файлов");
    }

    private void writeResp(HttpServletResponse resp, Model model, String message) {
        try {
            resp.sendRedirect("/");
            model.addAttribute("reason", message);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
