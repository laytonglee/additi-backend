package groupproject.additibackend.service.impl;

import groupproject.additibackend.config.R2Config;
import groupproject.additibackend.exception.FileStorageException;
import groupproject.additibackend.service.R2StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j

public class R2StorageServiceImpl implements R2StorageService {

    private final S3Client s3Client;
    private final R2Config r2Config;

    private static final List<String> ALLOWED_EXTENSIONS = List.of("jpg", "jpeg", "png", "gif", "webp");
    private  static  final  long MAX_FILE_SIZE = 10 * 1024 * 1024;


    @Override
    public String uploadFile(MultipartFile file, String folder) throws IOException {
        validateFile(file);

        String fileName = generateUniqueFileName(file.getOriginalFilename());
        String fileKey = folder + "/" + fileName;

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(r2Config.getBucketName())
                    .key(fileKey)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            s3Client.putObject(putObjectRequest,
                    RequestBody.fromBytes(file.getBytes()));

            log.info("File uploaded successfully: {}", fileKey);
            return fileKey;

        } catch (S3Exception e) {
            log.error("Failed to upload file to R2: {}", e.getMessage());
            throw new FileStorageException("Failed to upload file: " + e.getMessage());
        }

    }

    @Override
    public List<String> uploadMultipleFiles(List<MultipartFile> files, String folder) throws IOException {
        List<String> uploadedKeys = new ArrayList<>();

        for (MultipartFile file : files) {
            String fileKey = uploadFile(file, folder);
            uploadedKeys.add(fileKey);
        }

        return uploadedKeys;
    }


    @Override
    public void deleteFile(String fileKey) {
        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(r2Config.getBucketName())
                    .key(fileKey)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
            log.info("File deleted successfully: {}", fileKey);

        } catch (S3Exception e) {
            log.error("Failed to delete file from R2: {}", e.getMessage());
            throw new FileStorageException("Failed to delete file: " + e.getMessage());
        }
    }

    @Override
    public String getPublicUrl(String fileKey) {
        return r2Config.getPublicUrl() + "/" + fileKey;
    }

    @Override
    public byte[] downloadFile(String fileKey) {
        return new byte[0];
    }

    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            throw new FileStorageException("File has no extension");
        }
        return filename.substring(lastDotIndex + 1);
    }

    private String generateUniqueFileName(String originalFilename) {
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        String extension = getFileExtension(originalFilename);

        return timestamp + "_" + uuid + "." + extension;
    }

    private  void validateFile(MultipartFile file){

        if(file.isEmpty()){
            throw  new FileStorageException("File is empty");
        }

        if(file.getSize() > MAX_FILE_SIZE){
            throw new FileStorageException("File is invalid");
        }

        String  originalFilename = file.getOriginalFilename();

        if(originalFilename==null){
            throw new FileStorageException("File name is invalid");
        }

        String extension = getFileExtension(originalFilename);
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new FileStorageException("File type not allowed. Allowed types: " + ALLOWED_EXTENSIONS);
        }

    }
}
