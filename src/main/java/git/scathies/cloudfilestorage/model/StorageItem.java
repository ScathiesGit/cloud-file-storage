package git.scathies.cloudfilestorage.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StorageItem {

    private String path;
    private String name;
    private Long size;
    private boolean isFolder;

    public StorageItem(String path, String name) {
        this.path = path;
        this.name = name;
    }
}
