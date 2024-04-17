package git.scathies.cloudfilestorage.model;

public record DownloadObject(String name, byte[] content, String contentType) {

}
