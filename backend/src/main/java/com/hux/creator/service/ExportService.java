package com.hux.creator.service;

public interface ExportService {

    String exportMarkdown(Long userId, Long projectId);

    String exportJson(Long userId, Long projectId);

    byte[] exportWord(Long userId, Long projectId);
}
