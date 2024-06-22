package git.scathies.cloudfilestorage.configuration.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "service")
@Getter
@Setter
public class ServiceConfigProperties {

    private String rootFolderTemplate;
}
