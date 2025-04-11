package org.fontory.fontorybe.file.validator;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fontory.fontorybe.file.domain.exception.SingleFileRequiredException;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MultipartFileValidator {
    public static MultipartFile extractSingleMultipartFile(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            log.error("No file uploaded. Exactly one file must be provided.");
            throw new SingleFileRequiredException();
        }
        if (files.size() != 1) {
            log.error("Invalid file count: {}. Exactly one file must be uploaded.", files.size());
            throw new SingleFileRequiredException();
        }

        MultipartFile file = files.get(0);

        if (file.isEmpty()) {
            log.error("Uploaded file is empty.");
            throw new SingleFileRequiredException();
        }

        log.info("Single file extracted successfully: filename={}, size={} bytes",
                file.getOriginalFilename(), file.getSize());
        return file;
    }
}
