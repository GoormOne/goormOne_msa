package com.example.storeservice.service;


import com.example.storeservice.entity.Menu;
import com.example.storeservice.repository.MenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MenuService {
    private final MenuRepository menuRepository;
    public Menu getMenu(UUID uuid) {
        return menuRepository.findById(uuid).orElse(null);
    }
}
