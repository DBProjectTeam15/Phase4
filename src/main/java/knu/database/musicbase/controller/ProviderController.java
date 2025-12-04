package knu.database.musicbase.controller;

import knu.database.musicbase.dto.ProviderDto;
import knu.database.musicbase.dto.SongDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("/api/providers")
@RestController
public class ProviderController {

    @GetMapping
    public ResponseEntity<List<ProviderDto>> getAllProviders() {
        return ResponseEntity.ok(List.of(
                ProviderDto.builder().id(1).link("link").name("provider name").build(),
                ProviderDto.builder().id(2).link("link").name("provider name").build()
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProvider(@PathVariable long id) {
        return ResponseEntity.ok().build();
    }
}
