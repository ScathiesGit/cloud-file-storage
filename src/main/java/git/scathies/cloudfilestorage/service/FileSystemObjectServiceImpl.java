package git.scathies.cloudfilestorage.service;

import git.scathies.cloudfilestorage.model.DownloadObject;
import git.scathies.cloudfilestorage.model.User;
import git.scathies.cloudfilestorage.repository.FileSystemObjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FileSystemObjectServiceImpl implements FileSystemObjectService {

    private final FileSystemObjectRepository fileSystemObjectRepository;

    public void remove(User user, String path, String name) {
        fileSystemObjectRepository.delete(user, path, name);
    }

    public void createFolder(User user, String path, String name) {
        fileSystemObjectRepository.saveFolder(user, path, name);
    }

    @Override
    public void rename(User user, String path, String oldName, String newName) {
        fileSystemObjectRepository.update(user, path, oldName, newName);
    }

    @Override
    public DownloadObject download(User user, String path) {
        return fileSystemObjectRepository.download(user, path);
    }

    @Override
    public void upload(User user, String path, List<MultipartFile> files) {
        fileSystemObjectRepository.upload(user, path, files);
    }
}
