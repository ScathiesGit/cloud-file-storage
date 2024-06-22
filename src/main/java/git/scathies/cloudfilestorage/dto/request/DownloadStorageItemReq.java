package git.scathies.cloudfilestorage.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DownloadStorageItemReq {

    private String path;
    private String name;
    private Boolean isFolder;

    public Boolean isFolder() {
        return isFolder;
    }
}
