package ropold.backend.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mbox")
public class MboxController {

    @Value("${mbox}")
    private String mbox;

    @GetMapping("/72c81498-f6b2-4a8a-911c-cd217a65e0da")
    public String getMapBoxy() {
        return mbox;
    }
}