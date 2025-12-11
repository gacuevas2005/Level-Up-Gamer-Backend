package com.LevelUpGamer.proyecto.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/version")
public class VersionController {

    @GetMapping("/latest")
    public Map<String, Object> getLatestVersion() {
        // Cada vez que subas una app nueva, cambia este número manualmente aquí
        return Map.of(
                "versionCode", 7,
                "url", "/app-release.apk"
        );
    }
}
