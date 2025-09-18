package com.capstone.service.impl;

import com.capstone.dto.response.PaginatedResponseDto;
import com.capstone.dto.response.SkillCapsuleResponseDto;
import com.capstone.exception.*;
import com.capstone.mapper.SkillCapsuleEventMapper;
import com.capstone.mapper.SkillCapsuleMapper;
import com.capstone.model.CapsuleAtomMapping;
import com.capstone.model.SkillAtomSnapshot;
import com.capstone.model.SkillCapsuleSnapshot;
import com.capstone.repository.SkillCapsuleSnapshotRepository;
import com.capstone.service.SkillAtomSnapshotService;
import com.capstone.service.SkillCapsuleSnapshotService;
import com.capstone.util.PaginationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.common.event.SkillCapsuleEvent;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class SkillCapsuleSnapshotServiceImpl implements SkillCapsuleSnapshotService {

    private final SkillCapsuleSnapshotRepository skillCapsuleSnapshotRepository;
    private final SkillCapsuleEventMapper skillCapsuleEventMapper;
    private final SkillCapsuleMapper skillCapsuleMapper;
    private final SkillAtomSnapshotService skillAtomSnapshotService;

    @Override
    @CacheEvict(value = {"skill-capsules", "skill-capsules-with-atoms", "skill-capsules-search", "skill-capsule-single"}, allEntries = true)
    public SkillCapsuleSnapshot processSkillCapsuleEvent(SkillCapsuleEvent event) {
        log.info("Processing skill capsule event for capsuleId: {}", event.getId());

        try {
            Optional<SkillCapsuleSnapshot> existingCapsule = skillCapsuleSnapshotRepository.findBySkillCapsuleId(event.getId());

            if (existingCapsule.isPresent()) {
                log.info("Capsule exists, updating capsule with ID: {}", event.getId());
                return updateSkillCapsule(existingCapsule.get(), event);
            } else {
                log.info("Capsule does not exist, creating new capsule with ID: {}", event.getId());
                return createSkillCapsule(event);
            }

        } catch (Exception e) {
            log.error("Error processing skill capsule event for capsuleId: {}", event.getId(), e);
            throw new SkillCapsuleProcessingException("Failed to process skill capsule event", e);
        }
    }

    @Override
    public SkillCapsuleSnapshot createSkillCapsule(SkillCapsuleEvent event) {
        log.debug("Creating new skill capsule from event: {}", event);

        try {

            SkillCapsuleSnapshot newCapsule = skillCapsuleEventMapper.toSkillCapsuleSnapshot(event);
            SkillCapsuleSnapshot savedCapsule = skillCapsuleSnapshotRepository.save(newCapsule);
            log.info("Successfully created skill capsule with ID: {}", savedCapsule.getSkillCapsuleId());

            //Process atom mappings
            if (event.getSkillAtom() != null && !event.getSkillAtom().isEmpty()) {
                smartUpdateCapsuleAtomMappings(savedCapsule, event.getSkillAtom());
                log.info("Successfully created {} atom mappings for capsule {}",
                        event.getSkillAtom().size(), savedCapsule.getSkillCapsuleId());
            }

            return savedCapsule;

        } catch (DataIntegrityViolationException e) {
            log.error("Data integrity violation when creating capsule with ID: {}", event.getId(), e);
            throw new DuplicateSkillCapsuleException(event.getId());
        } catch (Exception e) {
            log.error("Unexpected error creating capsule with ID: {}", event.getId(), e);
            throw new SkillCapsuleProcessingException("Failed to create skill capsule", e);
        }
    }

    @Override
    public SkillCapsuleSnapshot updateSkillCapsule(SkillCapsuleSnapshot existingCapsule, SkillCapsuleEvent event) {
        log.debug("Updating existing capsule {} with event data", existingCapsule.getSkillCapsuleId());

        try {

            skillCapsuleEventMapper.updateSkillCapsuleSnapshot(event, existingCapsule);
            SkillCapsuleSnapshot updatedCapsule = skillCapsuleSnapshotRepository.save(existingCapsule);

            //Smart update atom mappings
            if (event.getSkillAtom() != null && !event.getSkillAtom().isEmpty()) {
                smartUpdateCapsuleAtomMappings(updatedCapsule, event.getSkillAtom());
                log.info("Successfully updated atom mappings for capsule {}", updatedCapsule.getSkillCapsuleId());
            }

            log.info("Successfully updated skill capsule with ID: {}", updatedCapsule.getSkillCapsuleId());
            return updatedCapsule;

        } catch (DataIntegrityViolationException e) {
            log.error("Data integrity violation when updating capsule with ID: {}", existingCapsule.getSkillCapsuleId(), e);
            throw new SkillCapsuleProcessingException("Capsule update failed due to data constraint violation", e);
        } catch (Exception e) {
            log.error("Unexpected error updating capsule with ID: {}", existingCapsule.getSkillCapsuleId(), e);
            throw new SkillCapsuleProcessingException("Failed to update skill capsule", e);
        }
    }

    @Override
    public void smartUpdateCapsuleAtomMappings(SkillCapsuleSnapshot capsule, List<Map<UUID, Integer>> skillAtomMappings) {
        log.debug("Smart updating atom mappings for capsule: {}", capsule.getSkillCapsuleId());

        try {
            // PHASE 1: VERIFY ALL ATOMS EXIST - Fail Fast Strategy
            List<AtomSequencePair> newMappings = verifyAndParseAtoms(skillAtomMappings);
            log.debug("Verified {} atom mappings for capsule {}", newMappings.size(), capsule.getSkillCapsuleId());

            // PHASE 2: ANALYZE CHANGES - Smart Diff Algorithm
            UpdateAnalysis analysis = analyzeChanges(capsule, newMappings);
            log.debug("Analysis for capsule {}: {} to add, {} to update",
                    capsule.getSkillCapsuleId(), analysis.getToAdd().size(), analysis.getToUpdate().size());

            // PHASE 3: APPLY UPDATES - Atomic Operation
            applyAtomMappingUpdates(capsule, analysis);

            log.info("Smart update completed for capsule {}: {} added, {} updated, {} preserved",
                    capsule.getSkillCapsuleId(), analysis.getToAdd().size(),
                    analysis.getToUpdate().size(), analysis.getPreserved());

        } catch (SkillAtomNotFoundException e) {
            log.error("Atom verification failed for capsule {}: {}", capsule.getSkillCapsuleId(), e.getMessage());
            throw new CapsuleAtomMappingException("Atom not found: " + e.getMessage());
        } catch (Exception e) {
            log.error("Smart update failed for capsule {}: {}", capsule.getSkillCapsuleId(), e.getMessage(), e);
            throw new CapsuleAtomMappingException( "Smart update failed", e);
        }
    }

    /**
     * Verify all atoms exist and parse into structured format
     */
    private List<AtomSequencePair> verifyAndParseAtoms(List<Map<UUID, Integer>> skillAtomMappings) {
        List<AtomSequencePair> parsedMappings = new ArrayList<>();

        for (Map<UUID, Integer> atomMap : skillAtomMappings) {
            for (Map.Entry<UUID, Integer> entry : atomMap.entrySet()) {
                UUID atomId = entry.getKey();
                Integer sequence = entry.getValue();

                // Verify atom exists
                SkillAtomSnapshot atom = skillAtomSnapshotService.findBySkillAtomId(atomId)
                        .orElseThrow(() -> new SkillAtomNotFoundException(atomId));

                // Validate sequence order
                if (sequence == null || sequence < 1) {
                    throw new CapsuleAtomMappingException(null, atomId, "Invalid sequence order: " + sequence);
                }

                parsedMappings.add(new AtomSequencePair(atom, sequence));
            }
        }

        return parsedMappings;
    }

    /**
     * Analyze differences between existing and new mappings
     */
    private UpdateAnalysis analyzeChanges(SkillCapsuleSnapshot capsule, List<AtomSequencePair> newMappings) {
        // Build lookup map of existing mappings - O(n)
        Map<UUID, CapsuleAtomMapping> existingMap = capsule.getCapsuleAtomMappings()
                .stream()
                .collect(Collectors.toMap(
                        mapping -> mapping.getSkillAtom().getSkillAtomId(),
                        mapping -> mapping
                ));

        List<CapsuleAtomMapping> toAdd = new ArrayList<>();
        List<CapsuleAtomMapping> toUpdate = new ArrayList<>();
        int preserved = 0;

        // Process each new mapping - O(m)
        for (AtomSequencePair newMapping : newMappings) {
            UUID atomId = newMapping.getAtom().getSkillAtomId();

            if (existingMap.containsKey(atomId)) {
                // ATOM EXISTS - Check if sequence changed
                CapsuleAtomMapping existing = existingMap.get(atomId);
                if (!existing.getSequenceOrder().equals(newMapping.getSequence())) {
                    existing.setSequenceOrder(newMapping.getSequence());
                    toUpdate.add(existing);
                } else {
                    preserved++; // No change needed
                }
                // Remove from map to track what remains
                existingMap.remove(atomId);
            } else {
                // NEW ATOM - Create mapping
                CapsuleAtomMapping newMappingEntity = CapsuleAtomMapping.builder()
                        .skillCapsule(capsule)
                        .skillAtom(newMapping.getAtom())
                        .sequenceOrder(newMapping.getSequence())
                        .build();
                toAdd.add(newMappingEntity);
            }
        }

        // Remaining mappings in existingMap are preserved (not in new event)
        preserved += existingMap.size();

        return new UpdateAnalysis(toAdd, toUpdate, preserved);
    }

    /**
     * Apply the analyzed updates to the capsule
     */
    private void applyAtomMappingUpdates(SkillCapsuleSnapshot capsule, UpdateAnalysis analysis) {
        if (!analysis.getToAdd().isEmpty()) {
            capsule.getCapsuleAtomMappings().addAll(analysis.getToAdd());
        }
    }


    @Override
    @Transactional(readOnly = true)
    public Optional<SkillCapsuleSnapshot> findBySkillCapsuleId(UUID skillCapsuleId) {
        log.debug("Finding capsule by skillCapsuleId: {}", skillCapsuleId);
        return skillCapsuleSnapshotRepository.findBySkillCapsuleId(skillCapsuleId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsBySkillCapsuleId(UUID skillCapsuleId) {
        log.debug("Checking if capsule exists by skillCapsuleId: {}", skillCapsuleId);
        return skillCapsuleSnapshotRepository.existsBySkillCapsuleId(skillCapsuleId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SkillCapsuleSnapshot> findBySkillCapsuleIdWithAtomMappings(UUID skillCapsuleId) {
        log.debug("Finding capsule with atom mappings by skillCapsuleId: {}", skillCapsuleId);
        return skillCapsuleSnapshotRepository.findBySkillCapsuleIdWithAtomMappings(skillCapsuleId);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "skill-capsules", key = "#pageable.pageNumber + '-' + #pageable.pageSize")
    public PaginatedResponseDto<SkillCapsuleResponseDto> findAllBasic(Pageable pageable) {
        log.debug("Finding all skill capsules with basic info, page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());
        Page<SkillCapsuleSnapshot> pageData = skillCapsuleSnapshotRepository.findAll(pageable);
        Page<SkillCapsuleResponseDto> dtoPage = pageData.map(skillCapsuleMapper::toBasicResponseDto);
        return PaginationUtil.toPaginatedResponse(dtoPage);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "skill-capsules-with-atoms", key = "#pageable.pageNumber + '-' + #pageable.pageSize")
    public PaginatedResponseDto<SkillCapsuleResponseDto> findAllWithAtomSummaries(Pageable pageable) {
        log.debug("Finding all skill capsules with atom summaries, page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());
        Page<SkillCapsuleSnapshot> pageData = skillCapsuleSnapshotRepository.findAllWithAtomMappings(pageable);
        Page<SkillCapsuleResponseDto> dtoPage = pageData.map(skillCapsuleMapper::toResponseDtoWithAtoms);
        return PaginationUtil.toPaginatedResponse(dtoPage);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "skill-capsules-search", key = "#capsuleName + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public PaginatedResponseDto<SkillCapsuleResponseDto> searchByCapsuleNameBasic(String capsuleName, Pageable pageable) {
        log.debug("Searching skill capsules by name: '{}', page: {}, size: {}", capsuleName, pageable.getPageNumber(), pageable.getPageSize());
        Page<SkillCapsuleSnapshot> pageData = skillCapsuleSnapshotRepository.findByCapsuleNameContainingIgnoreCase(capsuleName, pageable);
        Page<SkillCapsuleResponseDto> dtoPage = pageData.map(skillCapsuleMapper::toBasicResponseDto);
        return PaginationUtil.toPaginatedResponse(dtoPage);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "skill-capsules-search", key = "#difficultyLevel + '-difficulty-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public PaginatedResponseDto<SkillCapsuleResponseDto> findByDifficultyLevelBasic(String difficultyLevel, Pageable pageable) {
        log.debug("Finding skill capsules by difficulty level: '{}', page: {}, size: {}", difficultyLevel, pageable.getPageNumber(), pageable.getPageSize());
        Page<SkillCapsuleSnapshot> pageData = skillCapsuleSnapshotRepository.findByDifficultyLevel(difficultyLevel, pageable);
        Page<SkillCapsuleResponseDto> dtoPage = pageData.map(skillCapsuleMapper::toBasicResponseDto);
        return PaginationUtil.toPaginatedResponse(dtoPage);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "skill-capsule-single", key = "#skillCapsuleId")
    public Optional<SkillCapsuleResponseDto> findBySkillCapsuleIdBasic(UUID skillCapsuleId) {
        log.debug("Finding skill capsule by ID with basic info: {}", skillCapsuleId);
        return skillCapsuleSnapshotRepository.findBySkillCapsuleId(skillCapsuleId)
                .map(skillCapsuleMapper::toBasicResponseDto);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "skill-capsule-single", key = "#skillCapsuleId + '-with-atoms'")
    public Optional<SkillCapsuleResponseDto> findBySkillCapsuleIdWithAtomSummaries(UUID skillCapsuleId) {
        log.debug("Finding skill capsule by ID with atom summaries: {}", skillCapsuleId);
        return skillCapsuleSnapshotRepository.findBySkillCapsuleIdWithAtomMappings(skillCapsuleId)
                .map(skillCapsuleMapper::toResponseDtoWithAtoms);
    }

    // Helper classes

    @lombok.Data
    @lombok.AllArgsConstructor
    private static class AtomSequencePair {
        private SkillAtomSnapshot atom;
        private Integer sequence;
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    private static class UpdateAnalysis {
        private List<CapsuleAtomMapping> toAdd;
        private List<CapsuleAtomMapping> toUpdate;
        private int preserved;
    }
}