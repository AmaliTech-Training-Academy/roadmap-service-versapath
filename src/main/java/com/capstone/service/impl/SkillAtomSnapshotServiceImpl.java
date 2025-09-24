package com.capstone.service.impl;

import com.capstone.dto.response.PaginatedResponseDto;
import com.capstone.dto.response.SkillAtomResponseDto;
import com.capstone.exception.SkillAtomProcessingException;
import com.capstone.mapper.SkillAtomEventMapper;
import com.capstone.mapper.SkillAtomMapper;
import com.capstone.model.SkillAtomSnapshot;
import com.capstone.repository.SkillAtomSnapshotRepository;
import com.capstone.service.SkillAtomSnapshotService;
import com.capstone.util.PaginationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.common.event.SkillAtomEvent;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class SkillAtomSnapshotServiceImpl implements SkillAtomSnapshotService {

    private final SkillAtomSnapshotRepository skillAtomSnapshotRepository;
    private final SkillAtomEventMapper skillAtomEventMapper;
    private final SkillAtomMapper skillAtomMapper;

    @Override
    @CacheEvict(value = {
            "skill-atoms", "skill-atoms-search", "skill-atom-single",
            "skill-capsules-with-atoms", "skill-capsule-single"
    }, allEntries = true)
    public SkillAtomSnapshot processSkillAtomEvent(SkillAtomEvent event) {
        log.info("Processing skill atom event for skillAtomId: {}", event.getId());

        try {
            Optional<SkillAtomSnapshot> existingSkillAtom = skillAtomSnapshotRepository.findBySkillAtomId(event.getId());

            if (existingSkillAtom.isPresent()) {
                log.info("Skill atom exists, updating skill atom with ID: {}", event.getId());
                return updateSkillAtom(existingSkillAtom.get(), event);
            } else {
                log.info("Skill atom does not exist, creating new skill atom with ID: {}", event.getId());
                return createSkillAtom(event);
            }

        } catch (Exception e) {
            log.error("Error processing skill atom event for skillAtomId: {}", event.getId(), e);
            throw new SkillAtomProcessingException("Failed to process skill atom event", e);
        }
    }

    @Override
    public SkillAtomSnapshot createSkillAtom(SkillAtomEvent event) {
        log.debug("Creating new skill atom from event: {}", event);

        try {
            SkillAtomSnapshot newSkillAtom = skillAtomEventMapper.toSkillAtomSnapshot(event);
            SkillAtomSnapshot savedSkillAtom = skillAtomSnapshotRepository.save(newSkillAtom);
            log.info("Successfully created skill atom with ID: {}", savedSkillAtom.getSkillAtomId());
            return savedSkillAtom;

        } catch (DataIntegrityViolationException e) {
            log.error("Data integrity violation when creating skill atom with ID: {}", event.getId(), e);
            throw new SkillAtomProcessingException("Skill atom creation failed due to data constraint violation", e);
        } catch (Exception e) {
            log.error("Unexpected error creating skill atom with ID: {}", event.getId(), e);
            throw new SkillAtomProcessingException("Failed to create skill atom", e);
        }
    }

    @Override
    public SkillAtomSnapshot updateSkillAtom(SkillAtomSnapshot existingSkillAtom, SkillAtomEvent event) {
        log.debug("Updating existing skill atom {} with event data: {}", existingSkillAtom.getSkillAtomId(), event);

        try {
            skillAtomEventMapper.updateSkillAtomSnapshot(event, existingSkillAtom);
            SkillAtomSnapshot updatedSkillAtom = skillAtomSnapshotRepository.save(existingSkillAtom);
            log.info("Successfully updated skill atom with ID: {}", updatedSkillAtom.getSkillAtomId());
            return updatedSkillAtom;

        } catch (DataIntegrityViolationException e) {
            log.error("Data integrity violation when updating skill atom with ID: {}", existingSkillAtom.getSkillAtomId(), e);
            throw new SkillAtomProcessingException("Skill atom update failed due to data constraint violation", e);
        } catch (Exception e) {
            log.error("Unexpected error updating skill atom with ID: {}", existingSkillAtom.getSkillAtomId(), e);
            throw new SkillAtomProcessingException("Failed to update skill atom", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SkillAtomSnapshot> findBySkillAtomId(UUID skillAtomId) {
        log.debug("Finding skill atom by skillAtomId: {}", skillAtomId);
        return skillAtomSnapshotRepository.findBySkillAtomId(skillAtomId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsBySkillAtomId(UUID skillAtomId) {
        log.debug("Checking if skill atom exists by skillAtomId: {}", skillAtomId);
        return skillAtomSnapshotRepository.existsBySkillAtomId(skillAtomId);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "skill-atoms", key = "#pageable.pageNumber + '-' + #pageable.pageSize")
    public PaginatedResponseDto<SkillAtomResponseDto> findAllBasic(Pageable pageable) {
        log.debug("Finding all skill atoms with basic info, page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());
        Page<SkillAtomSnapshot> pageData = skillAtomSnapshotRepository.findAll(pageable);
        Page<SkillAtomResponseDto> dtoPage = pageData.map(skillAtomMapper::toResponseDto);
        return PaginationUtil.toPaginatedResponse(dtoPage);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "skill-atoms-search", key = "#name + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public PaginatedResponseDto<SkillAtomResponseDto> searchByNameBasic(String name, Pageable pageable) {
        log.debug("Searching skill atoms by name: '{}', page: {}, size: {}", name, pageable.getPageNumber(), pageable.getPageSize());
        Page<SkillAtomSnapshot> pageData = skillAtomSnapshotRepository.findByNameContainingIgnoreCase(name, pageable);
        Page<SkillAtomResponseDto> dtoPage = pageData.map(skillAtomMapper::toResponseDto);
        return PaginationUtil.toPaginatedResponse(dtoPage);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "skill-atom-single", key = "#skillAtomId")
    public Optional<SkillAtomResponseDto> findBySkillAtomIdBasic(UUID skillAtomId) {
        log.debug("Finding skill atom by ID with basic info: {}", skillAtomId);
        return skillAtomSnapshotRepository.findBySkillAtomId(skillAtomId)
                .map(skillAtomMapper::toResponseDto);
    }
}
