package com.hux.creator.controller;

import com.hux.creator.service.ExportService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/export")
@RequiredArgsConstructor
public class ExportController {

    private final ExportService exportService;

    @GetMapping("/markdown/{projectId}")
    public ResponseEntity<String> exportMarkdown(@PathVariable Long projectId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String markdown = exportService.exportMarkdown(userId, projectId);
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"content.md\"")
                .body(markdown);
    }

    @GetMapping("/json/{projectId}")
    public ResponseEntity<String> exportJson(@PathVariable Long projectId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String json = exportService.exportJson(userId, projectId);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"content.json\"")
                .body(json);
    }

    @GetMapping("/word/{projectId}")
    public ResponseEntity<byte[]> exportWord(@PathVariable Long projectId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        byte[] bytes = exportService.exportWord(userId, projectId);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"content.txt\"")
                .body(bytes);
    }
}
