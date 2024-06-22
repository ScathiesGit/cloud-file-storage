package git.scathies.cloudfilestorage.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class StorageItemDtoResp {

    private String path;
    private String name;
    private Long size;
    private boolean isFolder;
}
