package knu.database.musicbase.controller;

import jakarta.servlet.http.HttpSession;
import knu.database.musicbase.dto.ProviderDto;
import knu.database.musicbase.enums.AuthType;
import knu.database.musicbase.repository.ProviderRepository;
import knu.database.musicbase.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api/providers")
@RestController
public class ProviderController {

    @Autowired
    ProviderRepository providerRepository;
    @Autowired
    private AuthService authService;

    // 1. 제공원 검색
    @GetMapping("/search")
    public List<ProviderDto> searchProviders(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String link,
            @RequestParam(required = false, defaultValue = "name") String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String sortOrder
    ) {
        return providerRepository.searchProviders(name, link, sortBy, sortOrder);
    }

    @GetMapping
    public ResponseEntity<List<ProviderDto>> getAllProviders() {
        return ResponseEntity.ok(providerRepository.findAll());
    }

    // 제공원 정보 조회
    @GetMapping("/{id}")
    public ProviderDto getProviderDetails(@PathVariable Long id) {
        return providerRepository.getProviderDetails(id);
    }

    // 제공원 추가
    @PostMapping("")
    public ProviderDto addProvider(@RequestBody ProviderDto providerDto) {
        return providerRepository.addProvider(providerDto);
    }

    // 제공원 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<ProviderDto> deleteProvider(@PathVariable Long id, HttpSession session) {

        var authType = authService.getAuthType(session);
        if (authType == AuthType.MANAGER) {
            ProviderDto deletedProvider = providerRepository.deleteProvider(id);
            return ResponseEntity.ok(deletedProvider);
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
}
