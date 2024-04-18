package git.scathies.cloudfilestorage.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
@Builder
public class FileSystemObject {

    private String name;

    private Long size;

    private Map<String, String> userMetadata;
}
