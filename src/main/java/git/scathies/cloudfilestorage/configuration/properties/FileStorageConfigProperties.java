package git.scathies.cloudfilestorage.configuration.properties;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "file-storage")
@Getter
@Setter
@AllArgsConstructor
public class FileStorageConfigProperties {

    private String username;
    private String password;
    private String endpoint;
    private String bucketName;
}

