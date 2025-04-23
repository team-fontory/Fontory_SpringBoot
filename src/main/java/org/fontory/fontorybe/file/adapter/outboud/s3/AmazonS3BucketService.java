package org.fontory.fontorybe.file.adapter.outboud.s3;

import io.awspring.cloud.s3.S3Resource;
import io.awspring.cloud.s3.S3Template;
import lombok.RequiredArgsConstructor;
import org.fontory.fontorybe.common.application.ClockHolder;
import org.fontory.fontorybe.file.domain.FileCreate;
import org.fontory.fontorybe.file.domain.FileMetadata;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;

@Service
@RequiredArgsConstructor
public class AmazonS3BucketService implements CloudStorageService{

    @Value("${spring.cloud.aws.s3.bucket.profile-image}")
    private String profileImageBucketName;

    @Value("${spring.cloud.aws.s3.bucket.font-paper}")
    private String fontPaperBucketName;

    private final ClockHolder clockHolder;
    private final S3Template s3Template;

    @Override
    public FileMetadata uploadProfileImage(FileCreate request) {
        AmazonS3PutRequest amazonS3PutRequest = AmazonS3PutRequest.from(request, clockHolder.getCurrentTimeStamp());
        deleteLastImages(request.getFileName());

        try (InputStream inputStream = amazonS3PutRequest.getFile().getInputStream()) {
            S3Resource s3Resource = s3Template.upload(
                    profileImageBucketName,
                    amazonS3PutRequest.getKey(),
                    inputStream);

            String objectUrl = s3Resource.getURL().toExternalForm();

            return AmazonS3ObjectMetadata.from(amazonS3PutRequest, objectUrl).toModel();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void deleteLastImages(String fileName) {
        String fileNameWithoutExtension = fileName.substring(0, fileName.lastIndexOf('.'));
        s3Template.deleteObject(profileImageBucketName,fileNameWithoutExtension + ".jpg");
        s3Template.deleteObject(profileImageBucketName,fileNameWithoutExtension + ".jpeg");
        s3Template.deleteObject(profileImageBucketName,fileNameWithoutExtension + ".png");
    }

    @Override
    public FileMetadata uploadFontTemplateImage(FileCreate request) {
        AmazonS3PutRequest amazonS3PutRequest = AmazonS3PutRequest.from(request, clockHolder.getCurrentTimeStamp());

        try (InputStream inputStream = amazonS3PutRequest.getFile().getInputStream()) {
            S3Resource s3Resource = s3Template.upload(
                    fontPaperBucketName,
                    amazonS3PutRequest.getKey(),
                    inputStream);

            String objectUrl = s3Resource.getURL().toExternalForm();

            return AmazonS3ObjectMetadata.from(amazonS3PutRequest, objectUrl).toModel();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
