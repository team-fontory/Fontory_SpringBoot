package org.fontory.fontorybe.file.validator;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fontory.fontorybe.file.domain.exception.SingleFileRequiredException;
import org.fontory.fontorybe.file.domain.exception.InvalidMultipartRequestException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.util.List;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MultipartFileValidator {
    public static MultipartFile extractSingleMultipartFile(HttpServletRequest request) {
        if (!(request instanceof MultipartHttpServletRequest multipartRequest)) {
            log.error("Invalid request: Not a multipart request.");
            throw new InvalidMultipartRequestException();
        }

        log.debug("Multipart request received.");
        List<MultipartFile> files = multipartRequest.getFiles("file");
        log.info("Number of files received: {}", files.size());

        if (files.size() != 1) {
            log.error("Invalid file count: {}. Exactly one file must be uploaded.", files.size());
            throw new SingleFileRequiredException();
        }

        MultipartFile file = files.get(0);
        log.info("Single file extracted successfully: filename={}, size={} bytes",
                file.getOriginalFilename(), file.getSize());
        return file;
    }
}
