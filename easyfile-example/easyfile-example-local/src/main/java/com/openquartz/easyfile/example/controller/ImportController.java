package com.openquartz.easyfile.example.controller;

import com.openquartz.easyfile.common.bean.Notifier;
import com.openquartz.easyfile.common.request.RegisterImportRequest;
import com.openquartz.easyfile.storage.importer.ImportStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/easyfile/import")
@RequiredArgsConstructor
public class ImportController {

    private final ImportStorageService importStorageService;

    @PostMapping("/register")
    public Long register(@RequestBody RegisterImportRequest request) {
        if (request.getNotifier() == null) {
            Notifier notifier = new Notifier();
            notifier.setUserBy("admin");
            notifier.setUserName("Admin");
            notifier.setEmail("admin@example.com");
            request.setNotifier(notifier);
        }
        return importStorageService.register(request);
    }
}
