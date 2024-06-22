package git.scathies.cloudfilestorage.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class RenameStorageItemReq {

    private String pathToDirectory;
    private String oldName;
    private String newName;
    private Boolean isFolder;

    public Boolean isFolder() {
        return isFolder;
    }
}
