package com.studora.service;

import com.studora.dto.ImagemDto;
import com.studora.entity.Imagem;
import com.studora.repository.ImagemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ImagemService {
    
    @Autowired
    private ImagemRepository imagemRepository;
    
    public List<ImagemDto> getAllImagens() {
        return imagemRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public ImagemDto getImagemById(Long id) {
        Imagem imagem = imagemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Imagem não encontrada com ID: " + id));
        return convertToDto(imagem);
    }
    
    public ImagemDto createImagem(ImagemDto imagemDto) {
        Imagem imagem = convertToEntity(imagemDto);
        Imagem savedImagem = imagemRepository.save(imagem);
        return convertToDto(savedImagem);
    }
    
    public ImagemDto updateImagem(Long id, ImagemDto imagemDto) {
        Imagem existingImagem = imagemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Imagem não encontrada com ID: " + id));
        
        // Update fields
        existingImagem.setUrl(imagemDto.getUrl());
        existingImagem.setDescricao(imagemDto.getDescricao());
        
        Imagem updatedImagem = imagemRepository.save(existingImagem);
        return convertToDto(updatedImagem);
    }
    
    public void deleteImagem(Long id) {
        if (!imagemRepository.existsById(id)) {
            throw new RuntimeException("Imagem não encontrada com ID: " + id);
        }
        imagemRepository.deleteById(id);
    }
    
    private ImagemDto convertToDto(Imagem imagem) {
        ImagemDto dto = new ImagemDto();
        dto.setId(imagem.getId());
        dto.setUrl(imagem.getUrl());
        dto.setDescricao(imagem.getDescricao());
        return dto;
    }
    
    private Imagem convertToEntity(ImagemDto dto) {
        return new Imagem(dto.getUrl(), dto.getDescricao());
    }
}