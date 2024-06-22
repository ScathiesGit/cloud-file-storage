package git.scathies.cloudfilestorage.dto.response;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReadStorageItemResp {

    private String path;
    private String name;
    private Long size;
    private boolean isFolder;
}
