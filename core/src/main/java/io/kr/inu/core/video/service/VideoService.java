package io.kr.inu.core.video.service;

import io.kr.inu.core.common.Converter;
import io.kr.inu.core.user.service.UserValidateService;
import io.kr.inu.core.video.domain.VideoEntity;
import io.kr.inu.core.video.dto.EachVideoData;
import io.kr.inu.core.video.dto.FindVideoResponseDto;
import io.kr.inu.core.video.dto.MakeVideoReqDto;
import io.kr.inu.core.video.repository.VideoRepository;
import io.kr.inu.infra.s3.VideoS3Repository;
import io.kr.inu.infra.s3.MultipartDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import org.apache.tomcat.util.http.fileupload.MultipartStream;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class VideoService {

    private final VideoS3Repository videoS3Repository;
    private final VideoRepository videoRepository;
    private final VideoValidateService videoValidateService;
    private final UserValidateService userValidateService;

    @Transactional
    public String uploadVideo(MultipartFile video, MakeVideoReqDto videoReqDto) throws IOException {
        userValidateService.existUserByEmail(videoReqDto.getEmail());
        MultipartDto multipartDto = new MultipartDto(video.getOriginalFilename(), video.getSize(), video.getContentType(), video.getInputStream());
//        videoValidateService.validateHarmVideo(video);
        String videoUrl = videoS3Repository.saveVideo(multipartDto);
        String thumbnailUrl = videoS3Repository.saveVideoByStream(multipartDto.getOriginalFileName() + "thumbnail", extractThumbnail(video));

        videoRepository.save(VideoEntity.of(videoUrl, thumbnailUrl, videoReqDto));

        return videoUrl;
    }

    private File extractThumbnail(MultipartFile videoFile) throws IOException {
        log.info("extractThumbnail 시1작");
        // local
        FFmpeg ffMpeg = new FFmpeg("C:\\ffmpeg\\bin\\ffmpeg");
        FFprobe ffProbe = new FFprobe("C:\\ffmpeg\\bin\\ffprobe");

        // ec2
//          FFmpeg ffMpeg = new FFmpeg("/usr/bin/ffmpeg-6.1-amd64-static/ffmpeg");
//          FFprobe ffProbe = new FFprobe("/usr/bin/ffmpeg-6.1-amd64-static/ffprobe");


        File outputThumbnailFile = File.createTempFile("temp_", ".jpg");

        Path tempFilePath = outputThumbnailFile.toPath();
        Files.copy(videoFile.getInputStream(), tempFilePath, StandardCopyOption.REPLACE_EXISTING);

        File thumbnailOutputFile = File.createTempFile("thumbnail_", ".jpg");
        thumbnailOutputFile.deleteOnExit();

        FFmpegBuilder builder = new FFmpegBuilder()
                .setInput(outputThumbnailFile.toString())
                .overrideOutputFiles(true)
                .addOutput(thumbnailOutputFile.getAbsolutePath())
                .setFrames(1)
                .done();

        FFmpegExecutor executor = new FFmpegExecutor(ffMpeg, ffProbe);
        executor.createJob(builder).run();

        log.info("extractThumbnail 종료");
        return thumbnailOutputFile;
    }

    public FindVideoResponseDto findVideoByDate(String email, int page, int size) {
        userValidateService.existUserByEmail(email);
        Pageable pageable = PageRequest.of(page, size);
        List<EachVideoData> posts = videoRepository.findVideoByDate(pageable);
        boolean next = isNext(videoRepository.count(), page, size);

        return FindVideoResponseDto.builder()
                .allVideos(posts)
                .next(next)
                .build();
    }

    private boolean isNext(long count, int page, int size) {
        return (long) size * page + size < count;
    }

    public FindVideoResponseDto findVideoByEmail(String email, int page, int size) {
        userValidateService.existUserByEmail(email);
        Pageable pageable = PageRequest.of(page, size);
        List<EachVideoData> posts = videoRepository.findVideoByEmail(email, pageable);
        boolean next = isNext(videoRepository.count(), page, size);

        return FindVideoResponseDto.builder()
                .allVideos(posts)
                .next(next)
                .build();
    }
}
