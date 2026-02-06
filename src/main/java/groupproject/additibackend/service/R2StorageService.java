package groupproject.additibackend.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface R2StorageService {

    String uploadFile(MultipartFile file , String folder) throws IOException;

    List<String> uploadMultipleFiles(List<MultipartFile> files , String folder) throws IOException;

    void deleteFile(String  fileKey);

    String getPublicUrl(String fileKey);

    byte[] downloadFile(String fileKey);

}
