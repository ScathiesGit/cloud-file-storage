package git.scathies.cloudfilestorage.controller;

import git.scathies.cloudfilestorage.model.User;
import git.scathies.cloudfilestorage.service.SearchStorageItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.SessionAttribute;

@Controller
@RequiredArgsConstructor
public class FileSearchController {

    private final SearchStorageItemService searchStorageItemService;

    @GetMapping("/search")
    public String search(@SessionAttribute User user, String name, Model model) {
        model.addAttribute("found", searchStorageItemService.search(user, name));
        return "search";
    }
}
