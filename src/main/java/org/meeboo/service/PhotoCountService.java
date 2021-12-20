package org.meeboo.service;

import lombok.extern.slf4j.Slf4j;
import org.meeboo.entity.PhotoCountEntity;
import org.meeboo.repository.PhotoCountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
@Slf4j
public class PhotoCountService {

    @Autowired
    PhotoCountRepository photoCountRepository;

    public Optional<PhotoCountEntity> getPhotoCount() {
        return photoCountRepository.findById("12345");
    }
}
