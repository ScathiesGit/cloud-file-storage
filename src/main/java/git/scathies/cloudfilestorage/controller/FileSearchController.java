package git.scathies.cloudfilestorage.controller;

import git.scathies.cloudfilestorage.model.User;
import git.scathies.cloudfilestorage.service.SearchFileSystemObjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttribute;

@Controller
@RequestMapping("/search")
@RequiredArgsConstructor
public class FileSearchController {

    private final SearchFileSystemObjectService searchFileSystemObjectService;

    @GetMapping
    public String search(@SessionAttribute User user, String name, Model model) {
        model.addAttribute("found", searchFileSystemObjectService.search(user, name));
        return "search";
    }

    // первая проблема - в форме не указал enctype multipart/form-data атрибут тега форм
    // вторая проблема - загружался один файл вместо папки, в контроллере нужно было указать коллекцию MultipartFile

}
