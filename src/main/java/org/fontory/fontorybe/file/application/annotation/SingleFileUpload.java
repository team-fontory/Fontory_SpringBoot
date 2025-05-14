package org.fontory.fontorybe.file.application.annotation;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.MediaType;

import java.lang.annotation.*;

@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Parameter(
        description = "업로드할 파일. 정확히 1개의 파일만 제공되어야 합니다.",
        required = true,
        content = @Content(
                mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                array = @ArraySchema(
                        schema = @Schema(type = "string", format = "binary"),
                        maxItems = 1
                )
        )
)
public @interface SingleFileUpload {
}
