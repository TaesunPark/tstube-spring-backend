package com.example.video.dto.video;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// 비디오 생성할 때 요청하는 DTO 입니다.
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class CreateVideoRequestDto {
	@NotBlank(message = "제목은 필수입니다.")
	private String title;
	@NotBlank(message = "영상 소스는 필수입니다.")
	private String src;

	private String description;
	private String channelTitle;
	@NotBlank(message = "타입은 필수입니다")
	@Pattern(regexp = "^(iframe|upload)$", message = "타입은 iframe 또는 upload만 가능합니다.")
	private String type;
	private String fileName;
}
