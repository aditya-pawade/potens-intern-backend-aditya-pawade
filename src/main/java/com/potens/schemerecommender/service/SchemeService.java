package com.potens.schemerecommender.service;

import com.potens.schemerecommender.dto.request.SchemeRequest;
import com.potens.schemerecommender.dto.request.SchemeRuleRequest;
import com.potens.schemerecommender.dto.response.ExplainResponse;
import com.potens.schemerecommender.dto.response.SchemeResponse;
import com.potens.schemerecommender.entity.Scheme;
import com.potens.schemerecommender.entity.SchemeRule;
import com.potens.schemerecommender.enums.RuleType;
import com.potens.schemerecommender.exception.DuplicateResourceException;
import com.potens.schemerecommender.exception.InvalidRuleException;
import com.potens.schemerecommender.exception.ResourceNotFoundException;
import com.potens.schemerecommender.mapper.SchemeMapper;
import com.potens.schemerecommender.repository.SchemeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SchemeService {

    private static final Logger log = LoggerFactory.getLogger(SchemeService.class);

    private final SchemeRepository schemeRepository;

    public SchemeService(SchemeRepository schemeRepository) {
        this.schemeRepository = schemeRepository;
    }

    @Transactional(readOnly = true)
    public Page<SchemeResponse> getAllSchemes(Pageable pageable) {
        return schemeRepository.findAll(pageable)
                .map(SchemeMapper::toResponseWithoutRules);
    }

    @Transactional(readOnly = true)
    public SchemeResponse getSchemeById(Long id) {
        Scheme scheme = schemeRepository.findByIdWithRules(id)
                .orElseThrow(() -> new ResourceNotFoundException("Scheme", "id", id));
        return SchemeMapper.toResponse(scheme);
    }

    @Transactional
    public SchemeResponse createScheme(SchemeRequest request) {
        if (schemeRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Scheme", "name", request.getName());
        }

        validateRules(request);

        Scheme scheme = SchemeMapper.toEntity(request);
        Scheme saved = schemeRepository.save(scheme);
        log.info("Created scheme: {} (id={})", saved.getName(), saved.getId());
        return SchemeMapper.toResponse(saved);
    }

    @Transactional
    public SchemeResponse updateScheme(Long id, SchemeRequest request) {
        Scheme existing = schemeRepository.findByIdWithRules(id)
                .orElseThrow(() -> new ResourceNotFoundException("Scheme", "id", id));

        if (schemeRepository.existsByNameAndIdNot(request.getName(), id)) {
            throw new DuplicateResourceException("Scheme", "name", request.getName());
        }

        validateRules(request);

        // Update scalar fields
        existing.setName(request.getName());
        existing.setDescription(request.getDescription());
        existing.setCategory(request.getCategory());
        existing.setMinistry(request.getMinistry());
        existing.setBenefitAmount(request.getBenefitAmount());
        existing.setBenefitDescription(request.getBenefitDescription());
        existing.setEligibilitySummary(request.getEligibilitySummary());

        // Clear-and-replace rules (orphanRemoval deletes old, cascade inserts new)
        existing.getRules().clear();
        for (SchemeRuleRequest ruleReq : request.getRules()) {
            SchemeRule rule = SchemeMapper.toRuleEntity(ruleReq);
            existing.addRule(rule);
        }

        Scheme saved = schemeRepository.save(existing);
        log.info("Updated scheme: {} (id={})", saved.getName(), saved.getId());
        return SchemeMapper.toResponse(saved);
    }

    @Transactional
    public void deleteScheme(Long id) {
        if (!schemeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Scheme", "id", id);
        }
        schemeRepository.deleteById(id);
        log.info("Deleted scheme with id: {}", id);
    }

    @Transactional(readOnly = true)
    public ExplainResponse explainScheme(Long id) {
        Scheme scheme = schemeRepository.findByIdWithRules(id)
                .orElseThrow(() -> new ResourceNotFoundException("Scheme", "id", id));
        return SchemeMapper.toExplainResponse(scheme);
    }

    // ──────────────── Cross-Field Rule Validation ────────────────

    private void validateRules(SchemeRequest request) {
        if (request.getRules() == null) return;
        for (SchemeRuleRequest rule : request.getRules()) {
            validateRuleFields(rule);
        }
    }

    private void validateRuleFields(SchemeRuleRequest rule) {
        switch (rule.getRuleType()) {
            case ENUM_MATCH:
            case MIN_VALUE:
            case MAX_VALUE:
            case BOOLEAN_CHECK:
                if (rule.getValue() == null || rule.getValue().trim().isEmpty()) {
                    throw new InvalidRuleException(rule.getRuleType(), "value is required");
                }
                break;
            case RANGE_CHECK:
                if (rule.getMinValue() == null || rule.getMinValue().trim().isEmpty()) {
                    throw new InvalidRuleException(rule.getRuleType(), "minValue is required");
                }
                if (rule.getMaxValue() == null || rule.getMaxValue().trim().isEmpty()) {
                    throw new InvalidRuleException(rule.getRuleType(), "maxValue is required");
                }
                break;
            default:
                throw new IllegalStateException("Unhandled RuleType: " + rule.getRuleType());
        }
    }
}
