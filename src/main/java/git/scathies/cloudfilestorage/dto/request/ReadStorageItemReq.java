package git.scathies.cloudfilestorage.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReadStorageItemReq {

    private String path;
    private String name;
//    private boolean isFolder;

}
